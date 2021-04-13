package com.example.epledger.qaction

import android.app.Activity
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceManager
import com.example.epledger.util.ALWAYS_ON_NOTIFICATION_ID
import com.example.epledger.util.NotificationUtils
import com.example.epledger.util.SCREENCAP_NOTIFICATION_ID
import com.example.epledger.util.Store

private const val CLASS_NAME = "qaction.CKBackgroundActivity"

class CKForeground: Service() {
    companion object {
        fun launch(ctx: Context) {
            Log.d("qaction.CKBackgroundActivity", "launch() called")
            val intent = Intent(ctx, CKForeground::class.java)
            ContextCompat.startForegroundService(ctx, intent)
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // 设置环境，现在修改配置之后CKForeground会在不同的进程中执行，拿不到主进程的环境
        // 分成两个进程存粹是为了让应用的统计数据更加好看（其实结果差不多）
//        if (Store.shared.appContext == null) {
//            Store.shared.setAppContext(this.applicationContext)
//        }
        // 启动前台
        val notification = NotificationUtils.createQuickActionNotification(this)
        startForeground(ALWAYS_ON_NOTIFICATION_ID, notification)
        Log.d("qaction.CKForeground", "onStartCommand() called")
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        // 离开时关闭相应的通知
        this@CKForeground.stopForeground(true)
        super.onDestroy()
    }
}

/**
 * Load quick action module according to user preferences.
 * This is optional and may be called multiple times.
 */
fun loadQuickActionModule(ctx: Context) {
    // Decide whether to turn on quick actions-in-notification-center feature
    val perf = PreferenceManager.getDefaultSharedPreferences(ctx)
    val quickActionInNotification = perf.getBoolean("qa_notification", true)
    if (quickActionInNotification) {
        CKForeground.launch(ctx)
    }
}