package com.example.epledger.model

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.epledger.R
import com.example.epledger.settings.datamgr.Category
import com.example.epledger.settings.datamgr.Source
import kotlinx.coroutines.*

class GlobalDBViewModel: ViewModel() {
    // To prevent null-pointer exception, we have to initialize them
    val sources = MutableLiveData<ArrayList<Source>>(ArrayList(0))
    val categories = MutableLiveData<ArrayList<Category>>(ArrayList(0))

    /**
     * Fetch all data we need from database.
     */
    init {
        GlobalScope.launch {
            withContext(Dispatchers.IO) {
                // TODO: change debug code to: fetch data from DB
                delay(500)
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
                withContext(Dispatchers.Main) {
                    sources.value = srcList
                    categories.value = cateList
                }
            }
        }
    }

    fun getSources(): ArrayList<Source> {
        return sources.value!!
    }

    fun getCategories(): ArrayList<Category> {
        return categories.value!!
    }
}