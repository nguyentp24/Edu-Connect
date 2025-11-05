package com.example.educonnect.model;

public class ScheduleItem {
    private String start, end;          // hiển thị
    private String startIso, endIso;    // ISO để tính toán
    private String subject;
    private String klass;       // "12A1"
    private String status;      // "Đã kết thúc"...
    private boolean attended;   // true = đã điểm danh
    private String courseId;   // courseId để gọi API attendance

    public ScheduleItem(String start, String end, String startIso, String endIso, String subject, String klass, String status, boolean attended, String courseId) {
        this.start = start; this.end = end;
        this.startIso = startIso; this.endIso = endIso;
        this.subject = subject; this.klass = klass;
        this.status = status; this.attended = attended;
        this.courseId = courseId;
    }

    public String getStart() {
        return start;
    }

    public void setStart(String start) {
        this.start = start;
    }

    public String getEnd() {
        return end;
    }

    public void setEnd(String end) {
        this.end = end;
    }

    public String getStartIso() {
        return startIso;
    }

    public void setStartIso(String startIso) {
        this.startIso = startIso;
    }

    public String getEndIso() {
        return endIso;
    }

    public void setEndIso(String endIso) {
        this.endIso = endIso;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getKlass() {
        return klass;
    }

    public void setKlass(String klass) {
        this.klass = klass;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isAttended() {
        return attended;
    }

    public void setAttended(boolean attended) {
        this.attended = attended;
    }

    public String getCourseId() {
        return courseId;
    }

    public void setCourseId(String courseId) {
        this.courseId = courseId;
    }
}
