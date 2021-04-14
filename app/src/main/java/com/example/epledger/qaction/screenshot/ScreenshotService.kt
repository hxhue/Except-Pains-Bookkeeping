package com.example.epledger.qaction.screenshot

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.ImageReader
import android.media.projection.MediaProjectionManager
import android.os.Handler
import android.os.HandlerThread
import android.os.IBinder
import android.os.Process
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.epledger.qaction.data.Store
import com.example.epledger.util.NotificationUtils
import com.example.epledger.util.SCREENCAP_NOTIFICATION_ID
import com.example.epledger.R
import com.example.epledger.qaction.data.PairTask
import java.lang.RuntimeException

class ScreenshotService : Service() {
    lateinit var vDisplay: VirtualDisplay
    lateinit var mImageReader: ImageReader
    var callbackId: Int = -1;

    // 消除PixelFormat.RGBA_8888的值警告
    @SuppressLint("WrongConstant")
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // 1. 发送通知并注册为前台服务
        val builder = NotificationUtils.getStandardAlertBuilder(this)

        val notification = builder.setContentTitle(getString(R.string.screenshot_fgservice_title))
                .setContentText(getString(R.string.screenshot_fgservice_content))
                .setPriority(NotificationCompat.PRIORITY_MIN)
                .build()
        startForeground(SCREENCAP_NOTIFICATION_ID, notification)

        // If intent is null, just stop itself...
        // But why is service revoked without intent?
        if (intent == null) {
            stopSelf()
            Log.d("qaction.screenshot.ScreenshotService",
                    "Intent is null somehow. Check the result?")
            return super.onStartCommand(intent, flags, startId)
        }

        // 2. 设置回调事件的id
        callbackId = intent.getIntExtra("callback", -1)

        // 3. 初始化缓冲区等
        // 获取屏幕的宽高
        val w = Store.width
        val h = Store.height

        // 检查宽高
        if (w <= 0 || h <= 0) {
            Log.d("ScreenCap.CaptureService", "w and h invalid, stopping service")
            this.stopSelf()
            // stopSelf并不会马上返回
            return super.onStartCommand(intent, flags, startId)
        }

        // maxImages为2，1则会有一个buffer不够的警告（但实际上也能够成功）
        // 格式只有0x1才能够成功
        mImageReader = ImageReader.newInstance(w, h, PixelFormat.RGBA_8888, 2)

        // 4. 开始截图
        doCapture()
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        // 离开时关闭相应的通知
        this@ScreenshotService.stopForeground(true)
        super.onDestroy()
    }

    var backgroundHandler: Handler? = null

    private fun getBgHandler(): Handler? {
        if (backgroundHandler == null) {
            val backgroundThread = HandlerThread("captureService", Process.THREAD_PRIORITY_BACKGROUND)
            backgroundThread.start()
            backgroundHandler = Handler(backgroundThread.looper)
        }
        return backgroundHandler
    }

    private fun doCapture() {
        // 调用之前必须保证RuntimeContext中的确保存了带有权限的intent
        val data = Store.mediaProjectionIntent
                ?: throw RuntimeException("MediaProjectionIntent is null.")

        val ctx = this.applicationContext

        val manager = (ctx.getSystemService(Context.MEDIA_PROJECTION_SERVICE)
                as MediaProjectionManager)
        val projection = manager.getMediaProjection(Activity.RESULT_OK, data)

        // 注册画面完成时的回调
        mImageReader.setOnImageAvailableListener({ reader ->
            // 稍等一小段时间能够让状态栏收起
            // 100还是稍微有点吃紧，如果手机有点卡则截图效果不好
            Thread.sleep(150)

            // 处理图像
            val img = mImageReader.acquireLatestImage()
            val bitmap = ScreenshotUtils.argb8888ToBitmap(img)
            PairTask.finish(callbackId, bitmap)

            // 结束服务和释放资源
            vDisplay.release()
            /**
             * 释放资源的同时停掉回调过程（防止此过程被调用多次）
             * mImageReader的关闭是相当重要的。
             */
            mImageReader.close()
            projection.stop()
            this@ScreenshotService.stopSelf()
        }, getBgHandler())

        // 获取屏幕的宽高
        val w = Store.width
        val h = Store.height

        // 创建虚拟显示设备
        vDisplay = projection.createVirtualDisplay("screen-mirror",
                w, h, Resources.getSystem().displayMetrics.densityDpi,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                mImageReader.surface, null, null)
        Log.d("ScreenCap.ScreenCap.processActivityResult", "vDisplay created.")
    }
}