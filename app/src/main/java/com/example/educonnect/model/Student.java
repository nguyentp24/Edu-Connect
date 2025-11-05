package com.example.educonnect.model;

import com.google.gson.annotations.SerializedName;

public class Student {
    private String name;
    private String studentId; // ID từ API classroom
    private Status status;
    private String note;
    private String homework;
    private String focus;

    public enum Status { PRESENT, LATE, ABSENT }

    public Student(String name, Status st) {
        this.name = name;
        this.studentId = "";
        this.status = st;
        this.note = "";
        this.homework = "";
        this.focus = "";
    }

    public Student(String name, String studentId, Status st) {
        this.name = name;
        this.studentId = studentId != null ? studentId : "";
        this.status = st;
        this.note = "";
        this.homework = "";
        this.focus = "";
    }

    public Student(String name, String studentId, Status st, String note, String homework, String focus) {
        this.name = name;
        this.studentId = studentId != null ? studentId : "";
        this.status = st;
        this.note = note != null ? note : "";
        this.homework = homework != null ? homework : "";
        this.focus = focus != null ? focus : "";
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
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
