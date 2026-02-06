package com.example.academiccompanion.ui.view_notice;

public class Notice {
    public String noticeId;
    public String subject;
    public String noticeType;
    public String deadline;
    public String text;
    public String issueDateTime;
    public String department;
    public String year;
    public String deadlineTimestamp;  // ✅ New field

    // Required empty constructor for Firebase
    public Notice() {}

    public Notice(String noticeId, String subject, String noticeType,
                  String deadline, String text, String issueDateTime,
                  String department, String year, String deadlineTimestamp) {
        this.noticeId = noticeId;
        this.subject = subject;
        this.noticeType = noticeType;
        this.deadline = deadline;
        this.text = text;
        this.issueDateTime = issueDateTime;
        this.department = department;
        this.year = year;
        this.deadlineTimestamp = deadlineTimestamp;
    }
}
