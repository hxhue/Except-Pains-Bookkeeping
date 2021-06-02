package com.example.epledger.model

import android.graphics.Bitmap
import android.os.Parcel
import android.os.Parcelable
import java.util.Date

class Record() : Parcelable{
    var ID: Long? = null
    var date: Date = Date()
    var money: Double = -0.0
    var category: String? = null
    var source: String? = null
    var screenshot: Bitmap? = null
    var screenshotPath: String? = null
    var note: String? = null
    var starred: Boolean = false

    constructor(parcel: Parcel) : this() {
        ID = parcel.readValue(Long::class.java.classLoader) as? Long
        money = parcel.readValue(Double::class.java.classLoader) as Double
        category = parcel.readString()
        source = parcel.readString()
        screenshotPath = parcel.readString()
        note = parcel.readString()
        starred = parcel.readByte() != 0.toByte()
        date = parcel.readValue(Date::class.java.classLoader) as Date
    }

    override fun toString(): String {
        return "Record(date=$date, amount=$money, " +
                "type=$category, source=$source, screenshotPath=$screenshotPath, " +
                "note=$note, starred=$starred)"
    }

    fun getCopy(): Record {
        val another = Record()
        copyTo(another)
        return another
    }

    fun copyTo(another: Record) {
        another.ID = ID
        another.date = date
        another.money = money
        another.category = category
        another.source = source
        another.screenshot = screenshot
        another.note = note
        another.starred = starred
        another.screenshotPath = screenshotPath
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeValue(ID)
        parcel.writeValue(money)
        parcel.writeString(category)
        parcel.writeString(source)
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
        return (money != 0.0 && money != -0.0) && (!category.isNullOrBlank())
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