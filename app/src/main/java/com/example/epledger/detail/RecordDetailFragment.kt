package com.example.epledger.detail

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.*
import com.example.epledger.R
import com.example.epledger.nav.NavigationFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.android.synthetic.main.activity_detail_content.*
import kotlinx.android.synthetic.main.activity_detail_content.view.*
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*


class RecordDetailFragment(val bindingRecord: DetailRecord):
    NavigationFragment(), AdapterView.OnItemSelectedListener {
    companion object {
        // 没有被注明的来源或种类始终放在0号位
        const val UNSPECIFIED_ITEM_POSITION = 0
    }

    /**
     * 当前的记录是否正在被修改。
     */
    private var recordBeingEdited = (bindingRecord.ID == null)

    /**
     * 保存的记录副本，用来恢复。
     */
    val recordCopy = bindingRecord.getCopy()

    // 种类和来源属性需要绑定在滚轮上面
    private val sources: Array<String> =  arrayOf("Unspecified", "Alipay", "Wechat", "Cash")
    private val categories: Array<String> = arrayOf("Unspecified", "Daily", "Transportation", "Study")

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        require(parent != null)
        when (parent.id) {
            R.id.qa_src_spinner -> {
                Log.d("RecordDetailFragment",
                        "onItemSelected(): sourceSpinner has (${sources[position]}) selected.")
                bindingRecord.source = sources[position]
            }
            R.id.qa_type_spinner -> {
                Log.d("RecordDetailFragment",
                        "onItemSelected(): categorySpinner has (${categories[position]}) selected.")
                bindingRecord.type = categories[position]
            }
        }
    }

    override fun onNothingSelected(parent: AdapterView<*>?) { /* Nothing */ }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.activity_detail_scrollable_content, container, false)

        // 设置标题和返回箭头
        val newTitle = if (bindingRecord.ID == null) requireContext().getString(R.string.detail_page_title_create)
            else requireContext().getString(R.string.detail_page_title_modify)
        setNavigation(view, newTitle, view.detail_note_text, view.detail_money_text)

        // 设置页面内容
        setUpDetailView(view)
        syncViewWithRecord(view)

        return view
    }

    override fun onResume() {
        super.onResume()
    }

    /**
     * 从活动中取得信息。用来布局等。只能够用在界面完成初始化之后。
     */
    fun syncViewWithRecord(view: View) {
        // 更新截图组件显示，由于在更新按钮时要用到这个状态，因此要先更新这里
        setScreenshot(view, bindingRecord.screenshot)

        // 更新控件可操作性
        setContentModifiable(view, recordBeingEdited)

        // 更新日期组件显示
        if (bindingRecord.date == null) {
            clearDate(view)
        } else {
            val date = bindingRecord.date!!
            val cal = Calendar.getInstance()
            cal.time = date
            val month = cal.get(Calendar.MONTH)
            val year = cal.get(Calendar.YEAR)
            val dayOfMonth = cal.get(Calendar.DAY_OF_MONTH)
            setDate(view, year, month, dayOfMonth)
        }

        // 更新时间组件显示
        val hr = bindingRecord.hourOfDay
        val m = bindingRecord.minuteOfHour
        if (hr != null && m != null) {
            setTime(view, hr, m)
        } else {
            clearTime(view)
        }

        // 更新金额组件显示
        val moneyText = view.findViewById<EditText>(R.id.detail_money_text)
        if (bindingRecord.amount == null) {
            moneyText.setText("")
        } else {
            moneyText.setText(bindingRecord.amount.toString())
        }

        // 更新备注组件显示
        val noteText = view.findViewById<EditText>(R.id.detail_note_text)
        if (bindingRecord.note == null) {
            noteText.setText("")
        } else {
            noteText.setText(bindingRecord.note!!)
        }

        // 更新来源组件
        val sourceSpinner = view.findViewById<Spinner>(R.id.detail_src_spinner)
        if (bindingRecord.source == null) {
            sourceSpinner.setSelection(UNSPECIFIED_ITEM_POSITION)
        } else {
            val index = sources.indexOf(bindingRecord.source!!)
            if (index < 0) {
                sourceSpinner.setSelection(UNSPECIFIED_ITEM_POSITION)
            } else {
                sourceSpinner.setSelection(index)
            }
        }

        // 更新种类组件
        val categorySpinner = view.findViewById<Spinner>(R.id.detail_type_spinner)
        if (bindingRecord.type == null) {
            categorySpinner.setSelection(UNSPECIFIED_ITEM_POSITION)
        } else {
            val index = sources.indexOf(bindingRecord.type!!)
            if (index < 0) {
                categorySpinner.setSelection(UNSPECIFIED_ITEM_POSITION)
            } else {
                categorySpinner.setSelection(index)
            }
        }
    }

    private fun setUpDetailView(view: View) {
        // 获取当前的时间
        val now = Date()
        val cal = Calendar.getInstance()
        cal.time = now

        // 设置日期选择按钮回调
        val dateButton = view.findViewById<Button>(R.id.detail_date_button)
        dateButton.setOnClickListener {
            val dialog = DatePickerDialog(requireContext(), R.style.Theme_DatePicker_NoWhiteExtraSpace)
            dialog.setOnDateSetListener { _, year, month, dayOfMonth ->
                this.setDate(view, year, month, dayOfMonth)
            }
            dialog.setOnShowListener {
                val color = requireContext().getColor(R.color.lightColorPrimary)
                dialog.getButton(DatePickerDialog.BUTTON_POSITIVE).setTextColor(color)
                dialog.getButton(DatePickerDialog.BUTTON_NEGATIVE).setTextColor(color)
            }
            dialog.show()
        }

        // 时间组件
        val h = cal.get(Calendar.HOUR_OF_DAY)
        val m = cal.get(Calendar.MINUTE)

        // 设置时间选择按钮回调
        val timeButton = view.findViewById<Button>(R.id.detail_time_button)
        timeButton.setOnClickListener {
            val dialog = TimePickerDialog(requireContext(), R.style.Theme_TimePicker, { _, hour, minute ->
                setTime(view, hour, minute)
            }, h, m, true)
            dialog.setOnShowListener {
                val color = requireContext().getColor(R.color.lightColorSecondary)
                dialog.getButton(TimePickerDialog.BUTTON_POSITIVE).setTextColor(color)
                dialog.getButton(TimePickerDialog.BUTTON_NEGATIVE).setTextColor(color)
            }
            dialog.show()
        }

        // 设置种类和来源的滚轮
        val sourceSpinner = view.findViewById<Spinner>(R.id.detail_src_spinner)
        sourceSpinner.adapter = ArrayAdapter<String>(requireContext(),
                android.R.layout.simple_spinner_dropdown_item, sources)
        sourceSpinner.onItemSelectedListener = this

        val categorySpinner = view.findViewById<Spinner>(R.id.detail_type_spinner)
        categorySpinner.adapter = ArrayAdapter<String>(requireContext(),
                android.R.layout.simple_spinner_dropdown_item, categories)
        categorySpinner.onItemSelectedListener = this

        // 设置截图按钮的功能
        val removeButton = view.findViewById<Button>(R.id.detail_screenshot_remove_button)
        removeButton.setOnClickListener {
            setScreenshot(view, null)
        }
    }

    /**
     * 设置是否可以修改。在创建模式下或正在修改模式下，视图应当是可以修改的。否则视图只能显示不能够修改。
     * 注意：应当在setScreenshot至少被调用一次后调用。
     */
    private fun setContentModifiable(view: View, editing: Boolean) {
        val moneyEditText = view.findViewById<EditText>(R.id.detail_money_text)
        val noteEditText = view.findViewById<EditText>(R.id.detail_note_text)
        val screenshotRemovingButton = view.findViewById<Button>(R.id.detail_screenshot_remove_button)
        val timeSelectingButton = view.findViewById<Button>(R.id.detail_time_button)
        val dateSelectingButton = view.findViewById<Button>(R.id.detail_date_button)
        val srcSpinner = view.findViewById<Spinner>(R.id.detail_src_spinner)
        val typeSpinner = view.findViewById<Spinner>(R.id.detail_type_spinner)

        moneyEditText.isEnabled = editing
        noteEditText.isEnabled = editing
        timeSelectingButton.isEnabled = editing
        dateSelectingButton.isEnabled = editing
        srcSpinner.isEnabled = editing
        typeSpinner.isEnabled = editing

        val buttonTextColor = requireContext().getColor(if (editing) R.color.lightColorPrimary else R.color.silver)
        timeSelectingButton.setTextColor(buttonTextColor)
        dateSelectingButton.setTextColor(buttonTextColor)

        // 只有在有截图的情况下，截图按钮才会跟着变化
        if (bindingRecord.screenshot != null) {
            screenshotRemovingButton.isEnabled = editing
            screenshotRemovingButton.setTextColor(buttonTextColor)
        }

        // 设置moneyEditText获取焦点
        if (recordBeingEdited) {
            moneyEditText.requestFocus()
            val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
            imm?.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0)
        }

        // 重设OptionsMenu
        requireActivity().invalidateOptionsMenu()
    }

    // 设置菜单
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        // Clear old options
        menu.clear()

        // Add new options
        if (recordBeingEdited) {
            if (bindingRecord.ID == null) {
                inflater.inflate(R.menu.detail_menu_creating, menu)
            } else {
                inflater.inflate(R.menu.detail_menu_editing, menu)
            }
        } else {
            inflater.inflate(R.menu.detail_menu_non_editing, menu)
        }
    }

    // 设置菜单按钮的点击效果
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return super.onOptionsItemSelected(item) or when(item.itemId) {
            R.id.menu_item_detail_submit -> {
                Toast.makeText(context, "Submit", Toast.LENGTH_SHORT).show()
                // 拉取更改
                prepareRecord()
                // TODO: 提交数据库
                Log.d("prepareRecord", bindingRecord.toString())
                // 返回
                super.onBackPressed()
                true
            }
            R.id.menu_item_detail_discard -> {
                Toast.makeText(context, getString(R.string.discard_changes), Toast.LENGTH_SHORT).show()
                recordBeingEdited = false
                // 复原做出的修改
                recordCopy.copyTo(bindingRecord)
                syncViewWithRecord(requireView())
                true
            }
            R.id.menu_item_detail_edit -> {
                recordBeingEdited = true
                setContentModifiable(requireView(), true)
                true
            }
            R.id.menu_item_detail_delete -> {
                val dialog = MaterialAlertDialogBuilder(requireContext())
                        .setMessage(getString(R.string.rec_del_confirm))
                        .setPositiveButton(getString(R.string.sure)) { _, _ ->
                            // TODO：删除该条记录
                            // 返回
                            super.onBackPressed()
                        }
                        .setNegativeButton(getString(R.string.no)) { _, _ -> /* Nothing */ }
                dialog.show()
                true
            }
            else -> false
        }
    }
}

/**
 * 设置保存的记录的时间，并更新组件的视图。
 */
fun RecordDetailFragment.setTime(view: View, hourOfDay: Int, minuteOfHour: Int) {
    // 保存记录
    bindingRecord.hourOfDay = hourOfDay
    bindingRecord.minuteOfHour = minuteOfHour
    val str = String.format("%02d:%02d", hourOfDay, minuteOfHour)
    // 更新视图
    val timeText = view.findViewById<EditText>(R.id.detail_time_text)
    timeText.setText(str)
}

fun RecordDetailFragment.clearTime(view: View) {
    val timeText = view.findViewById<EditText>(R.id.detail_time_text)
    timeText.setText("")
}

fun RecordDetailFragment.clearDate(view: View) {
    val dateText = view.findViewById<EditText>(R.id.detail_date_text)
    dateText.setText("")
}

/**
 * 设置保存的记录的日期，并更新组件的视图。
 */
fun RecordDetailFragment.setDate(view: View, year: Int, month: Int, dayOfMonth: Int) {
    // 生成日期数据
    val cal = Calendar.getInstance()
    cal.set(year, month, dayOfMonth)
    val date = Date(cal.timeInMillis)
    // 保存记录
    bindingRecord.date = date
    // 更新视图
    val simpleFormat = SimpleDateFormat("yyyy/MM/dd", Locale.US)
    val dateText = view.findViewById<EditText>(R.id.detail_date_text)
    dateText.setText(simpleFormat.format(date))
}

/**
 * 设置截图和相关组件。必须被调用一次以保证界面的初始化状态合理，无论是空还是有一张截图。
 */
fun RecordDetailFragment.setScreenshot(view: View, bitmap: Bitmap?) {
    val removeButton = view.findViewById<Button>(R.id.detail_screenshot_remove_button)
    val promptText = view.findViewById<TextView>(R.id.detail_screenshot_empty_prompt)
    val imageView = view.findViewById<ImageView>(R.id.detail_screenshot)
    bindingRecord.screenshot = bitmap
    if (bitmap == null) {
        promptText.setText(R.string.empty_prompt)
        promptText.setTextColor(requireContext().getColor(R.color.silver))
        imageView.setImageDrawable(null)
        removeButton.isEnabled = false
        removeButton.setTextColor(requireContext().getColor(R.color.silver))
    } else {
        imageView.setImageBitmap(bitmap)
        promptText.setText("")
        removeButton.isEnabled = true
        removeButton.setTextColor(requireContext().getColor(R.color.lightColorPrimary))
    }
}

/**
 * 从组件上拉取信息构成DetailRecord。
 */
fun RecordDetailFragment.prepareRecord() {
    // 拉取金额
    bindingRecord.amount = try {
        detail_money_text.text.toString().toDouble()
    } catch (e: Exception) {
        null
    }

    // 拉取附注
    bindingRecord.note = detail_note_text.text.toString()
}