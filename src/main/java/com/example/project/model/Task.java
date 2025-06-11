package com.example.project.model;

public class Task {
    private int id;
    private String name;
    private String description;
    private String course;
    private String date;
    private String time;
    private String priority;
    private int progress;
    private boolean completed;
    private int reminderOffsetDays;
    private Integer parentId;
    private String attachmentStoredName;
    private String attachmentOriginalName;
    private String lastRemindedDate;

    public Task(int id, String name, String description, String course, String date,
                String time, String priority, int progress, boolean completed,
                int reminderOffsetDays, Integer parentId,
                String attachmentStoredName, String attachmentOriginalName,
                String lastRemindedDate) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.course = course;
        this.date = date;
        this.time = time;
        this.priority = priority;
        this.progress = progress;
        this.completed = completed;
        this.reminderOffsetDays = reminderOffsetDays;
        this.parentId = parentId;
        this.attachmentStoredName = attachmentStoredName;
        this.attachmentOriginalName = attachmentOriginalName;
        this.lastRemindedDate = lastRemindedDate;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCourse() {
        return course;
    }

    public void setCourse(String course) {
        this.course = course;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
        if (this.progress == 100) {
            this.completed = true;
        } else if (this.progress < 100 && this.completed) {
            this.completed = false;
        }
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
        if (this.completed) {
            this.progress = 100;
        }
    }

    public String getStatusDisplay() {
        return completed ? "Selesai" : "Belum Selesai";
    }

    public int getReminderOffsetDays() {
        return reminderOffsetDays;
    }

    public void setReminderOffsetDays(int reminderOffsetDays) {
        this.reminderOffsetDays = reminderOffsetDays;
    }

    public Integer getParentId() {
        return parentId;
    }

    public void setParentId(Integer parentId) {
        this.parentId = parentId;
    }

    public String getAttachmentStoredName() {
        return attachmentStoredName;
    }

    public void setAttachmentStoredName(String attachmentStoredName) {
        this.attachmentStoredName = attachmentStoredName;
    }

    public String getAttachmentOriginalName() {
        return attachmentOriginalName;
    }

    public void setAttachmentOriginalName(String attachmentOriginalName) {
        this.attachmentOriginalName = attachmentOriginalName;
    }

    public String getLastRemindedDate() {
        return lastRemindedDate;
    }

    public void setLastRemindedDate(String lastRemindedDate) {
        this.lastRemindedDate = lastRemindedDate;
    }
}