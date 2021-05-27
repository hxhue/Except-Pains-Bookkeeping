package com.example.epledger.model

import android.graphics.Bitmap
import android.os.Parcel
import android.os.Parcelable
import java.util.Date

class Record() : Parcelable{
    var ID: Long? = null
    var mDate: Date = Date()
    var moneyAmount: Double = -0.0
    var category: String? = null
    var source: String? = null
    var screenshot: Bitmap? = null
    var screenshotPath: String? = null
    var note: String? = null
    var starred: Boolean = false

    constructor(parcel: Parcel) : this() {
        ID = parcel.readValue(Long::class.java.classLoader) as? Long
        moneyAmount = parcel.readValue(Double::class.java.classLoader) as Double
        category = parcel.readString()
        source = parcel.readString()
        screenshotPath = parcel.readString()
        note = parcel.readString()
        starred = parcel.readByte() != 0.toByte()
        mDate = parcel.readValue(Date::class.java.classLoader) as Date
    }

    override fun toString(): String {
        return "LedgerRecord(date=$mDate, amount=$moneyAmount, " +
                "type=$category, source=$source, screenshot=$screenshot, " +
                "note=$note, starred=$starred)"
    }

    /*override fun getEntryId(): Int {
        return ID!!.toInt()
    }

    override fun setEntryId(id: Int) {
        this.ID = id.toLong()
    }

    override fun getAmount(): Double {
        return this.moneyAmount!!
    }

    override fun setAmount(amount: Double) {
        this.moneyAmount = amount
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

    override fun getDate(): Date {
        return this.mDate
    }

    override fun setDate(date: Date) {
        this.mDate = date
    }

    override fun getEntryCategory(): String? {
        return this.category
    }

    override fun setEntryCategory(s: String) {
        this.category = s
    }*/

    fun getCopy(): Record {
        val another = Record()
        copyTo(another)
        return another
    }

    fun copyTo(another: Record) {
        another.ID = ID
        another.mDate = mDate
        another.moneyAmount = moneyAmount
        another.category = category
        another.source = source
        another.screenshot = screenshot
        another.note = note
        another.starred = starred
        another.screenshotPath = screenshotPath
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeValue(ID)
        parcel.writeValue(moneyAmount)
        parcel.writeString(category)
        parcel.writeString(source)
        parcel.writeString(screenshotPath)
        parcel.writeString(note)
        parcel.writeByte(if (starred) 1 else 0)
        parcel.writeValue(mDate)
    }

    override fun describeContents(): Int {
        return 0
    }

    /**
     * 判断一个记录是否不完整。
     */
    fun isComplete(): Boolean {
        return (moneyAmount != 0.0 && moneyAmount != -0.0) && category != null
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
            o2.mDate.compareTo(o1.mDate)
        }
    }
}