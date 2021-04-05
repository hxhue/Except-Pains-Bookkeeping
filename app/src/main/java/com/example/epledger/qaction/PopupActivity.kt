package com.example.epledger.qaction

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.epledger.R
import com.example.epledger.qaction.screenshot.ScreenshotUtils
import com.example.epledger.util.Store


class PopupActivity : AppCompatActivity(), PairTask.Noticeable {
    private var screenshot: Bitmap? = null
    private var waitingEvent: Long = -1
    private var handler: Handler? = null
    private var shown = false
    private var noPermissionAlert: AlertDialog? = null

    // 和另外一个方法是有区别的，只有这个方法才能正常初始化
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("Notification.PopupActivity", "onCreate()")

        // 设置界面
        setContentView(R.layout.activity_popup_newrec)
        setTitle(R.string.act_popup_newrec_title)
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

    override fun onReceiveTaskResult(eid: Long, extra: Any?) {
        if (eid == waitingEvent) {
            show()
            screenshot = extra as Bitmap
            Log.d("Notification.PopupActivity", "onReceive called")
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
}