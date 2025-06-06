package com.example.project.controller;

import com.example.project.App;
import com.example.project.model.Database;
import com.example.project.model.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class AddSubtasksController {

    @FXML
    private Label parentTaskTitleLabel;
    @FXML
    private TextArea subtasksTextArea;
    @FXML
    private Button saveAllButton;
    @FXML
    private Button cancelButton;

    private Task parentTask;
    private Integer currentUserId;

    public void setParentTask(Task parentTask) {
        this.parentTask = parentTask;
        this.currentUserId = App.getCurrentUserId();
        if (parentTask != null) {
            parentTaskTitleLabel.setText("Tambah Sub-Tugas untuk: " + parentTask.getName());
        }
    }

    @FXML
    private void handleSaveAll() {
        if (parentTask == null || currentUserId == null) {
            showAlert("Error", "Informasi tugas induk atau pengguna tidak ditemukan.");
            return;
        }

        String[] subtaskNames = subtasksTextArea.getText().split("\\n");
        List<String> validNames = Arrays.stream(subtaskNames)
                .map(String::trim)
                .filter(name -> !name.isEmpty())
                .collect(Collectors.toList());

        if (validNames.isEmpty()) {
            showAlert("Peringatan", "Silakan masukkan setidaknya satu nama sub-tugas.");
            return;
        }

        List<Task> subtasksToInsert = new ArrayList<>();
        for (String name : validNames) {
            Task subtask = new Task(
                    0,
                    name,
                    "",
                    parentTask.getCourse(),
                    parentTask.getDate(),
                    parentTask.getTime(),
                    parentTask.getPriority(),
                    0,
                    false,
                    0,
                    parentTask.getId(),
                    null,
                    null,
                    null
            );
            subtasksToInsert.add(subtask);
        }

        boolean success = Database.insertMultipleTasks(subtasksToInsert, currentUserId);

        if (success) {
            showAlert("Sukses", validNames.size() + " sub-tugas berhasil ditambahkan.");
        } else {
            showAlert("Gagal", "Terjadi kesalahan saat menyimpan sub-tugas.");
        }

        closeWindow();
    }

    @FXML
    private void handleCancel() {
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) saveAllButton.getScene().getWindow();
        stage.close();
    }

    private void showAlert(String title, String message) {
        Alert.AlertType type = title.equalsIgnoreCase("sukses") ? Alert.AlertType.INFORMATION : Alert.AlertType.WARNING;
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
