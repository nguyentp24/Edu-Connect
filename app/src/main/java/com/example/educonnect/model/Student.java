package com.example.educonnect.model;

public class Student {
    public String name;
    public Status status;

    public enum Status { PRESENT, LATE, ABSENT }

    public Student(String name, Status st) { this.name = name; this.status = st; }
}
