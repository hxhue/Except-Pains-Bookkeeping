package com.example.epledger.db

import android.content.Context
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import com.example.epledger.model.Category
import com.example.epledger.model.Record
import com.example.epledger.model.Source
import java.lang.IllegalArgumentException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

/**
 * 数据库的接口。
 * 下面接口的实现中不必考虑异步问题，只需给出同步实现，异步部分在接口外做。
 * 默认的排序方式是时间的倒序：时间越大越靠前。
 */
abstract class LedgerDatabase {
    private val latestAccessTime = MutableLiveData<Date>(Date())

    fun updateAccessTime() {
        latestAccessTime.postValue(Date())
    }

    fun observe(owner: LifecycleOwner, observer: androidx.lifecycle.Observer<Date>) {
        latestAccessTime.observe(owner, observer)
    }

    /**
     * 从数据库中获取按照日期排序的记录。时间越靠近现在，排序后的位置越靠前。
     * 注意：查询结果中不包含不完整的记录。
     */
    abstract fun getRecordsOrderByDate(): List<Record>

    /**
     * 类似getRecordsOrderByDate，但结果中只包含不完整的记录。
     */
    abstract fun getIncompleteRecordsOrderByDate(): MutableList<Record>

    /**
     * 找出所有标星的记录，按照时间排列（排列规则同上）。
     * 注意：查询结果中不包含不完整的记录。
     */
    abstract fun getStarredRecords(): MutableList<Record>

    /**
     * 向数据库中插入一条记录，插入后返回id。由于指针有引用性，不要对这个参数做任何修改。
     */
    abstract fun insertRecord(record: Record): Long

    /**
     * 通过给定的ID删除一条记录。
     */
    abstract fun deleteRecordByID(id: Long)

    /**
     * 更新一条记录。id就是传入参数中的id。由于指针有引用性，不要对这个参数做任何修改。
     */
    abstract fun updateRecord(record: Record)

    /**
     * 获取所有的来源记录。
     */
    abstract fun getAllSources(): MutableList<Source>

    /**
     * 获取所有的种类记录。
     */
    abstract fun getAllCategories(): MutableList<Category>

    /**
     *  根据开始日期、结束日期、Source和Category查询Records，用于chart模块
     */
    abstract fun siftRecords(dateStart:Date, dateEnd:Date, sources:ArrayList<String>, categories:ArrayList<String>): MutableList<Record>

    abstract fun getAllSourceNames(): MutableList<String>

    abstract fun getAllCategoryNames(): MutableList<String>

    /**
     * 向数据库中插入一条新的种类。返回插入好后的id。
     */
    abstract fun insertCategory(category: Category): Int

    /**
     * 更新数据库中的种类。
     */
    abstract fun updateCategory(category: Category)

    /**
     * 删除数据库中的一条种类。
     */
    abstract fun deleteCategoryByID(id: Int)

    /**
     * 向数据库中插入一条新的来源（账户）信息。返回插入好后的id。
     */
    abstract fun insertSource(source: Source): Int

    /**
     * 更新数据库中的来源（账户）信息。
     */
    abstract fun updateSource(source: Source)

    /**
     * 删除数据库中的一条来源（账户）信息。
     */
    abstract fun deleteSourceByID(id: Int)

}

/**
 * 应用使用的数据库。
 */
private lateinit var AppDatabaseContent: LedgerDatabase
var AppDatabase: LedgerDatabase
    get() {
        AppDatabaseContent.updateAccessTime()
        return AppDatabaseContent
    }
    set(value) {
        AppDatabaseContent = value
    }

/**
 * 内存中数据库的模拟。有相同的接口。
 */
class MemoryDatabase(private val context: Context) : LedgerDatabase() {

    var currentId: Long = 20

    private val records: MutableList<Record> = run {
        val simpleFormat = SimpleDateFormat("yyyy/MM/dd hh:mm", Locale.US)
        val rec1 = Record().apply {
            id = 11
            money = -2021.0
            categoryID = 2
            sourceID = 2
            date = simpleFormat.parse("2020/12/31 12:13")!!
            note = "买了一个新的。"
        }
        val rec2 = Record().apply {
            id = 13
            money = -29.9
            categoryID = 3
            date = simpleFormat.parse("2021/01/01 14:37")!!
            note = "这是黄冈密卷，妈妈说这是她对我的爱。"
            starred = true
        }
        val rec3 = rec1.getCopy().apply {
            id = 12
            money = -3099.0
            sourceID = 4
            date = simpleFormat.parse("2020/12/31 08:19")!!
            starred = true
            note = "我是有钱人。"
        }
        val rec4 = rec3.getCopy().apply { id = 17 }
        val rec5 = rec4.getCopy().apply { id = 18 }
        val incompleteRec1 = Record().apply { id = 20 }
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

    override fun insertRecord(record: Record): Long {
        val recordToInsert = record.getCopy()
        recordToInsert.id = ++currentId
        records.add(recordToInsert)
        return currentId
    }

    override fun deleteRecordByID(id: Long) {
        records.removeIf {
            it.id == id
        }
    }

    override fun updateRecord(record: Record) {
        records.find { it.id == record.id }?.apply {
            record.copyTo(this)
        }
    }

    private val sources: MutableList<Source> = Source.getDefaultSources(context)

    override fun getAllSources(): MutableList<Source> {
        return sources.toMutableList()
    }

    val categories: MutableList<Category> = Category.getDefaultCategories(context)

    override fun getAllCategories(): MutableList<Category> {
        return categories.toMutableList()
    }

    override fun siftRecords(dateStart: Date, dateEnd: Date, sources: ArrayList<String>, categories: ArrayList<String>): ArrayList<Record> {
//        val sourceStrs=HashSet<String>()
//        val categoryStrs=HashSet<String>()
//        for(src in sources)
//            sourceStrs.add(src.name)
//        for(cat in categories)
//            categoryStrs.add(cat.name)
        val result=ArrayList<Record>()
        /*records.filter { it.date>dateStart&&it.date<dateEnd&&sources.contains(it.sourceID)&&categories.contains(it.categoryID)}
                .sortedWith(Record.dateReverseComparator)
                .forEach { result.add(it) }*/
        return result
    }

    override fun getAllSourceNames(): MutableList<String> {
        return getAllSources().map { it.name }.toMutableList()
    }

    override fun getAllCategoryNames(): MutableList<String> {
        return getAllCategories().map { it.name }.toMutableList()
    }

    override fun insertCategory(category: Category): Int {
        val copy = category.copy()
        ++currentId
        copy.ID = currentId.toInt()
        categories.add(copy)
        return copy.ID!!
    }

    override fun updateCategory(category: Category) {
        if (category.ID == null) {
            throw IllegalArgumentException("ID of category cannot be null")
        }
        categories.forEach {
            if (it.ID == category.ID) {
                it.apply {
                    name = category.name
                    iconResID = category.iconResID
                }
                return@forEach
            }
        }
    }

    override fun deleteCategoryByID(id: Int) {
        categories.removeIf { it.ID == id }
    }

    override fun insertSource(source: Source): Int {
        val copy = source.copy()
        ++currentId
        copy.ID = currentId.toInt()
        sources.add(copy)
        return copy.ID!!
    }

    override fun updateSource(source: Source) {
        if (source.ID == null) {
            throw IllegalArgumentException("ID of source cannot be null")
        }
        sources.forEach {
            if (source.ID == it.ID) {
                it.name = source.name
                return@forEach
            }
        }
    }

    override fun deleteSourceByID(id: Int) {
        sources.removeAll {
            it.ID == id
        }
    }
}