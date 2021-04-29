package com.example.epledger.model;

import android.content.Context;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class EntryLab {
    private static EntryLab sEntryLab;
    private List<Entry> mEntrys;

    private EntryLab(Context context) {
        SimpleDateFormat ft = new SimpleDateFormat("yyyy-MM-dd");
        mEntrys = new ArrayList<>();
        try {
            for (int i = 0; i < 5; i++) {
                mEntrys.add(new Entry(i, i * 4.3, "food", "eat", "支付宝", ft.parse("2021-04-29")));
            }
            for (int i = 0; i < 5; i++) {
                mEntrys.add(new Entry(i + 5, i * -2.8, "food", "eat", "alipay", ft.parse("2020-03-19")));
            }
        } catch (ParseException e) {
            System.out.println("Unparseable using " + ft);
        }
    }

    public static EntryLab get(Context context) {
        if (sEntryLab == null) {
            sEntryLab = new EntryLab(context);
        }
        return sEntryLab;
    }

    public Entry getEntry(int id) {
        for (Entry entry : mEntrys) {
            if (entry.getId() == id) {
                return entry;
            }
        }
        return null;
    }

    public List<Entry> getEntries() {
        return mEntrys;
    }
}
