package com.example.epledger.qaction

import android.util.Log
import android.util.SparseArray
import java.util.*

object PairTask {
//    private val eventMap: MutableMap<Long, Noticeable?> = TreeMap()
    private val eventMap = SparseArray<Noticeable>(4)
    private val randomGenerator = Random()

    // 注册并返回一个id
    fun observe(identity: Noticeable?): Int {
//        Log.d("Utils.PairTask.observe", "entered.")
        var eid: Int = -1
        synchronized(eventMap) {
            while (eid < 0 || eventMap.get(eid) != null) {
                eid = randomGenerator.nextInt()
            }
            eventMap.put(eid, identity)
        }
//        Log.d("Utils.PairTask.observe", "before returning ($eid)")
        return eid
    }

    // 完成本次任务委托
    fun finish(eid: Int, extra: Any?) {
//        Log.d("Utils.PairTask.finished", "entered. eid is $eid")
        var target: Noticeable?
        synchronized(eventMap) {
            target = eventMap[eid]
            eventMap.remove(eid)
        }
        if (target != null) {
//            Log.d("Utils.PairTask.finished", "calling target.onReceive($eid, extra).")
            target!!.onReceiveTaskResult(eid, extra)
        }
    }

    interface Noticeable {
        fun onReceiveTaskResult(eid: Int, extra: Any?)
    }
}