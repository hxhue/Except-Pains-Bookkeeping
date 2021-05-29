package com.example.epledger.inbox

import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.observe
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.epledger.MainActivity
import com.example.epledger.R
import com.example.epledger.asMainActivity
import com.example.epledger.nav.NavigationFragment
import com.example.epledger.inbox.event.list.EventFragment
import com.example.epledger.db.DatabaseModel
import com.example.epledger.db.AppDatabase
import com.example.epledger.detail.RecordDetailFragment
import com.example.epledger.home.EntryAdapter
import com.example.epledger.model.Record
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.android.synthetic.main.page_inbox.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.lang.IllegalArgumentException
import java.lang.IllegalStateException
import java.lang.RuntimeException
import java.util.*
import kotlin.collections.ArrayList

class InboxFragment : Fragment() {
    private val dbModel: DatabaseModel by activityViewModels()
    private var cachedEventFragment: EventFragment? = EventFragment()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.page_inbox, container, false)

        setUpView(view);

        // Turn on option menu
        setHasOptionsMenu(true)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        registerObservers(view)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        inflater.inflate(R.menu.inbox_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_item_inbox_notification -> {
                NavigationFragment.pushToStack(
                    requireActivity().supportFragmentManager,
                    cachedEventFragment!!,
                    true,
                    this.requireActivity().asMainActivity().viewCachePolicy
                )
                true
            }
            else -> false
        }
    }

    enum class InboxSectionType {
        INCOMPLETE, STARRED
    }

    private val sectionCheckSet = EnumSet.allOf(InboxSectionType::class.java)

    private fun checkSectionVisibility(sectionType: InboxSectionType, visibility: Int) {
        if (visibility != View.GONE && visibility != View.VISIBLE && visibility != View.INVISIBLE) {
            throw IllegalArgumentException("Unsupported visibility type for this function")
        }

        val rootView = requireView()

        // Update current section header (empty or not)
        when (sectionType) {
            InboxSectionType.INCOMPLETE -> {
                rootView.apply {
                    inbox_incomplete_section_header.visibility = visibility
                    inbox_incomplete_recycler_view.visibility = visibility
                }
            }
            InboxSectionType.STARRED -> {
                rootView.apply {
                    inbox_star_section_header.visibility = visibility
                    inbox_star_recycler_view.visibility = visibility
                }
            }
        }

        // Update info (If all section headers are empty, we should show info. Otherwise not.)
        if (visibility != View.GONE) {
            sectionCheckSet.add(sectionType)
        } else {
            sectionCheckSet.remove(sectionType)
        }

        if (sectionCheckSet.isEmpty()) {
            rootView.no_sections_msg.visibility = View.VISIBLE
        } else {
            rootView.no_sections_msg.visibility = View.GONE
        }
    }

    /**
     * 设置不完整的记录栏的显示数量。下面几个方法类似。
     */
    fun setInCompleteSectionNumber(number: Int) {
        if (number < 0) {
            throw IllegalArgumentException("number should be >= 0")
        }
        val newText = requireContext().getString(R.string.incomplete) + " ($number)"
        requireView().inbox_incomplete_section_label.text = newText

        setInBoxBadge(number)

        val visibility = if (number == 0) View.GONE else View.VISIBLE
        checkSectionVisibility(InboxSectionType.INCOMPLETE, visibility)
    }

    fun setStarredSectionNumber(number: Int) {
        if (number < 0) {
            throw IllegalArgumentException("number should be >= 0")
        }
        val newText = requireContext().getString(R.string.starred) + " ($number)"
        requireView().inbox_star_section_label.text = newText

        val visibility = if (number == 0) View.GONE else View.VISIBLE
        checkSectionVisibility(InboxSectionType.STARRED, visibility)
    }

    private fun setInBoxBadge(number: Int) {
        (this.activity as MainActivity).setInboxBadge(num = number)
    }

    private fun setUpView(view: View) {
        setUpIncompleteSection(view)
        setUpStarredSection(view)
    }

    private interface OnRecordSubmitListener {
        fun onRecordSubmit(adapter: EntryAdapter, record: Record)
    }

    /**
     * 几个栏目通用的设置方式。不同栏目的长按和删除的功能是一样的，不需要做差异化定制，因此没有参数控制。
     * @param onRecordSubmitListener 是在提交时提供的回调。
     * @param deleteEntryAfterSubmit 决定是否在调用onRecordSubmitRunnable.run()之后删除该项。
     */
    private fun setUpRecyclerView(recyclerView: RecyclerView,
                                  onRecordSubmitListener: OnRecordSubmitListener,
                                  deleteEntryAfterSubmit: Boolean,
                                  entryWasShownInHomePage: Boolean,
                                  entryEditingOnOpen: Boolean = false
    ) {
        val initialEntries = ArrayList<Record>(0)
        recyclerView.apply {
            val entryAdapter = EntryAdapter(initialEntries, dbModel)
            // Do not capture entryList too early, cause it may change
            // Capturing the adapter is then the wise choice
            // val entryList = entryAdapter.entries

            // 用于删除该位置的元素
            val deleteEntryRunnable = object : Runnable {
                var entryPos: Int? = null

                override fun run() {
                    if (entryPos == null) {
                        throw RuntimeException("setEntryPos() not called before run()")
                    }
                    val entryPos = entryPos!!
                    val entryList = entryAdapter.entries
                    // Capture it before it's too late to get the record ID
                    val recordToDelete = entryList[entryPos]

                    // Manage database and view in home page
                    GlobalScope.launch(Dispatchers.IO) {
                        AppDatabase.deleteRecordByID(recordToDelete.ID!!)
                        if (entryWasShownInHomePage) {
                            dbModel.deleteRecord(recordToDelete, requireActivity().asMainActivity().homeSectionAdapter!!)
                        }
                    }

                    // Manage view in this fragment
                    entryList.removeAt(entryPos)
                    entryAdapter.notifyItemRemoved(entryPos)
                    entryAdapter.notifyItemRangeChanged(entryPos, entryList.size)

                    // Manage section number display
                    when (recyclerView.id) {
                        R.id.inbox_incomplete_recycler_view -> {
                            setInCompleteSectionNumber(entryList.size)
                        }
                        R.id.inbox_star_recycler_view -> {
                            setStarredSectionNumber(entryList.size)
                        }
                        else -> throw IllegalStateException("Current recyclerView id is not expected.")
                    }
                }

                fun setEntryPosition(newPos: Int): Runnable {
                    this.entryPos = newPos
                    return this
                }
            }

            entryAdapter.setOnItemClickListener(object : EntryAdapter.OnItemClickListener {
                // Modifying an incomplete record
                override fun onItemClick(view: View?, position: Int) {
                    val newFragment = RecordDetailFragment()
                    newFragment.bindRecord(entryAdapter.entries[position])
                    newFragment.setDetailRecordMsgReceiver(object : RecordDetailFragment.DetailRecordMsgReceiver {
                        override fun onDetailRecordDelete(record: Record) {
                            deleteEntryRunnable.setEntryPosition(position).run()
                        }

                        override fun onDetailRecordSubmit(record: Record) {
                            onRecordSubmitListener.onRecordSubmit(entryAdapter, record)
                            if (deleteEntryAfterSubmit) {
                                deleteEntryRunnable.setEntryPosition(position).run()
                            }
                        }
                    })
                    NavigationFragment.pushToStack(
                        (requireActivity() as AppCompatActivity).supportFragmentManager,
                        newFragment, true
                    )
                }

                override fun onItemLongClick(view: View?, position: Int) {
                    val context = view!!.context
                    val dialog = MaterialAlertDialogBuilder(context)
                        .setMessage(context.getString(R.string.del_item_confirm))
                        .setNegativeButton(R.string.no) { _, _ -> }
                        .setPositiveButton(R.string.ok) { _, _ ->
                            deleteEntryRunnable.setEntryPosition(position).run()
                        }
                    dialog.show()
                }
            })
            adapter = entryAdapter
            layoutManager = LinearLayoutManager(recyclerView.context)
        }
    }

    private fun setUpStarredSection(view: View) {
        setUpRecyclerView(view.inbox_star_recycler_view, object : OnRecordSubmitListener {
            override fun onRecordSubmit(adapter: EntryAdapter, record: Record) {
                // 由于这里只显示完整的记录，因此记录首先必是完整的，且已经在主页面的列表中了
                // 1. 删除的时候要将主页面列表中的项目也一同删除（见setUpRecyclerView的另一个参数）
                // 2. 提交的时候检查当前是否仍然在标星Section中、是否仍然在截图Section中。

                require(record.ID != null)

                // 2021年05月27日 这里的提交并没有真正影响到数据库
                val indexInSection = adapter.entries.indexOfFirst {
                    record.ID == it.ID
                }

                if (record.starred) {
                    adapter.notifyItemChanged(indexInSection)
                } else {
                    adapter.entries.removeAt(indexInSection)
                    adapter.notifyItemRemoved(indexInSection)
                    adapter.notifyItemRangeChanged(indexInSection, adapter.entries.size)
                }

                // 3. 通知主页面的列表刷新这个项目
                dbModel.updateRecord(record, requireActivity().asMainActivity().homeSectionAdapter!!)
            }
        }, deleteEntryAfterSubmit = false, entryWasShownInHomePage = true)
    }

    /**
     * 这些回调之间应该相互检查，因为section是可能重叠显示的，即有些项目可能在多个section中出现。
     * 但目前规定在标星和有截图的栏目中不会显示不完整的项目。
     */
    private fun setUpIncompleteSection(view: View) {
        setUpRecyclerView(view.inbox_incomplete_recycler_view, object :
            OnRecordSubmitListener {
            override fun onRecordSubmit(adapter: EntryAdapter, record: Record) {
                // todo: check this updateRecord method
                // 2021-05-29 06:37:10
                dbModel.updateIncompleteRecord(record)
            }
        }, deleteEntryAfterSubmit = true, entryWasShownInHomePage = false)
    }

    private fun registerObservers(rootView: View) {
        dbModel.incompleteRecords.observe(viewLifecycleOwner) { it ->
            val incompleteRV = rootView.inbox_incomplete_recycler_view
            val adapter = (incompleteRV.adapter as EntryAdapter)
            adapter.entries = it
            adapter.notifyDataSetChanged()

            // Refresh section number display
            setInCompleteSectionNumber(it.size)
        }

        dbModel.starredRecords.observe(viewLifecycleOwner) {
            val starRV = rootView.inbox_star_recycler_view
            val adapter = (starRV.adapter as EntryAdapter)
            adapter.entries = it
            adapter.notifyDataSetChanged()

            setStarredSectionNumber(it.size)
        }
    }

    fun checkStarredSectionOnInsertion(record: Record) {
        if (record.starred) {
            // insert this record to starred section
            (requireView().inbox_star_recycler_view.adapter as EntryAdapter).apply {
                var indexToInsert = entries.indexOfFirst { it.mDate <= record.mDate }

                // 所有的记录日期都比当前的大时，插入到最后
                if (indexToInsert < 0) {
                    indexToInsert = entries.size
                }

                this.entries.add(indexToInsert, record)
                setStarredSectionNumber(entries.size)
                notifyItemInserted(indexToInsert)
            }
        }
    }

    fun checkStarredSectionOnUpdate(record: Record) {
        require(record.ID != null)

        (requireView().inbox_star_recycler_view.adapter as EntryAdapter).apply {
            val index = entries.indexOfFirst { record.ID == it.ID }

            // 错误：2021年05月26日：如果record本身不在这个section中，直接返回
            // 修正：2021年05月27日：如果record本身不在这个section中，而又是带有标星的项目，说明需要新增一项
            if (index < 0 && !record.starred) {
                return
            }

            // 此时index < 0说明record有标星，需要加进去
            if (index < 0) {
                var indexToInsert = entries.binarySearch(record, Record.dateReverseComparator)
                if (indexToInsert < 0) {
                    indexToInsert = -(indexToInsert + 1)
                }
                entries.add(indexToInsert, record)
                setStarredSectionNumber(entries.size)
                notifyItemInserted(indexToInsert)
                return
            }

            // 不再有标星时应该删除
            if (!record.starred) {
                entries.removeAt(index)
                setStarredSectionNumber(entries.size)
                notifyItemRemoved(index)
                notifyItemRangeChanged(index, entries.size)
            } else { // 仍有标星时就在列表更新
                notifyItemChanged(index)
            }
        }
    }

    fun checkStarredSectionOnRemoval(record: Record) {
        require(record.ID != null)

        (requireView().inbox_star_recycler_view.adapter as EntryAdapter).apply {
            val index = entries.indexOfFirst { record.ID == it.ID }

            // 如果record本身不在这个section中，直接返回
            if (index < 0) {
                return
            }

            // 无论是否标星，要是原来就在这个栏目中，现在也要一起被删除
            entries.removeAt(index)
            setStarredSectionNumber(entries.size)
            notifyItemRemoved(index)
            notifyItemRangeChanged(index, entries.size)
        }
    }

    fun checkIncompleteSectionOnUpdate(record: Record) {
        if (!record.isComplete()) {
            throw RuntimeException("All updates from MainActivity cannot involve an incomplete record")
        }
    }

    fun checkIncompleteSectionOnRemoval(record: Record) {
        if (record.ID == null) {
            return
        }
        val id = record.ID!!
        (requireView().inbox_incomplete_recycler_view.adapter as EntryAdapter).apply {
            val indexToFind = entries.indexOfFirst { it.ID == id }
            if (indexToFind < 0) {
                return
            }
            entries.removeAt(indexToFind)
            setInCompleteSectionNumber(entries.size)
            notifyItemRemoved(indexToFind)
            notifyItemRangeChanged(indexToFind, entries.size)
        }
    }

    fun checkIncompleteSectionOnInsertion(record: Record) {
        if (!record.isComplete()) {
            throw RuntimeException("All insertions from MainActivity cannot create an incomplete record")
        }
    }
}