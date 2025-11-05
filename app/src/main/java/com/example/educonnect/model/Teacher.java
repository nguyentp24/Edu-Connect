package com.example.educonnect.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class Teacher implements Serializable {

    @SerializedName("teacherId")
    private String teacherId;

    @SerializedName("userId")
    private String userId;

    @SerializedName("fullName")
    private String fullName;

    @SerializedName("firstName")
    private String firstName;

    @SerializedName("lastName")
    private String lastName;

    // Getters
    public String getTeacherId() {
        return teacherId;
    }

    public String getUserId() {
        return userId;
    }

    public String getFullName() {
        return fullName;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }
}