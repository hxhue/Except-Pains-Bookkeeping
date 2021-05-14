package com.example.epledger.settings.datamgr

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.epledger.R
import com.example.epledger.model.GlobalDBViewModel
import com.example.epledger.nav.NavigationFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.android.synthetic.main.dialog_text_input.view.*
import kotlinx.android.synthetic.main.mgr_rec_src.view.*
import kotlinx.android.synthetic.main.mgr_rec_src_item.view.*


class SourceManagerFragment: NavigationFragment() {
    private val dbModel: GlobalDBViewModel by activityViewModels()
    private val recyclerViewAdapter by lazy {
        val sourceList = dbModel.getSources()
        SourceAdapter(sourceList)
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

        // Floating button
        val floatingButton = view.floating_add
        floatingButton.setOnClickListener {
            val inflater = LayoutInflater.from(ctx)
            val dialogContent  = inflater.inflate(R.layout.dialog_text_input, null)
            dialogContent.layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
            )
            val dialog = MaterialAlertDialogBuilder(ctx)
                    .setView(dialogContent)
                    .setNegativeButton(R.string.no) { _, _ -> /**/ }
                    .setPositiveButton(R.string.ok) { _, _ ->
                        val newName = dialogContent.string_input_textview.text.toString()
                        // TODO: check duplicates of name
                        val newSource = Source(newName)
                        // TODO: add source to database and **fetch its ID**
                        val list = recyclerViewAdapter.sourceList
                        list.add(newSource)
                        recyclerViewAdapter.notifyItemInserted(list.size - 1)
                    }
                    .setTitle(getString(R.string.new_source))
                    .create()
            dialog.setOnShowListener {
                // Display keyboard
                val imm= ctx.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
                imm?.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0)
            }
            dialog.show()
        }
    }

    private fun setUpModel(view: View) {
        dbModel.sources.observe(viewLifecycleOwner, {
            // Update sourceList when reference changes
            recyclerViewAdapter.sourceList = it
            recyclerViewAdapter.notifyDataSetChanged()
        })
    }

    class SourceAdapter(
            var sourceList: ArrayList<Source>
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
                val inflater = LayoutInflater.from(ctx)
                val dialogContent  = inflater.inflate(R.layout.dialog_text_input, null)
                dialogContent.layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                )
                val dialog = MaterialAlertDialogBuilder(ctx)
                        .setView(dialogContent)
                        .setNegativeButton(R.string.no) { _, _ -> /**/ }
                        .setPositiveButton(R.string.ok) { _, _ ->
                            val newName = dialogContent.string_input_textview.text.toString()
                            // TODO: Check if name already exists
                            sourceList[position].name = newName
                            this.notifyItemChanged(position)
                            // TODO: write data into database
                            // TODO: inform other modules that this item has changed
                        }
                        .setTitle(ctx.getString(R.string.modify_name))
                        .create()
                dialog.setOnShowListener {
                    // Display keyboard
                    val imm= ctx.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
                    imm?.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0)
                }
                dialog.show()
            }

            // Long click to delete item
            view.setOnLongClickListener {
                val dialog = MaterialAlertDialogBuilder(ctx)
                        .setMessage(ctx.getString(R.string.del_item_confirm))
                        .setNegativeButton(R.string.no) { _, _ -> /* nothing */ }
                        .setPositiveButton(R.string.ok) { _, _ ->
                            sourceList.removeAt(position)
                            this.notifyItemRemoved(position)
                            this.notifyItemRangeChanged(position, sourceList.size)
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