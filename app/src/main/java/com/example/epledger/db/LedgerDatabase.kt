package com.example.epledger.db

import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import androidx.core.database.getStringOrNull
import com.example.epledger.R
import com.example.epledger.model.Category
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

    fun getAllSourceNames(): MutableList<String>
    /**
     * 获取所有的种类记录。
     */
    fun getAllCategories(): MutableList<Category>
    fun getAllCategoryNames(): MutableList<String>

    /**
     *  根据开始日期、结束日期、Source和Category查询Records，用于chart模块
     */
    fun siftRecords(dateStart:Date, dateEnd:Date, sources:List<Source>, categories:List<Category>): List<Record>
}

/**
 * 应用使用的数据库。
 */
val AppDatabase: LedgerDatabase = MemoryDatabase()

/**
 * 内存中数据库的模拟。有相同的接口。
 */
class MemoryDatabase : LedgerDatabase {
    val im=ImportDataFromExcel();
    val simpleFormat = SimpleDateFormat("yyyy/MM/dd hh:mm", Locale.US)

    /*val records: MutableList<Record> = run {
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
    }*/

    override fun getRecordsOrderByDate(): List<Record> {
        val sqLiteDatabase: SQLiteDatabase = im.dbHelper.getReadableDatabase()
        val cursor: Cursor = sqLiteDatabase.rawQuery(
                "SELECT * FROM " + MySQLiteOpenHelper.TABLE_NAME + " order by date desc",
                null
        )
        val res =ArrayList<Record>()
        if (cursor != null && cursor.count > 0) {
            while (cursor.moveToNext()) {
                val b = Record()
                val t=cursor.getLong(cursor.getColumnIndex(MySQLiteOpenHelper.record_id))
                val from_id = cursor.getInt(cursor.getColumnIndex(MySQLiteOpenHelper.from_id))
                val type_id = cursor.getInt(cursor.getColumnIndex(MySQLiteOpenHelper.type_id))
                val s = cursor.getInt(cursor.getColumnIndex(MySQLiteOpenHelper.star))==1
                val from1 = im.get_id_from(from_id, sqLiteDatabase)
                val type1 = im.get_id_type(type_id, sqLiteDatabase)
                val r = b.apply {
                    ID =t
                    moneyAmount =cursor.getDouble(cursor.getColumnIndex(MySQLiteOpenHelper.account))
                    source = from1
                    category=type1
                    mDate = simpleFormat.parse(cursor.getStringOrNull(cursor.getColumnIndex(MySQLiteOpenHelper.date1)))
                    starred = s
                    //screenshot: Bitmap? = null
                    screenshotPath=cursor.getStringOrNull(cursor.getColumnIndex(MySQLiteOpenHelper.bitmap))
                    note = cursor.getStringOrNull(cursor.getColumnIndex(MySQLiteOpenHelper.date1))
                }
                if(!r.isComplete()) res.add(r)
            }
            cursor.close()
        }
        return res
    }

    override fun getIncompleteRecordsOrderByDate(): MutableList<Record> {
        val sqLiteDatabase: SQLiteDatabase = im.dbHelper.getReadableDatabase()
        val cursor: Cursor = sqLiteDatabase.rawQuery(
                "SELECT * FROM " + MySQLiteOpenHelper.TABLE_NAME + " order by date desc",
                null
        )
        val simpleFormat = SimpleDateFormat("yyyy/MM/dd hh:mm", Locale.US)
        val res =ArrayList<Record>()
        if (cursor != null && cursor.count > 0) {
            while (cursor.moveToNext()) {
                val b = Record()
                val t=cursor.getLong(cursor.getColumnIndex(MySQLiteOpenHelper.record_id))
                val from_id = cursor.getInt(cursor.getColumnIndex(MySQLiteOpenHelper.from_id))
                val type_id = cursor.getInt(cursor.getColumnIndex(MySQLiteOpenHelper.type_id))
                val s = cursor.getInt(cursor.getColumnIndex(MySQLiteOpenHelper.star))==1
                val from1 = im.get_id_from(from_id, sqLiteDatabase)
                val type1 = im.get_id_type(type_id, sqLiteDatabase)
                val r = b.apply {
                    ID =t
                    moneyAmount =cursor.getDouble(cursor.getColumnIndex(MySQLiteOpenHelper.account))
                    source = from1
                    category=type1
                    mDate = simpleFormat.parse(cursor.getStringOrNull(cursor.getColumnIndex(MySQLiteOpenHelper.date1)))
                    starred = s
                    //screenshot: Bitmap? = null
                    screenshotPath=cursor.getStringOrNull(cursor.getColumnIndex(MySQLiteOpenHelper.bitmap))
                    note = cursor.getStringOrNull(cursor.getColumnIndex(MySQLiteOpenHelper.date1))
                }
                if(!r.isComplete()) res.add(r)
            }
            cursor.close()
        }
        return res
    }

    override fun getStarredRecords(): MutableList<Record> {
        val simpleFormat = SimpleDateFormat("yyyy/MM/dd hh:mm", Locale.US)
        val sqLiteDatabase: SQLiteDatabase = im.dbHelper.getReadableDatabase()
        val cursor:Cursor = sqLiteDatabase.rawQuery(
                "SELECT * FROM " + MySQLiteOpenHelper.TABLE_NAME + " WHERE star=1 order by date desc",
                null
        )

        val res =ArrayList<Record>()
        if (cursor != null && cursor.count > 0) {
            while (cursor.moveToNext()) {
                val b = Record()
                val t=cursor.getLong(cursor.getColumnIndex(MySQLiteOpenHelper.record_id))
                val from_id = cursor.getInt(cursor.getColumnIndex(MySQLiteOpenHelper.from_id))
                val type_id = cursor.getInt(cursor.getColumnIndex(MySQLiteOpenHelper.type_id))
                val s = cursor.getInt(cursor.getColumnIndex(MySQLiteOpenHelper.star))==1
                val from1 = im.get_id_from(from_id, sqLiteDatabase)
                val type1 = im.get_id_type(type_id, sqLiteDatabase)
                val r = b.apply {
                    ID =t
                    moneyAmount =cursor.getDouble(cursor.getColumnIndex(MySQLiteOpenHelper.account))
                    source = from1
                    category=type1
                    mDate =simpleFormat.parse(cursor.getStringOrNull(cursor.getColumnIndex(MySQLiteOpenHelper.date1)))
                    starred = s
                    //screenshot: Bitmap? = null
                    screenshotPath=cursor.getStringOrNull(cursor.getColumnIndex(MySQLiteOpenHelper.bitmap))
                    note = cursor.getStringOrNull(cursor.getColumnIndex(MySQLiteOpenHelper.date1))
                }
                res.add(r)
            }
            cursor.close()
        }
        return res
    }

//    override fun getRecordsWithPic(): MutableList<Record> {
//        val result = ArrayList<Record>(0)
//        records.filter { !it.screenshotPath.isNullOrBlank() }
//            .sortedWith(Record.dateReverseComparator)
//            .forEach { result.add(it) }
//        return result
//    }

    override fun insertRecord(record: Record): Long {
        val sqLiteDatabase: SQLiteDatabase = im.dbHelper.getWritableDatabase()
        val typeid=im.SelectTypeId(record.category,sqLiteDatabase)
        val fromid=im.SelectFromId(record.source,sqLiteDatabase)
        val simpleFormat = SimpleDateFormat("yyyy/MM/dd hh:mm", Locale.US)
        val s=0
        if (record.starred)
        {
            val s=1
        }
        val c=im.getContentValues(simpleFormat.format(record.mDate),record.moneyAmount,typeid,fromid,record.note,record.screenshotPath,s)
        sqLiteDatabase.insert(MySQLiteOpenHelper.TABLE_NAME, null, c)
        val id=im.FindRecordID(fromid,typeid,record.moneyAmount,sqLiteDatabase)
        return id
    }

    override fun deleteRecordByID(id: Long) {
        val tmp=id
        val sqLiteDatabase: SQLiteDatabase = im.dbHelper.getWritableDatabase()
        im.DeleteID(tmp,sqLiteDatabase)
    }

    override fun updateRecord(record: Record) {
        val sqLiteDatabase: SQLiteDatabase = im.dbHelper.getWritableDatabase()
        val typeid=im.SelectTypeId(record.category,sqLiteDatabase)
        val fromid=im.SelectFromId(record.source,sqLiteDatabase)
        val s=0
        if (record.starred)
        {
            val s=1
        }
        val c=im.getContentValues(simpleFormat.format(record.mDate),record.moneyAmount,typeid,fromid,record.note,record.screenshotPath,s)
        im.Update(record.ID ,c,sqLiteDatabase)
    }

    override fun getAllSources(): MutableList<Source> {
        val sqLiteDatabase: SQLiteDatabase = im.dbHelper.getReadableDatabase()
        val cursor: Cursor = sqLiteDatabase.rawQuery(
                "SELECT * FROM " + MySQLiteOpenHelper.TABLE_NAME5,
                null
        )
        val res =ArrayList<Source>()
        if (cursor != null && cursor.count > 0) {
            while (cursor.moveToNext()) {
                val t=cursor.getInt(cursor.getColumnIndex(MySQLiteOpenHelper.from_id))
                val from1=cursor.getString(cursor.getColumnIndex(MySQLiteOpenHelper.from1))
                val b=Source(from1,t)
                res.add(b)
            }
            cursor.close()
        }
        return res
    }

    override fun getAllCategories(): MutableList<Category> {
        val sqLiteDatabase: SQLiteDatabase = im.dbHelper.getReadableDatabase()
        val cursor: Cursor = sqLiteDatabase.rawQuery(
                "SELECT * FROM " + MySQLiteOpenHelper.TABLE_NAME2,
                null
        )
        val res =ArrayList<Category>()
        if (cursor != null && cursor.count > 0) {
            while (cursor.moveToNext()) {
                val t=cursor.getInt(cursor.getColumnIndex(MySQLiteOpenHelper.type_id))
                val icon=cursor.getInt(cursor.getColumnIndex(MySQLiteOpenHelper.iconresid))
                val type=cursor.getString(cursor.getColumnIndex(MySQLiteOpenHelper.type1))
                val b=Category(type,icon,t)
                res.add(b)
            }
            cursor.close()
        }
        return res
    }
    override fun getAllSourceNames(): MutableList<String>{
        val sqLiteDatabase: SQLiteDatabase = im.dbHelper.getReadableDatabase()
        val cursor: Cursor = sqLiteDatabase.rawQuery(
                "SELECT * FROM " + MySQLiteOpenHelper.TABLE_NAME5,
                null
        )
        val res =ArrayList<String>()
        if (cursor != null && cursor.count > 0) {
            while (cursor.moveToNext()) {
                val from1=cursor.getString(cursor.getColumnIndex(MySQLiteOpenHelper.from1))
                res.add(from1)
            }
            cursor.close()
        }
        return res
    }
    override fun getAllCategoryNames(): MutableList<String>{
        val sqLiteDatabase: SQLiteDatabase = im.dbHelper.getReadableDatabase()
        val cursor: Cursor = sqLiteDatabase.rawQuery(
                "SELECT * FROM " + MySQLiteOpenHelper.TABLE_NAME2,
                null
        )
        val res =ArrayList<String>()
        if (cursor != null && cursor.count > 0) {
            while (cursor.moveToNext()) {
                val from1=cursor.getString(cursor.getColumnIndex(MySQLiteOpenHelper.type_id))
                res.add(from1)
            }
            cursor.close()
        }
        return res
    }

    override fun siftRecords(
        dateStart:Date,
        dateEnd: Date,
        sources: List<Source>,
        categories: List<Category>
    ): List<Record> {
        // 2021-05-29 15:20:56 [Simon Yu]
        // After merging 2d10d9c
        // 编译错误：Class 'MemoryDatabase' is not abstract and does not implement abstract member public abstract fun siftRecords(dateStart: String, dateEnd: String, sources: List<Source>, categories: List<Category>): List<Record> defined in com.example.epledger.db.LedgerDatabase
        // 解决方式：补充了空实现以通过编译。请检查是否有实现没有被commit。
        //TODO("Not yet implemented")
        val im=ImportDataFromExcel();
        val sqLiteDatabase: SQLiteDatabase = im.dbHelper.getReadableDatabase()
        val simpleFormat = SimpleDateFormat("yyyy/MM/dd hh:mm", Locale.US)
        val start=simpleFormat.format(dateStart)
        val end=simpleFormat.format(dateEnd)
        val res=im.FindTimeFrom(sqLiteDatabase,start,end,sources, categories)
        return res
    }
}