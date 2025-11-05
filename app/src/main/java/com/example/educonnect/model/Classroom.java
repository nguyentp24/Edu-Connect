package com.example.educonnect.model;

import com.google.gson.annotations.SerializedName;

public class Classroom {

    @SerializedName("classId")
    private String classId;

    @SerializedName("className")
    private String className;

    @SerializedName("teacherId")
    private String teacherId;

    @SerializedName("schoolYearId")
    private String schoolYearId;

    @SerializedName("startDate")
    private String startDate;

    @SerializedName("endDate")
    private String endDate;

    // Getters
    public String getClassId() {
        return classId;
    }

    public String getClassName() {
        return className;
    }

    public String getTeacherId() {
        return teacherId;
    }

    public String getSchoolYearId() {
        return schoolYearId;
    }

    public String getStartDate() {
        return startDate;
    }

    public String getEndDate() {
        return endDate;
    }
}