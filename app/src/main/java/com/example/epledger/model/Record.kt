package com.example.epledger.model

import android.graphics.Bitmap
import android.os.Parcel
import android.os.Parcelable
import java.util.Date

class Record() : Parcelable {
    var id: Long? = null
    var date: Date = Date()
    var money: Double = -0.0
    var categoryID: Int? = null
    var sourceID: Int? = null
    var screenshot: Bitmap? = null
    var screenshotPath: String? = null
    var note: String? = null
    var starred: Boolean = false

    constructor(parcel: Parcel) : this() {
        id = parcel.readValue(Long::class.java.classLoader) as? Long
        money = parcel.readValue(Double::class.java.classLoader) as Double
        categoryID = parcel.readValue(Int::class.java.classLoader) as? Int
        sourceID = parcel.readValue(Int::class.java.classLoader) as? Int
        screenshotPath = parcel.readString()
        note = parcel.readString()
        starred = parcel.readByte() != 0.toByte()
        date = parcel.readValue(Date::class.java.classLoader) as Date
    }

    override fun toString(): String {
        return "Record(date=$date, amount=$money, " +
                "type=$categoryID, source=$sourceID, screenshotPath=$screenshotPath, " +
                "note=$note, starred=$starred)"
    }

    fun getCopy(): Record {
        val another = Record()
        copyTo(another)
        return another
    }

    fun copyTo(another: Record) {
        another.id = id
        another.date = date
        another.money = money
        another.categoryID = categoryID
        another.sourceID = sourceID
        another.screenshot = screenshot
        another.note = note
        another.starred = starred
        another.screenshotPath = screenshotPath
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeValue(id)
        parcel.writeValue(money)
        parcel.writeValue(categoryID)
        parcel.writeValue(sourceID)
        parcel.writeString(screenshotPath)
        parcel.writeString(note)
        parcel.writeByte(if (starred) 1 else 0)
        parcel.writeValue(date)
    }

    override fun describeContents(): Int {
        return 0
    }

    /**
     * 判断一个记录是否不完整。
     */
    fun isComplete(): Boolean {
        return (money != 0.0 && money != -0.0) && (categoryID != null)
    }

    companion object CREATOR : Parcelable.Creator<Record> {
        override fun createFromParcel(parcel: Parcel): Record {
            return Record(parcel)
        }

        override fun newArray(size: Int): Array<Record?> {
            return arrayOfNulls(size)
        }

        val dateReverseComparator = Comparator<Record> { o1, o2 ->
            // 将o2这个参数放到前面就能够得到相反的比较结果
            o2.date.compareTo(o1.date)
        }
    }
}