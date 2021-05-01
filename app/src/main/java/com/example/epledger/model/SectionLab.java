package com.example.epledger.model;

import android.content.Context;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class SectionLab {
    public static SectionLab sSectionLab;
    private List<Section> mSections;

    private SectionLab(Context context) {
        mSections = new ArrayList<>();
        SimpleDateFormat ft = new SimpleDateFormat("yyyy-MM-dd");
        try {
            for (int i = 0; i < 3; i++) {
                String date = "2021-04-" + 5 + i;
                List<Entry> entries = new ArrayList<>();
                for (int j = 0; j < 5; j++) {
                    entries.add(new Entry(i * 5 + j, i * 4.3 + j * 3.1, "food", "eat", "alipay", ft.parse(date)));
                }
                mSections.add(new Section(ft.parse(date), entries));
            }
        } catch (ParseException e) {
            System.out.println("Unparseable using " + ft);
        }
    }

    public static SectionLab get(Context context) {
        if (sSectionLab == null) {
            sSectionLab = new SectionLab(context);
        }
        return sSectionLab;
    }

    public Entry getEntry(int id) {
        for (Section section: mSections) {
            for (Entry entry: section.getEntryList()) {
                if (entry.getId() == id) {
                    return entry;
                }
            }
        }
        return null;
    }

    public List<Section> getSections() {
        return mSections;
    }
}
