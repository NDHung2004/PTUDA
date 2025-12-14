package com.example.sotaysv_nhom2.Models;

public class Note {
    private int id;
    private String code; // Mã Note tự sinh (UUID)
    private String title;
    private String content;
    private String alarmTime;
    private String dateTime;
    private int repeatType;
    private String subjectCode; // Mã của môn học liên kết

    private boolean isSelected = false;

    public Note(int id, String code, String title, String content, String alarmTime, String dateTime, int repeatType, String subjectCode) {
        this.id = id;
        this.code = code;
        this.title = title;
        this.content = content;
        this.alarmTime = alarmTime;
        this.dateTime = dateTime;
        this.repeatType = repeatType;
        this.subjectCode = subjectCode;
    }

    public int getId() { return id; }
    public String getCode() { return code; }
    public String getTitle() { return title; }
    public String getContent() { return content; }
    public String getAlarmTime() { return alarmTime; }
    public String getDateTime() { return dateTime; }
    public int getRepeatType() { return repeatType; }
    public String getSubjectCode() { return subjectCode; }

    public boolean isDaily() { return repeatType == 1; }
    public boolean isWeekly() { return repeatType == 2; }
}