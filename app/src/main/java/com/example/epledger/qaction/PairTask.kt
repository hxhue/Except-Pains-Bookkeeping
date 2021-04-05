package com.example.epledger.qaction

import android.util.Log
import java.util.*

object PairTask {
    private val eventMap: MutableMap<Long, Noticeable?> = HashMap()
    private val randomGenerator = Random()

    // 注册并返回一个id
    fun observe(identity: Noticeable?): Long {
        Log.d("Utils.PairTask.observe", "entered.")
        var eid: Long = -1
        synchronized(eventMap) {
            while (eid < 0 || eventMap.containsKey(eid)) {
                eid = randomGenerator.nextLong()
            }
            eventMap.put(eid, identity)
        }
        Log.d("Utils.PairTask.observe", "before returning ($eid)")
        return eid
    }

    // 完成本次任务委托
    fun finish(eid: Long, extra: Any?) {
        Log.d("Utils.PairTask.finished", "entered. eid is $eid")
        var target: Noticeable?
        synchronized(eventMap) {
            target = eventMap[eid]
            eventMap.remove(eid)
        }
        if (target != null) {
            Log.d("Utils.PairTask.finished", "calling target.onReceive($eid, extra).")
            target!!.onReceiveTaskResult(eid, extra)
        }
    }

    interface Noticeable {
        fun onReceiveTaskResult(eid: Long, extra: Any?)
    }
}