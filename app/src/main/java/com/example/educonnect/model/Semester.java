package com.example.educonnect.model;

import com.google.gson.annotations.SerializedName;

public class Semester {
    @SerializedName("semesterName")
    private String semesterName;
    @SerializedName("status")
    private String status;
    @SerializedName("startDate")
    private String startDate;
    @SerializedName("endDate")
    private String endDate;
    private String displayName;

    public String getSemesterName() { return semesterName; }
    public String getStatus() { return status; }
    public String getStartDate() { return startDate; }
    public String getEndDate() { return endDate; }
    public String getDisplayName() { return displayName; }
    public void setDisplayName(String name) { this.displayName = name; }
}