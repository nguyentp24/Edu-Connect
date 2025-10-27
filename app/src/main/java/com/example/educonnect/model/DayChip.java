// com/example/educonnect/model/DayChip.java
package com.example.educonnect.model;

import java.util.Calendar;

public class DayChip {
    public String dayNumber;   // "21"
    public String dayLabel;    // "Ba"
    public boolean selected;
    public Calendar date;      // ngày thật

    public DayChip(String number, String label, boolean selected, Calendar date) {
        this.dayNumber = number;
        this.dayLabel = label;
        this.selected = selected;
        this.date = date;
    }
}
