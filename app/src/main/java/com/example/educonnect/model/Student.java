package com.example.educonnect.model;

public class Student {
    public String name;
    public String studentId; // ID từ API classroom
    public Status status;

    public String note;
    public String homework;
    public String focus;

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
}
