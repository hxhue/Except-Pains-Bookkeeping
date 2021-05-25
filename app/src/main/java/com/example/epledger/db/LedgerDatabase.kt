package com.example.epledger.db.model

import com.example.epledger.model.Record
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

/**
 * 数据库的接口。
 * TODO: 增加Context参数
 * 下面接口的实现中不必考虑异步问题，只需给出同步实现，异步部分在接口外做。
 * 默认的排序方式是时间的倒序：时间越大越靠前。
 */
interface LedgerDatabase {
    /**
     * 从数据库中获取按照日期排序的记录。时间越靠近现在，排序后的位置越靠前。
     * 查询结果中不包含不完整的记录。
     */
    fun getRecordsOrderByDate(): List<Record>

    /**
     * 类似getRecordsOrderByDate，但结果中只包含不完整的记录。
     */
    fun getIncompleteRecordsOrderByDate(): MutableList<Record>

    /**
     * 找出所有标星的记录，按照时间排列（排列规则同上）。
     */
    fun getStarredRecords(): MutableList<Record>

    /**
     * 找出所有含有截图的记录，按照时间排列。
     */
    fun getRecordsWithPic(): MutableList<Record>

    /**
     * 向数据库中插入一条记录，插入后返回id。由于指针有引用性，不要对这个参数做任何修改。
     */
    fun insertRecord(record: Record): Long

    /**
     * 通过给定的ID删除一条记录。
     */
    fun deleteRecordByID(id: Long)

    /**
     * 更新一条记录。id就是传入参数中的id。由于指针有引用性，不要对这个参数做任何修改。
     */
    fun updateRecord(record: Record)
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
            note = "买了一个新球拍。"
        }
        val rec2 = Record().apply {
            ID = 13
            moneyAmount = -29.9
            category = "Study"
            mDate = simpleFormat.parse("2021/01/01 14:37")!!
            note = "这是黄冈密卷，妈妈说这是她对我的爱。"
        }
        val rec3 = rec1.getCopy().apply {
            ID = 12
            moneyAmount = -199.0
            source = "Wechat"
            mDate = simpleFormat.parse("2020/12/31 08:19")!!
            starred = true
            note = "我是有钱人，我又买了一个新球拍。但这次是用微信支付。"
        }
        val rec4 = rec3.getCopy().apply { ID = 17 }
        val rec5 = rec4.getCopy().apply { ID = 18 }
        // 不完整的记录也是有ID的，因为已经记录在数据库中了
        val incompleteRec1 = Record().apply { ID = 20 }
        arrayListOf(rec1, rec2, rec3, rec4, rec5, incompleteRec1)
    }

    override fun getRecordsOrderByDate(): List<Record> {
        return records.filter { it.isComplete() }.sortedWith(Record.dateReverseComparator)
    }

    override fun getIncompleteRecordsOrderByDate(): MutableList<Record> {
        val result = ArrayList<Record>(0)
        records.filter { !it.isComplete() }
            .sortedWith(Record.dateReverseComparator)
            .forEach { result.add(it) }
        return result
    }

    override fun getStarredRecords(): MutableList<Record> {
        val result = ArrayList<Record>(0)
        records.filter { it.starred }
            .sortedWith(Record.dateReverseComparator)
            .forEach { result.add(it) }
        return result
    }

    override fun getRecordsWithPic(): MutableList<Record> {
        val result = ArrayList<Record>(0)
        records.filter { !it.screenshotPath.isNullOrBlank() }
            .sortedWith(Record.dateReverseComparator)
            .forEach { result.add(it) }
        return result
    }

    override fun insertRecord(record: Record): Long {
        val recordToInsert = record.getCopy()
        recordToInsert.ID = ++currentId
        records.add(recordToInsert)
        return currentId
    }

    override fun deleteRecordByID(id: Long) {
        records.removeIf {
            it.ID == id
        }
    }

    override fun updateRecord(record: Record) {
        records.find { it.ID == record.ID }?.apply {
            record.copyTo(this)
        }
    }
}