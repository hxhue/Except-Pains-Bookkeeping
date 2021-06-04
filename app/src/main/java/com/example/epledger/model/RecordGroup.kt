package com.example.epledger.model

import java.lang.RuntimeException
import java.util.*
import kotlin.Comparator

class RecordGroup(var date: Date, var records: MutableList<Record>) {
    companion object  {
        val dateReverseComparator = Comparator<RecordGroup> { o1, o2 ->
            if (o1 == null || o2 == null) {
                throw RuntimeException("Can't compare null records.")
            }
            if (o1.date.after(o2.date)) -1 else if (o1.date.before(o2.date)) 1 else 0
        }
    }
}