package com.example.educonnect.model;

public class ScheduleItem {
    public String start, end;          // hiển thị
    public String startIso, endIso;    // ISO để tính toán
    public String subject;
    public String klass;       // "12A1"
    public String status;      // "Đã kết thúc"...
    public boolean attended;   // true = đã điểm danh
    public String courseId;   // courseId để gọi API attendance

    public ScheduleItem(String start, String end, String startIso, String endIso, String subject, String klass, String status, boolean attended, String courseId) {
        this.start = start; this.end = end;
        this.startIso = startIso; this.endIso = endIso;
        this.subject = subject; this.klass = klass;
        this.status = status; this.attended = attended;
        this.courseId = courseId;
    }
}
