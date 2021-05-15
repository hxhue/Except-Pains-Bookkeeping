package com.example.epledger.model

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.epledger.R
import com.example.epledger.detail.DetailRecord
import com.example.epledger.settings.datamgr.Category
import com.example.epledger.settings.datamgr.Source
import kotlinx.coroutines.*

class DatabaseViewModel: ViewModel() {
    // To prevent null-pointer exception, we have to initialize them
    val sources = MutableLiveData<ArrayList<Source>>(ArrayList(0))
    val categories = MutableLiveData<ArrayList<Category>>(ArrayList(0))
    val records = MutableLiveData<ArrayList<DetailRecord>>(ArrayList(0))

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
                        Category("Emergency", R.drawable.u_emergency, 2)
                )

                val rec1 = DetailRecord().apply {
                    ID = 11
                    amount = 123.0
                    category = "Sports"
                    source = "Alipay"
                    date = java.util.Date()
                    hourOfDay = 12
                    starred = true
                    minuteOfHour = 13
                    note = "Happy birthday!"
                }

                val rec2 = rec1.getCopy().apply {
                    ID = 12
                    amount = 18.0
                    note = "Nothing~"
                }

                val rec3 = rec2.getCopy().apply {
                    ID = 13
                    source = "Wechat"
                    category = "Emergency"
                    note = "Stupid algorithm"
                }

                val recordList = arrayListOf(
                    rec1, rec2, rec3
                )

                withContext(Dispatchers.Main) {
                    sources.value = srcList
                    categories.value = cateList
                    records.value = recordList
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

    fun requireRecords(): ArrayList<DetailRecord> {
        return this.records.value!!
    }
}