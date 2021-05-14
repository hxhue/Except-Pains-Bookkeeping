package com.example.epledger.inbox.event.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.epledger.inbox.event.item.EventItem

class EventItemViewModel: ViewModel() {
    val item = MutableLiveData<EventItem?>(null)

    fun getCurrentEvent(): EventItem {
        val v = item.value
        return v!!
    }
}