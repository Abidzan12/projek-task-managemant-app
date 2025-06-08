package com.example.project.controller;

import com.example.project.model.Database;
import com.example.project.model.Task;
import com.example.project.App;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.UUID;

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
    private CheckBox reminderCheckBox; // CheckBox untuk mengaktifkan/menonaktifkan pengingat
    @FXML
    private Button saveButton;
    @FXML
    private Button cancelButton;
    @FXML
    private Label parentTaskInfoLabel;
    @FXML
    private Button chooseFileButton;
    @FXML
    private Label attachmentNameLabel;
    @FXML
    private Button removeAttachmentButton;

    private Task taskToEdit = null;
    private boolean editMode = false;
    private Integer parentIdForNewTask = null;
    private File selectedAttachmentFile = null;
    private String existingStoredAttachmentName = null;
    private boolean attachmentActionTaken = false;

    private final String ATTACHMENTS_DIR = "data/attachments/";

    @FXML
    public void initialize() {
        prioritasBox.getItems().addAll("Rendah", "Sedang", "Tinggi");

        progressSlider.valueProperty().addListener((obs, oldVal, newVal) ->
                progressLabel.setText(String.format("%d%%", newVal.intValue())));
        progressLabel.setText(String.format("%d%%", (int)progressSlider.getValue()));

        SpinnerValueFactory<Integer> reminderValueFactory =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 30, 1, 1); // Minimal sekarang 1
        reminderOffsetSpinner.setValueFactory(reminderValueFactory);

        reminderOffsetSpinner.disableProperty().bind(reminderCheckBox.selectedProperty().not());

        if (parentTaskInfoLabel != null) {
            parentTaskInfoLabel.setVisible(false);
            parentTaskInfoLabel.setManaged(false);
        }
        updateAttachmentUI(null, null);
    }

    public void setEditTask(Task task) {
        this.parentIdForNewTask = null;
        this.selectedAttachmentFile = null;
        this.attachmentActionTaken = false;
        if (parentTaskInfoLabel != null) {
            parentTaskInfoLabel.setVisible(false);
            parentTaskInfoLabel.setManaged(false);
        }

        if (task != null) {
            this.taskToEdit = task;
            this.editMode = true;
            this.parentIdForNewTask = task.getParentId();
            this.existingStoredAttachmentName = task.getAttachmentStoredName();

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

            int reminderDays = task.getReminderOffsetDays();
            if (reminderDays > 0) {
                reminderCheckBox.setSelected(true);
                if (reminderOffsetSpinner.getValueFactory() != null) {
                    reminderOffsetSpinner.getValueFactory().setValue(reminderDays);
                }
            } else {
                reminderCheckBox.setSelected(false);
                if (reminderOffsetSpinner.getValueFactory() != null) {
                    reminderOffsetSpinner.getValueFactory().setValue(1); // Set ke nilai minimal
                }
            }

            updateAttachmentUI(task.getAttachmentOriginalName(), task.getAttachmentStoredName());
            saveButton.setText("Update Tugas");
        } else {
            this.editMode = false;
            this.taskToEdit = null;
            this.existingStoredAttachmentName = null;
            saveButton.setText("Simpan Tugas");
            clearFields();
        }
    }

    public void setParentTaskInfo(Integer parentId, String parentName) {
        this.parentIdForNewTask = parentId;
        this.editMode = false;
        this.taskToEdit = null;
        this.selectedAttachmentFile = null;
        this.existingStoredAttachmentName = null;
        this.attachmentActionTaken = false;
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
        reminderCheckBox.setSelected(false);
        if (reminderOffsetSpinner.getValueFactory() != null) {
            reminderOffsetSpinner.getValueFactory().setValue(1);
        }
        progressSlider.setValue(0);
        progressLabel.setText("0%");
        this.selectedAttachmentFile = null;
        this.attachmentActionTaken = false;
        updateAttachmentUI(null, null);
    }

    @FXML
    private void handleChooseFile(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Pilih File Lampiran");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Semua File", "*.*"),
                new FileChooser.ExtensionFilter("Gambar", "*.png", "*.jpg", "*.jpeg", "*.gif"),
                new FileChooser.ExtensionFilter("Dokumen PDF", "*.pdf"),
                new FileChooser.ExtensionFilter("Dokumen Word", "*.doc", "*.docx"),
                new FileChooser.ExtensionFilter("Dokumen Teks", "*.txt")
        );
        File file = fileChooser.showOpenDialog(chooseFileButton.getScene().getWindow());
        if (file != null) {
            this.selectedAttachmentFile = file;
            this.attachmentActionTaken = true;
            updateAttachmentUI(file.getName(), null);
        }
    }

    @FXML
    private void handleRemoveAttachment(ActionEvent event) {
        this.selectedAttachmentFile = null;
        this.attachmentActionTaken = true;
        updateAttachmentUI(null, null);
    }

    private void updateAttachmentUI(String originalNameFromTask, String storedNameFromTask) {
        if (selectedAttachmentFile != null) {
            attachmentNameLabel.setText(selectedAttachmentFile.getName());
            removeAttachmentButton.setVisible(true);
            removeAttachmentButton.setManaged(true);
        } else if (editMode && originalNameFromTask != null && !originalNameFromTask.isEmpty() && !attachmentActionTaken) {
            attachmentNameLabel.setText(originalNameFromTask);
            removeAttachmentButton.setVisible(true);
            removeAttachmentButton.setManaged(true);
        } else {
            attachmentNameLabel.setText("Tidak ada file dipilih");
            removeAttachmentButton.setVisible(false);
            removeAttachmentButton.setManaged(false);
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

        int reminderOffset = 0;
        if (reminderCheckBox.isSelected()) {
            reminderOffset = reminderOffsetSpinner.getValue();
        }


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

        String finalStoredAttachmentName = null;
        String finalOriginalAttachmentName = null;

        try {
            File attachmentsDirFile = new File(ATTACHMENTS_DIR);
            if (!attachmentsDirFile.exists()) {
                attachmentsDirFile.mkdirs();
            }

            if (attachmentActionTaken && selectedAttachmentFile == null) {
                if (existingStoredAttachmentName != null) {
                    Files.deleteIfExists(Paths.get(ATTACHMENTS_DIR + existingStoredAttachmentName));
                }
                finalStoredAttachmentName = null;
                finalOriginalAttachmentName = null;
            } else if (selectedAttachmentFile != null) {
                if (editMode && existingStoredAttachmentName != null) {
                    Files.deleteIfExists(Paths.get(ATTACHMENTS_DIR + existingStoredAttachmentName));
                }
                String originalFileName = selectedAttachmentFile.getName();
                String fileExtension = "";
                int i = originalFileName.lastIndexOf('.');
                if (i > 0 && i < originalFileName.length() - 1) {
                    fileExtension = originalFileName.substring(i);
                }
                finalStoredAttachmentName = UUID.randomUUID().toString() + fileExtension;
                finalOriginalAttachmentName = originalFileName;
                Files.copy(selectedAttachmentFile.toPath(), Paths.get(ATTACHMENTS_DIR + finalStoredAttachmentName), StandardCopyOption.REPLACE_EXISTING);
            } else if (editMode && taskToEdit != null) {
                finalStoredAttachmentName = taskToEdit.getAttachmentStoredName();
                finalOriginalAttachmentName = taskToEdit.getAttachmentOriginalName();
            }

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error File", "Gagal menyimpan atau menghapus file lampiran.");
            return;
        }

        String lastRemindedDate = null;
        if (editMode && taskToEdit != null && taskToEdit.getReminderOffsetDays() != reminderOffset) {
            lastRemindedDate = null;
        } else if (editMode && taskToEdit != null) {
            lastRemindedDate = taskToEdit.getLastRemindedDate();
        }

        if (editMode && taskToEdit != null) {
            Task updatedTask = new Task(
                    taskToEdit.getId(), nama, deskripsi, matkul, tanggal,
                    waktu, prioritas, progress, completed, reminderOffset,
                    taskToEdit.getParentId(), finalStoredAttachmentName, finalOriginalAttachmentName,
                    lastRemindedDate
            );
            boolean success = Database.updateTask(updatedTask, currentUserId);
            if (success) showAlert("Sukses", "Tugas berhasil diupdate.");
            else showAlert("Gagal", "Gagal mengupdate tugas di database.");
        } else {
            Task newTask = new Task(
                    0, nama, deskripsi, matkul, tanggal,
                    waktu, prioritas, progress, completed, reminderOffset,
                    this.parentIdForNewTask, finalStoredAttachmentName, finalOriginalAttachmentName,
                    null
            );
            boolean success = Database.insertTask(newTask, currentUserId);
            if (success) showAlert("Sukses", "Tugas berhasil disimpan.");
            else showAlert("Gagal", "Gagal menyimpan tugas ke database.");
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