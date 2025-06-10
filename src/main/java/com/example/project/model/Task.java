package com.example.project.model;

/**
 * Kelas Task adalah sebuah POJO (Plain Old Java Object) yang merepresentasikan
 * satu entitas tugas. Kelas ini bertindak sebagai model data, menyimpan semua
 * atribut yang terkait dengan sebuah tugas, seperti nama, deskripsi, prioritas, dll.
 */
public class Task {
    // Properti-properti (fields) dari sebuah tugas
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
    private Integer parentId; // ID dari tugas induk, null jika ini adalah tugas utama
    private String attachmentStoredName; // Nama file lampiran yang disimpan di sistem (unik)
    private String attachmentOriginalName; // Nama asli file lampiran untuk ditampilkan ke user
    private String lastRemindedDate; // Tanggal terakhir notifikasi pengingat dikirim

    /**
     * Constructor untuk membuat objek Task baru dengan semua atributnya.
     * Dipakai saat mengambil data dari database atau membuat tugas baru.
     */
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

    // --- Kumpulan Getter dan Setter ---
    // Getter digunakan untuk mengambil nilai dari sebuah properti.
    // Setter digunakan untuk mengubah nilai dari sebuah properti.

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

    /**
     * Setter untuk progress. Method ini juga mengandung logika bisnis:
     * Jika progress diatur menjadi 100, status `completed` otomatis menjadi true.
     * Jika progress kurang dari 100, status `completed` otomatis menjadi false.
     * @param progress Nilai progress baru (0-100).
     */
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

    /**
     * Setter untuk status `completed`. Method ini juga mengandung logika bisnis:
     * Jika `completed` diatur menjadi true, nilai `progress` otomatis menjadi 100.
     * @param completed Status selesai yang baru.
     */
    public void setCompleted(boolean completed) {
        this.completed = completed;
        if (this.completed) {
            this.progress = 100;
        }
    }

    /**
     * Method helper yang digunakan oleh UI (misalnya di TreeTableView)
     * untuk menampilkan status tugas dalam format String yang mudah dibaca.
     * @return "Selesai" atau "Belum Selesai".
     */
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
