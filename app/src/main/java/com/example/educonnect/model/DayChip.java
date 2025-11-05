// com/example/educonnect/model/DayChip.java
package com.example.educonnect.model;

import java.util.Calendar;

public class DayChip {
    private String dayNumber;   // "21"
    private String dayLabel;    // "Ba"
    private boolean selected;
    private Calendar date;      // ngày thật

    public DayChip(String number, String label, boolean selected, Calendar date) {
        this.dayNumber = number;
        this.dayLabel = label;
        this.selected = selected;
        this.date = date;
    }

    public String getDayNumber() {
        return dayNumber;
    }

    public void setDayNumber(String dayNumber) {
        this.dayNumber = dayNumber;
    }

    public String getDayLabel() {
        return dayLabel;
    }

    public void setDayLabel(String dayLabel) {
        this.dayLabel = dayLabel;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public Calendar getDate() {
        return date;
    }

    public void setDate(Calendar date) {
        this.date = date;
    }
}
