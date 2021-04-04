package com.example.ExceptPains.ScreenCap

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
import com.example.ExceptPains.Utils.Store
import com.example.ExceptPains.Notification.NotificationUtils
import com.example.ExceptPains.Notification.SCREENCAP_NOTIFICATION_ID
import com.example.ExceptPains.R
import com.example.ExceptPains.Utils.PairTask
import java.lang.RuntimeException

class CaptureService : Service() {
    lateinit var vDisplay: VirtualDisplay
    lateinit var mImageReader: ImageReader
    var callbackId: Long = -1;

    // 消除PixelFormat.RGBA_8888的值警告
    @SuppressLint("WrongConstant")
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // 1. 发送通知并注册为前台服务
        val builder = NotificationUtils.getStandardAlertBuilder()

        val notification = builder.setContentTitle(getString(R.string.screenshot_fgservice_title))
                .setContentText(getString(R.string.screenshot_fgservice_content))
                .setPriority(NotificationCompat.PRIORITY_MIN)
                .build()
        startForeground(SCREENCAP_NOTIFICATION_ID, notification)

        // 2. 设置回调事件的id
        callbackId = intent?.getLongExtra("callback", -1)!!

        // 3. 初始化缓冲区等
        // 获取屏幕的宽高
        val w = Store.shared.width
        val h = Store.shared.height

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
        this@CaptureService.stopForeground(true)
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
        val data = Store.shared.getMediaProjectionIntent()
                ?: throw RuntimeException("MediaProjectionIntent is null.")

        val manager = (Store.shared.getAppContext().getSystemService(Context.MEDIA_PROJECTION_SERVICE)
                as MediaProjectionManager)
        val projection = manager.getMediaProjection(Activity.RESULT_OK, data)

        // 注册画面完成时的回调
        mImageReader.setOnImageAvailableListener({ reader ->
            // 稍等一小段时间能够让状态栏收起，图片更加清晰
            Thread.sleep(100)

            // 处理图像
            val img = mImageReader.acquireLatestImage()
            val bitmap = ScreenCap.argb8888ToBitmap(img)
            PairTask.finish(callbackId, bitmap)

            // 结束服务和释放资源
            vDisplay.release()
            /**
             * 释放资源的同时停掉回调过程（防止此过程被调用多次）
             * mImageReader的关闭是相当重要的。
             */
            mImageReader.close()
            projection.stop()
            this@CaptureService.stopSelf()
        }, getBgHandler())

        // 获取屏幕的宽高
        val w = Store.shared.width
        val h = Store.shared.height

        // 创建虚拟显示设备
        vDisplay = projection.createVirtualDisplay("screen-mirror",
                w, h, Resources.getSystem().displayMetrics.densityDpi,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                mImageReader.surface, null, null)
        Log.d("ScreenCap.ScreenCap.processActivityResult", "vDisplay created.")
    }
}