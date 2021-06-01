package com.example.epledger.db

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.epledger.home.SectionAdapter
import com.example.epledger.inbox.InboxFragment
import com.example.epledger.model.Record
import com.example.epledger.model.Category
import com.example.epledger.model.RecordGroup
import com.example.epledger.model.Source
import com.example.epledger.nav.MainScreen
import kotlinx.coroutines.*
import java.lang.RuntimeException
import java.util.*
import kotlin.collections.ArrayList

class DatabaseModel: ViewModel() {
    // 对外开放的可观察属性
    // TODO: 把接口中的ArrayList去掉
    val sources = MutableLiveData<MutableList<Source>>(ArrayList(0))
    val categories = MutableLiveData<MutableList<Category>>(ArrayList(0))
    val groupedRecords = MutableLiveData<MutableList<RecordGroup>>(ArrayList(0))
    val incompleteRecords = MutableLiveData<MutableList<Record>>(ArrayList(0))
    val starredRecords = MutableLiveData<MutableList<Record>>(ArrayList(0))

    /**初始化了之后才能够调用这个函数。
     * （2021年5月20日13:43:15）Harmful，但是不知道原因
     */
    fun clearDatabase() {
        sources.postValue(ArrayList(0))
        categories.postValue(ArrayList(0))
        groupedRecords.postValue(ArrayList(0))
        // todo: 更多数据
    }

    /**
     * 重载数据库，拉取所有数据。
     */
    fun reloadDatabase() {
        GlobalScope.launch(Dispatchers.IO) {
            // TODO: change debug code to: fetch data from DB
            val srcList = AppDatabase.getAllSources()

            val cateList = AppDatabase.getAllCategories()

            val records = AppDatabase.getRecordsOrderByDate()

            Log.i("db", "database reloading. records from database: ${records.map { 
                "(amount=%.2f, source=%s)".format(it.money, it.source)
            }}")

            val groupResult = groupRecordsByDate(records)

            Log.i("db", "database reloading. records after grouping: ${groupResult.map {
                it.records.toString()
            }}")

            val incompleteRecordsToPost = AppDatabase.getIncompleteRecordsOrderByDate()
            val starredRecordsToPost = AppDatabase.getStarredRecords()

            sources.postValue(srcList)
            categories.postValue(cateList)
            groupedRecords.postValue(groupResult)
            incompleteRecords.postValue(incompleteRecordsToPost)
            starredRecords.postValue(starredRecordsToPost)
        }
    }

    fun requireSources(): MutableList<Source> {
        return sources.value!!
    }

    fun requireCategories(): MutableList<Category> {
        return categories.value!!
    }

    fun requireGroupedRecords(): MutableList<RecordGroup> {
        return groupedRecords.value!!
    }

    enum class DataModificationMethod {
        INSERT, UPDATE, DELETE
    }

    /**
     * 检查和更新对应的sections。注意已经更新的sections不要传入，此函数不保证幂等调用。
     * 在主线程中调用。
     */
    private fun checkModificationEffectsOnInboxSections(record: Record,
                                                method: DataModificationMethod,
                                                sectionsToCheck: EnumSet<InboxFragment.InboxSectionType> = EnumSet.allOf(InboxFragment.InboxSectionType::class.java)) {
        GlobalScope.launch(Dispatchers.Main) {
            val inboxFragment = MainScreen.INBOX.fragment as InboxFragment

            InboxFragment.InboxSectionType.values().forEach { enumValue ->
                if (enumValue in sectionsToCheck) {
                    when (enumValue) {
                        InboxFragment.InboxSectionType.INCOMPLETE -> {
                            when (method) {
                                DataModificationMethod.INSERT -> inboxFragment.checkIncompleteSectionOnInsertion(record)
                                DataModificationMethod.UPDATE -> inboxFragment.checkIncompleteSectionOnUpdate(record)
                                DataModificationMethod.DELETE -> inboxFragment.checkIncompleteSectionOnRemoval(record)
                            }
                        }

                        InboxFragment.InboxSectionType.STARRED -> {
                            when (method) {
                                DataModificationMethod.INSERT -> inboxFragment.checkStarredSectionOnInsertion(record)
                                DataModificationMethod.UPDATE -> inboxFragment.checkStarredSectionOnUpdate(record)
                                DataModificationMethod.DELETE -> inboxFragment.checkStarredSectionOnRemoval(record)
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * 找到record在组中的位置。要求record必须存在于组中，且list按照时间排序好。而判断record的依据是ID相等。
     * @return Pair<index_of_section, position_in_section>
     */
    private fun requireRecordIndex(record: Record, list: MutableList<RecordGroup>): Pair<Int, Int> {
        val roundedDateOfRecord = roundToDay(record.date)
        val index = list.binarySearch(
            RecordGroup(roundedDateOfRecord, ArrayList(0)),
            RecordGroup.dateReverseComparator
        )
        if (index < 0) {
            throw RuntimeException("The record to be deleted is not in the list.")
        }

        val currentGroup = list[index].records
        var recordIsFound = false
        var positionInSection = 0

        // 由于组内数量少，所以这里的还是很快的
        currentGroup.forEachIndexed { thisIndex, thisRecord ->
            if (thisRecord.ID!! == record.ID!!) {
                recordIsFound = true
                positionInSection = thisIndex
                return@forEachIndexed
            }
        }
        if (!recordIsFound) {
            throw RuntimeException("The record to be deleted is not in the list.")
        }

        return Pair(index, positionInSection)
    }

    /**
     * 搜索并删除record。性能会比另一种重载低一点，但是也能log(n)?
     */
    fun deleteRecord(record: Record, sectionAdapter: SectionAdapter) {
        GlobalScope.launch(Dispatchers.IO) {
            val groupedRecords = requireGroupedRecords()
            val (index, positionInSection) = requireRecordIndex(record, groupedRecords)

            // 因为下面这个重载调用了数据库接口，因此这里不用操作数据库
            deleteRecord(index, positionInSection, sectionAdapter)
        }
    }

    fun deleteRecord(section: Int, position: Int, sectionAdapter: SectionAdapter) {
        GlobalScope.launch(Dispatchers.IO) {
            val groupedRecords = requireGroupedRecords()
            // 在内存中找到数据，取出其ID
            val group = groupedRecords.elementAt(section)
            val record = group.records[position]

            // 在外存中删除
            AppDatabase.deleteRecordByID(record.ID!!)

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

            checkModificationEffectsOnInboxSections(record, DataModificationMethod.DELETE)
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
            val roundedDate = roundToDay(record.date)
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

            checkModificationEffectsOnInboxSections(record, DataModificationMethod.INSERT)
        }
    }

    /**
     * 大概是log(n)的查找复杂度
     */
    fun updateRecord(record: Record, sectionAdapter: SectionAdapter) {
        GlobalScope.launch(Dispatchers.IO) {
            val groupedRecords = requireGroupedRecords()
            val (section, position) = requireRecordIndex(record, groupedRecords)
            updateRecord(section, position, sectionAdapter, newRecord = record)
        }
    }

    /**
     * 更新一个不完整的记录。
     * 由于记录之前不在home界面，所以去查找更新会导致requireRecordIndex设计的运行时异常。
     */
    fun updateIncompleteRecord(record: Record, sectionAdapter: SectionAdapter) {
        GlobalScope.launch(Dispatchers.IO) {
            // Update database
            AppDatabase.updateRecord(record)

            if (record.isComplete()) {
                val list = requireGroupedRecords()
                val roundedDate = roundToDay(record.date)
                val index = list.binarySearch(
                    RecordGroup(roundedDate, ArrayList(0)),
                    RecordGroup.dateReverseComparator
                )

                // 2021-06-01 22:58:19
                // A little bit in hurry
                // God bless me!!!!
                if (index < 0) {
                    // Time to insert a new group
                    val newGroup = RecordGroup(roundedDate, arrayListOf(record.getCopy()))
                    val i = -(index + 1)
                    list.add(i, newGroup)
                    withContext(Dispatchers.Main) {
                        sectionAdapter.notifyItemInserted(i)
                        sectionAdapter.notifyItemRangeChanged(i, list.size)
                    }
                } else {
                    // insert into the group
                    val group = list[index]
                    var indexToInsert = group.records.binarySearch(record, Record.dateReverseComparator)
                    if (indexToInsert < 0) {
                        indexToInsert = -(indexToInsert + 1)
                    }
                    group.records.add(indexToInsert, record.getCopy())
                    withContext(Dispatchers.Main) {
                        sectionAdapter.notifyItemChanged(index)
                    }
                }
            }

            // Update view
            withContext(Dispatchers.Main) {
                checkModificationEffectsOnInboxSections(record, DataModificationMethod.UPDATE)
            }
        }
    }

    /**
     * 更新主页面中的record记录。如果有newRecord则需要替换，否则单纯进行刷新。
     */
    fun updateRecord(section: Int, position: Int, sectionAdapter: SectionAdapter, newRecord: Record? = null) {
        GlobalScope.launch(Dispatchers.IO) {
            val groupedRecords = requireGroupedRecords()
            val group = groupedRecords[section]

            if (newRecord != null) {
                group.records[position] = newRecord
            }

            val recordToUpdate = group.records[position]

            // Update database
            AppDatabase.updateRecord(recordToUpdate)

            // Update view
            withContext(Dispatchers.Main) {
                sectionAdapter.notifySingleItemChanged(section, position)
                checkModificationEffectsOnInboxSections(recordToUpdate, DataModificationMethod.UPDATE)
            }
        }
    }

    /**
     * 接受records，返回按照时间排序好的多个RecordGroup。
     * @param recordsOrderByDate 必须是已经按照date排序好的records
     * 注意，此方法会过滤掉那些不完整的记录。
     */
    private fun groupRecordsByDate(recordsOrderByDate: List<Record>): MutableList<RecordGroup> {
        val groupResult = ArrayList<RecordGroup>()
        var group = ArrayList<Record>()

        recordsOrderByDate.forEach {
            if (!it.isComplete()) {
                // Do nothing if it's incomplete
                Log.d("DatabaseModel#groupRecordsByDate()", "An incomplete record is found.")
            } else if (group.isEmpty()) {
                group.add(it.getCopy())
            } else if (onSameDay(group.first().date, it.date)) {
                group.add(it.getCopy())
            } else {
                // Not on the same day
                // 分组里面的时间必须抹除小时、分钟、秒
                val groupDate = roundToDay(group.first().date)
                groupResult.add(RecordGroup(groupDate, group))
                group = ArrayList()
                group.add(it.getCopy())
            }
        }

        // After iteration, we check for the leftover
        if (group.isNotEmpty()) {
            val groupDate = roundToDay(group.first().date)
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