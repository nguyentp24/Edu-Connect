package com.example.educonnect.model;

public class AttendanceItem {
    private String atID;
    private String studentId;
    private String courseId;
    private String participation;  // "Có mặt", "Vắng mặt", "Đi trễ"
    private String note;
    private String homework;
    private String focus;

    public String getAtID() {
        return atID;
    }

    public void setAtID(String atID) {
        this.atID = atID;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getCourseId() {
        return courseId;
    }

    public void setCourseId(String courseId) {
        this.courseId = courseId;
    }

    public String getParticipation() {
        return participation;
    }

    public void setParticipation(String participation) {
        this.participation = participation;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getHomework() {
        return homework;
    }

    public void setHomework(String homework) {
        this.homework = homework;
    }

    public String getFocus() {
        return focus;
    }

    public void setFocus(String focus) {
        this.focus = focus;
    }
}
