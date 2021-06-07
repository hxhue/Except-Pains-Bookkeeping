package com.example.epledger.db
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import androidx.core.database.getStringOrNull
import com.example.epledger.model.Category
import com.example.epledger.model.Filter
import com.example.epledger.model.Record
import com.example.epledger.model.Source
import com.example.epledger.chart.ChartsFragment
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class SqliteDatabase(context: Context) : LedgerDatabase() {
    val im = ImportDataFromExcel(context)
    val simpleFormat = SimpleDateFormat("yyyy/MM/dd hh:mm")
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
                val date1=cursor.getStringOrNull(cursor.getColumnIndex(MySQLiteOpenHelper.date1))
//                val from1 = im.get_id_from(from_id, sqLiteDatabase)
//                val type1 = im.get_id_type(type_id, sqLiteDatabase)
                val r = b.apply {
                    id =t
                    money = cursor.getDouble(cursor.getColumnIndex(MySQLiteOpenHelper.account))

                    // 2021-06-02 17:39:24
                    // We use int for these fields now
                    sourceID = if (from_id <= 0) null else from_id
                    categoryID = if (type_id <= 0) null else type_id
                    date = simpleFormat.parse(date1)
                    starred = s
                    //screenshot: Bitmap? = null
                    screenshotPath=cursor.getStringOrNull(cursor.getColumnIndex(MySQLiteOpenHelper.bitmap))
                    note = cursor.getStringOrNull(cursor.getColumnIndex(MySQLiteOpenHelper.memo))
                }
                if(r.isComplete()) res.add(r)
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
//                val from1 = im.get_id_from(from_id, sqLiteDatabase)
//                val type1 = im.get_id_type(type_id, sqLiteDatabase)
                val r = b.apply {
                    id =t
                    money =cursor.getDouble(cursor.getColumnIndex(MySQLiteOpenHelper.account))

                    // 2021-06-02 17:40:41
                    // We use int for these fields now
                    sourceID = if (from_id <= 0) null else from_id
                    categoryID = if (type_id <= 0) null else type_id

                    date = simpleFormat.parse(cursor.getStringOrNull(cursor.getColumnIndex(MySQLiteOpenHelper.date1)))
                    starred = s
                    //screenshot: Bitmap? = null
                    screenshotPath=cursor.getStringOrNull(cursor.getColumnIndex(MySQLiteOpenHelper.bitmap))
                    note = cursor.getStringOrNull(cursor.getColumnIndex(MySQLiteOpenHelper.memo))
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
//                val from1 = im.get_id_from(from_id, sqLiteDatabase)
//                val type1 = im.get_id_type(type_id, sqLiteDatabase)
                val r = b.apply {
                    id =t
                    money =cursor.getDouble(cursor.getColumnIndex(MySQLiteOpenHelper.account))

                    // 2021-06-02 17:40:56
                    // We use int for these fields now
                    sourceID = if (from_id < 0) null else from_id
                    categoryID = if (type_id < 0) null else type_id

                    date =simpleFormat.parse(cursor.getStringOrNull(cursor.getColumnIndex(MySQLiteOpenHelper.date1)))
                    starred = s
                    //screenshot: Bitmap? = null
                    screenshotPath=cursor.getStringOrNull(cursor.getColumnIndex(MySQLiteOpenHelper.bitmap))
                    note = cursor.getStringOrNull(cursor.getColumnIndex(MySQLiteOpenHelper.memo))
                }

                /**
                 * 2021-6-7 18:06:17 Now we only consider complete records
                 */
                if (r.isComplete()) {
                    res.add(r)
                }
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

//        val typeid = if (record.category == null) -1 else im.SelectTypeId(record.category,sqLiteDatabase)
//        val fromid = if (record.source == null) -1 else im.SelectFromId(record.source,sqLiteDatabase)

        // 2021-06-02 17:42:38
        // We use int for these fields now
        val typeid = record.categoryID ?: -1
        val fromid = record.sourceID ?: -1

        val simpleFormat = SimpleDateFormat("yyyy/MM/dd hh:mm", Locale.US)
        val s = if (record.starred) 1 else 0
        val c=im.getContentValues(simpleFormat.format(record.date),record.money,typeid,fromid,record.note,record.screenshotPath,s)
        sqLiteDatabase.insert(MySQLiteOpenHelper.TABLE_NAME, null, c)
        val id=im.FindRecordID(fromid,typeid,record.money,sqLiteDatabase)
        return id
    }

    override fun deleteRecordByID(id: Long) {
        val tmp=id
        val sqLiteDatabase: SQLiteDatabase = im.dbHelper.getWritableDatabase()
        im.DeleteID(tmp,sqLiteDatabase)
    }

    override fun updateRecord(record: Record) {
        val sqLiteDatabase: SQLiteDatabase = im.dbHelper.getWritableDatabase()

        // Just for sure, we check if the record is in database
        // Checking: step 1
        if (record.id == null) {
            throw RuntimeException("This record have null ID")
        }
        // Checking: step 2
//        val cursor = sqLiteDatabase.rawQuery("select * from " + MySQLiteOpenHelper.TABLE_NAME +
//                " where record_id=${record.ID}", null)
//        if (cursor.moveToNext()) {
//            throw RuntimeException("This record is never in database")
//        }
        // The way of checking may be wrong...

//        val typeid = if (record.category == null) -1 else im.SelectTypeId(record.category,sqLiteDatabase)
//        val fromid = if (record.source == null) - 1 else im.SelectFromId(record.source,sqLiteDatabase)

        // 2021-06-02 17:42:38
        // We use int for these fields now
        val typeid = record.categoryID ?: -1
        val fromid = record.sourceID ?: -1

        val s = if (record.starred) 1 else 0
        val c=im.getContentValues(simpleFormat.format(record.date),record.money,typeid,fromid,record.note,record.screenshotPath,s)
        im.Update(record.id, c, sqLiteDatabase)
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
                val from1=cursor.getString(cursor.getColumnIndex(MySQLiteOpenHelper.type1))
                res.add(from1)
            }
            cursor.close()
        }
        return res
    }
    override fun insertCategory(category: Category): Int {
        val sqLiteDatabase: SQLiteDatabase = im.dbHelper.getWritableDatabase()
        im.AddNewType(sqLiteDatabase,category.name,category.iconResID)
        val res=im.SelectTypeId(category.name,sqLiteDatabase)
        print("我被插入了类型，")
        /*val chart=ChartsFragment()
        chart.addCategory(category.name)*/
        println(category.name)
        return res
    }

    override fun updateCategory(category: Category) {

        val sqLiteDatabase: SQLiteDatabase = im.dbHelper.getWritableDatabase()
        category.ID?.let { im.updateType(sqLiteDatabase,category.name,category.iconResID, it) }
    }

    override fun deleteCategoryByID(id: Int) {

        val sqLiteDatabase: SQLiteDatabase = im.dbHelper.getWritableDatabase()
        im.deleteType(sqLiteDatabase,id)


    }

    override fun insertSource(source: Source): Int {
        val sqLiteDatabase: SQLiteDatabase = im.dbHelper.getWritableDatabase()
        im.AddNewFrom(sqLiteDatabase,source.name)
        print("我被插入了来源，")
        println(source.name)
        val res=im.SelectFromId(source.name,sqLiteDatabase)
        return res
    }

    override fun updateSource(source: Source) {

        val sqLiteDatabase: SQLiteDatabase = im.dbHelper.getWritableDatabase()
        source.ID?.let { im.updateFrom(sqLiteDatabase,source.name, it) }
    }

    override fun deleteSourceByID(id: Int) {

        val sqLiteDatabase: SQLiteDatabase = im.dbHelper.getWritableDatabase()
        im.deleteFrom(sqLiteDatabase,id)
    }

    override fun filterRecords(filter: Filter): List<Record> {
        val sqLiteDatabase: SQLiteDatabase = im.dbHelper.getReadableDatabase()
        val simpleFormat = SimpleDateFormat("yyyy/MM/dd")
        val start=simpleFormat.format(filter.startDate)
        val rectifiedDateEnd=Date(filter.endDate.time+24*60*60*1000)
        val end=simpleFormat.format(rectifiedDateEnd)
        val res=im.filterRecords(sqLiteDatabase,start,end,filter.sources, filter.categories,filter.minAmount,filter.maxAmount)
        return res
    }

    override fun siftRecords(
            dateStart:Date,
            dateEnd: Date,
            sources: ArrayList<String>,
            categories: ArrayList<String>
    ): ArrayList<Record> {
        // 2021-05-29 15:20:56 [Simon Yu]
        // After merging 2d10d9c
        // 编译错误：Class 'MemoryDatabase' is not abstract and does not implement abstract member public abstract fun siftRecords(dateStart: String, dateEnd: String, sources: List<Source>, categories: List<Category>): List<Record> defined in com.example.epledger.db.LedgerDatabase
        // 解决方式：补充了空实现以通过编译。请检查是否有实现没有被commit。
        val sqLiteDatabase: SQLiteDatabase = im.dbHelper.getReadableDatabase()
        val simpleFormat = SimpleDateFormat("yyyy/MM/dd")
        val start=simpleFormat.format(dateStart)
        val rectifiedDateEnd=Date(dateEnd.time+24*60*60*1000)
        val end=simpleFormat.format(rectifiedDateEnd)
        val res=im.FindTimeFrom(sqLiteDatabase,start,end,sources, categories)
        return res
    }
}