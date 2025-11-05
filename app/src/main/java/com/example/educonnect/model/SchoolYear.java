package com.example.educonnect.model;

import com.google.gson.annotations.SerializedName;

public class SchoolYear {
    @SerializedName("schoolYearId")
    private String schoolYearId;
    @SerializedName("startDate")
    private String startDate;
    @SerializedName("endDate")
    private String endDate;
    @SerializedName("status")
    private String status;

    private String label;
    private String value;

    public String getSchoolYearId() { return schoolYearId; }
    public String getStartDate() { return startDate; }
    public String getEndDate() { return endDate; }
    public String getStatus() { return status; }

    // Getters/Setters
    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }
    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }
}