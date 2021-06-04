package com.example.epledger.db

import android.util.Log
import android.util.SparseArray
import androidx.core.util.set
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.epledger.home.SectionAdapter
import com.example.epledger.inbox.InboxFragment
import com.example.epledger.model.*
import com.example.epledger.nav.MainScreen
import kotlinx.coroutines.*
import java.lang.RuntimeException
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class DatabaseModel: ViewModel() {
    // 对外开放的可观察属性
    val sources = MutableLiveData<MutableList<Source>>(ArrayList(0))
    val categories = MutableLiveData<MutableList<Category>>(ArrayList(0))
    val groupedRecords = MutableLiveData<MutableList<RecordGroup>>(ArrayList(0))
    val incompleteRecords = MutableLiveData<MutableList<Record>>(ArrayList(0))
    val starredRecords = MutableLiveData<MutableList<Record>>(ArrayList(0))
    val filterGroupedRecords = MutableLiveData<List<RecordGroup>>(ArrayList(0))

    private val sourceMap = SparseArray<Source>(16)
    private val sourceNameMap = HashMap<String, Source>(16)
    private val categoryMap = SparseArray<Category>(16)
    private val categoryNameMap = HashMap<String, Category>(16)

    var databaseHasLoaded = false
        private set

    init {
        reloadDatabase()
    }

    /**
     * 重载数据库，拉取所有数据。
     */
    fun reloadDatabase() {
        GlobalScope.launch(Dispatchers.IO) {
            val sourceList = AppDatabase.getAllSources()
            val categoryList = AppDatabase.getAllCategories()
            val records = AppDatabase.getRecordsOrderByDate()
            val groupResult = groupRecordsByDate(records)
            val incompleteRecordsToPost = AppDatabase.getIncompleteRecordsOrderByDate()
            val starredRecordsToPost = AppDatabase.getStarredRecords()

            Log.i("db", "database reloading. records after grouping: ${groupResult.map {
                it.records.toString()
            }}")

            // Construct maps
            sourceMap.clear()
            sourceNameMap.clear()
            categoryMap.clear()
            categoryNameMap.clear()

            for (source in sourceList) {
                val sourceCopy = source.copy()
                sourceMap.append(source.ID!!, sourceCopy)
                sourceNameMap[source.name] = sourceCopy
            }
            for (category in categoryList) {
                val categoryCopy = category.copy()
                categoryMap.append(category.ID!!, categoryCopy)
                categoryNameMap[category.name] = categoryCopy
            }

            sources.postValue(sourceList)
            categories.postValue(categoryList)
            groupedRecords.postValue(groupResult)
            incompleteRecords.postValue(incompleteRecordsToPost)
            starredRecords.postValue(starredRecordsToPost)
            filterGroupedRecords.postValue(groupResult)

            // So we can use this flag to indicate whether database is loading or already loaded
            // That will be helpful for home page to know whether it should show an empty box picture
            databaseHasLoaded = true
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

    fun requireGroupedRecordsByFilter(filter: Filter): List<RecordGroup> {
        filterGroupedRecords.postValue(filterGroupedRecordsByFilter(filter))
        return filterGroupedRecords.value!!
    }

    private fun filterGroupedRecordsByFilter(filter: Filter): List<RecordGroup> {
        val records = AppDatabase.filterRecords(filter)
        return groupRecordsByDate(records)
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
     * 找到给定的record所属的group的下标，找不到则返回-(indexToInsert + 1)
     */
    private fun findRecordGroupIndex(record: Record, list: MutableList<RecordGroup>): Int {
        val roundedDate = roundToDay(record.date)
        return list.binarySearch(
            RecordGroup(roundedDate, ArrayList(0)),
            RecordGroup.dateReverseComparator
        )
    }

    /**
     * 找到record在组中的位置。要求record必须存在于组中，且list按照时间排序好。而判断record的依据是ID相等。
     * @return Pair<index_of_section, position_in_section>
     */
    private fun requireRecordIndex(record: Record, list: MutableList<RecordGroup>): Pair<Int, Int> {
        val index = findRecordGroupIndex(record, list)
        if (index < 0) {
            throw RuntimeException("The record to be deleted is not in the list.")
        }

        val currentGroup = list[index].records
        var recordIsFound = false
        var positionInSection = 0

        // Find index inside the group
        // This is O(n) but the data set is so small (like less than 5) so it's effective
        currentGroup.forEachIndexed { thisIndex, thisRecord ->
            if (thisRecord.id!! == record.id!!) {
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
            AppDatabase.deleteRecordByID(record.id!!)

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

    fun insertRecord(rec: Record) {
        GlobalScope.launch(Dispatchers.IO) {
            // 获取拷贝
            val record = rec.getCopy()

            // 插入记录到数据库
            val newID = AppDatabase.insertRecord(record)
            record.id = newID

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
     * @param viewRefreshMethod 是更新视图的方式，如果不提供则会用默认方式更新视图
     */
    fun deleteCategoryByID(id: Int, viewRefreshMethod: (()->Unit)? = null) {
        GlobalScope.launch(Dispatchers.IO) {
            AppDatabase.deleteCategoryByID(id)

            // modify map
            categoryNameMap.remove(categoryMap[id].name)
            categoryMap.delete(id)

            withContext(Dispatchers.Main) {
                val requiredCategories = requireCategories()
                requiredCategories.removeIf {
                    it.ID == id
                }

                if (viewRefreshMethod == null) {
                    // Rebind to outlet the modification
                    categories.postValue(categories.value)
                } else {
                    viewRefreshMethod()
                }

                // outlet a notification
                onCategoriesChanged()
            }
        }
    }

    /**
     * @param viewRefreshMethod 是更新视图的方式，如果不提供则会用默认方式更新视图
     */
    fun insertCategory(category: Category, viewRefreshMethod: (()->Unit)? = null) {
        GlobalScope.launch(Dispatchers.IO) {
            // Update in database
            val id = AppDatabase.insertCategory(category)
            category.ID = id

            // modify map
            categoryMap.put(category.ID!!, category)
            categoryNameMap[category.name] = category

            withContext(Dispatchers.Main) {
                // Update in memory
                requireCategories().add(category)

                if (viewRefreshMethod == null) {
                    // Rebind to outlet the modification
                    categories.postValue(categories.value)
                } else {
                    viewRefreshMethod()
                }
//                categories.postValue(categories.value)

                // outlet a notification
                onCategoriesChanged()
            }
        }

    }

    /**
     * @param viewRefreshMethod 是更新视图的方式，如果不提供则会用默认方式更新视图
     */
    fun updateCategory(category: Category, viewRefreshMethod: (()->Unit)? = null) {
        GlobalScope.launch(Dispatchers.IO) {
            // Update in database
            AppDatabase.updateCategory(category)

            // Categories in map should be separate from the arguments outside
            // So the map still have old versions which has their name information
            val originalCategory = categoryMap[category.ID!!]
            categoryNameMap.remove(originalCategory.name)

            // After removal of Name-Category pair, we insert or update new data
            categoryMap[category.ID!!] = category
            categoryNameMap[category.name] = category

            // To avoid concurrent access exception
            withContext(Dispatchers.Main) {
                // Update in memory
                for (requireCategory in requireCategories()) {
                    if (requireCategory.ID == category.ID) {
                        requireCategory.copyAllExceptID(category)
                        break
                    }
                }

                if (viewRefreshMethod == null) {
                    // Rebind to outlet the modification
                    categories.postValue(categories.value)
                } else {
                    viewRefreshMethod()
                }
//                categories.postValue(categories.value)

                // outlet a notification
                onCategoriesChanged()
            }
        }
    }

    fun updateSource(source: Source, viewRefreshMethod: (()->Unit)? = null) {
        GlobalScope.launch(Dispatchers.IO) {
            AppDatabase.updateSource(source)

            // Sources in map should be separate from the arguments outside
            // So the map still have old versions which has their name information
            val originalSource = sourceMap[source.ID!!]
            sourceNameMap.remove(originalSource.name)
            sourceMap[source.ID!!] = source
            sourceNameMap[source.name] = source

            withContext(Dispatchers.Main) {
                val requiredSources = requireSources()
                for (requiredSource in requiredSources) {
                    if (requiredSource.ID == source.ID) {
                        requiredSource.copyAllExceptID(source)
                        break
                    }
                }

                if (viewRefreshMethod == null) {
                    sources.postValue(sources.value)
                } else {
                    viewRefreshMethod()
                }

                onSourcesChanged()
            }
        }
    }

    fun insertSource(source: Source, viewRefreshMethod: (()->Unit)? = null) {
        GlobalScope.launch(Dispatchers.IO) {
            source.ID = AppDatabase.insertSource(source)

            // modify map
            sourceMap.put(source.ID!!, source)
            sourceNameMap[source.name] = source

            withContext(Dispatchers.Main) {
                requireSources().add(source)

                if (viewRefreshMethod == null) {
                    sources.postValue(sources.value)
                } else {
                    viewRefreshMethod()
                }

                onSourcesChanged()
            }
        }
    }

    fun deleteSourceByID(id: Int, viewRefreshMethod: (()->Unit)? = null) {
        GlobalScope.launch(Dispatchers.IO) {
            AppDatabase.deleteSourceByID(id)

            val sourceName = sourceMap[id].name
            sourceNameMap.remove(sourceName)
            sourceMap.delete(id)

            withContext(Dispatchers.Main) {
                requireSources().removeIf {
                    it.ID == id
                }

                if (viewRefreshMethod == null) {
                    sources.postValue(sources.value)
                } else {
                    viewRefreshMethod()
                }

                onSourcesChanged()
            }
        }
    }

    private fun onSourcesChanged() {
        groupedRecords.postValue(groupedRecords.value)
        starredRecords.postValue(starredRecords.value)
        incompleteRecords.postValue(incompleteRecords.value)
    }

    private fun onCategoriesChanged() {
        groupedRecords.postValue(groupedRecords.value)
        starredRecords.postValue(starredRecords.value)
        incompleteRecords.postValue(incompleteRecords.value)
    }

    fun findSource(id: Int): Source? {
        val source = sourceMap.get(id)
        if (source != null) {
            return source.copy()
        }
        return null
    }

    fun findCategory(id: Int): Category? {
        val category = categoryMap.get(id)
        if (category != null) {
            return category.copy()
        }
        return null
    }

    fun findSource(name: String): Source? {
        val source = sourceNameMap[name]
        if (source != null) {
            return source.copy()
        }
        return null
    }

    fun findCategory(name: String): Category? {
        val category = categoryNameMap[name]
        if (category != null) {
            return category.copy()
        }
        return null
    }

    /**
     * 更新主页面中的record记录。如果有newRecord则需要替换，否则单纯进行刷新。
     */
    fun updateRecord(section: Int, position: Int, sectionAdapter: SectionAdapter, newRecord: Record? = null) {
        GlobalScope.launch(Dispatchers.IO) {
            val groupedRecords = requireGroupedRecords()
            val group = groupedRecords[section]

            // If new record is not provided, we assume that the record is modified by reference
            val recordToUpdate = newRecord ?: group.records[position]

            // Update database
            AppDatabase.updateRecord(recordToUpdate)

            // Update view
            // 2021-06-02 15:11:41
            // If the date is changed, we should move this record to another position
            // So we take a delete-then-insert strategy
            withContext(Dispatchers.Main) {
//                sectionAdapter.notifySingleItemChanged(section, position)
                var index = findRecordGroupIndex(recordToUpdate, groupedRecords)

                // Stage 1: delete record
                group.records.removeAt(position)
                sectionAdapter.notifyItemChanged(section)

                // Stage 2: insert record
                // If the record is now in a group that exists, we insert into the group
                if (index >= 0) {
                    groupedRecords[index].records.apply {
                        add(recordToUpdate)
                        sortWith(Record.dateReverseComparator)
                    }
                    // We don't care whether index == section
                    // If that is the case, we just waste a single update action (which is cheap)
                    sectionAdapter.notifyItemChanged(index)
                }
                // If the record is in a new group, we create one to insert
                else {
                    index = -(index + 1)
                    val roundedDate = roundToDay(recordToUpdate.date)
                    val newGroup = RecordGroup(roundedDate, arrayListOf(recordToUpdate))
                    groupedRecords.add(index, newGroup)
                    sectionAdapter.notifyItemInserted(index)
                }

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