package com.example.educonnect.model;

public class TermRequest {
    private String startTime;
    private String endTime;
    private String mode;

    public TermRequest(String startTime, String endTime, String mode) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.mode = mode;
    }
}