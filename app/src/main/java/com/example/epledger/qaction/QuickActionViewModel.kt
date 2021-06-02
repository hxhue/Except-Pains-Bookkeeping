package com.example.epledger.qaction

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.epledger.R
import com.example.epledger.db.AppDatabase
import com.example.epledger.model.Category
import com.example.epledger.model.Source
import kotlinx.coroutines.*

class QuickActionViewModel: ViewModel() {
    // To prevent null-pointer exception, we have to initialize them
    val sources = MutableLiveData<MutableList<Source>>(ArrayList(0))
    val categories = MutableLiveData<MutableList<Category>>(ArrayList(0))

    private val sourceNameMap = HashMap<String, Source>()
    private val categoryNameMap = HashMap<String, Category>()

    /**
     * Fetch all data we need from database.
     */
    init {
        GlobalScope.launch(Dispatchers.IO) {
            val sourceList = AppDatabase.getAllSources()
            val categoryList = AppDatabase.getAllCategories()

            sources.postValue(sourceList)
            categories.postValue(categoryList)

            // Construct maps
            sourceList.forEach {
                sourceNameMap[it.name] = it
            }
            categoryList.forEach {
                categoryNameMap[it.name] = it
            }
        }
    }

    fun getSources(): MutableList<Source> {
        return sources.value!!
    }

    fun getCategories(): MutableList<Category> {
        return categories.value!!
    }

    fun requireSource(name: String): Source {
        return sourceNameMap[name]!!
    }

    fun requireCategories(name: String): Category {
        return categoryNameMap[name]!!
    }
}