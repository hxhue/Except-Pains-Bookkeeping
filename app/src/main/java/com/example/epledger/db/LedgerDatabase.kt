package com.example.epledger.db.model

import com.example.epledger.model.Record
import java.text.SimpleDateFormat
import java.util.*

interface LedgerDatabase {
    data class RecordGroup(val date: Date, val records: MutableList<Record>)

    /**
     * 从数据库中获取按照日期排序的记录。时间越靠近现在，排序后的位置越靠前。
     */
    fun getRecordsOrderByDate(): List<Record>

    /**
     * 向数据库中插入一条记录，插入后返回id。
     * 建议：根据是否完整插入考虑插入到不同的位置以提高之后查找的效率。
     */
    fun insertRecord(record: Record): Long
}

/**
 * 应用使用的数据库。
 */
val AppDatabase: LedgerDatabase = MemoryDatabase()

/**
 * 内存中数据库的模拟。有相同的接口。
 */
class MemoryDatabase : LedgerDatabase {

    var currentId: Long = 20

    val records: MutableList<Record> = run {
        val simpleFormat = SimpleDateFormat("yyyy/MM/dd hh:mm", Locale.US)
        val rec1 = Record().apply {
            ID = 11
            moneyAmount = -199.0
            category = "Sports"
            source = "Alipay"
            mDate = simpleFormat.parse("2020/12/31 12:13")!!
//            hourOfDay = 12
//            minuteOfHour = 13
            note = "买了一个新球拍。"
        }
        val rec2 = Record().apply {
            ID = 13
            moneyAmount = -29.9
            category = "Study"
            mDate = simpleFormat.parse("2021/01/01 14:37")!!
//            hourOfDay = 18
//            minuteOfHour = 37
            note = "这是黄冈密卷，妈妈说这是她对我的爱。"
        }
        val rec3 = rec1.getCopy().apply {
            ID = 12
            moneyAmount = -199.0
            source = "Wechat"
            mDate = simpleFormat.parse("2020/12/31 08:19")!!
//            hourOfDay = 9
            starred = true
            note = "我是有钱人，我又买了一个新球拍。但这次是用微信支付。"
        }
        arrayListOf(rec1, rec2, rec3)
    }

    override fun getRecordsOrderByDate(): List<Record> {
        return records.sortedWith(Record.dateReverseComparator)
    }

    override fun insertRecord(record: Record): Long {
        val recordToInsert = record.getCopy()
        recordToInsert.ID = ++currentId
        records.add(recordToInsert)
        return currentId
    }
}