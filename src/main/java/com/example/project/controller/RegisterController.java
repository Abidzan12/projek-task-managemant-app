package com.example.project.controller;

import com.example.project.model.Database;
import com.example.project.App;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.IOException;

public class RegisterController {

    // Elemen UI yang di-inject dari file FXML.
    @FXML
    private TextField emailField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Label statusLabel;

    /**
     * Method ini menangani event ketika tombol 'Register' ditekan.
     * Fungsinya adalah untuk mengambil input dari user, melakukan validasi dasar,
     * memanggil method database untuk mendaftarkan user baru, dan memberikan feedback visual
     * melalui statusLabel.
     * @param event Informasi event dari klik tombol.
     */
    @FXML
    protected void handleRegister(ActionEvent event) {
        String email = emailField.getText().trim();
        String password = passwordField.getText();

        // Validasi input: memastikan tidak ada field yang kosong.
        if (email.isEmpty() || password.isEmpty()) {
            statusLabel.setTextFill(Color.RED);
            statusLabel.setText("username dan password tidak boleh kosong.");
            return; // Menghentikan eksekusi method jika validasi gagal.
        }

        // Memanggil method di Database class untuk memasukkan data user baru.
        boolean success = Database.registerUser(email, password);

        // Memberikan feedback ke user berdasarkan hasil operasi database.
        if (success) {
            statusLabel.setTextFill(Color.GREEN);
            statusLabel.setText("Registrasi berhasil! Silakan login.");
        } else {
            statusLabel.setTextFill(Color.RED);
            statusLabel.setText("Registrasi gagal. Username mungkin sudah digunakan.");
        }
    }

    /**
     * Method ini menangani event saat tombol 'Sudah punya akun? Login' diklik.
     * Fungsinya untuk melakukan navigasi dari halaman registrasi kembali ke halaman login.
     * @param event Informasi event dari klik tombol.
     */
    @FXML
    protected void goToLogin(ActionEvent event) {
        try {
            // Mengambil Stage (jendela) saat ini sebelum mengganti scene.
            Stage stage = (Stage) emailField.getScene().getWindow();

            // Mengganti root dari scene utama ke halaman login.
            App.setRoot("login");

            // Mengubah judul jendela menjadi "Login".
            if (stage != null) {
                stage.setTitle("Login");
            }
        } catch (IOException e) {
            e.printStackTrace();
            if (statusLabel != null) {
                statusLabel.setTextFill(Color.RED);
                statusLabel.setText("Gagal memuat halaman login.");
            }
        }
    }
}
