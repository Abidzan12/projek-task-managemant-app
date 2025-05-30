package com.example.project.controller;

import com.example.project.model.Database;
import com.example.project.model.Task;
import com.example.project.App;
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
    private DatePicker tanggalPicker;
    @FXML
    private TextField waktuField;
    @FXML
    private ComboBox<String> prioritasBox;
    @FXML
    private Slider progressSlider;
    @FXML
    private Label progressLabel;
    @FXML
    private Spinner<Integer> reminderOffsetSpinner;
    @FXML
    private Button saveButton;
    @FXML
    private Button cancelButton;
    @FXML
    private Label parentTaskInfoLabel;

    private Task taskToEdit = null;
    private boolean editMode = false;
    private Integer parentIdForNewTask = null;

    @FXML
    public void initialize() {
        prioritasBox.getItems().addAll("Rendah", "Sedang", "Tinggi");

        progressSlider.valueProperty().addListener((obs, oldVal, newVal) ->
                progressLabel.setText(String.format("%d%%", newVal.intValue())));
        progressLabel.setText(String.format("%d%%", (int)progressSlider.getValue()));

        SpinnerValueFactory<Integer> reminderValueFactory =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 30, 0, 1);
        reminderOffsetSpinner.setValueFactory(reminderValueFactory);
        reminderOffsetSpinner.setEditable(true);

        if (parentTaskInfoLabel != null) {
            parentTaskInfoLabel.setVisible(false);
            parentTaskInfoLabel.setManaged(false);
        }
    }

    public void setEditTask(Task task) {
        this.parentIdForNewTask = null;
        if (parentTaskInfoLabel != null) {
            parentTaskInfoLabel.setVisible(false);
            parentTaskInfoLabel.setManaged(false);
        }

        if (task != null) {
            this.taskToEdit = task;
            this.editMode = true;
            this.parentIdForNewTask = task.getParentId();

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
            } else {
                tanggalPicker.setValue(null);
            }
            waktuField.setText(task.getTime());
            prioritasBox.setValue(task.getPriority());
            progressSlider.setValue(task.getProgress());
            progressLabel.setText(String.format("%d%%", task.getProgress()));

            if (reminderOffsetSpinner.getValueFactory() != null) {
                reminderOffsetSpinner.getValueFactory().setValue(task.getReminderOffsetDays());
            }

            saveButton.setText("Update Tugas");
        } else {
            this.editMode = false;
            this.taskToEdit = null;
            saveButton.setText("Simpan Tugas");
            clearFields();
        }
    }

    public void setParentTaskInfo(Integer parentId, String parentName) {
        this.parentIdForNewTask = parentId;
        this.editMode = false;
        this.taskToEdit = null;
        saveButton.setText("Simpan Sub-Tugas");
        clearFields();

        if (parentId != null && parentTaskInfoLabel != null) {
            parentTaskInfoLabel.setText("Sub-tugas dari: " + parentName);
            parentTaskInfoLabel.setVisible(true);
            parentTaskInfoLabel.setManaged(true);
        } else if (parentTaskInfoLabel != null) {
            parentTaskInfoLabel.setVisible(false);
            parentTaskInfoLabel.setManaged(false);
        }
    }

    private void clearFields() {
        namaField.clear();
        deskripsiField.clear();
        matkulField.clear();
        tanggalPicker.setValue(null);
        waktuField.clear();
        prioritasBox.getSelectionModel().clearSelection();
        if (reminderOffsetSpinner.getValueFactory() != null) {
            reminderOffsetSpinner.getValueFactory().setValue(0);
        }
        progressSlider.setValue(0);
        progressLabel.setText("0%");
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
        int reminderOffset = reminderOffsetSpinner.getValue();

        Integer currentUserId = App.getCurrentUserId();
        if (currentUserId == null) {
            showAlert("Error", "Sesi pengguna tidak ditemukan. Silakan login kembali.");
            return;
        }

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
                    completed,
                    reminderOffset,
                    taskToEdit.getParentId()
            );
            boolean success = Database.updateTask(updatedTask, currentUserId);
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
                    completed,
                    reminderOffset,
                    this.parentIdForNewTask
            );
            boolean success = Database.insertTask(newTask, currentUserId);
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