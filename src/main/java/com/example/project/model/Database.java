package com.example.project.model;

import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Kelas Database berfungsi sebagai Data Access Object (DAO).
 * Semua interaksi dengan database SQLite, seperti query SELECT, INSERT, UPDATE, dan DELETE,
 * dikelola secara terpusat di dalam kelas ini.
 */
public class Database {
    // URL koneksi ke file database SQLite.
    private static final String DB_URL = "jdbc:sqlite:data/task.db";

    /**
     * Method untuk mendaftarkan user baru ke dalam database.
     * PERINGATAN KEAMANAN: Password disimpan sebagai plain text. Ini sangat tidak aman.
     * Seharusnya, password di-hash menggunakan algoritma seperti BCrypt sebelum disimpan.
     * @param username Nama user yang akan didaftarkan.
     * @param password Password user.
     * @return `true` jika registrasi berhasil, `false` jika gagal (misalnya, username sudah ada).
     */
    public static boolean registerUser(String username, String password) {
        String sql = "INSERT INTO users (username, password) VALUES (?, ?)";
        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.out.println("Register failed: " + e.getMessage());
            return false;
        }
    }

    /**
     * Method untuk memvalidasi kredensial login user.
     * PERINGATAN KEAMANAN: Method ini membandingkan password dalam bentuk plain text.
     * @param username Username yang dimasukkan.
     * @param password Password yang dimasukkan.
     * @return `Integer` berisi ID user jika login valid, `null` jika tidak valid.
     */
    public static Integer validateLogin(String username, String password) {
        String sql = "SELECT id FROM users WHERE username = ? AND password = ?";
        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("id");
            } else {
                return null;
            }
        } catch (SQLException e) {
            System.out.println("Login failed: " + e.getMessage());
            return null;
        }
    }

    /**
     * Method untuk mendapatkan nama user berdasarkan ID-nya.
     * @param userId ID dari user yang namanya ingin dicari.
     * @return `String` berisi username, atau `null` jika tidak ditemukan.
     */
    public static String getUserNameById(int userId) {
        String sql = "SELECT username FROM users WHERE id = ?";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getString("username");
            }
        } catch (SQLException e) {
            System.err.println("Gagal mengambil username untuk userId " + userId + ": " + e.getMessage());
        }
        return null;
    }

    /**
     * Method utilitas untuk membuat koneksi ke database SQLite.
     * Mengaktifkan dukungan foreign key untuk menjaga integritas data.
     * @return Objek `Connection` yang siap digunakan.
     */
    public static Connection connect() {
        Connection conn = null;
        try {
            Properties config = new Properties();
            config.setProperty("foreign_keys", "on");
            conn = DriverManager.getConnection(DB_URL, config);
        } catch (SQLException e) {
            throw new RuntimeException("Gagal terhubung ke database", e);
        }
        return conn;
    }

    /**
     * Method untuk mengambil semua tugas (tasks) milik seorang user.
     * @param userId ID user yang tugasnya akan diambil.
     * @return `List` dari objek `Task`.
     */
    public static List<Task> getAllTasks(int userId) {
        List<Task> tasks = new ArrayList<>();
        String sql = "SELECT * FROM tasks WHERE user_id = ?";
        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                tasks.add(extractTask(rs));
            }
        } catch (SQLException e) {
            System.err.println("Gagal mengambil semua tugas untuk userId " + userId + ": " + e.getMessage());
        }
        return tasks;
    }

    /**
     * Method untuk mengambil tugas berdasarkan status penyelesaiannya (selesai atau belum).
     * @param userId ID user pemilik tugas.
     * @param completed Status penyelesaian (`true` untuk selesai, `false` untuk belum).
     * @return `List` dari objek `Task` yang sudah difilter.
     */
    public static List<Task> getTasksByCompletion(int userId, boolean completed) {
        List<Task> tasks = new ArrayList<>();
        String sql = "SELECT * FROM tasks WHERE user_id = ? AND completed = ?";
        try (Connection conn = connect(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setBoolean(2, completed);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                tasks.add(extractTask(rs));
            }
        } catch (SQLException e) {
            System.err.println("Gagal mengambil tugas berdasarkan status untuk userId " + userId + ": " + e.getMessage());
        }
        return tasks;
    }

    /**
     * Method untuk memperbarui status 'completed' dan 'progress' dari sebuah tugas.
     * @param taskId ID tugas yang akan diupdate.
     * @param completed Status selesai yang baru.
     * @param progress Nilai progress yang baru (0-100).
     * @param userId ID user pemilik tugas untuk keamanan.
     * @return `true` jika update berhasil, `false` jika gagal.
     */
    public static boolean updateTaskCompletion(int taskId, boolean completed, int progress, int userId) {
        String sql = "UPDATE tasks SET completed = ?, progress = ? WHERE id = ? AND user_id = ?";
        try (Connection conn = connect(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setBoolean(1, completed);
            stmt.setInt(2, progress);
            stmt.setInt(3, taskId);
            stmt.setInt(4, userId);
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Gagal memperbarui status penyelesaian tugas ID " + taskId + ": " + e.getMessage());
            return false;
        }
    }

    /**
     * Method untuk menyisipkan (insert) record tugas baru ke dalam database.
     * @param task Objek `Task` yang berisi data untuk disimpan.
     * @param userId ID user pemilik tugas.
     * @return `true` jika berhasil, `false` jika gagal.
     */
    public static boolean insertTask(Task task, int userId) {
        String sql = "INSERT INTO tasks (name, description, course, date, time, priority, progress, completed, reminder_offset_days, parent_id, user_id, attachment_stored_name, attachment_original_name, last_reminded_date) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = connect(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, task.getName());
            stmt.setString(2, task.getDescription());
            stmt.setString(3, task.getCourse());
            stmt.setString(4, task.getDate());
            stmt.setString(5, task.getTime());
            stmt.setString(6, task.getPriority());
            stmt.setInt(7, task.getProgress());
            stmt.setBoolean(8, task.isCompleted());
            stmt.setInt(9, task.getReminderOffsetDays());
            if (task.getParentId() != null) {
                stmt.setInt(10, task.getParentId());
            } else {
                stmt.setNull(10, Types.INTEGER);
            }
            stmt.setInt(11, userId);
            stmt.setString(12, task.getAttachmentStoredName());
            stmt.setString(13, task.getAttachmentOriginalName());
            stmt.setString(14, task.getLastRemindedDate());
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Gagal menyimpan tugas untuk userId " + userId + ": " + e.getMessage());
            return false;
        }
    }

    /**
     * Method untuk memperbarui (update) data dari sebuah tugas yang sudah ada.
     * @param task Objek `Task` dengan data terbaru.
     * @param userId ID user pemilik tugas.
     * @return `true` jika berhasil, `false` jika gagal.
     */
    public static boolean updateTask(Task task, int userId) {
        String sql = "UPDATE tasks SET name = ?, description = ?, course = ?, date = ?, " +
                "time = ?, priority = ?, progress = ?, completed = ?, reminder_offset_days = ?, parent_id = ?, " +
                "attachment_stored_name = ?, attachment_original_name = ?, last_reminded_date = ? " +
                "WHERE id = ? AND user_id = ?";
        try (Connection conn = connect(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, task.getName());
            stmt.setString(2, task.getDescription());
            stmt.setString(3, task.getCourse());
            stmt.setString(4, task.getDate());
            stmt.setString(5, task.getTime());
            stmt.setString(6, task.getPriority());
            stmt.setInt(7, task.getProgress());
            stmt.setBoolean(8, task.isCompleted());
            stmt.setInt(9, task.getReminderOffsetDays());
            if (task.getParentId() != null) {
                stmt.setInt(10, task.getParentId());
            } else {
                stmt.setNull(10, Types.INTEGER);
            }
            stmt.setString(11, task.getAttachmentStoredName());
            stmt.setString(12, task.getAttachmentOriginalName());
            stmt.setString(13, task.getLastRemindedDate());
            stmt.setInt(14, task.getId());
            stmt.setInt(15, userId);
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Gagal mengupdate tugas ID " + task.getId() + " untuk userId " + userId + ": " + e.getMessage());
            return false;
        }
    }

    /**
     * Method untuk menghapus sebuah tugas dari database berdasarkan ID-nya.
     * @param taskId ID tugas yang akan dihapus.
     * @param userId ID user pemilik tugas.
     * @return `true` jika berhasil, `false` jika gagal.
     */
    public static boolean deleteTask(int taskId, int userId) {
        String sql = "DELETE FROM tasks WHERE id = ? AND user_id = ?";
        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, taskId);
            pstmt.setInt(2, userId);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Gagal menghapus tugas ID " + taskId + " untuk userId " + userId + ": " + e.getMessage());
            return false;
        }
    }

    /**
     * Method helper privat untuk mengubah satu baris dari `ResultSet` menjadi objek `Task`.
     * Ini adalah best practice untuk menghindari duplikasi kode.
     * @param rs `ResultSet` yang didapat dari query database.
     * @return Objek `Task` yang sudah diisi data.
     * @throws SQLException Jika terjadi error saat mengakses data dari ResultSet.
     */
    private static Task extractTask(ResultSet rs) throws SQLException {
        Integer parentId = rs.getInt("parent_id");
        if (rs.wasNull()) {
            parentId = null;
        }
        return new Task(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getString("description"),
                rs.getString("course"),
                rs.getString("date"),
                rs.getString("time"),
                rs.getString("priority"),
                rs.getInt("progress"),
                rs.getBoolean("completed"),
                rs.getInt("reminder_offset_days"),
                parentId,
                rs.getString("attachment_stored_name"),
                rs.getString("attachment_original_name"),
                rs.getString("last_reminded_date")
        );
    }

    /**
     * Method untuk memperbarui kolom `last_reminded_date` menjadi tanggal hari ini.
     * Dipanggil setelah notifikasi pengingat ditampilkan.
     * @param taskId ID tugas yang tanggal pengingatnya diupdate.
     */
    public static void updateLastRemindedDate(int taskId) {
        String sql = "UPDATE tasks SET last_reminded_date = ? WHERE id = ?";
        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, LocalDate.now().toString());
            pstmt.setInt(2, taskId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Gagal update last_reminded_date untuk task ID " + taskId + ": " + e.getMessage());
        }
    }

    /**
     * Method untuk mengambil semua tugas yang memenuhi kriteria untuk ditampilkan sebagai pengingat.
     * @param userId ID user pemilik tugas.
     * @return `List` dari objek `Task` yang perlu diingatkan.
     */
    public static List<Task> getTasksForReminder(int userId) {
        List<Task> tasksToRemind = new ArrayList<>();
        LocalDate today = LocalDate.now();
        String todayString = today.toString();
        String sql = "SELECT * FROM tasks WHERE completed = 0 AND reminder_offset_days > 0 AND user_id = ? AND (last_reminded_date IS NULL OR last_reminded_date < ?)";
        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setString(2, todayString);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Task task = extractTask(rs);
                if (task.getDate() != null && !task.getDate().isEmpty()) {
                    try {
                        LocalDate deadlineDate = LocalDate.parse(task.getDate());
                        LocalDate reminderDate = deadlineDate.minusDays(task.getReminderOffsetDays());
                        if (!reminderDate.isAfter(today)) {
                            tasksToRemind.add(task);
                        }
                    } catch (DateTimeParseException e) {
                        System.err.println("Format tanggal salah: " + task.getDate());
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Gagal mengambil tugas untuk pengingat: " + e.getMessage());
        }
        return tasksToRemind;
    }

    /**
     * Method untuk mengambil semua sub-tugas (anak langsung) dari sebuah tugas induk.
     * @param parentId ID dari tugas induk.
     * @param userId ID user pemilik tugas.
     * @return `List` dari objek `Task` yang merupakan sub-tugas.
     */
    public static List<Task> getSubTasks(int parentId, int userId) {
        List<Task> subTasks = new ArrayList<>();
        String sql = "SELECT * FROM tasks WHERE parent_id = ? AND user_id = ?";
        try (Connection conn = connect(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, parentId);
            stmt.setInt(2, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                subTasks.add(extractTask(rs));
            }
        } catch (SQLException e) {
            System.err.println("Gagal mengambil sub-tugas untuk parent_id " + parentId + " dan userId " + userId + ": " + e.getMessage());
        }
        return subTasks;
    }

    /**
     * Method untuk menyisipkan beberapa tugas sekaligus dalam satu transaksi database.
     * Ini lebih efisien daripada melakukan insert satu per satu.
     * Menggunakan `setAutoCommit(false)` dan `commit()`/`rollback()` untuk manajemen transaksi.
     * @param tasks `List` dari objek `Task` yang akan disimpan.
     * @param userId ID user pemilik tugas.
     * @return `true` jika semua berhasil disimpan, `false` jika terjadi error.
     */
    public static boolean insertMultipleTasks(List<Task> tasks, int userId) {
        String sql = "INSERT INTO tasks (name, description, course, date, time, priority, progress, completed, reminder_offset_days, parent_id, user_id, attachment_stored_name, attachment_original_name, last_reminded_date) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        Connection conn = null;
        try {
            conn = connect();
            conn.setAutoCommit(false); // Memulai transaksi
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                for (Task task : tasks) {
                    stmt.setString(1, task.getName());
                    stmt.setString(2, task.getDescription());
                    stmt.setString(3, task.getCourse());
                    stmt.setString(4, task.getDate());
                    stmt.setString(5, task.getTime());
                    stmt.setString(6, task.getPriority());
                    stmt.setInt(7, task.getProgress());
                    stmt.setBoolean(8, task.isCompleted());
                    stmt.setInt(9, task.getReminderOffsetDays());
                    if (task.getParentId() != null) {
                        stmt.setInt(10, task.getParentId());
                    } else {
                        stmt.setNull(10, Types.INTEGER);
                    }
                    stmt.setInt(11, userId);
                    stmt.setString(12, task.getAttachmentStoredName());
                    stmt.setString(13, task.getAttachmentOriginalName());
                    stmt.setString(14, task.getLastRemindedDate());
                    stmt.addBatch(); // Menambahkan statement ke dalam batch
                }
                stmt.executeBatch(); // Mengeksekusi semua statement dalam batch
            }
            conn.commit(); // Menyelesaikan transaksi
            return true;
        } catch (SQLException e) {
            System.err.println("Gagal menyimpan batch tugas untuk userId " + userId + ": " + e.getMessage());
            if (conn != null) {
                try {
                    conn.rollback(); // Batalkan transaksi jika terjadi error
                } catch (SQLException ex) {
                    System.err.println("Gagal melakukan rollback: " + ex.getMessage());
                }
            }
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.close(); // Selalu tutup koneksi
                } catch (SQLException ex) {
                    System.err.println("Gagal menutup koneksi: " + ex.getMessage());
                }
            }
        }
    }
}
