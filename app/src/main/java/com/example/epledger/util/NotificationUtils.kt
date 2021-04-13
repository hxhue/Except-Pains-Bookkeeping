package com.example.epledger.util

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.preference.PreferenceManager
import com.example.epledger.MainActivity
import com.example.epledger.qaction.PopupActivity
import com.example.epledger.R

//-TODO: 若干发送通知的功能中，图标均未设置正确

// 一定不能够为0，0会导致前台服务CRASH
const val ALWAYS_ON_NOTIFICATION_ID = 1
const val SCREENCAP_NOTIFICATION_ID = 2

object NotificationUtils {
    /**
     * 构建快捷操作通知。
     */
    fun createQuickActionNotification(ctx: Context): Notification {
        val channelId = ctx.getString(R.string.always_on_channel_id)
        val builder = NotificationCompat.Builder(ctx, channelId) //-TODO: icon需要调整
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setVisibility(NotificationCompat.VISIBILITY_SECRET)
                .setCategory(NotificationCompat.CATEGORY_SERVICE)
                .setOngoing(true)
                .setGroup(ctx.getString(R.string.always_on_group_id))

        // 指定通知的内容布局
        val remoteViews = RemoteViews(ctx.packageName, R.layout.notification_quick_actions)

        // 创建打开主界面的intent
        val launchHomeIntent = createHomeLaunchIntent(ctx)

        // 创建打开记录卡片的intent
        val newRecCardIntent = createNewRecordCardIntent(ctx)

        // 设置intent为按钮的目标
        remoteViews.setOnClickPendingIntent(R.id.btn_qa_new_record, newRecCardIntent)
        remoteViews.setOnClickPendingIntent(R.id.btn_qa_open, launchHomeIntent)

        builder.setStyle(NotificationCompat.DecoratedCustomViewStyle())
                .setCustomContentView(remoteViews)

        return builder.build()
    }

    /**
     * 显示快捷操作通知。
     * 现已不在内部此模块内部专门使用。
     */
    fun displayQuickActions(ctx: Context) {
        val notification = createQuickActionNotification(ctx)
        with(NotificationManagerCompat.from(ctx)) {
            notify(ALWAYS_ON_NOTIFICATION_ID, notification)
        }
    }

    /**
     * 提供title和text，直接发送通知。
     * 目标是提供最简单的通知，点击这则通知直接进入主应用。
     */
    fun standardAlert(ctx: Context, title: String, text: String) {
        /**
         * 不要在这里指定通知组，指定通知组之后，这些消息不会自动折叠
         */
        val builder = getStandardAlertBuilder(ctx)
                .setContentText(text)
                .setContentTitle(title)
        buildAndSendAlert(ctx, builder)
    }

    /**
     * 使用自定义的通知创建器来创建通知。
     * 一般来说，可以通过getStandardAlertBuilder()获取一个alert渠道通知的模板。
     */
    fun buildAndSendAlert(ctx: Context, builder: NotificationCompat.Builder) {
        val message = builder.build()
        with(NotificationManagerCompat.from(ctx)) {
            notify(newAlertNotificationID(), message)
        }
    }

    /**
     * getStandardAlertBuilder() with ctx parameter.
     * More robust...
     */
    fun getStandardAlertBuilder(ctx: Context): NotificationCompat.Builder {
        val channelId = ctx.getString(R.string.alert_channel_id)
        val pendingIntent = createHomeLaunchIntent(ctx)

        //-TODO: icon需要调整
        val builder = NotificationCompat.Builder(ctx, channelId)
                .setAutoCancel(true)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
        return builder
    }

    /**
     * 返回一个能够打开主界面的intent。该intent保证主界面只会被打开一次。
     */
    private fun createHomeLaunchIntent(ctx: Context): PendingIntent {
        // 创建进入主界面的Intent
        val mainIntent = Intent(ctx, MainActivity::class.java)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
                .setAction(Intent.ACTION_MAIN)
        // 创建pendingIntent
        val pendingIntent = PendingIntent.getActivity(ctx, 0, mainIntent,0);
        return pendingIntent
    }

    /**
     * 返回一个能够打开新记录卡片的intent。
     */
    private fun createNewRecordCardIntent(ctx: Context): PendingIntent {
        val intent = Intent(ctx, PopupActivity::class.java)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
                .setAction(Intent.ACTION_MAIN)
        return PendingIntent.getActivity(ctx, 1, intent, 0)
    }
}

/**
 * 一般通知的ID，跳过10以内（即便是在不同的频道也会发生冲突）
 * 10以内预留给特殊用途的通知
 */
private var alertID = 0
private fun newAlertNotificationID(): Int {
    if (++alertID <= 10) {
        alertID = 11;
    }
    return alertID;
}

/**
 * 注册通知组。包含快速操作通知组和通用通知组。
 */
private fun registerNotificationGroups(ctx: Context) {
    // The id of the group.
    // The user-visible name of the group.
    val notificationManager = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    notificationManager.createNotificationChannelGroup(
            NotificationChannelGroup(
                    ctx.getString(R.string.always_on_group_id),
                    ctx.getString(R.string.always_on_group))
    )
    notificationManager.createNotificationChannelGroup(
            NotificationChannelGroup(
                    ctx.getString(R.string.alert_group_id),
                    ctx.getString(R.string.alert_group)
            )
    )
}

/**
 * 创建快捷操作通知频道。
 */
private fun createQuickActionChannel(ctx: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val name = ctx.getString(R.string.always_on_channel)
        val descriptionText = ctx.getString(R.string.always_on_channel_description)
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channelId = ctx.getString(R.string.always_on_channel_id)
        val channel = NotificationChannel(channelId, name, importance).apply {
            this.description = descriptionText
            this.setSound(null, null)
        }
        // Register the channel with the system
        val notificationManager: NotificationManager =
                ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }
}

/**
 * 创建一般通知频道。
 */
private fun createAlertChannel(ctx: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val name = ctx.getString(R.string.alert_channel)
        val descriptionText = ctx.getString(R.string.alert_channel_description)
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channelId = ctx.getString(R.string.alert_channel_id)
        val channel = NotificationChannel(channelId, name, importance).apply {
            this.description = descriptionText
        }
        // Register the channel with the system
        val notificationManager: NotificationManager =
                ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }
}

/**
 * 创建两个通知频道。包含一般通知频道和快捷操作通知频道。
 */
private fun createNotificationChannels(ctx: Context) {
    createQuickActionChannel(ctx)
    createAlertChannel(ctx)
}

/**
 * Load notification module.
 * This is essential and must be called when the app is started.
 */
fun loadNotificationModule(ctx: Context) {
    // Register groups and channels
    registerNotificationGroups(ctx)
    createNotificationChannels(ctx)
}