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

    // FXML-injected fields
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

    // Class variables
    private Task parentTask;
    private Integer currentUserId;

    /**
     * Method ini dipanggil secara otomatis saat FXML dimuat.
     * Method ini akan memulai tampilan dengan satu field input untuk sub-tugas baru.
     */
    @FXML
    public void initialize() {
        addNewSubtaskField(); // Mulai dengan satu field input
    }

    /**
     * Method ini digunakan untuk mengatur tugas induk dari sub-tugas yang akan dibuat.
     * Informasi ini diterima dari DashboardController.
     * @param parentTask Objek Task yang menjadi induk.
     */
    public void setParentTask(Task parentTask) {
        this.parentTask = parentTask;
        this.currentUserId = App.getCurrentUserId();
        if (parentTask != null) {
            parentTaskTitleLabel.setText("untuk: " + parentTask.getName());
        }
    }

    /**
     * Method ini dieksekusi ketika tombol 'Tambah Lagi' diklik.
     * Ini akan memanggil method addNewSubtaskField() untuk menambahkan baris input baru.
     */
    @FXML
    private void handleAddMore() {
        addNewSubtaskField();
    }

    /**
     * Method privat ini bertanggung jawab untuk membuat dan menambahkan
     * baris input baru (TextField dan tombol hapus) secara dinamis ke dalam VBox.
     */
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
            // Jangan hapus baris terakhir, cukup kosongkan isinya
            if (subtasksContainer.getChildren().size() > 1) {
                subtasksContainer.getChildren().remove(newRow);
            } else {
                subtaskField.clear();
            }
        });

        subtasksContainer.getChildren().add(newRow);
        subtaskField.requestFocus();
    }

    /**
     * Method ini dieksekusi saat tombol 'Simpan Semua' diklik.
     * Ini mengumpulkan semua nama sub-tugas yang valid, membuatnya menjadi objek Task,
     * dan menyimpannya ke database secara batch menggunakan insertMultipleTasks.
     */
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
            // Membuat objek Task baru untuk setiap sub-tugas
            Task subtask = new Task(
                    0,
                    name,
                    "", // Deskripsi dikosongkan
                    parentTask.getCourse(),
                    parentTask.getDate(),
                    parentTask.getTime(),
                    parentTask.getPriority(),
                    0,
                    false,
                    0, // Pengingat dinonaktifkan secara default
                    parentTask.getId(), // Mengatur ID induk
                    null,
                    null,
                    null
            );
            subtasksToInsert.add(subtask);
        }

        // Menyimpan semua sub-tugas ke database dalam satu transaksi
        boolean success = Database.insertMultipleTasks(subtasksToInsert, currentUserId);

        if (success) {
            showAlert("Sukses", validNames.size() + " sub-tugas berhasil ditambahkan.");
        } else {
            showAlert("Gagal", "Terjadi kesalahan saat menyimpan sub-tugas.");
        }

        closeWindow();
    }

    /**
     * Method ini dieksekusi saat tombol 'Batal' diklik.
     * Ini akan menutup jendela (dialog) tambah sub-tugas.
     */
    @FXML
    private void handleCancel() {
        closeWindow();
    }

    /**
     * Method privat untuk menutup jendela (Stage) saat ini.
     */
    private void closeWindow() {
        Stage stage = (Stage) saveAllButton.getScene().getWindow();
        stage.close();
    }

    /**
     * Method utilitas untuk menampilkan dialog peringatan (Alert) kepada pengguna.
     * Jenis alert (INFORMASI atau PERINGATAN) disesuaikan berdasarkan judul.
     * @param title Judul alert.
     * @param message Pesan yang akan ditampilkan dalam alert.
     */
    private void showAlert(String title, String message) {
        Alert.AlertType type = title.equalsIgnoreCase("sukses") ? Alert.AlertType.INFORMATION : Alert.AlertType.WARNING;
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
