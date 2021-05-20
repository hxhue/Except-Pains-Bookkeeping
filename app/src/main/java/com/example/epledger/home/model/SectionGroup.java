package com.example.epledger.home.model;

import java.util.ArrayList;
import java.util.List;

//public class SectionGroup {
//    public List<Section> getSections() {
//        return mSections;
//    }
//
//    public void setSections(List<Section> sections) {
//        mSections = sections;
//    }
//
//    private List<Section> mSections = new ArrayList<>();
//
//    public SectionGroup(List<Section> groupedEntries) {
//        setSections(groupedEntries);
//    }
//
//    // 您觉得效率高吗？ :-)
//    public Entry getEntry(int id) {
//        for (Section section: mSections) {
//            for (Entry entry: section.getEntryList()) {
//                if (entry.getEntryId() == id) {
//                    return entry;
//                }
//            }
//        }
//        return null;
//    }
////
////    public void setSections(List<List<Entry>> groupedEntries) {
////        mSections.clear();
////        for (List<Entry> list: groupedEntries) {
////            if (!list.isEmpty()) {
////                mSections.add(new Section(list.get(0).getDate(), list));
////            }
////        }
////    }
//}
