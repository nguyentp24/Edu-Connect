package com.example.educonnect.model;

public class ScheduleItem {
    public String start, end;
    public String subject;
    public String classroom;
    public String status;
    public boolean attended;

    public ScheduleItem(String start, String end, String subject, String classroom, String status, boolean attended) {
        this.start = start; this.end = end;
        this.subject = subject; this.classroom = classroom;
        this.status = status; this.attended = attended;
    }
}
