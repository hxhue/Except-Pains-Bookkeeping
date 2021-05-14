package com.example.epledger.inbox.event.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.epledger.inbox.event.item.EventItem
import java.lang.RuntimeException

class EventViewModel: ViewModel() {
    val events: MutableLiveData<ArrayList<EventItem>> = {
        val dt = java.util.Date()
        val list = ArrayList<EventItem>(32)
        for (i in 1..2) {
            list.add(EventItem("House", dt, 3, EventItem.CycleUnit.DAY))
            list.add(EventItem("Insurance", dt, 1, EventItem.CycleUnit.MONTH))
        }
        MutableLiveData(list)
    }()

    fun refreshEvents() {
        events.value = events.value
    }

    // 和item页面相关的属性，在进入前需要设定
    // 页面正在被编辑
    val editing: MutableLiveData<Boolean> = MutableLiveData(false)
    // 正在创建一个新的事件
    val newEvent: MutableLiveData<Boolean> = MutableLiveData(false)
    // 正在编辑的事件
    val currentEvent: MutableLiveData<EventItem?> = MutableLiveData(null)
    // 正在编辑的属性在列表中的下标，负数表示是一个新的事件
    var eventIndex = -1

    fun setEditing(value: Boolean) {
        editing.value = value
    }

    fun setNewEvent(value: Boolean) {
        newEvent.value = value
    }

    // TODO: remove
    fun setCurrentEvent(e: EventItem) {
        currentEvent.value = e
    }

    fun isEditing(): Boolean {
        return editing.value == true
    }

    fun isNewEvent(): Boolean {
        return newEvent.value == true
    }

    fun getCurrentEvent(): EventItem {
        return currentEvent.value
                ?: throw RuntimeException("Current event is not set. " +
                        "Make sure you set it before entering EventItemFragment.")
    }

    // 图标
}