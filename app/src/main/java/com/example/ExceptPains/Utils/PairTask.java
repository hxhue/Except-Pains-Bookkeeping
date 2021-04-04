package com.example.ExceptPains.Utils;

import android.util.Log;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.WeakHashMap;

public class PairTask {
    private static Map<Long, Noticeable> eventMap = new HashMap<>();
    private static Random randomGenerator = new Random();

    public interface Noticeable {
        public void onReceiveTaskResult(long eid, Object extra);
    }

    // 注册并返回一个id
    public static long observe(Noticeable identity) {
        Log.d("Utils.PairTask.observe", "entered.");

        long eid = -1;
        synchronized (eventMap) {
            while (eid < 0 || eventMap.containsKey(eid)) {
                eid = randomGenerator.nextLong();
            }
            eventMap.put(eid, identity);
        }

        Log.d("Utils.PairTask.observe", "before returning (" + eid + ")");
        return eid;
    }

    // 完成本次任务委托
    public static void finish(long eid, Object extra) {
        Log.d("Utils.PairTask.finished", "entered. eid is " + eid);

        Noticeable target;
        synchronized (eventMap) {
            target = eventMap.get(eid);
            eventMap.remove(eid);
        }
        if (target != null) {
            Log.d("Utils.PairTask.finished", "calling target.onReceive(" + eid + ", extra).");
            target.onReceiveTaskResult(eid, extra);
        }
    }
}
