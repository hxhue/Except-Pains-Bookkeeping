package com.example.epledger.model.entry;

import android.content.Context;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class SectionLab {
    private final List<Section> mSections = new ArrayList<>();

    public SectionLab(List<List<Entry>> groupedEntries) {
        setSections(groupedEntries);
    }

    // 您觉得效率高吗？ :-)
    public Entry getEntry(int id) {
        for (Section section: mSections) {
            for (Entry entry: section.getEntryList()) {
                if (entry.getEntryId() == id) {
                    return entry;
                }
            }
        }
        return null;
    }

    public List<Section> getSections() {
        return mSections;
    }

    public void setSections(List<List<Entry>> groupedEntries) {
        mSections.clear();
        for (List<Entry> list: groupedEntries) {
            if (!list.isEmpty()) {
                mSections.add(new Section(list.get(0).getDate(), list));
            }
        }
    }
}
