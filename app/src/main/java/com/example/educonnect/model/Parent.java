package com.example.educonnect.model;

// Parent.java
import com.google.gson.annotations.SerializedName;

public class Parent {

    @SerializedName("parentId")
    private String parentId;

    @SerializedName("userId")
    private String userId;

    @SerializedName("fullName")
    private String fullName;

    @SerializedName("email")
    private String email;

    @SerializedName("phoneNumber")
    private String phoneNumber;


    // --- Getters ---
    public String getParentId() { return parentId; }
    public String getUserId() { return userId; }
    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
    public String getPhoneNumber() { return phoneNumber; }

}

