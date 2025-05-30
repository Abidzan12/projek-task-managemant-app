
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

    public Task(int id, String name, String description, String course, String date,
                String time, String priority, int progress, boolean completed) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.course = course;
        this.date = date;
        this.time = time;
        this.priority = priority;
        this.progress = progress;
        this.completed = completed;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getCourse() {
        return course;
    }

    public String getDate() {
        return date;
    }

    public String getTime() {
        return time;
    }

    public String getPriority() {
        return priority;
    }

    public int getProgress() {
        return progress;
    }

    public boolean isCompleted() {
        return completed;
    }

    public String getStatus() {
        return completed ? "Selesai" : "Belum Selesai";
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }
}
