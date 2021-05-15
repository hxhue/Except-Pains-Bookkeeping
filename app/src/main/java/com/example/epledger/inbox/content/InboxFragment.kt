package com.example.epledger.inbox.content

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.epledger.MainActivity
import com.example.epledger.R
import com.example.epledger.model.entry.SectionLab
import com.example.epledger.nav.NavigationFragment
import com.xwray.groupie.ExpandableGroup
import com.xwray.groupie.GroupieAdapter
import com.example.epledger.inbox.event.list.EventFragment
import com.example.epledger.model.DatabaseViewModel
import com.example.epledger.model.entry.Entry

class InboxFragment : Fragment() {
    private val dbModel: DatabaseViewModel by activityViewModels()
//    var numOfStarredItems = 0
//        set(value) {
//            view?.let {
//                val starLabel = it.findViewById<TextView>(R.id.inbox_star_label)
//                starLabel.text = String.format("%s (%d)", getString(R.string.inbox_label_star), value)
//            }
//            field = value
//        }
//
//    var numOfIncompleteItems = 0
//        set(value) {
//            // 数量标识
//            view?.let {
//                val starLabel = it.findViewById<TextView>(R.id.inbox_incomplete_label)
//                starLabel.text = String.format("%s (%d)", getString(R.string.inbox_label_incomplete), value)
//            }
//            field = value
//            // 更新徽章
//            val mainActivity = this.activity as MainActivity
//            mainActivity.setInboxBadge(value)
//        }
//
//    var numOfScreenshotItems = 0
//        set(value) {
//            view?.let {
//                val starLabel = it.findViewById<TextView>(R.id.inbox_shot_label)
//                starLabel.text = String.format("%s (%d)", getString(R.string.inbox_label_screenshot), value)
//            }
//            field = value
//        }

//    // 缓存的待提交的提醒时间
//    var cachedMinuteOfHour = 0
//    var cachedHourOfDay = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.page_inbox, container, false)

        // Turn on option menu
        setHasOptionsMenu(true)

        // TODO: remove this debug section
//        val button = view.findViewById<Button>(R.id.inbox_button_debug_create)
//        button.setOnClickListener {
//            GlobalScope.launch {
//                withContext(Dispatchers.IO) {
//                    val rec = DetailRecord()
//                    rec.ID = 11
//                    rec.amount = 123.0
//                    rec.source = "Alipay"
//                    rec.date = Date()
//                    rec.hourOfDay = 12
//                    rec.starred = true
//                    rec.minuteOfHour = 13
//                    rec.note = "Happy birthday!"
//                    try {
////                        rec.screenshot = ScreenshotUtils.loadBitmap(
////                                requireContext(),
////                                "/storage/emulated/0/Android/data/com.example.epledger/files/Pictures/1619754991133.jpg"
////                        )
//                    } catch (e: Exception) {
//                        rec.screenshot = null
//                    }
//                    withContext(Dispatchers.Main) {
//                        val newFragment = RecordDetailFragment()
//                        newFragment.bindRecord(rec)
//                        NavigationFragment.pushToStack(requireActivity().supportFragmentManager, newFragment, true)
//                    }
//                }
//            }
//        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val sectionLab = SectionLab(arrayListOf(dbModel.requireRecords()) as List<MutableList<Entry>>?)
        val sections = sectionLab.sections

        val recyclerView = view.findViewById<RecyclerView>(R.id.inbox_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(view.context)
        recyclerView.itemAnimator = DefaultItemAnimator()

        val adapter = GroupieAdapter()

        // TODO: delete this
        val sectionTitles = arrayOf<InboxHeaderItem.InboxHeaderTitle>(
                InboxHeaderItem.InboxHeaderTitle.EVENTS,
                InboxHeaderItem.InboxHeaderTitle.STARRED,
                InboxHeaderItem.InboxHeaderTitle.SCREENSHOTS
        )
        for (i in 0 until sections.size) {
            val entryList = sections[i].entryList
            val header = InboxExpandableHeaderItem(sectionTitles[i], entryList)
            val group = ExpandableGroup(header, true)
            for (j in 0 until entryList.size) {
                group.add(InboxEntryItem(entryList[j]))
            }
            adapter.add(group)
        }

        recyclerView.adapter = adapter
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.inbox_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_item_inbox_notification -> {
                val newFragment = EventFragment()
                NavigationFragment.pushToStack(requireActivity().supportFragmentManager, newFragment, true)
                true
            }
            else -> false
        }
    }

    override fun onResume() {
        super.onResume()
        // TODO: Remove this debug code
//        numOfStarredItems = 1
//        numOfIncompleteItems = 13
//        numOfScreenshotItems = 7
        // TODO: Remove this debug code
        // TODO: 实际上在刚加载好数据库时这个已经需要加载，否则会有延迟感
    }

    fun setInBoxBadge(number: Int) {
        (this.activity as MainActivity).setInboxBadge(num = number)
    }
}