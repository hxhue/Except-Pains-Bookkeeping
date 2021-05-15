package com.example.epledger.inbox

import android.os.Bundle
import android.view.*
import android.widget.Button
import android.widget.TimePicker
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.epledger.R
import com.example.epledger.detail.DetailRecord
import com.example.epledger.detail.RecordDetailFragment
import com.example.epledger.model.SectionLab
import com.example.epledger.nav.NavigationFragment
import com.example.epledger.qaction.screenshot.ScreenshotUtils
import com.xwray.groupie.ExpandableGroup
import com.xwray.groupie.GroupieAdapter
import com.xwray.groupie.Section
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*
import kotlin.random.Random

class InboxFragment : Fragment() {
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

    // 缓存的待提交的提醒时间
    var cachedMinuteOfHour = 0
    var cachedHourOfDay = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.page_inbox, container, false)

        // Turn on option menu
        setHasOptionsMenu(true)

        // TODO: remove this debug section
        val button = view.findViewById<Button>(R.id.inbox_button_debug_create)
        button.setOnClickListener {
            GlobalScope.launch {
                withContext(Dispatchers.IO) {
                    val rec = DetailRecord()
                    rec.ID = 11
                    rec.amount = 123.0
                    rec.source = "Alipay"
                    rec.date = Date()
                    rec.hourOfDay = 12
                    rec.starred = true
                    rec.minuteOfHour = 13
                    rec.note = "Happy birthday!"
                    rec.screenshot = ScreenshotUtils.loadBitmap(
                        requireContext(),
                        "/storage/emulated/0/Android/data/com.example.epledger/files/Pictures/1619754991133.jpg"
                    )
                    val newFragment = RecordDetailFragment()
                    newFragment.bindRecord(rec)
                    NavigationFragment.pushToStack(requireActivity() as AppCompatActivity, newFragment)
                }
            }
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val sectionLab = SectionLab.get(context)
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
                val newFragment = NotificationFragment()
                NavigationFragment.pushToStack(this.activity as AppCompatActivity, newFragment)
                true
//                val dialog = MaterialAlertDialogBuilder(requireContext())
//                        .setView(R.layout.dialog_inbox_notification)
//                        .setTitle("")
//                        .setPositiveButton(getString(R.string.ok)) { dialog, which ->
//                            // TODO
//                            Toast.makeText(requireContext(),
//                                    "We should submit changes. " +
//                                            "Cached hour: ${cachedHourOfDay}, cached minute: ${cachedMinuteOfHour}",
//                                    Toast.LENGTH_SHORT).show()
//                        }
//                        .setCancelable(false)
//                        .create()
//
//                // dialog出现后的回调（只有出现后视图接口才显现）
//                dialog.setOnShowListener {
//                    // 设置按钮的颜色
//                    val color = requireContext().getColor(R.color.lightColorSecondary)
//                    dialog.getButton(TimePickerDialog.BUTTON_POSITIVE).setTextColor(color)
//
//                    // 设置timePicker为24小时模式
//                    val timePicker = dialog.findViewById<TimePicker>(R.id.inbox_dialog_time_picker)
//                    timePicker!!.setIs24HourView(true)
//
//                    // TODO
//                    setCachedTime(timePicker)
//
//                    // 设置timePicker选中后的数据保存
//                    timePicker.setOnTimeChangedListener { _, hourOfDay, minute ->
//                        cachedHourOfDay = hourOfDay
//                        cachedMinuteOfHour = minute
//                    }
//                }
//                dialog.show()
            }
            else -> false
        }
    }

    private fun setCachedTime(timePicker: TimePicker) {
        TODO()
        // 根据当前有没有已经存在的记录设置当前的时间，
        // 如果没有，设置为当前时间
        // timePicker上的显示时间和cached时间必须一致
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

    override fun onPause() {
        super.onPause()
        // TODO: Remove this debug code
//        numOfIncompleteItems = 6
    }
}