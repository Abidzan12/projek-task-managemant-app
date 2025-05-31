package com.example.project.controller;

import com.example.project.model.Database;
import com.example.project.App;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.scene.paint.Color;
import java.io.IOException;

public class LoginController {

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label statusLabel;

    @FXML
    protected void handleLogin(ActionEvent event) {
        String email = emailField.getText().trim();
        String password = passwordField.getText();

        if (email.isEmpty() || password.isEmpty()) {
            statusLabel.setTextFill(Color.RED);
            statusLabel.setText("Email dan password tidak boleh kosong!");
            return;
        }

        Integer userId = Database.validateLogin(email, password);

        if (userId != null) {
            App.setCurrentUserId(userId);
            statusLabel.setTextFill(Color.GREEN);
            statusLabel.setText("Login berhasil! Memuat dashboard...");
            try {
                Stage stage = (Stage) emailField.getScene().getWindow(); // Ambil Stage SEBELUM setRoot

                App.setRoot("dashboard"); // Ganti root scene

                if (stage != null) { // Gunakan referensi stage yang sudah diambil
                    stage.setTitle("Dashboard");
                }
            } catch (IOException e) {
                e.printStackTrace();
                statusLabel.setTextFill(Color.RED);
                statusLabel.setText("Gagal memuat dashboard: " + e.getMessage());
            }
        } else {
            statusLabel.setTextFill(Color.RED);
            statusLabel.setText("Login gagal. Email atau password salah.");
        }
    }

    @FXML
    protected void goToRegister(ActionEvent event) {
        try {
            Stage stage = (Stage) emailField.getScene().getWindow(); // Ambil Stage SEBELUM setRoot

            App.setRoot("register"); // Ganti root scene

            if (stage != null) { // Gunakan referensi stage yang sudah diambil
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