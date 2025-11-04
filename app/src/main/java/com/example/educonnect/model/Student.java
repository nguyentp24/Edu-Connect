package com.example.educonnect.model;

import com.google.gson.annotations.SerializedName;

public class Student {
    public String name;
    public Status status;

    @SerializedName("studentId")
    public String studentId;

    public enum Status { PRESENT, LATE, ABSENT }

    public Student(String name, Status st) { this.name = name; this.status = st; }

    // Constructor mới, dùng khi lấy dữ liệu có studentId từ API
    public Student(String studentId, String name) {
        this.studentId = studentId;
        this.name = name;
        this.status = Status.PRESENT; // Gán một trạng thái mặc định
    }
}
