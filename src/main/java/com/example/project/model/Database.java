package com.example.project.model;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Database {
    private static final String DB_URL = "jdbc:sqlite:data/task.db";

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

    public static boolean validateLogin(String username, String password) {
        String sql = "SELECT * FROM users WHERE username = ? AND password = ?";
        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            ResultSet rs = pstmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            System.out.println("Login failed: " + e.getMessage());
            return false;
        }
    }

    public static Connection connect() {
        try {
            return DriverManager.getConnection(DB_URL);
        } catch (SQLException e) {
            throw new RuntimeException("Gagal terhubung ke database", e);
        }
    }

    public static List<Task> getAllTasks() {
        List<Task> tasks = new ArrayList<>();
        String sql = "SELECT * FROM tasks";

        try (Connection conn = connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                tasks.add(extractTask(rs));
            }

        } catch (SQLException e) {
            System.err.println("Gagal mengambil semua tugas: " + e.getMessage());
        }
        return tasks;
    }

    public static List<Task> getTasksByCompletion(boolean completed) {
        List<Task> tasks = new ArrayList<>();
        String sql = "SELECT * FROM tasks WHERE completed = ?";

        try (Connection conn = connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setBoolean(1, completed);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                tasks.add(extractTask(rs));
            }
        } catch (SQLException e) {
            System.err.println("Gagal mengambil tugas berdasarkan status: " + e.getMessage());
        }
        return tasks;
    }

    public static boolean updateTaskCompletion(int taskId, boolean completed, int progress) {
        String sql = "UPDATE tasks SET completed = ?, progress = ? WHERE id = ?";
        try (Connection conn = connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setBoolean(1, completed);
            stmt.setInt(2, progress);
            stmt.setInt(3, taskId);
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Gagal memperbarui status penyelesaian tugas ID " + taskId + ": " + e.getMessage());
            return false;
        }
    }

    public static boolean insertTask(Task task) {
        String sql = "INSERT INTO tasks (name, description, course, date, time, priority, progress, completed) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, task.getName());
            stmt.setString(2, task.getDescription());
            stmt.setString(3, task.getCourse());
            stmt.setString(4, task.getDate());
            stmt.setString(5, task.getTime());
            stmt.setString(6, task.getPriority());
            stmt.setInt(7, task.getProgress());
            stmt.setBoolean(8, task.isCompleted());

            stmt.executeUpdate();
            System.out.println("Tugas berhasil disimpan ke database");
            return true;
        } catch (SQLException e) {
            System.err.println("Gagal menyimpan tugas: " + e.getMessage());
            return false;
        }
    }

    public static boolean updateTask(Task task) {
        String sql = "UPDATE tasks SET name = ?, description = ?, course = ?, date = ?, " +
                "time = ?, priority = ?, progress = ?, completed = ? WHERE id = ?";
        try (Connection conn = connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, task.getName());
            stmt.setString(2, task.getDescription());
            stmt.setString(3, task.getCourse());
            stmt.setString(4, task.getDate());
            stmt.setString(5, task.getTime());
            stmt.setString(6, task.getPriority());
            stmt.setInt(7, task.getProgress());
            stmt.setBoolean(8, task.isCompleted());
            stmt.setInt(9, task.getId());

            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Gagal mengupdate tugas ID " + task.getId() + ": " + e.getMessage());
            return false;
        }
    }

    public static boolean deleteTask(int taskId) {
        String sql = "DELETE FROM tasks WHERE id = ?";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, taskId);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Gagal menghapus tugas ID " + taskId + ": " + e.getMessage());
            return false;
        }
    }

    private static Task extractTask(ResultSet rs) throws SQLException {
        return new Task(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getString("description"),
                rs.getString("course"),
                rs.getString("date"),
                rs.getString("time"),
                rs.getString("priority"),
                rs.getInt("progress"),
                rs.getBoolean("completed")
        );
    }
}