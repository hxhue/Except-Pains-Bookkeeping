package com.example.ExceptPains.Notification

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Point
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.ExceptPains.R
import com.example.ExceptPains.ScreenCap.ScreenCap
import com.example.ExceptPains.Utils.PairTask
import com.example.ExceptPains.Utils.Store


class PopupActivity : AppCompatActivity(), PairTask.Noticeable {
    private var screenshot: Bitmap? = null
    private var waitingEvent: Long = -1
    private var handler: Handler? = null
    private var shown = false

    // 和另外一个方法是有区别的，只有这个方法才能正常初始化
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
        ScreenCap.shotScreen(this, waitingEvent)
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
//            this.window.setWindowAnimations(WindowManager.LayoutParams.ALPHA_CHANGED)
            this.window.setDimAmount(0.6f)
            this.window.decorView.rootView.apply {
                animate().alpha(1.0f)
            }
            shown = true
        }
    }

    override fun onReceiveTaskResult(eid: Long, extra: Any) {
        if (eid == waitingEvent) {
            show()
            screenshot = extra as Bitmap
            Log.d("Notification.PopupActivity", "onReceive called")
            ScreenCap.saveToGallery(screenshot!!, "${System.currentTimeMillis()}")
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // 处理屏幕截取权限的申请结果
        val hasPermission = ScreenCap.processPermissionAskingResult(requestCode, resultCode, data)
        // 尝试恢复当前活动
        if (hasPermission) {
            ScreenCap.shotScreen(this, waitingEvent)
        } else {
            promptNoPermission()
        }
    }

    // 提示没有权限信息
    private fun promptNoPermission() {
        //-TODO: 给出一个提示框然后结束
        this.finish()
    }
}