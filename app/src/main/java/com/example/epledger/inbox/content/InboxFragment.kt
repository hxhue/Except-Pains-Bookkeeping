package com.example.epledger.inbox.content

import android.os.Bundle
import android.telecom.Call
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.epledger.MainActivity
import com.example.epledger.R
import com.example.epledger.asMainActivity
import com.example.epledger.nav.NavigationFragment
import com.xwray.groupie.ExpandableGroup
import com.xwray.groupie.GroupieAdapter
import com.example.epledger.inbox.event.list.EventFragment
import com.example.epledger.db.DatabaseModel
import com.example.epledger.db.model.AppDatabase
import com.example.epledger.detail.RecordDetailFragment
import com.example.epledger.home.EntryAdapter
import com.example.epledger.home.SectionAdapter
import com.example.epledger.model.Record
import com.example.epledger.model.RecordGroup
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.android.synthetic.main.page_inbox.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.lang.RuntimeException
import java.util.concurrent.Callable

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

    fun setInBoxBadge(number: Int) {
        (this.activity as MainActivity).setInboxBadge(num = number)
    }

    private fun setUpView(view: View) {
        val initialIncompleteEntries = ArrayList<Record>(0)
        view.inbox_incomplete_recycler_view.apply {
            val entryAdapter = EntryAdapter(initialIncompleteEntries, dbModel)
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
                    val recordIDToDelete = entryList[entryPos].ID!!

                    // Manage database
                    GlobalScope.launch(Dispatchers.IO) {
                        // 由于list是直接关联的，因此不应该由dbModel再删除一次，这样会出现异常
                        // dbModel.deleteIncompleteRecord(entryPos)
                        AppDatabase.deleteRecordByID(recordIDToDelete)
                    }
                    // Manage view
                    entryList.removeAt(entryPos)
                    entryAdapter.notifyItemRemoved(entryPos)
                    entryAdapter.notifyItemRangeChanged(entryPos, entryList.size)
                    // Manage badge
                    setInBoxBadge(entryList.size)
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
                            // When submitted, this record must have become complete
                            // 1. Insert it into global records
                            dbModel.insertRecord(record)
                            // 2. Remove it from current incomplete records
                            deleteEntryRunnable.setEntryPosition(position).run()
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
                        .setMessage(context.getString(R.string.incomplete_item_removal_confirm))
                        .setNegativeButton(R.string.no) { _, _ -> }
                        .setPositiveButton(R.string.ok) { _, _ ->
                            deleteEntryRunnable.setEntryPosition(position).run()
                        }
                    dialog.show()
                }
            })
            adapter = entryAdapter
            layoutManager = LinearLayoutManager(view.context)
        }
    }

    private fun registerObservers(rootView: View) {
        dbModel.incompleteRecords.observe(viewLifecycleOwner, {
            val incompleteRV = rootView.inbox_incomplete_recycler_view
            val adapter = (incompleteRV.adapter as EntryAdapter)
            adapter.entries = it
            adapter.notifyDataSetChanged()

            // Refresh badge of inbox page
            setInBoxBadge(it.size)
        })
    }
}