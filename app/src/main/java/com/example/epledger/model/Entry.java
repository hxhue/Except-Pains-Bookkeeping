package com.example.epledger.model;

public class Entry {
    private int id;
    private double amount;
    private String label;
    private String info;
    private String source;

    public Entry(int id, double amount, String label, String info, String source) {
        this.id = id;
        this.amount = amount;
        this.label = label;
        this.info = info;
        this.source = source;
    }

    public Entry(double amount, String label, String info, String source) {
        this.amount = amount;
        this.label = label;
        this.info = info;
        this.source = source;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }
}
