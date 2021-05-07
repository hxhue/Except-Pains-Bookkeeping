package com.example.epledger.qaction.tool

import android.content.Intent
import android.graphics.Point
import androidx.appcompat.app.AppCompatActivity

/**
 * Data used by PopupActivity.
 */
object Store {
    var height = -1 // 屏幕的高度
        private set

    var width = -1 // 屏幕的宽度
        private set

    var mediaProjectionIntent: Intent? = null // 带有截图权限的intent

    /**
     * @param activity
     * 从活动中加载屏幕尺寸。
     */
    fun loadScreenSize(activity: AppCompatActivity) {
        // 存储宽高
        val display = activity.windowManager.defaultDisplay
        val size = Point()
        display.getRealSize(size)
        width = size.x
        height = size.y
    }
}