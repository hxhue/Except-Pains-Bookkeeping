package com.example.epledger.db

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.epledger.R
import com.example.epledger.db.model.AppDatabase
import com.example.epledger.db.model.LedgerDatabase
import com.example.epledger.detail.DetailRecord
import com.example.epledger.settings.datamgr.Category
import com.example.epledger.settings.datamgr.Source
import kotlinx.coroutines.*
import java.util.*
import kotlin.Comparator
import kotlin.collections.ArrayList

class DatabaseModel: ViewModel() {
    // 对外开放的可观察属性
    // TODO: 把接口中的ArrayList去掉
    val sources = MutableLiveData<ArrayList<Source>>(ArrayList(0))
    val categories = MutableLiveData<ArrayList<Category>>(ArrayList(0))
    val groupedRecords = MutableLiveData<Iterable<LedgerDatabase.RecordGroup>>(ArrayList(0))

    // 所有的记录
    private val records = MutableLiveData<ArrayList<DetailRecord>>(ArrayList(0))

    // 反向排序的treeMap
    private var groupedRecordsWithDate = TreeMap<Date, LedgerDatabase.RecordGroup>(Collections.reverseOrder())

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
        records.postValue(ArrayList(0))

        // 非ViewModel的数据是可以在IO线程更新的，也不必post到主线程
        GlobalScope.launch(Dispatchers.IO) {
            groupedRecordsWithDate.clear()
        }
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
            this@DatabaseModel.groupedRecordsWithDate = groupRecordsByDate(records)
            val groups = this@DatabaseModel.groupedRecordsWithDate.values

            sources.postValue(srcList)
            categories.postValue(cateList)
            groupedRecords.postValue(groups)
        }
    }

    fun requireSources(): ArrayList<Source> {
        return sources.value!!
    }

    fun requireCategories(): ArrayList<Category> {
        return categories.value!!
    }

    fun requireGroupedRecords(): Iterable<LedgerDatabase.RecordGroup> {
        return groupedRecords.value!!
    }

//    fun requireRecords(): ArrayList<DetailRecord> {
//        return this.records.value!!
//    }

//    fun requireDueEvents(): ArrayList<EventItem> {
//        return dueEvents.value!!
//    }
//
//    fun requireIncompleteRecords(): ArrayList<DetailRecord> {
//        return incompleteRecords.value!!
//    }
//
//    fun requireShotsIncludedRecords(): ArrayList<DetailRecord> {
//        return shotsIncludedRecords.value!!
//    }
//
//    fun requireStarredRecords(): ArrayList<DetailRecord> {
//        return starredRecords.value!!
//    }

    private fun requireRecords(): ArrayList<DetailRecord> {
        return records.value!!
    }

    fun insertNewRecord(detailRecord: DetailRecord) {
        GlobalScope.launch(Dispatchers.IO) {
            // 获取拷贝
            val record = detailRecord.getCopy()

            // 插入记录到数据库
            val newID = AppDatabase.insertRecord(record)
            record.ID = newID

            // 插入数据到主存并更新信息
            // 1. 更新records
            requireRecords().add(record)

            // 2. 更新groupedRecordsWithDate
            val roundedDate = roundToDay(record.mDate)
            try {
                val recordsWithDate = this@DatabaseModel.groupedRecordsWithDate.getValue(roundedDate)
                // 如果能够找到这样的组，则直接加入
                recordsWithDate.records.apply {
                    add(record)
                    // 加入后排序，由于每一天的记录不会太多，排序是很快的，用列表就足够
                    sortWith(DetailRecord.dateReverseComparator)
                }
            } catch (e: NoSuchElementException) {
                // 找不到这样的组则新建一个组加入
                val newGroup = LedgerDatabase.RecordGroup(roundedDate, arrayListOf(record))
                this@DatabaseModel.groupedRecordsWithDate[roundedDate] = newGroup
            }

            // 3. 更新groupedRecords
            groupedRecords.postValue(groupedRecordsWithDate.values)

            // TODO: 检查其他相关信息，目前只做了首页
        }
    }

    /**
     * 接受records，返回按照时间排序好的多个RecordGroup。
     * @param recordsOrderByDate 必须是已经按照date排序好的records
     * 注意，此方法会过滤掉那些不完整的记录。
     */
    private fun groupRecordsByDate(recordsOrderByDate: List<DetailRecord>): TreeMap<Date, LedgerDatabase.RecordGroup> {
        val map = TreeMap<Date, LedgerDatabase.RecordGroup>(reverseOrder())
        var group = ArrayList<DetailRecord>()

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
                map[groupDate] = LedgerDatabase.RecordGroup(groupDate, group)
                group = ArrayList()
                group.add(it.getCopy())
            }
        }

        // After iteration, we check for the leftover
        if (group.isNotEmpty()) {
            val groupDate = roundToDay(group.first().mDate)
            map[groupDate] = LedgerDatabase.RecordGroup(groupDate, group)
        }

        return map
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