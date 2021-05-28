package com.example.epledger.detail

import android.graphics.Bitmap
import android.os.Parcel
import android.os.Parcelable
import com.example.epledger.model.entry.Entry
import com.example.epledger.settings.datamgr.Category
import java.util.Date

class DetailRecord() : Parcelable, Entry {
    var ID: Long? = null
    var startingDate: Date? = null
    var hourOfDay: Int? = null
    var minuteOfHour: Int? = null
    var amount: Double? = 0.0
    var category: String? = null
    var source: String? = null
    var screenshot: Bitmap? = null
    var screenshotPath: String? = null
    var note: String? = null
    var starred: Boolean = false

    constructor(parcel: Parcel) : this() {
        ID = parcel.readValue(Long::class.java.classLoader) as? Long
        hourOfDay = parcel.readValue(Int::class.java.classLoader) as? Int
        minuteOfHour = parcel.readValue(Int::class.java.classLoader) as? Int
        amount = parcel.readValue(Double::class.java.classLoader) as? Double
        category = parcel.readString()
        source = parcel.readString()
        screenshotPath = parcel.readString()
        note = parcel.readString()
        starred = parcel.readByte() != 0.toByte()
        startingDate = parcel.readValue(Date::class.java.classLoader) as? Date
    }

    // 是否完整可以通过上面关系自动判断，至少有前三个即为完整
    override fun toString(): String {
        return "LedgerRecord(date=$startingDate, hourOfDay=$hourOfDay, minuteOfHour=$minuteOfHour, amount=$amount, " +
                "type=$category, source=$source, screenshot=$screenshot, note=$note, starred=$starred)"
    }

    override fun getEntryId(): Int {
        return ID!!.toInt()
    }

    override fun setEntryId(id: Int) {
        this.ID = id.toLong()
    }

    override fun getAmount(): Double {
        return this.amount!!
    }

    override fun setAmount(amount: Double) {
        this.amount = amount
    }

    override fun getLabel(): String? {
        return this.category
    }

    override fun setLabel(label: String?) {
        this.category = label!!
    }

    override fun getInfo(): String? {
        return this.note
    }

    override fun setInfo(info: String?) {
        this.note = info!!
    }

    override fun getEntrySource(): String? {
        return this.source
    }

    override fun setEntrySource(source: String?) {
        this.source = source!!
    }

    override fun getDate(): Date? {
        return this.startingDate
    }

    override fun setDate(date: Date?) {
        this.startingDate = date!!
    }

    override fun getEntryCategory(): String? {
        return this.category
    }

    override fun setEntryCategory(s: String) {
        this.category = s
    }

    fun getCopy(): DetailRecord {
        val another = DetailRecord()
        copyTo(another)
        return another
    }

    fun copyTo(another: DetailRecord) {
        another.ID = ID
        another.startingDate = startingDate
        another.hourOfDay = hourOfDay
        another.minuteOfHour = minuteOfHour
        another.amount = amount
        another.category = category
        another.source = source
        another.screenshot = screenshot
        another.note = note
        another.starred = starred
        another.screenshotPath = screenshotPath
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeValue(ID)
        parcel.writeValue(hourOfDay)
        parcel.writeValue(minuteOfHour)
        parcel.writeValue(amount)
        parcel.writeString(category)
        parcel.writeString(source)
        parcel.writeString(screenshotPath)
        parcel.writeString(note)
        parcel.writeByte(if (starred) 1 else 0)
        parcel.writeValue(startingDate)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<DetailRecord> {
        override fun createFromParcel(parcel: Parcel): DetailRecord {
            return DetailRecord(parcel)
        }

        override fun newArray(size: Int): Array<DetailRecord?> {
            return arrayOfNulls(size)
        }
    }
}