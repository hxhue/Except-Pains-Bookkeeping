package com.example.epledger.detail

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.fragment.app.activityViewModels
import com.example.epledger.R
import com.example.epledger.model.DatabaseViewModel
import com.example.epledger.nav.NavigationFragment
import com.example.epledger.qaction.screenshot.ScreenshotUtils
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.android.synthetic.main.activity_detail_content.*
import kotlinx.android.synthetic.main.activity_detail_content.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.Exception
import java.lang.RuntimeException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

/**
 * A fragment to input all information of an expenditure or income record in database.
 * To use this fragment, you need:
 * 1. Get a DetailRecord instance. You may fetch a record in **IO thread**
 *    and transform it into DetailRecord.
 * 2. Create this fragment in **Main thread** (All follow steps are suggested performing
 *    in the Main thread.)
 * 3. Call bindRecord()
 * 4. Call setDetailRecordMsgReceiver() to set a listener.
 * 5. Call NavigationFragment.pushToStack(activity.supportFragmentManager, newFragment)
 */
class RecordDetailFragment:
    NavigationFragment(), AdapterView.OnItemSelectedListener {
    private val dbModel: DatabaseViewModel by activityViewModels()

    companion object {
        // 没有被注明的来源或种类始终放在0号位
        const val UNSPECIFIED_ITEM_POSITION = 0
    }

    // 创建一个空记录
    private var bindingRecord: DetailRecord? = null

    /**
     * 当前的记录是否正在被修改。
     */
    private var editing = false

    fun isEditing(): Boolean {
        return editing
    }

    /**
     * 保存的记录副本，用来恢复。
     */
    private var recordCopy: DetailRecord? = null

    /**
     * 在创建后必须被调用一次。
     */
    fun bindRecord(record: DetailRecord) {
        editing = (record.ID == null)
        recordCopy = record.getCopy()
        bindingRecord = record
    }

    fun getBindingRecord(): DetailRecord {
        if (bindingRecord == null) {
            throw RuntimeException("Record is not bound yet. You can't get a record out of null.")
        }
        return bindingRecord!!
    }

    // 种类和来源属性需要绑定在滚轮上面
    private lateinit var sources: ArrayList<String>
    private lateinit var categories: ArrayList<String>
    private lateinit var sourceSpinnerAdapter: ArrayAdapter<String>
    private lateinit var categorySpinnerAdapter: ArrayAdapter<String>

    interface DetailRecordMsgReceiver {
        fun onDetailRecordSubmitted(record: DetailRecord)
        fun onDetailRecordSDeleted(record: DetailRecord)
    }

    private var receiver: DetailRecordMsgReceiver? = null

    fun setDetailRecordMsgReceiver(rv: DetailRecordMsgReceiver?) {
        this.receiver = rv
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        require(parent != null)
        val bindingRecord = bindingRecord!!
        when (parent.id) {
            R.id.qa_src_spinner -> {
                Log.d("RecordDetailFragment",
                        "onItemSelected(): sourceSpinner has (${sources[position]}) selected.")
                bindingRecord.source = sources[position]
            }
            R.id.qa_type_spinner -> {
                Log.d("RecordDetailFragment",
                        "onItemSelected(): categorySpinner has (${categories[position]}) selected.")
                bindingRecord.category = categories[position]
            }
        }
    }

    override fun onNothingSelected(parent: AdapterView<*>?) { /* Nothing */ }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.activity_detail_scrollable_content, container, false)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Add observers
        dbModel.categories.observe(viewLifecycleOwner, {
            categories.clear()
            categories.add(getString(R.string.unspecified))
            categories.addAll(it.map { category -> category.name })
            categorySpinnerAdapter.notifyDataSetChanged()
        })

        dbModel.sources.observe(viewLifecycleOwner, {
            sources.clear()
            sources.add(getString(R.string.unspecified))
            sources.addAll(it.map { src -> src.name })
            sourceSpinnerAdapter.notifyDataSetChanged()
        })
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        // Late Initialization for spinner data
        val initialString = getString(R.string.unspecified)
        sources = arrayListOf(initialString)
        categories = arrayListOf(initialString)
        categorySpinnerAdapter = ArrayAdapter<String>(requireContext(),
                android.R.layout.simple_spinner_dropdown_item, categories)
        sourceSpinnerAdapter = ArrayAdapter<String>(requireContext(),
                android.R.layout.simple_spinner_dropdown_item, sources)
    }

    override fun onResume() {
        val bindingRecord = bindingRecord!!
        val view = requireView()

        // 设置标题
        val newTitle =  if (bindingRecord.ID == null) requireContext().getString(R.string.detail_page_title_create)
                        else requireContext().getString(R.string.detail_page_title_modify)
        setNavigation(view, newTitle)

        // 设置页面内容
        setUpDetailView(view)
        syncViewWithRecord(view)
        super.onResume()
    }

    /**
     * 从活动中取得信息。用来布局等。只能够用在界面完成初始化之后。
     */
    private fun syncViewWithRecord(view: View) {
        val bindingRecord = bindingRecord!!

        // 更新截图组件显示，由于在更新按钮时要用到这个状态，因此要先更新这里
        syncScreenshot(view)

        // 更新控件可操作性
        setContentModifiable(view, editing)

        // 更新标星属性
        view.detail_star.isChecked = bindingRecord.starred

        // 更新日期组件显示
        if (bindingRecord.startingDate == null) {
            clearDate(view)
        } else {
            val date = bindingRecord.startingDate!!
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
        if (bindingRecord.category == null) {
            categorySpinner.setSelection(UNSPECIFIED_ITEM_POSITION)
        } else {
            val index = categories.indexOf(bindingRecord.category!!)
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
            val dialog = DatePickerDialog(requireContext())
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
            val dialog = TimePickerDialog(requireContext(), { _, hour, minute ->
                setTime(view, hour, minute)
            }, h, m, true)
            dialog.show()
        }

        // 设置种类和来源的滚轮
        val sourceSpinner = view.findViewById<Spinner>(R.id.detail_src_spinner)
        sourceSpinner.adapter = sourceSpinnerAdapter
        sourceSpinner.onItemSelectedListener = this

        val categorySpinner = view.findViewById<Spinner>(R.id.detail_type_spinner)
        categorySpinner.adapter = categorySpinnerAdapter
        categorySpinner.onItemSelectedListener = this

        // 设置截图按钮的功能
        val removeButton = view.findViewById<Button>(R.id.detail_screenshot_remove_button)
        removeButton.setOnClickListener {
            clearScreenshot()
        }
    }

    /**
     * 设置是否可以修改。在创建模式下或正在修改模式下，视图应当是可以修改的。否则视图只能显示不能够修改。
     * 注意：应当在setScreenshot至少被调用一次后调用。
     */
    private fun setContentModifiable(view: View, editing: Boolean) {
        val bindingRecord = bindingRecord!!

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
        view.detail_star.isEnabled = editing

        val buttonTextColor = requireContext().getColor(if (editing) R.color.lightColorPrimary else R.color.silver)
        timeSelectingButton.setTextColor(buttonTextColor)
        dateSelectingButton.setTextColor(buttonTextColor)

        // 只有在有截图的情况下，截图按钮才会跟着变化
        if (bindingRecord.screenshot != null) {
            screenshotRemovingButton.isEnabled = editing
            screenshotRemovingButton.setTextColor(buttonTextColor)
        }

        // 设置moneyEditText获取焦点
        if (this.editing) {
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
        val bindingRecord = bindingRecord!!
        if (editing) {
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
            R.id.menu_item_submit -> {
                Toast.makeText(context, "Submit", Toast.LENGTH_SHORT).show()
                // 拉取更改
                prepareRecord()
                // TODO: 提交数据库
                receiver?.onDetailRecordSubmitted(bindingRecord!!)
                Log.d("prepareRecord", bindingRecord.toString())
                // 返回
                super.exitNavigationFragment()
                true
            }
            R.id.menu_item_discard -> {
                Toast.makeText(context, getString(R.string.discard_changes), Toast.LENGTH_SHORT).show()
                editing = false
                // 复原做出的修改
                recordCopy!!.copyTo(bindingRecord!!)
                syncViewWithRecord(requireView())
                true
            }
            R.id.menu_item_edit -> {
                editing = true
                setContentModifiable(requireView(), true)
                true
            }
            R.id.menu_item_delete -> {
                val dialog = MaterialAlertDialogBuilder(requireContext())
                        .setMessage(getString(R.string.rec_del_confirm))
                        .setPositiveButton(getString(R.string.sure)) { _, _ ->
                            // TODO：删除该条记录
                            receiver?.onDetailRecordSDeleted(bindingRecord!!)
                            // 返回
                            super.exitNavigationFragment()
                        }
                        .setNegativeButton(getString(R.string.no)) { _, _ -> /* Nothing */ }
                dialog.show()
                true
            }
            else -> false
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        prepareRecord()
        outState.putBoolean("editing", editing)
        outState.putParcelable("record", bindingRecord)
        outState.putParcelable("recordCopy", recordCopy)
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        val fragment = this
        savedInstanceState?.apply {
            fragment.editing = getBoolean("editing")
            val record = getParcelable<DetailRecord>("record")
            if (record != null) {
                fragment.bindingRecord = record
            }
            val copy = getParcelable<DetailRecord>("recordCopy")
            if (copy != null) {
                fragment.recordCopy = copy
            }
        }
    }
}

/**
 * 设置保存的记录的时间，并更新组件的视图。
 */
fun RecordDetailFragment.setTime(view: View, hourOfDay: Int, minuteOfHour: Int) {
    // 保存记录
    val bindingRecord = getBindingRecord()
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
    val bindingRecord = getBindingRecord()
    // 生成日期数据
    val cal = Calendar.getInstance()
    cal.set(year, month, dayOfMonth)
    val date = Date(cal.timeInMillis)
    // 保存记录
    bindingRecord.startingDate = date
    // 更新视图
    val simpleFormat = SimpleDateFormat("yyyy/MM/dd", Locale.US)
    val dateText = view.findViewById<EditText>(R.id.detail_date_text)
    dateText.setText(simpleFormat.format(date))
}

/**
 * 利用model设置截图和相关组件。必须被调用一次以保证界面的初始化状态合理，无论是空还是有一张截图。
 */
fun RecordDetailFragment.syncScreenshot(view: View) {
    val removeButton = view.findViewById<Button>(R.id.detail_screenshot_remove_button)
    val promptText = view.findViewById<TextView>(R.id.detail_screenshot_empty_prompt)
    val imageView = view.findViewById<ImageView>(R.id.detail_screenshot)

    var bitmap: Bitmap? = null
    val record = getBindingRecord()

    GlobalScope.launch {
        withContext(Dispatchers.IO) {
            if (record.screenshot != null) {
                bitmap = record.screenshot
            } else if (record.screenshotPath != null) {
                try {
                    record.screenshot = ScreenshotUtils.loadBitmap(requireContext(),
                            record.screenshotPath!!)
                } catch (e: Exception) {
                    record.screenshot = null
                } finally {
                    bitmap = record.screenshot
                }
            }
            Handler(Looper.getMainLooper()).post {
                record.screenshot = bitmap
                if (bitmap == null) {
                    promptText.setText(R.string.empty_prompt)
                    promptText.setTextColor(requireContext().getColor(R.color.silver))
                    removeButton.isEnabled = false
                    removeButton.setTextColor(requireContext().getColor(R.color.silver))
                    imageView.setImageDrawable(null)
                } else {
                    imageView.setImageBitmap(bitmap)
                    promptText.setText("")
                    removeButton.isEnabled = isEditing()
                    removeButton.setTextColor(  if (isEditing()) requireContext().getColor(R.color.lightColorPrimary)
                                                else requireContext().getColor(R.color.silver))
                }
            }
        }
    }
}

fun RecordDetailFragment.clearScreenshot() {
    val bindingRecord = getBindingRecord()
    bindingRecord.screenshot = null
    bindingRecord.screenshotPath = null
    syncScreenshot(requireView())
}

/**
 * 从组件上拉取信息构成DetailRecord。
 */
fun RecordDetailFragment.prepareRecord() {
    val bindingRecord = getBindingRecord()
    // 拉取金额
    bindingRecord.amount = try {
        detail_money_text.text.toString().toDouble()
    } catch (e: Exception) {
        null
    }

    // 拉取附注
    bindingRecord.note = detail_note_text.text.toString()

    // 拉取标星属性
    bindingRecord.starred = detail_star.isChecked
}