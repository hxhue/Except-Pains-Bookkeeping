package com.example.ExceptPains.Notification

import android.app.NotificationChannel
import android.app.NotificationChannelGroup
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.example.ExceptPains.Utils.Store
import com.example.ExceptPains.R

/**
 * 注册通知组。包含快速操作通知组和通用通知组。
 */
private fun registerNotificationGroups() {
    // The id of the group.
    val ctx = Store.shared.getAppContext()
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
private fun createQuickActionChannel() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val ctx = Store.shared.getAppContext()
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
private fun createAlertChannel() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val ctx = Store.shared.getAppContext()
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
private fun createNotificationChannels() {
    createQuickActionChannel()
    createAlertChannel()
}

/**
 * 每个模块都提供一个载入的接口。
 * 如果该模块不需载入就能够使用，则提供一个空的实现。
 * 下面的方法用来加载通知模块。
 */
public fun loadNotificationModule() {
    registerNotificationGroups()
    createNotificationChannels()
    NotificationUtils.displayAlwaysOnActions()
}