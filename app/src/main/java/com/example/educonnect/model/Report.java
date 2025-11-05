package com.example.educonnect.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class Report implements Serializable {

    @SerializedName("reportId")
    private String reportId;

    @SerializedName("title")
    private String title;

    @SerializedName("classId")
    private String classId;

    @SerializedName("className")
    private String className;

    @SerializedName("teacherId")
    private String teacherId;

    @SerializedName("teacherName")
    private String teacherName;

    @SerializedName("termId")
    private String termId;

    @SerializedName("description")
    private String description;


    // Getters
    public String getReportId() { return reportId; }
    public String getTitle() { return title; }
    public String getClassId() { return classId; }
    public String getClassName() { return className; }
    public String getTeacherId() { return teacherId; }
    public String getTeacherName() { return teacherName; }
    public String getTermId() { return termId; }
    public String getDescription() { return description; }
    private String createdAt;
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public String getCreatedAt() { return createdAt; }
}