package com.example.epledger.qaction

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.PreferenceManager
import com.example.epledger.R
import com.example.epledger.db.AppDatabase
import com.example.epledger.model.Record
import com.example.epledger.detail.RecordDetailFragment
import com.example.epledger.qaction.tool.PairTask
import com.example.epledger.qaction.tool.Store
import com.example.epledger.qaction.screenshot.ScreenshotUtils
import com.example.epledger.util.Fmt
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.switchmaterial.SwitchMaterial
import java.math.RoundingMode
import java.text.DecimalFormat
import java.util.*

/**
 * This is an activity for the popped-up card when you click our TileService toggle,
 * or quick actions in the notification center.
 */
class PopupActivity : AppCompatActivity(), PairTask.Noticeable, AdapterView.OnItemSelectedListener {
    private val cardViewModel: CardViewModel by viewModels()

    private var waitingEvent: Int = -1
    private lateinit var handler: Handler
    private var shown = false
    private var noPermissionAlert: AlertDialog? = null

    private var ledgerRecord = Record()

    // 用户的偏好设置
    private var briefMode = true
    private var screenshotAtStart = false

    // For spinners
    private lateinit var sources: ArrayList<String>
    private lateinit var types: ArrayList<String>
    private lateinit var sourcesSpinnerAdapter: ArrayAdapter<String>
    private lateinit var typesSpinnerAdapter: ArrayAdapter<String>

    // References of widgets(to fetch user inputs)
    private lateinit var screenshotSwitch: SwitchMaterial // Screenshot switch
    private lateinit var noteEditText: EditText           // EditText of note
    private lateinit var moneyEditText: EditText          // EditText of amount
    private lateinit var starToggleButton: ToggleButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Must be performed before the view's setup
        loadUserPreference()
        // 设置界面
        setupViews()
        // May need something from a set up view?
        Store.loadScreenSize(this)

        handler = Handler(Looper.myLooper()!!)

        // 根据用户偏好决定是否截屏
        if (screenshotAtStart) {
            performScreenshot()
        }

        // 注册observers
        registerObservers()
    }

    private fun registerObservers() {
        cardViewModel.categories.observeForever {
            types.clear()
            types.add(getString(R.string.unspecified))
            types.addAll(it.map { category -> category.name })
            typesSpinnerAdapter.notifyDataSetChanged()
        }

        cardViewModel.sources.observeForever {
            sources.clear()
            sources.add(getString(R.string.unspecified))
            sources.addAll(it.map { source -> source.name })
            sourcesSpinnerAdapter.notifyDataSetChanged()
        }
    }

    /**
     * Load user preferences from default shared preferences.
     */
    private fun loadUserPreference() {
        val perf = PreferenceManager.getDefaultSharedPreferences(this)
        briefMode = perf.getBoolean("qa_brief_mode", true)
        screenshotAtStart = perf.getBoolean("qa_screenshot_im", false)
    }

    /**
     * Perform screenshot action, and save the result to ledgerRecord.
     * May fail because of no privilege, then nothing happens.
     */
    private fun performScreenshot() {
        // Do not perform screenshot when it's already done
        synchronized(ledgerRecord) {
            if (ledgerRecord.screenshot != null) {
                return
            }
        }
        // Hide the interface
        hide()
        // Submit the screenshot task
        waitingEvent = PairTask.observe(this)
        ScreenshotUtils.shotScreen(this, waitingEvent)
    }

    // Hide the card
    private fun hide() {
        this.window.setDimAmount(0.0f)
        this.window.decorView.rootView.alpha = 0.0f
        shown = false
    }

    // Show the card
    private fun show() {
        handler.post {
            this.window.setDimAmount(0.3f)
            this.window.decorView.rootView.apply {
                animate().setDuration(100).alpha(1.0f)
            }
            shown = true
        }
    }

    // Receive screenshot result
    override fun onReceiveTaskResult(eid: Int, extra: Any?) {
        if (eid == waitingEvent) {
            show()
            val screenshot = extra as Bitmap
            Log.d("qaction.PopupActivity", "screenshot received as Bitmap")
            // Save the screenshot to ledgerRecord
            ledgerRecord.screenshot = screenshot
            // reset the toggle state
            handler.post {
                screenshotSwitch.isChecked = true
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // 处理屏幕截取权限的申请结果
        val hasPermission = ScreenshotUtils.processPermissionAskingResult(this, requestCode, resultCode, data)
        // 尝试恢复当前活动
        if (hasPermission) {
            performScreenshot()
        } else {
            promptNoPermission()
        }
    }

    // 提示没有权限信息
    private fun promptNoPermission() {
        val activity = this

        // 给出一个提示框
        val dialog = MaterialAlertDialogBuilder(activity)
                .setTitle(R.string.screenshot_failure)
                .setMessage(R.string.screenshot_failure_description)
                .setPositiveButton(R.string.ok) { _, _ -> activity.show() }
                .setOnDismissListener { activity.show() }
                .create()

        // 展示会话
        dialog.show()

        // Save reference
        this.noPermissionAlert = dialog
        // Reset state of toggle
        handler.post {
            screenshotSwitch.isChecked = false
        }
    }

    override fun onStop() {
        super.onStop()
        // 在停止（退出）时马上销毁，防止因为退出按键语义不同导致界面残留
        // 销毁是通过no_history标志完成的（见清单文件）
        this.noPermissionAlert?.dismiss() // dismiss以防止窗口泄露
        Log.d("PopupActivity", "onStop()")
    }

    override fun onDestroy() {
        // Dismiss the keyboard
        this.currentFocus?.let { view ->
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
            imm?.hideSoftInputFromWindow(view.windowToken, 0)
        }
        super.onDestroy()
        Log.d("PopupActivity", "onDestroy()")
    }

    // 设置好界面
    private fun setupViews() {
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.activity_popup_newrec)

        // 禁用黑暗模式
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        // Set up ScrollView
        (this.findViewById(R.id.qa_scrollview) as ScrollView).isScrollbarFadingEnabled = false

        // 创建时读取用户设置，这里就能够通过配置来选择不同的显示方式
        if (briefMode) {
            discardDatePickerWidget()
            discardTimePickerWidget()
        } else {
            setupDatePickerWidget()
            setupTimePickerWidget()
        }

        // Essential widgets
        setupSpinners()
        setupButtons()
        setupScreenshotOption()
        setupEditText()
        setupStarButton()
    }

    private fun setupStarButton() {
        starToggleButton = findViewById(R.id.qa_star) // Save reference
    }

    private fun setupEditText() {
        // Save reference
        noteEditText = findViewById<EditText>(R.id.qa_note_text)
        moneyEditText = findViewById<EditText>(R.id.qa_money_text)
        moneyEditText.apply {
            setText("-0.0")
            setSelection(text.length)
        }
        // 让moneyEditText获取焦点
        window.apply {
            clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM)
            setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
        }
        moneyEditText.requestFocus()
    }

    private fun setupSpinners() {
        // Late init here
        val unspecifiedString = getString(R.string.unspecified)
        sources = arrayListOf(unspecifiedString)
        types = arrayListOf(unspecifiedString)
        sourcesSpinnerAdapter = ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_dropdown_item, sources)
        typesSpinnerAdapter = ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_dropdown_item, types)

        // Set up sourceSpinner
        val sourceSpinner = findViewById<Spinner>(R.id.qa_src_spinner)
        sourceSpinner.adapter = sourcesSpinnerAdapter
        sourceSpinner.onItemSelectedListener = this

        // Set up typeSpinner
        val typeSpinner = findViewById<Spinner>(R.id.qa_type_spinner)
        typeSpinner.adapter = typesSpinnerAdapter
        typeSpinner.onItemSelectedListener = this
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        require(parent != null)
        if (parent.id == R.id.qa_src_spinner) {
            Log.d("qaction.PopupActivity",
                    "onItemSelected(): sourceSpinner has (${sources[position]}) selected.")
            if (position == RecordDetailFragment.UNSPECIFIED_ITEM_POSITION) {
                ledgerRecord.source = null
            } else {
                ledgerRecord.source = sources[position]
            }
        } else if (parent.id == R.id.qa_type_spinner) {
            Log.d("qaction.PopupActivity",
                    "onItemSelected(): typeSpinner has (${types[position]}) selected.")

            if (position == RecordDetailFragment.UNSPECIFIED_ITEM_POSITION) {
                ledgerRecord.category = null
            } else {
                ledgerRecord.category = types[position]
            }
        }
    }

    // Empty but necessary implementation
    override fun onNothingSelected(parent: AdapterView<*>?) {
        // Nothing goes there
    }

    private fun discardDatePickerWidget() {
        // Set data
        ledgerRecord.mDate = Date()

        // Set the view
        val view = findViewById<View>(R.id.qa_date_compo)
        view.layoutParams.height = 1

        val button = findViewById<Button>(R.id.qa_date_button)
        button.isEnabled = false
        button.layoutParams.height = 1

        val textDisplay = findViewById<EditText>(R.id.qa_date_text)
        textDisplay.layoutParams.height = 1

        val label = findViewById<TextView>(R.id.qa_date_label)
        label.layoutParams.height = 1
    }

    private fun discardTimePickerWidget() {
        // Set data
        ledgerRecord.mDate.let { date ->
            val cal = Calendar.getInstance()
            val h = cal.get(Calendar.HOUR_OF_DAY)
            val m = cal.get(Calendar.MINUTE)

            // After fetching current time, calendar can be reset
            cal.timeInMillis = date.time
            cal.set(Calendar.HOUR_OF_DAY, h)
            cal.set(Calendar.MINUTE, m)
            date.time = cal.timeInMillis
        }

        // Set the view
        val view = findViewById<View>(R.id.qa_time_compo)
        view.layoutParams.height = 1

        val textDisplay = findViewById<EditText>(R.id.qa_time_text)
        textDisplay.layoutParams.height = 1

        val button = findViewById<Button>(R.id.qa_time_button)
        button.isEnabled = false
        button.layoutParams.height = 1

        val label = findViewById<TextView>(R.id.qa_time_label)
        label.layoutParams.height = 1
    }

    private fun setupScreenshotOption() {
        val switch = findViewById<SwitchMaterial>(R.id.qa_screenshot_switch)
        switch.isChecked = (ledgerRecord.screenshot != null)
        switch.setOnClickListener {
            // Dismiss the keyboard
            this.currentFocus?.let { view ->
                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
                imm?.hideSoftInputFromWindow(view.windowToken, 0)
                // view.clearFocus() // Not necessary
            }
            // Check state
            if (switch.isChecked) {
                performScreenshot()
            } else {
                // Nothing? Cause we don't want a record to lose
            }
        }
        // Save reference
        screenshotSwitch = switch
    }

    // 设置好时间相关控件
    private fun setupTimePickerWidget() {
        val timeText = findViewById<EditText>(R.id.qa_time_text)
        val timePickerButton = findViewById<Button>(R.id.qa_time_button)

        // Display default time
        val cal = Calendar.getInstance()
        val hour = cal.get(Calendar.HOUR_OF_DAY)
        val minute = cal.get(Calendar.MINUTE)
        val formatString = "%02d:%02d"
        timeText.setText(String.format(formatString, hour, minute))

        ledgerRecord.mDate.let { date ->
            // hour和minute已经取得，因此cal可以重用了
            cal.timeInMillis = date.time
            cal.set(Calendar.HOUR_OF_DAY, hour)
            cal.set(Calendar.MINUTE, minute)
            date.time = cal.timeInMillis
        }
//        ledgerRecord.hourOfDay = hour
//        ledgerRecord.minuteOfHour = minute

        // Create a dialog and add it into callback
        val dialog = TimePickerDialog(this, R.style.Theme_Dialog_WithOurColors, { picker, h, m ->
            // Save the time
            ledgerRecord.mDate.let { date ->
                val tempCalender = Calendar.getInstance()
                tempCalender.timeInMillis = date.time
                tempCalender.set(Calendar.HOUR_OF_DAY, h)
                tempCalender.set(Calendar.MINUTE, m)
                date.time = tempCalender.timeInMillis
            }
//            ledgerRecord.hourOfDay = h
//            ledgerRecord.minuteOfHour = m

            // Format the string
            timeText.setText(String.format(formatString, h, m))
        }, hour, minute, true)

        dialog.setOnShowListener {
            val color = getColor(R.color.lightColorPrimary)
            dialog.getButton(TimePickerDialog.BUTTON_POSITIVE).setTextColor(color)
            dialog.getButton(TimePickerDialog.BUTTON_NEGATIVE).setTextColor(color)
        }

        timePickerButton.setOnClickListener {
            dialog.show()
        }
    }

    // 设置好日期相关控件
    private fun setupDatePickerWidget() {
        // 保存控件引用
        val dateText = findViewById<EditText>(R.id.qa_date_text)
        val datePickerButton = findViewById<Button>(R.id.qa_date_button)

        // 设置默认日期为今日
        val dateOfNow = Date()
        ledgerRecord.mDate = dateOfNow
        dateText.setText(Fmt.date.format(dateOfNow))

        // 添加按钮回调
        val dialog = DatePickerDialog(this, R.style.Theme_Dialog_WithOurColors)
        dialog.setOnDateSetListener { _, year, month, dayOfMonth ->
            val cal = Calendar.getInstance()
            cal.timeInMillis = ledgerRecord.mDate.time
            cal.set(year, month, dayOfMonth)
            ledgerRecord.mDate.time = cal.timeInMillis
            dateText.setText(Fmt.date.format(ledgerRecord.mDate))
        }

        dialog.setOnShowListener {
            // 调整button颜色
            val color = getColor(R.color.lightColorPrimary)
            dialog.getButton(DatePickerDialog.BUTTON_POSITIVE).setTextColor(color)
            dialog.getButton(DatePickerDialog.BUTTON_NEGATIVE).setTextColor(color)
        }

        datePickerButton.setOnClickListener {
            dialog.show()
        }
    }

    // Set up the function of buttons and make dismissal void
    private fun setupButtons() {
        // Store reference
        val cancelButton = this.findViewById<Button>(R.id.qa_button_cancel)
        val confirmButton = this.findViewById<Button>(R.id.qa_button_confirm)
        // Set up callbacks
        cancelButton.setOnClickListener {
            discardFields()
            this.finish()
        }
        confirmButton.setOnClickListener {
            commitToStorage()
            this.finish()
        }
        // Make dismissal void
        this.setFinishOnTouchOutside(false)
    }

    // 提交到存储中
    private fun commitToStorage() {
        val rec = ledgerRecord

        // Get all data from widgets
        rec.note = noteEditText.text.toString()

        // Money amount
        try {
            rec.moneyAmount = moneyEditText.text.toString().toDouble()
            if (rec.moneyAmount == 0.0) {
                rec.moneyAmount = -0.0
            } else {
                // 格式化金额
                val format = DecimalFormat("0.##")
                format.roundingMode = RoundingMode.FLOOR
                rec.moneyAmount = format.format(rec.moneyAmount).toDouble()
            }
        } catch (e: Exception) {
            rec.moneyAmount = -0.0
        }

        // Star status
        rec.starred = starToggleButton.isChecked

        // Print the record to trace and debug the procedure
        Log.d("************************ Save record", "$ledgerRecord")

        rec.screenshot?.let {
            ScreenshotUtils.saveToSandbox(this, it)
            rec.screenshot = null
        }

        // 同步插入数据库（因为这个活动马上就要停止，如果不同步插入可能会插入失败）
        AppDatabase.insertRecord(rec)
    }

    /**
     * 将卡片上面输入的所有信息丢弃。主要是丢掉截图信息。
     */
    private fun discardFields() {
        ledgerRecord.screenshot = null
    }

}