package com.example.epledger.db.model

import com.example.epledger.detail.DetailRecord
import java.text.SimpleDateFormat
import java.util.*
import kotlin.Comparator
import kotlin.collections.ArrayList

interface LedgerDatabase {
    data class RecordGroup(val date: Date, val records: List<DetailRecord>)

    /**
     * 从数据库中获取按照日期排序的记录。时间越靠近现在，排序后的位置越靠前。
     */
    fun getRecordsOrderByDate(): List<DetailRecord>

    companion object {
        /**
         * 接受records，返回按照时间排序好的多个RecordGroup。
         * @param recordsOrderByDate 必须是已经按照date排序好的records
         */
        fun groupRecordsByDate(recordsOrderByDate: List<DetailRecord>): List<RecordGroup> {
            val groupedLists = ArrayList<RecordGroup>()
            var group = ArrayList<DetailRecord>()

            recordsOrderByDate.forEach {
                if (!recordIsComplete(it)) {
                    // Do nothing if it's incomplete
                } else if (group.isEmpty()) {
                    group.add(it.getCopy())
                } else if (onSameDay(group.first().mDate!!, it.mDate!!)) {
                    group.add(it.getCopy())
                } else {
                    // Not on the same day
                    groupedLists.add(RecordGroup(group.first().mDate!!, group))
                    group = ArrayList()
                    group.add(it.getCopy())
                }
            }

            // After iteration, we check for the leftover
            if (group.isNotEmpty()) {
                groupedLists.add(RecordGroup(group.first().mDate!!, group))
            }

            return groupedLists
        }

        /**
         * 判断一个记录是否不完整。
         * When ID is null, it's also considered incomplete.
         */
        fun recordIsComplete(record: DetailRecord): Boolean {
            return record.ID != null &&
                    record.amount != null &&
                    record.hourOfDay != null &&
                    record.minuteOfHour != null &&
                    record.mDate != null &&
                    record.category != null
        }

        /**
         * 判断两个日期是否在同一天。
         */
        fun onSameDay(date1: Date, date2: Date): Boolean {
            val cal1 = Calendar.getInstance()
            val cal2 = Calendar.getInstance()
            cal1.time = date1;
            cal2.time = date2;
            return cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR) &&
                    cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR);
        }
    }
}

/**
 * 应用使用的数据库。
 */
val AppDatabase: LedgerDatabase = MemoryDatabase()

/**
 * 内存中数据库的模拟。有相同的接口。
 */
class MemoryDatabase : LedgerDatabase {

    val records: List<DetailRecord> = run {
        val simpleFormat = SimpleDateFormat("yyyy/MM/dd", Locale.US)
        val rec1 = DetailRecord().apply {
            ID = 11
            amount = -199.0
            category = "Sports"
            source = "Alipay"
            mDate = simpleFormat.parse("2020/12/31")
            hourOfDay = 12
            minuteOfHour = 13
            note = "买了一个新球拍。"
        }
        val rec2 = DetailRecord().apply {
            ID = 13
            amount = -29.9
            category = "Study"
            mDate = simpleFormat.parse("2021/01/01")
            hourOfDay = 18
            minuteOfHour = 37
            note = "这是黄冈密卷，妈妈说这是她对我的爱。"
        }
        val rec3 = rec1.getCopy().apply {
            ID = 12
            amount = -199.0
            source = "Wechat"
            mDate = simpleFormat.parse("2020/12/31")
            hourOfDay = 9
            starred = true
            note = "我是有钱人，我又买了一个新球拍。但这次是用微信支付。"
        }
        arrayListOf(rec1, rec2, rec3)
    }

//    override fun getRecordsGroupedByDate(): List<LedgerDatabase.RecordGroup> {
//        val groupedLists = ArrayList<LedgerDatabase.RecordGroup>()
//
//        var group = ArrayList<DetailRecord>()
//        records.forEach {
//            if (!LedgerDatabase.recordIsComplete(it)) {
//                // Do nothing if it's incomplete
//            } else if (group.isEmpty()) {
//                group.add(it.getCopy())
//            } else if (LedgerDatabase.onSameDay(group.first().mDate!!, it.mDate!!)) {
//                group.add(it.getCopy())
//            } else {
//                // Not on the same day
//                groupedLists.add(
//                    LedgerDatabase.RecordGroup(group.first().mDate!!, group)
//                )
//                group = ArrayList()
//                group.add(it.getCopy())
//            }
//        }
//
//        // After iteration, we check for the leftover
//        if (!group.isEmpty()) {
//            groupedLists.add(
//                LedgerDatabase.RecordGroup(group.first().mDate!!, group)
//            )
//        }
//
//        return groupedLists
//    }

    override fun getRecordsOrderByDate(): List<DetailRecord> {
        return records.sortedWith(object : Comparator<DetailRecord> {
            override fun compare(o1: DetailRecord, o2: DetailRecord): Int {
                if (o1.date!!.equals(o2.date!!)) {
                    return 0;
                } else if (o1.date!!.after(o2.date!!)) {
                    return -1;
                } else {
                    return 1;
                }
            }
        })
    }
}