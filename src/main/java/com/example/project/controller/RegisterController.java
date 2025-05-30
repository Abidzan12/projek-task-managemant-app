package com.example.project.controller;

import com.example.project.model.Database;
import com.example.project.App;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;

import java.io.IOException;

public class RegisterController {

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label statusLabel;

    @FXML
    protected void handleRegister(ActionEvent event) {
        String email = emailField.getText();
        String password = passwordField.getText();

        if (email.isEmpty() || password.isEmpty()) {
            statusLabel.setTextFill(Color.RED);
            statusLabel.setText("Email dan password tidak boleh kosong.");
            return;
        }

        boolean success = Database.registerUser(email, password);
        if (success) {
            statusLabel.setTextFill(Color.GREEN);
            statusLabel.setText("Registrasi berhasil! Silakan login.");
        } else {
            statusLabel.setTextFill(Color.RED);
            statusLabel.setText("Registrasi gagal. Email mungkin sudah digunakan.");
        }
    }

    @FXML
    protected void goToLogin(ActionEvent event) {
        try {
            App.setRoot("login");
        } catch (IOException e) {
            e.printStackTrace();
            if (statusLabel != null) {
                statusLabel.setTextFill(Color.RED);
                statusLabel.setText("Gagal memuat halaman login.");
            }
        }
    }
}