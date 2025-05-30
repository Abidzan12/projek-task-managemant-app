package com.example.project.controller;

import com.example.project.model.Database;
import com.example.project.model.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.time.LocalDate;

public class AddTaskController {

    @FXML
    private TextField namaField;
    @FXML
    private TextArea deskripsiField;
    @FXML
    private TextField matkulField;
    @FXML
    private DatePicker tanggalPicker; // Mengganti TextField dengan DatePicker
    @FXML
    private TextField waktuField;
    @FXML
    private ComboBox<String> prioritasBox;
    @FXML
    private Slider progressSlider;
    @FXML
    private Label progressLabel; // Label untuk menampilkan nilai progress
    @FXML
    private Button saveButton;
    @FXML
    private Button cancelButton;

    private Task taskToEdit = null;
    private boolean editMode = false;

    @FXML
    public void initialize() {
        prioritasBox.getItems().addAll("Rendah", "Sedang", "Tinggi");
        progressSlider.valueProperty().addListener((obs, oldVal, newVal) ->
                progressLabel.setText(String.format("%d%%", newVal.intValue())));
        progressLabel.setText(String.format("%d%%", (int)progressSlider.getValue()));
    }

    public void setEditTask(Task task) {
        if (task != null) {
            this.taskToEdit = task;
            this.editMode = true;

            namaField.setText(task.getName());
            deskripsiField.setText(task.getDescription());
            matkulField.setText(task.getCourse());
            if (task.getDate() != null && !task.getDate().isEmpty()) {
                try {
                    tanggalPicker.setValue(LocalDate.parse(task.getDate()));
                } catch (Exception e) {
                    System.err.println("Format tanggal salah saat memuat tugas: " + task.getDate());
                    tanggalPicker.setValue(null);
                }
            }
            waktuField.setText(task.getTime());
            prioritasBox.setValue(task.getPriority());
            progressSlider.setValue(task.getProgress());
            progressLabel.setText(String.format("%d%%", task.getProgress()));
            saveButton.setText("Update Tugas");
        } else {
            this.editMode = false;
            this.taskToEdit = null;
            saveButton.setText("Simpan Tugas");
            // Anda bisa mengosongkan field di sini jika form ini juga dipakai untuk 'Tambah Baru' setelah mode edit
            // namaField.clear();
            // deskripsiField.clear();
            // ... dan seterusnya
        }
    }

    @FXML
    private void handleSave() {
        String nama = namaField.getText().trim();
        String deskripsi = deskripsiField.getText().trim();
        String matkul = matkulField.getText().trim();
        String tanggal = "";
        if (tanggalPicker.getValue() != null) {
            tanggal = tanggalPicker.getValue().toString();
        }
        String waktu = waktuField.getText().trim();
        String prioritas = prioritasBox.getValue();
        int progress = (int) progressSlider.getValue();
        boolean completed = (progress == 100);

        if (nama.isEmpty() || prioritas == null || prioritas.isEmpty()) {
            showAlert("Peringatan", "Nama dan Prioritas harus diisi.");
            return;
        }

        if (tanggal.isEmpty()) {
            showAlert("Peringatan", "Tanggal harus diisi.");
            return;
        }


        if (editMode && taskToEdit != null) {
            Task updatedTask = new Task(
                    taskToEdit.getId(),
                    nama,
                    deskripsi,
                    matkul,
                    tanggal,
                    waktu,
                    prioritas,
                    progress,
                    completed
            );
            boolean success = Database.updateTask(updatedTask);
            if (success) {
                showAlert("Sukses", "Tugas berhasil diupdate.");
            } else {
                showAlert("Gagal", "Gagal mengupdate tugas di database.");
            }
        } else {
            Task newTask = new Task(
                    0,
                    nama,
                    deskripsi,
                    matkul,
                    tanggal,
                    waktu,
                    prioritas,
                    progress,
                    completed
            );
            boolean success = Database.insertTask(newTask);
            if (success) {
                showAlert("Sukses", "Tugas berhasil disimpan.");
            } else {
                showAlert("Gagal", "Gagal menyimpan tugas ke database.");
            }
        }
        closeWindow();
    }

    @FXML
    private void handleCancel() {
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) saveButton.getScene().getWindow();
        stage.close();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        if (title.equalsIgnoreCase("Gagal") || title.equalsIgnoreCase("Peringatan")){
            alert.setAlertType(Alert.AlertType.WARNING);
        }
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}