package com.example.epledger.qaction.data

import android.graphics.Bitmap
import java.util.Date


class LedgerRecord {
    var date: Date? = null
    var hourOfDay: Int? = null
    var minuteOfHour: Int? = null
    var amount: Double? = 0.0
    var type: String? = null
    var source: String? = null
    var screenshot: Bitmap? = null
    var note: String? = null
//    是否完整可以通过上面关系自动判断
//    至少有前三个即为完整
}