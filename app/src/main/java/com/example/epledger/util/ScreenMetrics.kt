package com.example.epledger.util

import android.content.Context
import android.content.res.Resources

object ScreenMetrics {
    fun pxToDp(px: Int): Int {
        return (px / Resources.getSystem().getDisplayMetrics().density).toInt()
    }

    fun dpToPx(dp: Int): Int {
        return (dp * Resources.getSystem().getDisplayMetrics().density).toInt()
    }
}