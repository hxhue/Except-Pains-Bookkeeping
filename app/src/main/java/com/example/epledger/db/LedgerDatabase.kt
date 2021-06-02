package com.example.epledger.db

import com.example.epledger.R
import com.example.epledger.model.Category
import com.example.epledger.model.Filter
import com.example.epledger.model.Record
import com.example.epledger.model.Source
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
     * 注意：查询结果中不包含不完整的记录。
     */
    fun getRecordsOrderByDate(): List<Record>

    /**
     * 类似getRecordsOrderByDate，但结果中只包含不完整的记录。
     */
    fun getIncompleteRecordsOrderByDate(): MutableList<Record>

    /**
     * 找出所有标星的记录，按照时间排列（排列规则同上）。
     * 注意：查询结果中不包含不完整的记录。
     */
    fun getStarredRecords(): MutableList<Record>

//    /**
//     * 找出所有含有截图的记录，按照时间排列。
//     */
//    fun getRecordsWithPic(): MutableList<Record>

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

    /**
     * 获取所有的来源记录。
     */
    fun getAllSources(): MutableList<Source>

    /**
     * 获取所有的种类记录。
     */
    fun getAllCategories(): MutableList<Category>

    /**
     *  根据开始日期、结束日期、Source和Category查询Records，用于chart模块
     */
    fun siftRecords(dateStart:Date,dateEnd:Date,sources:ArrayList<String>,categories:ArrayList<String>): MutableList<Record>

    fun getAllSourceNames(): MutableList<String>

    fun getAllCategoryNames(): MutableList<String>

    /**
     * 通过filter筛选记录，默认按日期从早到晚排序
     */
    fun filterRecords(filter: Filter): List<Record>

}

/**
 * 应用使用的数据库。
 */
val AppDatabase: LedgerDatabase = SqliteDatabase()

/**
 * 内存中数据库的模拟。有相同的接口。
 */
class MemoryDatabase : LedgerDatabase {

    var currentId: Long = 20

    val records: MutableList<Record> = run {
        val simpleFormat = SimpleDateFormat("yyyy/MM/dd hh:mm", Locale.US)
        val rec1 = Record().apply {
            ID = 11
            moneyAmount = -2021.0
            category = "Digital"
            source = "Alipay"
            mDate = simpleFormat.parse("2020/12/31 12:13")!!
            note = "买了一个新的。"
        }
        val rec2 = Record().apply {
            ID = 13
            moneyAmount = -29.9
            category = "Study"
            mDate = simpleFormat.parse("2021/01/01 14:37")!!
            note = "这是黄冈密卷，妈妈说这是她对我的爱。"
            starred = true
        }
        val rec3 = rec1.getCopy().apply {
            ID = 12
            moneyAmount = -3099.0
            source = "Wechat"
            mDate = simpleFormat.parse("2020/12/31 08:19")!!
            starred = true
            note = "我是有钱人。"
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

//    override fun getRecordsWithPic(): MutableList<Record> {
//        val result = ArrayList<Record>(0)
//        records.filter { !it.screenshotPath.isNullOrBlank() }
//            .sortedWith(Record.dateReverseComparator)
//            .forEach { result.add(it) }
//        return result
//    }

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

    override fun getAllSources(): MutableList<Source> {
        return arrayListOf(
                Source("Alipay", 1),
                Source("Wechat", 2),
                Source("Cash", 3),
        )
    }

    override fun getAllCategories(): MutableList<Category> {
        return arrayListOf(
                Category("Emergency", R.drawable.ic_fas_asterisk, 2),
                Category("Study", R.drawable.ic_fas_pencil_alt,3),
                Category("Food", R.drawable.ic_fas_utensils, 4),
                Category("Shopping", R.drawable.ic_fas_shopping_cart, 5),
                Category("Transportation", R.drawable.ic_fas_bus, 6),
                Category("Digital", R.drawable.ic_fas_mobile_alt, 7),
                Category("Coffee", R.drawable.ic_fas_coffee, 8),
                Category("Present", R.drawable.ic_fas_gift, 9),
        )
    }

    override fun siftRecords(dateStart: Date, dateEnd: Date, sources: ArrayList<String>, categories: ArrayList<String>): ArrayList<Record> {
//        val sourceStrs=HashSet<String>()
//        val categoryStrs=HashSet<String>()
//        for(src in sources)
//            sourceStrs.add(src.name)
//        for(cat in categories)
//            categoryStrs.add(cat.name)
        val result=ArrayList<Record>()
        records.filter { it.mDate>dateStart&&it.mDate<dateEnd&&sources.contains(it.source)&&categories.contains(it.category)}
                .sortedWith(Record.dateReverseComparator)
                .forEach { result.add(it) }
        return result
    }

    override fun getAllSourceNames(): MutableList<String> {
        return arrayListOf("Alipay","Wechat","Cash")
    }

    override fun getAllCategoryNames(): MutableList<String> {
        return arrayListOf("Emergency", "Study", "Food", "Shopping", "Transportation"
                , "Digital", "Coffee", "Present")
    }

    // TODO: 现在还不知道应该用id还是string索引category和source，之后再改
    override fun filterRecords(filter: Filter): List<Record> {
        TODO()
//        val result = ArrayList<Record>()
//        records.filter { filter.minAmount <= it.moneyAmount &&
//                it.moneyAmount <= filter.maxAmount &&
//                filter.startDate <= it.mDate &&
//                it.mDate <= filter.endDate &&
//                filter.sources.contains(it.sourceId) &&
//                filter.categories.contains(it.categoryId)
//        }
//                .sortedWith(Record.dateReverseComparator)
//                .forEach {result.add(it)}
//        return result
    }
}