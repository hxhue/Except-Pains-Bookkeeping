package com.example.epledger.inbox.event.item

import android.app.DatePickerDialog
import android.graphics.Point
import android.os.Bundle
import android.view.*
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.GridLayoutManager
import com.example.epledger.R
import com.example.epledger.inbox.event.viewmodel.EventItemViewModel
import com.example.epledger.inbox.event.viewmodel.EventViewModel
import com.example.epledger.nav.NavigationFragment
import com.example.epledger.inbox.event.item.SpaceItemDecoration
import com.example.epledger.util.IconAsset
import com.example.epledger.util.ScreenMetrics
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.android.synthetic.main.fragment_event_item.view.*
import kotlinx.coroutines.launch
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*

/**
 * 每次进入之前都应该设置shouldCopyItem为true。
 * 因为这个类是可以缓存下来的，因此要设置额外的标志。
 */
class EventItemFragment: NavigationFragment(),
        AdapterView.OnItemSelectedListener, IconItemAdapter.OnPositionClickListener {
    private val eventsModel: EventViewModel by activityViewModels()
    private val itemModel: EventItemViewModel by viewModels()
    private val units: Array<String> by lazy {
        arrayOf(
            getString(R.string.unit_day),
            getString(R.string.unit_month),
            getString(R.string.unit_year)
        )
    }

    private var firstTimeForIcons = true
    var shouldCopyItem = true

    // TODO: move to another place
    private val iconIDs = IconAsset.assets

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        if (eventsModel.isNewEvent()) {
            inflater.inflate(R.menu.detail_menu_creating, menu)
        } else if (eventsModel.isEditing()) {
            inflater.inflate(R.menu.detail_menu_editing, menu)
        } else {
            inflater.inflate(R.menu.detail_menu_non_editing, menu)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            // Somewhat useless? (API 30)
            android.R.id.home -> {
                if (eventsModel.isEditing()) {
                    val dialog = MaterialAlertDialogBuilder(requireContext())
                        .setMessage(getString(R.string.discard_changes_confirm))
                        .setNegativeButton(R.string.no) {_, _ -> }
                        .setPositiveButton(R.string.ok) {_, _ ->
                            exitNavigationFragment()
                        }
                    dialog.show()
                } else {
                    exitNavigationFragment()
                }
                true
            }
            R.id.menu_item_delete -> {
                if (!eventsModel.isNewEvent() && eventsModel.eventIndex >= 0) {
                    val dialog = MaterialAlertDialogBuilder(requireContext())
                        .setMessage(getString(R.string.del_event_confirmation))
                        .setNegativeButton(R.string.no) { _, _ -> }
                        .setPositiveButton(R.string.ok) { _, _ ->
                            // TODO: delete item in DB
                            val events = eventsModel.events.value!!
                            events.removeAt(eventsModel.eventIndex)
                            // Rebind
                            eventsModel.events.value = events
                            exitNavigationFragment()
                        }
                    dialog.show()
                }
                true
            }
            R.id.menu_item_edit -> {
                if (!eventsModel.isNewEvent() && eventsModel.eventIndex >= 0) {
                    eventsModel.setEditing(true)
                }
                true
            }
            R.id.menu_item_discard -> {
                if (!eventsModel.isNewEvent() && eventsModel.eventIndex >= 0) {
                    eventsModel.setEditing(false)
                    // Resume previous status && no need to prepare
                    itemModel.item.value = eventsModel.getCurrentEvent().copy()
                    Toast.makeText(requireContext(), getString(R.string.restore_prev_state), Toast.LENGTH_SHORT).show()
                } else {
                    throw java.lang.IllegalStateException()
                }
                true
            }
            R.id.menu_item_submit -> {
                prepareItemRebind()
                // TODO: 检查输入合法性

                // TODO: make changes in DB
                if (eventsModel.isNewEvent()) {
                    val events = eventsModel.events.value!!
                    events.add(itemModel.getCurrentEvent())
                    // Rebind events
                    eventsModel.events.value = events
                    exitNavigationFragment()
                } else {
                    // Submitting an existing event
                    val cur = eventsModel.getCurrentEvent()
                    cur.copyFrom(itemModel.getCurrentEvent())
                    // Rebind
                    eventsModel.currentEvent.value = cur
                    exitNavigationFragment()
                }
                true
            }
            else -> false
        }
    }

    override fun onResume() {
        super.onResume()
        if (shouldCopyItem) {
            // Copy EventItem
            itemModel.item.value = eventsModel.getCurrentEvent().copy()
            shouldCopyItem = false
            firstTimeForIcons = true
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_event_item, container, false)
        val title = if (eventsModel.isNewEvent()) getString(R.string.create_event)
                    else getString(R.string.modify_event)
        setNavigation(view, title)
        setUpView(view)
        return view
    }

    private fun setUpView(view: View) {
        // Add observations
        itemModel.item.observe(viewLifecycleOwner, {
            it?.let { item ->
                // 更新日期
                val simpleFormat = SimpleDateFormat("yyyy/MM/dd", Locale.US)
                val dateText = view.findViewById<EditText>(R.id.event_item_date_text)
                dateText.setText(simpleFormat.format(item.startingDate))

                // 更新周期的滚轮
                view.event_item_cycle_unit_spinner.setSelection(
                    when (item.unit) {
                        EventItem.CycleUnit.DAY -> 0
                        EventItem.CycleUnit.MONTH -> 1
                        EventItem.CycleUnit.YEAR -> 2
                    }
                )

                // 更新周期数显示
                view.event_item_cycle_text.setText(item.cycle.toString())

                // 更新名称
                view.event_item_name_text.setText(item.name)

                // TODO: 更新模板提示，但首先需要在模板右边加上一个文本信息控件

                // TODO: 更新图标显示
                if (item.iconResID != null) {
                    view.event_item_icon_image.setImageDrawable(
                        ContextCompat.getDrawable(requireContext(), item.iconResID!!))
                } else {
                    view.event_item_icon_image.setImageDrawable(null)
                }
            }
        })

        eventsModel.editing.observe(viewLifecycleOwner, { editing ->
            // Recreate OptionsMenu
            this.requireActivity().invalidateOptionsMenu()
            // Set widgets to '$editing' mode
            view.event_item_date_button.isEnabled = editing
            view.event_item_cycle_text.isEnabled = editing
            view.event_item_cycle_unit_spinner.isEnabled = editing
            view.event_item_name_text.isEnabled = editing
            view.event_item_icon_recycler_view.apply {
                if (firstTimeForIcons and !editing) {
                    this.alpha = 0.0f
                } else {
                    animate().alpha(if (editing) 1.0f else 0.0f).setDuration(100)
                }
                firstTimeForIcons = false
            }
            view.event_item_template_button.isEnabled = editing
        })

        // Add listeners
        view.event_item_date_button.setOnClickListener {
            val dialog = DatePickerDialog(requireContext())
            dialog.setOnDateSetListener { view, year, month, dayOfMonth ->
                // 生成日期数据
                val cal = Calendar.getInstance()
                cal.set(year, month, dayOfMonth)
                val date = Date(cal.timeInMillis)

                // Get current event
                val eventItem = itemModel.getCurrentEvent()
                eventItem.startingDate = date

                // Rebind
                prepareItemRebind()
                itemModel.item.value = eventItem
            }
            dialog.show()
        }

        view.event_item_cycle_unit_spinner.adapter = ArrayAdapter<String>(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item, units
        )
        view.event_item_cycle_unit_spinner.onItemSelectedListener = this

        // Get Icon RecyclerView
        val iconRecyclerView = view.event_item_icon_recycler_view
        iconRecyclerView.overScrollMode = View.OVER_SCROLL_ALWAYS
        // Count of span
        val display = requireActivity().windowManager.defaultDisplay
        val size = Point()
        display.getRealSize(size)
        val w = ScreenMetrics.pxToDp(display.width)
        // TODO: get rid of hard code
        // CAUTION: Hard code!
        val spanCount = ((w - 16.0f * 2 + 8.0f) / (44.0f + 8.0f)).toInt()
        val layoutMgr = object: GridLayoutManager(requireContext(), spanCount) {}
        iconRecyclerView.layoutManager = layoutMgr
        // Adjust space
        val spacingInPixels = resources.getDimensionPixelSize(R.dimen.icon_recycler_view_space)
        iconRecyclerView.addItemDecoration(SpaceItemDecoration(spacingInPixels, spanCount))
        iconRecyclerView.adapter = IconItemAdapter(iconIDs, this)
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        val event = itemModel.getCurrentEvent()
        event.unit = when(position) {
            0 -> EventItem.CycleUnit.DAY
            1 -> EventItem.CycleUnit.MONTH
            2 -> EventItem.CycleUnit.YEAR
            else -> throw RuntimeException("event_item_cycle_unit_spinner: Unit out of bound.")
        }
        // Spinner is aware of the change. But list of last fragment is not.
        // Force refresh
        prepareItemRebind()
        itemModel.item.value = event
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
        //
    }

    // 设定当前的event，因为在绑定之前需要拉取当前文本框等没有回调的控件
    private fun prepareItemRebind() {
        val view = requireView()
        val name = view.event_item_name_text.text.toString()
        val cycle = try {
            view.event_item_cycle_text.text.toString().toInt()
        } catch (e: Exception) {
            eventsModel.getCurrentEvent().cycle
        }
        val eventItem = itemModel.getCurrentEvent()
        eventItem.cycle = cycle
        eventItem.name = name
        // Do not rebind here
        // We do not want double rebind actions
    }

    override fun onPositionClick(position: Int) {
        // Do not perform change when view is not being edited
        if (!eventsModel.isEditing()) {
            return
        }

        // Change iconResID
        val e = itemModel.getCurrentEvent()
        e.iconResID = iconIDs[position]
        prepareItemRebind()
        itemModel.item.value = e
    }
}

//fun View.setAllEnabled(enabled: Boolean) {
//    isEnabled = enabled
//    if (this is ViewGroup) this.children.forEach { child -> child.setAllEnabled(enabled) }
//}