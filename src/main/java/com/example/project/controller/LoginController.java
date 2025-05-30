package com.example.project.controller;

import com.example.project.model.Database;
import com.example.project.App;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.scene.paint.Color;

import java.io.IOException;
import java.net.URL;

public class LoginController {

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label statusLabel;

    @FXML
    protected void handleLogin(ActionEvent event) {
        String email = emailField.getText();
        String password = passwordField.getText();

        if (email.isEmpty() || password.isEmpty()) {
            statusLabel.setTextFill(Color.RED);
            statusLabel.setText("Email dan password tidak boleh kosong!");
            return;
        }

        boolean success = Database.validateLogin(email, password);
        if (success) {
            statusLabel.setTextFill(Color.GREEN);
            statusLabel.setText("Login berhasil! Memuat dashboard...");
            try {
                String dashboardPath = "/com/example/project/fxml/dashboard.fxml";
                URL dashboardResourceUrl = getClass().getResource(dashboardPath);

                if (dashboardResourceUrl == null) {
                    System.err.println("GAGAL MEMUAT RESOURCE: " + dashboardPath + " tidak ditemukan di classpath.");
                    statusLabel.setTextFill(Color.RED);
                    statusLabel.setText("Kesalahan Internal: File UI Dashboard tidak ditemukan.");
                    return;
                }

                System.out.println("Path FXML Dashboard ditemukan: " + dashboardResourceUrl.toExternalForm());

                FXMLLoader loader = new FXMLLoader(dashboardResourceUrl);
                Parent root = loader.load();
                Stage stage = (Stage) emailField.getScene().getWindow();

                Scene dashboardScene = new Scene(root);

                stage.setScene(dashboardScene);
                stage.setTitle("Dashboard");

            } catch (IOException e) {
                e.printStackTrace();
                statusLabel.setTextFill(Color.RED);
                statusLabel.setText("Gagal memuat dashboard: " + e.getMessage());
            } catch (Exception e) {
                e.printStackTrace();
                statusLabel.setTextFill(Color.RED);
                statusLabel.setText("Terjadi error tidak terduga saat memuat dashboard.");
            }
        } else {
            statusLabel.setTextFill(Color.RED);
            statusLabel.setText("Login gagal. Email atau password salah.");
            System.out.println("Login gagal");
        }
    }

    @FXML
    protected void goToRegister(ActionEvent event) {
        try {
            App.setRoot("register");
        } catch (IOException e) {
            e.printStackTrace();
            if (statusLabel != null) {
                statusLabel.setTextFill(Color.RED);
                statusLabel.setText("Gagal memuat halaman registrasi.");
            }
        }
    }
}