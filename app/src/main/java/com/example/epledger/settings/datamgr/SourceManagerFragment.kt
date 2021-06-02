package com.example.epledger.settings.datamgr

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.observe
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.epledger.R
import com.example.epledger.db.DatabaseModel
import com.example.epledger.model.Source
import com.example.epledger.nav.NavigationFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.android.synthetic.main.dialog_text_input.*
import kotlinx.android.synthetic.main.dialog_text_input.view.*
import kotlinx.android.synthetic.main.mgr_rec_src.view.*
import kotlinx.android.synthetic.main.mgr_rec_src_item.view.*


class SourceManagerFragment: NavigationFragment() {
    private val dbModel: DatabaseModel by activityViewModels()
    private val recyclerViewAdapter by lazy {
        val sourceList = dbModel.requireSources()
        SourceAdapter(sourceList, dbModel)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.mgr_rec_src, container, false)
        setUpView(view)
        setUpModel(view)
        return view
    }

    private fun setUpView(view: View) {
        setNavigation(view, getString(R.string.sources))
        val recyclerView = view.mgr_rec_src_recyclerview
        val ctx = recyclerView.context
        recyclerView.layoutManager = LinearLayoutManager(ctx)
        recyclerView.adapter = recyclerViewAdapter

        // **User Intention: add a new item
        val floatingButton = view.floating_add
        floatingButton.setOnClickListener {
            val dialog = createSourceDialog(ctx, null, object : OnSourceSubmitListener {
                override fun onSourceSubmit(sourceName: String) {
                    val newSource = Source(sourceName)
                    dbModel.insertSource(newSource) {
                        recyclerViewAdapter.notifyItemInserted(recyclerViewAdapter.sourceList.size - 1)
                    }

//                    val list = recyclerViewAdapter.sourceList
//                    list.add(newSource)
//                    recyclerViewAdapter.notifyItemInserted(list.size - 1)
                }
            })
            dialog.show()
        }
    }

    private fun setUpModel(view: View) {
        dbModel.sources.observeForever {
            // Update sourceList when reference changes
            recyclerViewAdapter.sourceList = it
            recyclerViewAdapter.notifyDataSetChanged()
        }
    }

    class SourceAdapter(
            var sourceList: MutableList<Source>,
            private val dbModel: DatabaseModel
    ): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        class ViewHolder(view: View): RecyclerView.ViewHolder(view) {}

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val view = LayoutInflater.from(parent.context).
                inflate(R.layout.mgr_rec_src_item, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val view = holder.itemView
            val ctx = view.context

            // Short click to modify name
            view.setOnClickListener {
                val adapter = this
                val dialog = createSourceDialog(ctx, sourceList[position].name, object : OnSourceSubmitListener {
                    override fun onSourceSubmit(sourceName: String) {
                        // This is safe because update CAN happen multiple times
                        sourceList[position].name = sourceName
                        dbModel.updateSource(sourceList[position]) {
                            adapter.notifyItemChanged(position)
                        }
//                        sourceList[position].name = sourceName
//                        adapter.notifyItemChanged(position)
//                        // TODO: write data into database
//                        // TODO: inform other modules that this item has changed
                    }
                })
                dialog.show()
            }

            // Long click to delete item
            view.setOnLongClickListener {
                val dialog = MaterialAlertDialogBuilder(ctx)
                        .setMessage(ctx.getString(R.string.del_item_confirm))
                        .setNegativeButton(R.string.no) { _, _ -> /* nothing */ }
                        .setPositiveButton(R.string.ok) { _, _ ->
                            dbModel.deleteSourceByID(sourceList[position].ID!!) {
                                this.notifyItemRemoved(position)
                                this.notifyItemRangeChanged(position, sourceList.size)
                            }
//                            sourceList.removeAt(position)
                            // TODO: delete data from database
                        }
                dialog.show()
                true
            }

            // Set text
            val textView = view.mgr_rec_src_item_textview
            textView.setText(sourceList[position].name)
        }

        override fun getItemCount(): Int {
            return sourceList.size
        }
    }
}

/**
 * 创建一个对话，用来提交修改或新增source条目。
 * @param contentView: 当前对话的contentView
 * @param sourceName: 已有source的名称。如果为空，表明是正在新建一个source
 * @param onSubmitListener: 提交时的回调接口
 */
private fun createSourceDialog(ctx: Context, sourceName: CharSequence?,
                               onSubmitListener: OnSourceSubmitListener): Dialog {
    val inflater = LayoutInflater.from(ctx)
    val contentView  = inflater.inflate(R.layout.dialog_text_input, null).apply {
        layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        // 如果这是一个已存在的source记录，则在文本框中显示当前的名称
        sourceName?.let {
            this.string_input_edittext.apply {
                // 设置文本
                setText(sourceName)
                // 把输入光标移动到文本最后面
                setSelection(text.length)
            }
        }
    }

    val dialog = MaterialAlertDialogBuilder(ctx)
        .setView(contentView)
        .setNegativeButton(R.string.no) { _, _ -> /**/ }
        .setPositiveButton(R.string.ok) { _, _ ->
            // Check if it's empty or duplicate
            val stringToSubmit = contentView.string_input_edittext.text.toString()
            if (stringToSubmit.isBlank()) {
                val emptyCheckFailureDialog = MaterialAlertDialogBuilder(ctx)
                    .setPositiveButton(R.string.ok) { _, _ -> }
                    .setTitle(ctx.getString(R.string.changes_not_saved))
                    .setMessage(ctx.getString(R.string.source_name_empty_prompt))
                emptyCheckFailureDialog.show()
            } else {
                // TODO: Check source name duplication
                onSubmitListener.onSourceSubmit(stringToSubmit)
            }
        }
        .setTitle(if (sourceName != null) {
            ctx.getString(R.string.modify_name)
        } else {
            ctx.getString(R.string.new_source)
        })
        .create()
    dialog.setOnShowListener {
        dialog.window!!.apply {
            clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM)
            setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
        }
        contentView.string_input_edittext.requestFocus()
    }
    return dialog
}

private interface OnSourceSubmitListener {
    fun onSourceSubmit(sourceName: String)
}