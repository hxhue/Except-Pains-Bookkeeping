package com.example.epledger.db

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.epledger.R
import com.example.epledger.db.model.AppDatabase
import com.example.epledger.db.model.LedgerDatabase
import com.example.epledger.settings.datamgr.Category
import com.example.epledger.settings.datamgr.Source
import kotlinx.coroutines.*

class DatabaseModel: ViewModel() {
    // To prevent null-pointer exception, we have to initialize them
    val sources = MutableLiveData<ArrayList<Source>>(ArrayList(0))
    val categories = MutableLiveData<ArrayList<Category>>(ArrayList(0))

    // Remove ↓
//    val records = MutableLiveData<ArrayList<DetailRecord>>(ArrayList(0))

    val groupedRecords = MutableLiveData<List<LedgerDatabase.RecordGroup>>(
        ArrayList<LedgerDatabase.RecordGroup>()
    )

//    val dueEvents = MutableLiveData<ArrayList<EventItem>>(ArrayList(0))
//    val incompleteRecords = MutableLiveData<ArrayList<DetailRecord>>(ArrayList(0))
//    val shotsIncludedRecords = MutableLiveData<ArrayList<DetailRecord>>(ArrayList(0))
//    val starredRecords = MutableLiveData<ArrayList<DetailRecord>>(ArrayList(0))

    /**
     * Fetch all data we need from database.
     */
    init {
        GlobalScope.launch {
            withContext(Dispatchers.IO) {
                // TODO: change debug code to: fetch data from DB
                delay(200)
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
                val groups = LedgerDatabase.groupRecordsByDate(records)

                withContext(Dispatchers.Main) {
                    sources.value = srcList
                    categories.value = cateList
                    groupedRecords.value = groups
//                    records.value = recordList
//                    incompleteRecords.value = arrayListOf(rec2, rec3)
//                    starredRecords.value = arrayListOf(rec1)
//                    shotsIncludedRecords.value = arrayListOf(rec3)
                }
            }
        }
    }

    fun requireSources(): ArrayList<Source> {
        return sources.value!!
    }

    fun requireCategories(): ArrayList<Category> {
        return categories.value!!
    }

    fun requireGroupedRecords(): List<LedgerDatabase.RecordGroup> {
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
}