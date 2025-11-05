package com.example.educonnect.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class Term implements Serializable {

    @SerializedName("termId")
    private String termId;

    @SerializedName("mode")
    private String mode;

    @SerializedName("startTime")
    private String startTime;

    @SerializedName("endTime")
    private String endTime;

    @SerializedName("createdAt")
    private String createdAt;

    // Getters
    public String getTermId() {
        return termId;
    }

    public String getMode() {
        return mode;
    }

    public String getStartTime() {
        return startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public String getCreatedAt() {
        return createdAt;
    }
}