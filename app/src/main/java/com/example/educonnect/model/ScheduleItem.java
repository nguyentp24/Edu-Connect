package com.example.educonnect.model;

public class ScheduleItem {
    private String start, end;
    private String startIso, endIso;
    private String subject;
    private String classId;
    private String className; // Tên lớp từ API
    private String status;
    private boolean attended;
    private String courseId;

    public ScheduleItem(String start, String end, String startIso, String endIso, String subject, String classId, String status, boolean attended, String courseId) {
        this.start = start;
        this.end = end;
        this.startIso = startIso;
        this.endIso = endIso;
        this.subject = subject;
        this.classId = classId;
        this.className = null; // Sẽ được set từ API
        this.status = status;
        this.attended = attended;
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

    public String getClassId() {
        return classId;
    }

    public void setClassId(String classId) {
        this.classId = classId;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
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
