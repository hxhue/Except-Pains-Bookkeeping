package com.example.epledger.inbox.event.item

import com.example.epledger.model.Record
import java.lang.RuntimeException


// TODO: add ID field to EventItem
class EventItem(var name: String,
                var startingDate: java.util.Date,
                var cycle: Int,
                var unit: CycleUnit,
                var template: Record? = null,
                var iconResID: Int? = null) {

    init {
        if (cycle <= 0) {
            throw RuntimeException("amount of Cycle can not be <= 0!")
        }
    }

    enum class CycleUnit {
        DAY, MONTH, YEAR;
    }

    fun copy(): EventItem {
        return EventItem(name, startingDate, cycle, unit, template, iconResID)
    }

    fun copyFrom(other: EventItem) {
        name = other.name
        startingDate = other.startingDate
        cycle = other.cycle
        unit = other.unit
        template = other.template
        iconResID = other.iconResID
    }
}