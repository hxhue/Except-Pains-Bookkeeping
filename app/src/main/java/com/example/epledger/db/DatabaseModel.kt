package com.example.epledger.db

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.epledger.R
import com.example.epledger.db.model.AppDatabase
import com.example.epledger.home.SectionAdapter
import com.example.epledger.model.Record
import com.example.epledger.model.Category
import com.example.epledger.model.RecordGroup
import com.example.epledger.model.Source
import kotlinx.coroutines.*
import java.util.*
import kotlin.collections.ArrayList

class DatabaseModel: ViewModel() {
    // 对外开放的可观察属性
    // TODO: 把接口中的ArrayList去掉
    val sources = MutableLiveData<ArrayList<Source>>(ArrayList(0))
    val categories = MutableLiveData<ArrayList<Category>>(ArrayList(0))
//    val groupedRecords = MutableLiveData<MutableCollection<RecordGroup>>(ArrayList(0))
    val groupedRecords = MutableLiveData<MutableList<RecordGroup>>(ArrayList(0))

    // 所有的记录
//    private val records = MutableLiveData<ArrayList<Record>>(ArrayList(0))

    // 反向排序的treeMap
//    private var groupedRecordsWithDate = TreeMap<Date, RecordGroup>(Collections.reverseOrder())

//    val dueEvents = MutableLiveData<ArrayList<EventItem>>(ArrayList(0))
//    val incompleteRecords = MutableLiveData<ArrayList<DetailRecord>>(ArrayList(0))
//    val shotsIncludedRecords = MutableLiveData<ArrayList<DetailRecord>>(ArrayList(0))
//    val starredRecords = MutableLiveData<ArrayList<DetailRecord>>(ArrayList(0))

    /**
     * 清空所有记录，释放存储。但必须已经初始化了之后才能够调用这个函数。
     * （2021年5月20日13:43:15）Harmful，但是不知道原因
     */
    fun clearDatabase() {
        sources.postValue(ArrayList(0))
        categories.postValue(ArrayList(0))
        groupedRecords.postValue(ArrayList(0))
//        records.postValue(ArrayList(0))

        // 非ViewModel的数据是可以在IO线程更新的，也不必post到主线程
//        GlobalScope.launch(Dispatchers.IO) {
//            groupedRecordsWithDate.clear()
//        }
    }

    /**
     * 重载数据库，拉取所有数据。
     */
    fun reloadDatabase() {
        GlobalScope.launch(Dispatchers.IO) {
            // TODO: change debug code to: fetch data from DB
            val srcList = arrayListOf(
                Source("Alipay"),
                Source("Wechat"),
                Source("Cash"),
                Source("狗狗币")
            )

            val cateList = arrayListOf(
                Category("Sports", R.drawable.u_sports_tennis, 1),
                Category("Emergency", R.drawable.u_emergency, 2),
                Category("Study", R.drawable.ic_fas_pencil_alt,3)
            )

            val records = AppDatabase.getRecordsOrderByDate()
            val groupResult = groupRecordsByDate(records)

            sources.postValue(srcList)
            categories.postValue(cateList)
            groupedRecords.postValue(groupResult)
        }
    }

    fun requireSources(): ArrayList<Source> {
        return sources.value!!
    }

    fun requireCategories(): ArrayList<Category> {
        return categories.value!!
    }

    fun requireGroupedRecords(): MutableList<RecordGroup> {
        return groupedRecords.value!!
    }

    fun deleteRecord(section: Int, position: Int, sectionAdapter: SectionAdapter) {
        val groupedRecords = requireGroupedRecords()
        GlobalScope.launch(Dispatchers.IO) {
            // 在内存中找到数据，取出其ID
            val group = groupedRecords.elementAt(section)
            val recordID = group.records[position].ID!!

            // 在外存中删除
            AppDatabase.deleteRecordByID(recordID)

            // 如果该组有多个元素则删除单个元素即可
            if (group.records.size > 1) {
                // 在内存中删除
                // 注意：由于使用的records是引用，因此不要重复删除结构中的记录
                group.records.removeAt(position)

                // 通知视图变更（不要更新value，否则整个视图都会刷新）
                withContext(Dispatchers.Main) {
                    sectionAdapter.notifySingleItemRemoved(section, position, group.records.size)
                }
            } else { // 如果该组只有一个元素，则应该删除整个组而不是单个元素
                // 在内存中删除
                // 注意：由于sections是引用关系，因此不需要再删除一次，否则会出现异常
                groupedRecords.removeAt(section)
                // 通知视图变更
                withContext(Dispatchers.Main) {
                    sectionAdapter.notifyItemRemoved(section)
                    sectionAdapter.notifyItemRangeChanged(section, sectionAdapter.sections.size)
                }
            }
        }
    }

    fun insertRecord(record: Record) {
        GlobalScope.launch(Dispatchers.IO) {
            // 获取拷贝
            val record = record.getCopy()

            // 插入记录到数据库
            val newID = AppDatabase.insertRecord(record)
            record.ID = newID

            // 插入数据到主存并更新信息
            val roundedDate = roundToDay(record.mDate)
            val groupedRecords = requireGroupedRecords()

            val indexToInsert = groupedRecords.binarySearch(
                RecordGroup(roundedDate, ArrayList()),
                RecordGroup.dateReverseComparator
            )

            // 在记录中找到了，直接插入组中
            if (indexToInsert >= 0) {
                groupedRecords[indexToInsert].records.apply {
                    add(record)
                    sortWith(Record.dateReverseComparator)
                }
            } else { // 找不到这样的组则新建一个组加入
                val newGroup = RecordGroup(roundedDate, arrayListOf(record))
                val realIndexToInsert = -(indexToInsert + 1)
                groupedRecords.add(realIndexToInsert, newGroup)
            }

            this@DatabaseModel.groupedRecords.postValue(groupedRecords)

            // TODO: 检查其他相关信息，目前只做了首页
        }
    }

    fun updateRecord(section: Int, position: Int, sectionAdapter: SectionAdapter) {
        val groupedRecords = requireGroupedRecords()
        GlobalScope.launch(Dispatchers.IO) {
            val group = groupedRecords[section]
            val recordToUpdate = group.records[position]
            // Update database
            AppDatabase.updateRecord(recordToUpdate)
            // Update view
            withContext(Dispatchers.Main) {
                sectionAdapter.notifySingleItemChanged(section, position)
            }
        }
    }

    /**
     * 接受records，返回按照时间排序好的多个RecordGroup。
     * @param recordsOrderByDate 必须是已经按照date排序好的records
     * 注意，此方法会过滤掉那些不完整的记录。
     */
    private fun groupRecordsByDate(recordsOrderByDate: List<Record>): MutableList<RecordGroup> {
//        val map = TreeMap<Date, RecordGroup>(reverseOrder())
        val groupResult = ArrayList<RecordGroup>()
        var group = ArrayList<Record>()

        recordsOrderByDate.forEach {
            if (!it.isComplete()) {
                // Do nothing if it's incomplete
                Log.d("DatabaseModel#groupRecordsByDate()", "An incomplete record is found.")
            } else if (group.isEmpty()) {
                group.add(it.getCopy())
            } else if (onSameDay(group.first().mDate, it.mDate)) {
                group.add(it.getCopy())
            } else {
                // Not on the same day
                // 分组里面的时间必须抹除小时、分钟、秒
                val groupDate = roundToDay(group.first().mDate)
                groupResult.add(RecordGroup(groupDate, group))
                group = ArrayList()
                group.add(it.getCopy())
            }
        }

        // After iteration, we check for the leftover
        if (group.isNotEmpty()) {
            val groupDate = roundToDay(group.first().mDate)
            groupResult.add(RecordGroup(groupDate, group))
        }

        return groupResult
    }

    // 把日期保留到天
    // https://stackoverflow.com/a/7930591/13785815
    private fun roundToDay(date: Date): Date {
        val cal = Calendar.getInstance()
        cal.timeInMillis = date.time

        // 清除较小位置的时间
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)

        // 返回
        return Date(cal.timeInMillis)
    }

    /**
     * 判断两个日期是否在同一天。
     */
    private fun onSameDay(date1: Date, date2: Date): Boolean {
        val cal1 = Calendar.getInstance()
        val cal2 = Calendar.getInstance()
        cal1.time = date1;
        cal2.time = date2;
        return cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR) &&
                cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR);
    }
}