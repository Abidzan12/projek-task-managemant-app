package com.example.project.controller;

import com.example.project.model.Database;
import com.example.project.model.Task;
import com.example.project.App;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.scene.paint.Color;
import java.io.IOException;
import java.util.List;

public class LoginController {

    // Elemen UI yang terhubung dari file FXML
    @FXML
    private TextField emailField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Label statusLabel;

    /**
     * Method ini menangani event ketika tombol 'Login' ditekan.
     * Fungsinya adalah mengambil input user, melakukan validasi,
     * dan mengautentikasi kredensial ke database.
     * Jika berhasil, method ini akan mengatur session user dan memuat halaman dashboard.
     */
    @FXML
    protected void handleLogin(ActionEvent event) {
        String email = emailField.getText().trim();
        String password = passwordField.getText();

        // Validasi dasar: memastikan field tidak kosong
        if (email.isEmpty() || password.isEmpty()) {
            statusLabel.setTextFill(Color.RED);
            statusLabel.setText("Email dan password tidak boleh kosong!");
            return;
        }

        // Memanggil method di Database class untuk validasi login
        Integer userId = Database.validateLogin(email, password);

        if (userId != null) { // Jika login berhasil (userId ditemukan)
            // Mengatur 'session' dengan menyimpan ID user yang sedang aktif
            App.setCurrentUserId(userId);

            // Memeriksa dan menampilkan notifikasi pengingat tugas
            checkAndShowReminders(userId);

            statusLabel.setTextFill(Color.GREEN);
            statusLabel.setText("Login berhasil! Memuat dashboard...");

            // Melakukan navigasi ke halaman dashboard
            try {
                Stage stage = (Stage) emailField.getScene().getWindow();
                App.setRoot("dashboard");
                if (stage != null) {
                    stage.setTitle("Dashboard");
                }
            } catch (IOException e) {
                e.printStackTrace();
                statusLabel.setTextFill(Color.RED);
                statusLabel.setText("Gagal memuat dashboard: " + e.getMessage());
            }
        } else { // Jika login gagal
            statusLabel.setTextFill(Color.RED);
            statusLabel.setText("Login gagal. Email atau password salah.");
        }
    }

    /**
     * Method privat untuk memeriksa tugas yang memerlukan pengingat.
     * Jika ada, notifikasi desktop akan ditampilkan kepada pengguna.
     * @param userId ID user yang sedang login untuk mengambil tugas yang relevan.
     */
    private void checkAndShowReminders(int userId) {
        List<Task> tasksToRemind = Database.getTasksForReminder(userId);
        if (tasksToRemind != null && !tasksToRemind.isEmpty()) {
            System.out.println("Menemukan " + tasksToRemind.size() + " pengingat saat login.");
            for (Task task : tasksToRemind) {
                // Memanggil method di App class untuk menampilkan notifikasi
                App.showDesktopNotification(task);
            }
        }
    }

    /**
     * Method ini menangani event saat tombol 'Buat Akun' diklik.
     * Fungsinya untuk melakukan navigasi ke halaman registrasi.
     */
    @FXML
    protected void goToRegister(ActionEvent event) {
        try {
            Stage stage = (Stage) emailField.getScene().getWindow();
            // Mengganti scene root saat ini ke halaman register
            App.setRoot("register");
            if (stage != null) {
                stage.setTitle("Registrasi Pengguna");
            }
        } catch (IOException e) {
            e.printStackTrace();
            if (statusLabel != null) {
                statusLabel.setTextFill(Color.RED);
                statusLabel.setText("Gagal memuat halaman registrasi.");
            }
        }
    }
}
