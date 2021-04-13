package com.example.epledger.qaction

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.PreferenceManager
import com.example.epledger.R
import com.example.epledger.qaction.screenshot.ScreenshotUtils
import com.example.epledger.util.Store
import java.text.SimpleDateFormat
import java.util.*

const val MEDIA_PROJECTION_INTENT = "com.example.qaction.PopupActivity.MEDIA_PROJECTION_INTENT"

class PopupActivity : AppCompatActivity(), PairTask.Noticeable, AdapterView.OnItemSelectedListener {
//    private var screenshot: Bitmap? = null
    private var waitingEvent: Int = -1
    private lateinit var handler: Handler
    private var shown = false
    private var noPermissionAlert: AlertDialog? = null

    private var ledgerRecord = LedgerRecord()
    // 用户的偏好设置
    private var briefMode = true
    private var screenshotAtStart = false

    // Wondering if we really need those references...
    // 日期选择按钮
    private lateinit var datePickerButton: Button
    // 日期输入文本框
    private lateinit var dateText: EditText
    // 取消按钮
    private lateinit var cancelButton: Button
    // 确认按钮
    private lateinit var confirmButton: Button

    // For spinners
    private val sources: Array<String> =  arrayOf("Unspecified", "Alipay", "Wechat", "Cash")
    private val types: Array<String> = arrayOf("Unspecified", "Daily", "Transportation", "Study")

    // Screenshot toggle
    private lateinit var screenshotToggle: ToggleButton

    // 和另外一个方法是有区别的，只有这个方法才能正常初始化
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("qaction.PopupActivity", "onCreate()")

        // Must be performed before the view's setup
        loadUserPreference()
        // 设置界面
        setupViews()
        // May need something from a set up view?
        Store.loadFromActivity(this)

        handler = Handler(Looper.myLooper()!!)

        // 根据用户偏好决定是否截屏
        if (screenshotAtStart) {
            performScreenshot()
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
            this.window.setDimAmount(0.6f)
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
            Log.d("qaction.PopupActivity", "onReceive() called")
            // Save the screenshot to ledgerRecord
            ledgerRecord.screenshot = screenshot
            // reset the toggle state
            handler.post {
                screenshotToggle.isChecked = true
            }
            // TODO: remove the debug section
            // DEBUG-ONLY option
            ScreenshotUtils.saveToGallery(this, screenshot, "${System.currentTimeMillis()}")
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
        val dialog = AlertDialog.Builder(activity)
                .setTitle(R.string.screenshot_failure)
                .setMessage(R.string.screenshot_failure_description)
                .setPositiveButton(R.string.ok) { _, _ -> activity.show() }
                .setOnDismissListener { activity.show() }
                .create()

        // 展示会话
        dialog.show()

        // 保存这个会话窗口供检查
        this.noPermissionAlert = dialog
        // Reset state of toggle
        handler.post {
            screenshotToggle.isChecked = false
        }
    }

    override fun onStop() {
        super.onStop()
        /**
         * 在停止（退出）时马上销毁，防止因为退出按键语义不同导致界面残留
         * 销毁是通过no_history标志完成的（见清单文件）
         */
        this.noPermissionAlert?.dismiss() // dismiss以防止窗口泄露
        Log.d("PopupActivity", "onStop()")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("PopupActivity", "onDestroy()")
    }

//    // 跳过截图阶段
//    private fun skipScreenshot() {
//        show()
//    }

    // 设置好界面
    private fun setupViews() {
        setContentView(R.layout.activity_popup_newrec)
        setTitle(R.string.act_popup_newrec_title)
        // 禁用黑暗模式（配色困难）
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        // Set up ScrollView
        (this.findViewById(R.id.qactionScrollView) as ScrollView).isScrollbarFadingEnabled = false
        // Other widgets
        // 创建时读取用户设置，然后根据设置来决定是否启用lite模式
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
    }

    private fun setupSpinners() {
        // Set up sourceSpinner
        val sourceSpinner = findViewById<Spinner>(R.id.qactionSourceSpinner)
        sourceSpinner.adapter = ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_dropdown_item, sources)
        sourceSpinner.onItemSelectedListener = this
        // Set up typeSpinner
        val typeSpinner = findViewById<Spinner>(R.id.qactionTypeSpinner)
        typeSpinner.adapter = ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_dropdown_item, types)
        typeSpinner.onItemSelectedListener = this
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        require(parent != null)
        if (parent.id == R.id.qactionSourceSpinner) {
            Log.d("qaction.PopupActivity",
                    "onItemSelected(): sourceSpinner has (${sources[position]}) selected.")
            ledgerRecord.source = sources[position]
        } else if (parent.id == R.id.qactionTypeSpinner) {
            Log.d("qaction.PopupActivity",
                    "onItemSelected(): typeSpinner has (${types[position]}) selected.")
            ledgerRecord.type = types[position]
        }
    }

    // Empty but necessary implementation
    override fun onNothingSelected(parent: AdapterView<*>?) {
        // Nothing goes there
    }

    private fun discardDatePickerWidget() {
        // Set data
        ledgerRecord.date = Date()

        // Set the view
        val noHeightLayout = ViewGroup.LayoutParams(0, 1)

        val view = findViewById<View>(R.id.qactionDateComponents)
        view.layoutParams = noHeightLayout

        val button = findViewById<Button>(R.id.qactionDatePickerButton)
        button.isEnabled = false
        button.layoutParams = noHeightLayout

        val textDisplay = findViewById<EditText>(R.id.qactionDateText)
        textDisplay.layoutParams = noHeightLayout

        val label = findViewById<TextView>(R.id.qactionDateLabel)
        label.layoutParams = noHeightLayout
    }

    private fun discardTimePickerWidget() {
        // Set data
        val cal = Calendar.getInstance()
        val h = cal.get(Calendar.HOUR_OF_DAY)
        val m = cal.get(Calendar.MINUTE)
        ledgerRecord.hourOfDay = h
        ledgerRecord.minuteOfHour = m

        // Set the view
        val noHeightLayout = ViewGroup.LayoutParams(0, 1)

        val view = findViewById<View>(R.id.qactionTimeComponents)
        view.layoutParams = noHeightLayout

        val textDisplay = findViewById<EditText>(R.id.qactionTimeText)
        textDisplay.layoutParams = noHeightLayout

        val button = findViewById<Button>(R.id.qactionTimePickerButton)
        button.isEnabled = false
        button.layoutParams = noHeightLayout

        val label = findViewById<TextView>(R.id.qactionTimeLabel)
        label.layoutParams = noHeightLayout
    }

    private fun setupScreenshotOption() {
        val toggleButton = findViewById<ToggleButton>(R.id.qactionScreenshotToggle)
        // Initial state
        toggleButton.isChecked = (ledgerRecord.screenshot != null)
        // Add listener
        toggleButton.setOnClickListener {
            val toggle = it as ToggleButton
            if (!toggle.isChecked) {
                // Delete screenshot
//                ledgerRecord.screenshot = null
                // Do not delete the old screenshot... Multiple services leads to bugs!
            } else {
                // Take another screenshot
                performScreenshot()
            }
        }
        // Save reference
        screenshotToggle = toggleButton
    }

    // 设置好时间相关控件
    private fun setupTimePickerWidget() {
        val timeText = findViewById<EditText>(R.id.qactionTimeText)
        val timePickerButton = findViewById<Button>(R.id.qactionTimePickerButton)

        // Display default time
        val cal = Calendar.getInstance()
        val hour = cal.get(Calendar.HOUR_OF_DAY)
        val minute = cal.get(Calendar.MINUTE)
        val formatString = "%02d:%02d"
        val str = String.format(formatString, hour, minute)
        timeText.setText(str)
        ledgerRecord.hourOfDay = hour
        ledgerRecord.minuteOfHour = minute

        // Create a dialog and add it into callback
        val dialog = TimePickerDialog(this, { picker, h, m ->
            // Save the time
            ledgerRecord.hourOfDay = h
            ledgerRecord.minuteOfHour = m
            // Format the string
            val str = String.format(formatString, h, m)
            timeText.setText(str)
        }, hour, minute, true)
        dialog.setOnShowListener {
            val buttonTextColor = getColor(R.color.design_default_color_secondary)
            dialog.getButton(TimePickerDialog.BUTTON_POSITIVE).setTextColor(buttonTextColor)
            dialog.getButton(TimePickerDialog.BUTTON_NEGATIVE).setTextColor(buttonTextColor)
        }

        timePickerButton.setOnClickListener {
            dialog.show()
        }
    }

    // 设置好日期相关控件
    private fun setupDatePickerWidget() {
        // 保存控件引用
        // Necessary?
        dateText = findViewById(R.id.qactionDateText)
        datePickerButton = findViewById(R.id.qactionDatePickerButton)

        // 设置默认日期为今日
        val simpleFormat = SimpleDateFormat("yyyy/MM/dd", Locale.US)
        val dateOfNow = Date()
        ledgerRecord.date = dateOfNow
        dateText.setText(simpleFormat.format(dateOfNow))
//        saveAndDisplayDate(Date())

        // 添加按钮回调
        val dialog = DatePickerDialog(this, R.style.CustomDatePicker)
        dialog.setOnDateSetListener { view, year, month, dayOfMonth ->
            val cal = Calendar.getInstance()
            cal.set(year, month, dayOfMonth)
            val date = Date(cal.timeInMillis)
            ledgerRecord.date = date
            dateText.setText(simpleFormat.format(date))
        }
        dialog.setOnShowListener {
            // 只有show之后才能访问button，才不会报null
            // 调整button颜色
            val buttonTextColor = getColor(R.color.design_default_color_primary)
            dialog.getButton(DatePickerDialog.BUTTON_POSITIVE).setTextColor(buttonTextColor)
            dialog.getButton(DatePickerDialog.BUTTON_NEGATIVE).setTextColor(buttonTextColor)
        }

        datePickerButton.setOnClickListener {
            dialog.show()
        }
    }

    // 保存和显示日期
//    private fun saveAndDisplayDate(date: Date) {
//        val simpleFormat = SimpleDateFormat("yyyy/MM/dd", Locale.US)
//        ledgerRecord.date = date
//        dateText.setText(simpleFormat.format(date))
//    }

    // Set up the function of buttons and make dismissal void
    private fun setupButtons() {
        // Store reference
        cancelButton = this.findViewById(R.id.qactionButttonCancel)
        confirmButton = this.findViewById(R.id.qactionButttonConfirm)
        // Set up callbacks
        cancelButton.setOnClickListener {
            this.finish()
        }
        confirmButton.setOnClickListener {
            commitToStorage()
            this.finish() // Close right now?
        }
        // Make dismissal void
        this.setFinishOnTouchOutside(false)
    }

    // 提交到存储中
    private fun commitToStorage() {
        Log.d("qaction.PopupActivity", "commitToStorage() called. (to be implemented)")
    }
}