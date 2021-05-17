package com.example.epledger.home.model;

import java.util.Date;
import java.util.List;

public class Section {
    private Date date;
    private List<Entry> entryList;
    // Section name
    // Section icon res id

    public Section(Date date, List<Entry> entryList) {
        this.date = date;
        this.entryList = entryList;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public List<Entry> getEntryList() {
        return entryList;
    }

    public void setEntryList(List<Entry> entryList) {
        this.entryList = entryList;
    }

    public enum SectionType {
        STARRED, INCOMPLETE, SCREENSHOTS, EVENTS
    }
}
