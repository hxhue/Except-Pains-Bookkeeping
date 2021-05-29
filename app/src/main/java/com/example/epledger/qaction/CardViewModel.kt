package com.example.epledger.qaction

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.epledger.R
import com.example.epledger.db.AppDatabase
import com.example.epledger.model.Category
import com.example.epledger.model.Source
import kotlinx.coroutines.*

class CardViewModel: ViewModel() {
    // To prevent null-pointer exception, we have to initialize them
    val sources = MutableLiveData<MutableList<Source>>(ArrayList(0))
    val categories = MutableLiveData<MutableList<Category>>(ArrayList(0))

    /**
     * Fetch all data we need from database.
     */
    init {
        GlobalScope.launch(Dispatchers.IO) {
            sources.postValue(AppDatabase.getAllSources())
            categories.postValue(AppDatabase.getAllCategories())
        }
    }

    fun getSources(): MutableList<Source> {
        return sources.value!!
    }

    fun getCategories(): MutableList<Category> {
        return categories.value!!
    }
}