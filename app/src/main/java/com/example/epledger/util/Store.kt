package com.example.epledger.util

import android.content.Context
import android.content.Intent
import android.graphics.Point
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import java.io.File

/**
 * 用来存储启动时保存的环境信息。
 * 被本身没有Context和其它属性的工具类来使用。
 * 用bundle之类的手段做存储会更好一点。这个只要离线就没了。
 */
object Store {
    @get: Synchronized
    var height = -1 // 屏幕的高度
        private set

    @get: Synchronized
    var width = -1 // 屏幕的宽度
        private set

    @get: Synchronized
    @set: Synchronized
    var mediaProjectionIntent: Intent? = null // 带有截图权限的intent

    // No need to synchronize. Used only in synchronized blocks.
    private var screenOnceFlag = false

    // 设置宽高
    @Synchronized
    fun saveWidthAndHeight(w: Int, h: Int) {
        require(!(w <= 0 || h <= 0)) { "Width and height must be > 0." }
        if (!screenOnceFlag) {
            screenOnceFlag = true
            width = w
            height = h
            Log.d(
                "util.Store.saveWidthAndHeight",
                "Width and height stored."
            )
        }
    }

    /**
     * @param activity
     * 从活动中加载部分重要内容。
     */
    fun loadFromActivity(activity: AppCompatActivity) {
        // 存储宽高
        val display = activity.windowManager.defaultDisplay
        val size = Point()
        display.getRealSize(size)
        val width = size.x
        val height = size.y
        saveWidthAndHeight(width, height)
    }
}