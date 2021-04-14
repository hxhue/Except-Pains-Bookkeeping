package com.example.epledger.qaction

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceManager
import com.example.epledger.util.ALWAYS_ON_NOTIFICATION_ID
import com.example.epledger.util.NotificationUtils

private const val CLASS_NAME = "qaction.CKBackground"

class CKForeground: Service() {
    companion object {
        const val STOP_FOREGROUND = "ACTION_STOP_FOREGROUND_SERVICE"

        fun launch(ctx: Context) {
            Log.d(CLASS_NAME, "launch() called")
            val intent = Intent(ctx, CKForeground::class.java)
            ContextCompat.startForegroundService(ctx, intent)
        }

        fun stop(ctx: Context) {
            Log.d(CLASS_NAME, "stop() called")
            val intent = Intent(ctx, CKForeground::class.java)
            intent.action = STOP_FOREGROUND
            ContextCompat.startForegroundService(ctx, intent)
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // 检查intent是否为null
        if (intent == null) {
            Log.d(CLASS_NAME, "onStartCommand() received a null intent => ignored")
            return super.onStartCommand(intent, flags, startId)
        }

        // 启动前台
        val notification = NotificationUtils.createQuickActionNotification(this)
        startForeground(ALWAYS_ON_NOTIFICATION_ID, notification)

        // 有结束请求时提前中止
        if (STOP_FOREGROUND == intent.action) {
            Log.d(CLASS_NAME, "Stop action received. Bye.")
            stopForeground(true)
            stopSelfResult(startId);
            return super.onStartCommand(intent, flags, startId)
        }

        return START_STICKY
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
    } else {
        CKForeground.stop(ctx)
        Toast.makeText(ctx, "Stopping foreground service. Please be patient.", Toast.LENGTH_SHORT).show()
    }
}