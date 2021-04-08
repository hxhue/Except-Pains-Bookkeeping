package com.example.epledger.qaction

import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.ScrollView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.example.epledger.R
import com.example.epledger.qaction.screenshot.ScreenshotUtils
import com.example.epledger.util.Store
import java.text.SimpleDateFormat
import java.util.*

const val MEDIA_PROJECTION_INTENT = "com.example.qaction.PopupActivity.MEDIA_PROJECTION_INTENT"

class PopupActivity : AppCompatActivity(), PairTask.Noticeable {
    private var screenshot: Bitmap? = null
    private var waitingEvent: Int = -1
    private var handler: Handler? = null
    private var shown = false
    private var noPermissionAlert: AlertDialog? = null

    private var ledgerRecord = LedgerRecord()

    // Wondering if we need those references
    // 日期选择按钮
    private lateinit var datePickerButton: Button
    // 日期输入文本框
    private lateinit var dateText: EditText
    // 取消按钮
    private lateinit var cancelButton: Button
    // 确认按钮
    private lateinit var confirmButton: Button

    // 和另外一个方法是有区别的，只有这个方法才能正常初始化
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("qaction.PopupActivity", "onCreate()")

        // 设置界面
        setupViews()
        // 加载环境（重要）
        Store.shared.loadFromActivity(this)

        // 隐藏界面
        hide()
        // 保存下来当前线程的handler
        handler = Handler(Looper.myLooper()!!)
        // 创建等待事件
        waitingEvent = PairTask.observe(this)
        // 开始异步截屏
        ScreenshotUtils.shotScreen(this, waitingEvent)
//        skipScreenshot() // 测试中，跳过截屏阶段
    }

    // 不能在其他线程中调用。
    private fun hide() {
        this.window.setDimAmount(0.0f)
        this.window.decorView.rootView.alpha = 0.0f
        shown = false
    }

    // 可以在其他线程中调用。
    private fun show() {
        handler?.post {
            this.window.setDimAmount(0.6f)
            this.window.decorView.rootView.apply {
                animate().setDuration(100).alpha(1.0f)
            }
            shown = true
        }
    }

    override fun onReceiveTaskResult(eid: Int, extra: Any?) {
        if (eid == waitingEvent) {
            show()
            screenshot = extra as Bitmap
            Log.d("qaction.PopupActivity", "onReceive() called")
            ScreenshotUtils.saveToGallery(screenshot!!, "${System.currentTimeMillis()}")
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // 处理屏幕截取权限的申请结果
        val hasPermission = ScreenshotUtils.processPermissionAskingResult(requestCode, resultCode, data)
        // 尝试恢复当前活动
        if (hasPermission) {
            ScreenshotUtils.shotScreen(this, waitingEvent)
        } else {
            promptNoPermission()
        }
    }

    // 提示没有权限信息
    private fun promptNoPermission() {
        val activity = this

        // 给出一个提示框然后结束
        val dialog = AlertDialog.Builder(activity)
                .setTitle(R.string.screenshot_failure)
                .setMessage(R.string.screenshot_failure_description)
                .setPositiveButton(R.string.ok) { _, _ -> activity.finish() }
                .setOnDismissListener { activity.finish() }
                .create()

        // 展示会话
        dialog.show()

        // 保存这个会话窗口供检查
        this.noPermissionAlert = dialog
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

    // 跳过截图阶段
    private fun skipScreenshot() {
        show()
    }

    // 设置好界面
    private fun setupViews() {
        setContentView(R.layout.activity_popup_newrec)
        setTitle(R.string.act_popup_newrec_title)
        // 禁用黑暗模式（配色困难）
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        // Set up ScrollView
        (this.findViewById(R.id.qactionScrollView) as ScrollView).isScrollbarFadingEnabled = false
        // Other widgets
        setupDatePickerWidget()
        setupButtons()
    }

    // 设置好日期相关空间
    private fun setupDatePickerWidget() {
        // 保存控件引用
        dateText = findViewById(R.id.qactionDateText)
        datePickerButton = findViewById(R.id.qactionDatePickerButton)

        // 设置默认日期为今日
        saveAndDisplayDate(Date())

        // 添加按钮回调
        val dialog = DatePickerDialog(this, R.style.CustomDatePicker)
        dialog.setOnDateSetListener { view, year, month, dayOfMonth ->
            val cal = Calendar.getInstance()
            cal.set(year, month, dayOfMonth)
            val date = Date(cal.timeInMillis)
            saveAndDisplayDate(date)
        }
        dialog.setOnShowListener {
            // 只有show之后才能访问button，才不会报null
            // 调整button颜色
            val buttonTextColor = getColor(R.color.design_default_color_primary)
            dialog.getButton(DatePickerDialog.BUTTON_POSITIVE).setTextColor(buttonTextColor)
            dialog.getButton(DatePickerDialog.BUTTON_NEGATIVE).setTextColor(buttonTextColor)
        }

        // -TODO: 有白边
        datePickerButton.setOnClickListener {
            dialog.show()
        }
    }

    // 保存和显示日期
    private fun saveAndDisplayDate(date: Date) {
        val simpleFormat = SimpleDateFormat("yyyy/MM/dd", Locale.US)
        ledgerRecord.date = date
        dateText.setText(simpleFormat.format(date))
    }

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