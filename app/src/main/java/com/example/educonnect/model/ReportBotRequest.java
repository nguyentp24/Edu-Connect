package com.example.educonnect.model;

public class ReportBotRequest {
    String termId;
    String classId;

    public ReportBotRequest(String termId, String classId) {
        this.termId = termId;
        this.classId = classId;
    }
}