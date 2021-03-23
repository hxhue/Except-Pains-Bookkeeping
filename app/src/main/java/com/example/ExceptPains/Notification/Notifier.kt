package com.example.ExceptPains.Notification

import android.app.PendingIntent
import android.content.Intent
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.ExceptPains.MainActivity
import com.example.ExceptPains.R
import com.example.ExceptPains.getMainContext

//-TODO: 若干发送通知的功能中，图标均未设置正确

const val ALWAYS_ON_NOTIFICATION_ID = 0

/**
 * 一般通知的ID，跳过10以内（即便是在不同的频道也会发生冲突）
 * 10以内预留给特殊用途的通知
 */
private var alertNotificationId = 0
private fun newAlertNotificationId(): Int {
    if (++alertNotificationId <= 10) {
        alertNotificationId = 11;
    }
    return alertNotificationId;
}

class Notifier {
    /**
     * 显示常驻通知。此方法在调用本模块的load功能时自动调用。
     */
    public fun displayAlwaysOnActions() {
        val ctx = getMainContext();
        val channelId = ctx.getString(R.string.always_on_channel_id)
        val builder = NotificationCompat.Builder(ctx, channelId) //-TODO: icon需要调整
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("这是标题")
            .setContentText("这是常驻通知！图标可能还要再改改")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setVisibility(NotificationCompat.VISIBILITY_SECRET)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setOngoing(true)
            .setGroup(ctx.getString(R.string.always_on_group_id))

        // 指定通知的内容布局
        val remoteViews = RemoteViews(ctx.packageName, R.layout.view_quick_actions)
        val pendingIntent = PendingIntent.getActivity(ctx, 0, Intent(ctx, MainActivity::class.java).setAction(Intent.ACTION_MAIN), PendingIntent.FLAG_UPDATE_CURRENT)
        remoteViews.setOnClickPendingIntent(R.id.btn_qa_new_record, pendingIntent)
        remoteViews.setOnClickPendingIntent(R.id.btn_qa_open, pendingIntent)

        builder.setStyle(NotificationCompat.DecoratedCustomViewStyle())
                .setCustomContentView(remoteViews)

        with(NotificationManagerCompat.from(ctx)) {
            notify(ALWAYS_ON_NOTIFICATION_ID, builder.build())
        }
    }

    /**
     * 提供title和text，直接发送通知。
     * 目标是提供最简单的通知，点击这则通知直接进入主应用。
     */
    public fun standardAlert(title: String, text: String) {
        /**
         * 不要在这里指定通知组，指定通知组之后，这些消息不会自动折叠
         */
        val builder = getStandardAlertBuilder()
                .setContentText(text)
                .setContentTitle(title)
        buildAndSendAlert(builder)
    }

    /**
     * 使用自定义的通知创建器来创建通知。
     * 一般来说，可以通过getStandardAlertBuilder()获取一个alert渠道通知的模板。
     */
    public fun buildAndSendAlert(builder: NotificationCompat.Builder) {
        val message = builder.build()
        with(NotificationManagerCompat.from(getMainContext())) {
            notify(newAlertNotificationId(), message)
        }
    }

    /**
     * 提供本应用标准的通知信息模板。
     * 获取模板之后稍加修改就能够使用buildAndSendAlert发送到alert渠道。
     */
    public fun getStandardAlertBuilder(): NotificationCompat.Builder {
        val ctx = getMainContext()
        val channelId = ctx.getString(R.string.alert_channel_id)

        // 创建进入主界面的Intent
        val mainIntent = Intent(ctx, MainActivity::class.java)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
                .addCategory(Intent.CATEGORY_LAUNCHER)
                .setAction(Intent.ACTION_MAIN)
        // 创建pendingIntent
        // 因为是根界面，不需要完整栈
        val pendingIntent = PendingIntent.getActivity(ctx, 0, mainIntent,0);

        //-TODO: icon需要调整
        val builder = NotificationCompat.Builder(ctx, channelId)
                .setAutoCancel(true)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
        return builder
    }
}

