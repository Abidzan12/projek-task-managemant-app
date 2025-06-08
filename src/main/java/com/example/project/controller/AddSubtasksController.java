package com.example.project.controller;

import com.example.project.App;
import com.example.project.model.Database;
import com.example.project.model.Task;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class AddSubtasksController {

    @FXML
    private Label parentTaskTitleLabel;
    @FXML
    private VBox subtasksContainer;
    @FXML
    private Button addMoreButton;
    @FXML
    private Button saveAllButton;
    @FXML
    private Button cancelButton;

    private Task parentTask;
    private Integer currentUserId;

    @FXML
    public void initialize() {
        addNewSubtaskField(); // Mulai dengan satu field input
    }

    public void setParentTask(Task parentTask) {
        this.parentTask = parentTask;
        this.currentUserId = App.getCurrentUserId();
        if (parentTask != null) {
            parentTaskTitleLabel.setText("untuk: " + parentTask.getName());
        }
    }

    @FXML
    private void handleAddMore() {
        addNewSubtaskField();
    }

    private void addNewSubtaskField() {
        TextField subtaskField = new TextField();
        subtaskField.setPromptText("Nama sub-tugas baru...");
        subtaskField.getStyleClass().add("form-input");
        HBox.setHgrow(subtaskField, javafx.scene.layout.Priority.ALWAYS);

        FontIcon removeIcon = new FontIcon(FontAwesomeSolid.TIMES_CIRCLE);
        removeIcon.getStyleClass().add("remove-icon");
        removeIcon.setIconSize(18);
        removeIcon.setCursor(Cursor.HAND);

        HBox newRow = new HBox(8, subtaskField, removeIcon);
        newRow.setAlignment(Pos.CENTER_LEFT);

        removeIcon.setOnMouseClicked(event -> {
            // Jangan hapus baris terakhir
            if (subtasksContainer.getChildren().size() > 1) {
                subtasksContainer.getChildren().remove(newRow);
            } else {
                subtaskField.clear(); // Cukup kosongkan jika hanya satu
            }
        });

        subtasksContainer.getChildren().add(newRow);
        subtaskField.requestFocus();
    }


    @FXML
    private void handleSaveAll() {
        if (parentTask == null || currentUserId == null) {
            showAlert("Error", "Informasi tugas induk atau pengguna tidak ditemukan.");
            return;
        }

        List<String> validNames = subtasksContainer.getChildren().stream()
                .filter(node -> node instanceof HBox)
                .map(node -> (HBox) node)
                .map(hbox -> (TextField) hbox.getChildren().get(0))
                .map(textField -> textField.getText().trim())
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
