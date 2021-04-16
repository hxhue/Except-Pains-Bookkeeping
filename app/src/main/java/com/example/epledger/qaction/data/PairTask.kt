package com.example.epledger.qaction.data

import android.util.SparseArray
import java.util.*

object PairTask {
    private val eventMap = SparseArray<Noticeable>(16)
    private val randomGenerator = Random()

    // 注册并返回一个id
    fun observe(identity: Noticeable?): Int {
        var eid: Int = -1
        synchronized(eventMap) {
            while (eid < 0 || eventMap.get(eid) != null) {
                eid = randomGenerator.nextInt()
            }
            eventMap.put(eid, identity)
        }
        return eid
    }

    // 完成本次任务委托
    fun finish(eid: Int, extra: Any?) {
        var target: Noticeable?
        synchronized(eventMap) {
            target = eventMap[eid]
            eventMap.remove(eid)
        }
        if (target != null) {
            target!!.onReceiveTaskResult(eid, extra)
        }
    }

    interface Noticeable {
        fun onReceiveTaskResult(eid: Int, extra: Any?)
    }
}