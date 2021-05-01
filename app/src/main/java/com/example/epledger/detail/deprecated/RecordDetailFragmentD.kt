package com.example.epledger.detail.deprecated

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.example.epledger.R
import com.example.epledger.detail.DetailRecord
import java.text.SimpleDateFormat
import java.util.*

// 没有被注明的来源或种类始终放在0号位
const val UNSPECIFIED_ITEM_POSITION = 0

class RecordDetailFragmentD: Fragment(), AdapterView.OnItemSelectedListener,
        DetailFragmentInterface {
    // 为了方便因此就先创建了一个对象使得它从不为空，然后再赋值，代价是白白浪费了一个记录大小的空间。
    // 这个记录不能完全追踪所有的控件值，但是能够追踪一大部分。同时在活动发起要求时，控件信息会根据记录信息更新。
    var bindingRecord = DetailRecord()

    // 种类和来源属性需要绑定在滚轮上面。
    private val sources: Array<String> =  arrayOf("Unspecified", "Alipay", "Wechat", "Cash")
    private val categories: Array<String> = arrayOf("Unspecified", "Daily", "Transportation", "Study")

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.activity_detail_scrollable_content, container, false)
        setupDetailView(view)
        return view
    }

    override fun onResume() {
        super.onResume()
        updateUI()
    }

    /**
     * 将所有的剩余信息收集放到ledgerRecord中。因为依赖了view，只能够在view创建完成后调用。
     */
    override fun prepareRecordToCommit() {
        TODO("Not yet implemented")
    }

    /**
     * 从活动中取得信息。用来布局等。只能够用在界面完成初始化之后。
     */
    override fun updateUI() {
        val activity = activity as RecordDetailActivity
        val view = requireView()

        // 向RecordDetailActivity注册自己
        activity.updatableFragment = this

        // 同步ledgerRecord引用（同步多次没有关系，因为这个操作具有幂等性）
        bindingRecord = activity.bindingRecord

        // 更新截图组件显示，由于在更新按钮时要用到这个状态，因此要先更新这里
        setScreenshot(view, bindingRecord.screenshot)

        // 更新控件可操作性
        setContentModifiable(requireView(), activity.recordBeingEdited)

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

    /**
     * 对view做出修改。因为view是引用类型的变量，因此不需要返回值。
     */
    private fun setupDetailView(view: View) {
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

    override fun onNothingSelected(parent: AdapterView<*>?) {}

    /**
     * 设置是否可以修改。在创建模式下或正在修改模式下，视图应当是可以修改的。否则视图只能显示不能够修改。
     * 注意：应当在setScreenshot至少被调用一次后调用。
     */
    fun setContentModifiable(view: View, flag: Boolean) {
        val moneyEditText = view.findViewById<EditText>(R.id.detail_money_text)
        val noteEditText = view.findViewById<EditText>(R.id.detail_note_text)
        val screenshotRemovingButton = view.findViewById<Button>(R.id.detail_screenshot_remove_button)
        val timeSelectingButton = view.findViewById<Button>(R.id.detail_time_button)
        val dateSelectingButton = view.findViewById<Button>(R.id.detail_date_button)
        val srcSpinner = view.findViewById<Spinner>(R.id.detail_src_spinner)
        val typeSpinner = view.findViewById<Spinner>(R.id.detail_type_spinner)

        moneyEditText.isEnabled = flag
        noteEditText.isEnabled = flag
        timeSelectingButton.isEnabled = flag
        dateSelectingButton.isEnabled = flag
        srcSpinner.isEnabled = flag
        typeSpinner.isEnabled = flag

        val buttonTextColor = requireContext().getColor(if (flag) R.color.lightColorPrimary else R.color.silver)
        timeSelectingButton.setTextColor(buttonTextColor)
        dateSelectingButton.setTextColor(buttonTextColor)

        // 只有在有截图的情况下，截图按钮才会跟着变化
        if (bindingRecord.screenshot != null) {
            screenshotRemovingButton.isEnabled = flag
            screenshotRemovingButton.setTextColor(buttonTextColor)
        }
    }
}

/**
 * 设置保存的记录的时间，并更新组件的视图。
 */
fun RecordDetailFragmentD.setTime(view: View, hourOfDay: Int, minuteOfHour: Int) {
    // 保存记录
    bindingRecord.hourOfDay = hourOfDay
    bindingRecord.minuteOfHour = minuteOfHour
    val str = String.format("%02d:%02d", hourOfDay, minuteOfHour)
    // 更新视图
    val timeText = view.findViewById<EditText>(R.id.detail_time_text)
    timeText.setText(str)
}

fun RecordDetailFragmentD.clearTime(view: View) {
    val timeText = view.findViewById<EditText>(R.id.detail_time_text)
    timeText.setText("")
}

fun RecordDetailFragmentD.clearDate(view: View) {
    val dateText = view.findViewById<EditText>(R.id.detail_date_text)
    dateText.setText("")
}

/**
 * 设置保存的记录的日期，并更新组件的视图。
 */
fun RecordDetailFragmentD.setDate(view: View, year: Int, month: Int, dayOfMonth: Int) {
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
fun RecordDetailFragmentD.setScreenshot(view: View, bitmap: Bitmap?) {
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