package com.example.epledger.model;

import java.util.Date;
import java.util.List;

public class Section {
    private Date date;
    private List<Record> entryList;
    // Section name
    // Section icon res id

    public Section(Date date, List<Record> entryList) {
        this.date = date;
        this.entryList = entryList;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public List<Record> getEntryList() {
        return entryList;
    }

    public void setEntryList(List<Record> entryList) {
        this.entryList = entryList;
    }

    public enum SectionType {
        STARRED, INCOMPLETE, SCREENSHOTS, EVENTS
    }
}
