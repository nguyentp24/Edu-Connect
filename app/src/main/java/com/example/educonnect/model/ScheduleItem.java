package com.example.educonnect.model;

public class ScheduleItem {
    public String start, end;
    public String subject;
    public String klass;       // "12A1"
    public String status;      // "Đã kết thúc"...
    public boolean attended;   // true = đã điểm danh

    public ScheduleItem(String start, String end, String subject, String klass, String status, boolean attended) {
        this.start = start; this.end = end;
        this.subject = subject; this.klass = klass;
        this.status = status; this.attended = attended;
    }
}
