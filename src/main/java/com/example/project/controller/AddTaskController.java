package com.example.project.controller;

import com.example.project.model.Database;
import com.example.project.model.Task;
import com.example.project.App;
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

    // Elemen-elemen UI yang di-inject dari FXML
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
    private CheckBox reminderCheckBox;
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

    // Variabel untuk menyimpan state atau data sementara
    private Task taskToEdit = null; // Menyimpan tugas yang sedang diedit
    private boolean editMode = false; // Flag untuk menandakan mode edit
    private Integer parentIdForNewTask = null; // Menyimpan ID tugas induk jika ini adalah sub-tugas
    private File selectedAttachmentFile = null; // File lampiran yang dipilih oleh pengguna
    private String existingStoredAttachmentName = null; // Nama file lampiran yang sudah ada (untuk mode edit)
    private boolean attachmentActionTaken = false; // Flag untuk melacak aksi pada lampiran

    // Konstanta untuk direktori penyimpanan lampiran
    private final String ATTACHMENTS_DIR = "data/attachments/";

    /**
     * Method `initialize` dijalankan secara otomatis setelah FXML selesai dimuat.
     * Digunakan untuk setup awal komponen UI.
     */
    @FXML
    public void initialize() {
        // Mengisi pilihan untuk ComboBox prioritas
        prioritasBox.getItems().addAll("Rendah", "Sedang", "Tinggi");

        // Menambahkan listener ke slider untuk memperbarui label persentase secara real-time
        progressSlider.valueProperty().addListener((obs, oldVal, newVal) ->
                progressLabel.setText(String.format("%d%%", newVal.intValue())));
        progressLabel.setText(String.format("%d%%", (int)progressSlider.getValue()));

        // Mengatur Spinner untuk pilihan hari pengingat (1-30 hari)
        SpinnerValueFactory<Integer> reminderValueFactory =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 30, 1, 1);
        reminderOffsetSpinner.setValueFactory(reminderValueFactory);

        // Menonaktifkan Spinner jika CheckBox pengingat tidak dicentang
        reminderOffsetSpinner.disableProperty().bind(reminderCheckBox.selectedProperty().not());

        // Menyembunyikan label info tugas induk secara default
        if (parentTaskInfoLabel != null) {
            parentTaskInfoLabel.setVisible(false);
            parentTaskInfoLabel.setManaged(false);
        }
        // Mengatur tampilan UI lampiran ke state awal
        updateAttachmentUI(null, null);
    }

    /**
     * Method ini dipanggil dari `DashboardController` untuk mengisi form
     * dengan data dari tugas yang sudah ada. Ini mengaktifkan 'edit mode'.
     * @param task Objek Task yang akan diedit. Jika null, form akan disiapkan untuk tugas baru.
     */
    public void setEditTask(Task task) {
        // Reset state
        this.parentIdForNewTask = null;
        this.selectedAttachmentFile = null;
        this.attachmentActionTaken = false;
        if (parentTaskInfoLabel != null) {
            parentTaskInfoLabel.setVisible(false);
            parentTaskInfoLabel.setManaged(false);
        }

        if (task != null) { // Jika ada tugas yang diedit
            this.taskToEdit = task;
            this.editMode = true;
            this.parentIdForNewTask = task.getParentId();
            this.existingStoredAttachmentName = task.getAttachmentStoredName();

            // Mengisi semua field form dengan data dari objek 'task'
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

            // Mengatur state CheckBox dan Spinner pengingat
            int reminderDays = task.getReminderOffsetDays();
            if (reminderDays > 0) {
                reminderCheckBox.setSelected(true);
                reminderOffsetSpinner.getValueFactory().setValue(reminderDays);
            } else {
                reminderCheckBox.setSelected(false);
                reminderOffsetSpinner.getValueFactory().setValue(1); // Reset ke nilai default
            }

            updateAttachmentUI(task.getAttachmentOriginalName(), task.getAttachmentStoredName());
            saveButton.setText("Update");
        } else { // Jika membuat tugas baru
            this.editMode = false;
            this.taskToEdit = null;
            this.existingStoredAttachmentName = null;
            saveButton.setText("Simpan");
            clearFields();
        }
    }

    /**
     * Method ini dipanggil saat membuat sub-tugas. Ini mengatur ID induk dan
     * menampilkan nama tugas induk di UI.
     * @param parentId ID dari tugas induk.
     * @param parentName Nama dari tugas induk.
     */
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

    /**
     * Method privat untuk mengosongkan semua field input di form.
     */
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

    /**
     * Method ini menangani event saat tombol 'Pilih File' diklik.
     * Membuka dialog FileChooser untuk memilih file lampiran.
     */
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

    /**
     * Method ini menangani event saat tombol hapus (X) pada lampiran diklik.
     * Ini akan menghapus referensi file yang dipilih.
     */
    @FXML
    private void handleRemoveAttachment(ActionEvent event) {
        this.selectedAttachmentFile = null;
        this.attachmentActionTaken = true;
        updateAttachmentUI(null, null);
    }

    /**
     * Method privat untuk memperbarui tampilan UI yang terkait dengan lampiran file.
     * Menampilkan nama file dan tombol hapus jika ada file.
     * @param originalNameFromTask Nama file asli dari tugas yang ada (saat edit).
     * @param storedNameFromTask Nama file yang disimpan di sistem (saat edit).
     */
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

    /**
     * Method utama yang dijalankan saat tombol 'Simpan' atau 'Update' diklik.
     * Ini mengumpulkan semua data dari form, melakukan validasi,
     * memproses file lampiran, dan kemudian menyimpan atau memperbarui tugas di database.
     */
    @FXML
    private void handleSave() {
        // Mengambil semua nilai dari field input
        String nama = namaField.getText().trim();
        String deskripsi = deskripsiField.getText().trim();
        String matkul = matkulField.getText().trim();
        String tanggal = (tanggalPicker.getValue() != null) ? tanggalPicker.getValue().toString() : "";
        String waktu = waktuField.getText().trim();
        String prioritas = prioritasBox.getValue();
        int progress = (int) progressSlider.getValue();
        boolean completed = (progress == 100);

        int reminderOffset = reminderCheckBox.isSelected() ? reminderOffsetSpinner.getValue() : 0;

        Integer currentUserId = App.getCurrentUserId();
        if (currentUserId == null) {
            showAlert("Error", "Sesi pengguna tidak ditemukan. Silakan login kembali.");
            return;
        }

        // Validasi input dasar
        if (nama.isEmpty() || prioritas == null || prioritas.isEmpty()) {
            showAlert("Peringatan", "Nama dan Prioritas harus diisi.");
            return;
        }
        if (tanggal.isEmpty()) {
            showAlert("Peringatan", "Tanggal harus diisi.");
            return;
        }
        // Validasi format waktu menggunakan regex
        if (!waktu.isEmpty() && !waktu.matches("^([01][0-9]|2[0-3]):[0-5][0-9]$")) {
            showAlert("Peringatan", "Format Waktu Deadline salah.\nHarap gunakan format hh:mm (contoh: 09:30 atau 23:59).");
            return;
        }

        String finalStoredAttachmentName = null;
        String finalOriginalAttachmentName = null;

        try {
            // Memastikan direktori untuk lampiran ada
            File attachmentsDirFile = new File(ATTACHMENTS_DIR);
            if (!attachmentsDirFile.exists()) {
                attachmentsDirFile.mkdirs();
            }

            // Logika untuk menghapus, mengganti, atau menyimpan file lampiran
            if (attachmentActionTaken && selectedAttachmentFile == null) { // Aksi: Hapus lampiran
                if (existingStoredAttachmentName != null) {
                    Files.deleteIfExists(Paths.get(ATTACHMENTS_DIR + existingStoredAttachmentName));
                }
            } else if (selectedAttachmentFile != null) { // Aksi: Tambah/Ganti lampiran
                if (editMode && existingStoredAttachmentName != null) {
                    Files.deleteIfExists(Paths.get(ATTACHMENTS_DIR + existingStoredAttachmentName));
                }
                String originalFileName = selectedAttachmentFile.getName();
                String fileExtension = "";
                int i = originalFileName.lastIndexOf('.');
                if (i > 0) fileExtension = originalFileName.substring(i);

                finalStoredAttachmentName = UUID.randomUUID().toString() + fileExtension;
                finalOriginalAttachmentName = originalFileName;
                Files.copy(selectedAttachmentFile.toPath(), Paths.get(ATTACHMENTS_DIR + finalStoredAttachmentName), StandardCopyOption.REPLACE_EXISTING);
            } else if (editMode && taskToEdit != null) { // Aksi: Tidak ada perubahan pada lampiran
                finalStoredAttachmentName = taskToEdit.getAttachmentStoredName();
                finalOriginalAttachmentName = taskToEdit.getAttachmentOriginalName();
            }
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error File", "Gagal menyimpan atau menghapus file lampiran.");
            return;
        }

        // Logika untuk mereset tanggal pengingat jika pengaturannya diubah
        String lastRemindedDate = (editMode && taskToEdit != null) ? taskToEdit.getLastRemindedDate() : null;
        if (editMode && taskToEdit != null && taskToEdit.getReminderOffsetDays() != reminderOffset) {
            lastRemindedDate = null; // Reset jika offset berubah, agar notifikasi bisa muncul lagi
        }

        // Memutuskan apakah akan melakukan INSERT (tugas baru) atau UPDATE (tugas lama)
        if (editMode && taskToEdit != null) {
            Task updatedTask = new Task(taskToEdit.getId(), nama, deskripsi, matkul, tanggal, waktu, prioritas, progress, completed, reminderOffset, taskToEdit.getParentId(), finalStoredAttachmentName, finalOriginalAttachmentName, lastRemindedDate);
            boolean success = Database.updateTask(updatedTask, currentUserId);
            if (success) showAlert("Sukses", "Tugas berhasil diupdate.");
            else showAlert("Gagal", "Gagal mengupdate tugas di database.");
        } else {
            Task newTask = new Task(0, nama, deskripsi, matkul, tanggal, waktu, prioritas, progress, completed, reminderOffset, this.parentIdForNewTask, finalStoredAttachmentName, finalOriginalAttachmentName, null);
            boolean success = Database.insertTask(newTask, currentUserId);
            if (success) showAlert("Sukses", "Tugas berhasil disimpan.");
            else showAlert("Gagal", "Gagal menyimpan tugas ke database.");
        }
        closeWindow();
    }

    /**
     * Method ini menangani event saat tombol 'Batal' diklik.
     */
    @FXML
    private void handleCancel() {
        closeWindow();
    }

    /**
     * Method privat untuk menutup jendela (Stage) saat ini.
     */
    private void closeWindow() {
        Stage stage = (Stage) saveButton.getScene().getWindow();
        stage.close();
    }

    /**
     * Method utilitas untuk menampilkan dialog peringatan (Alert) kepada pengguna.
     * @param title Judul untuk window alert.
     * @param message Pesan yang ingin ditampilkan.
     */
    private void showAlert(String title, String message) {
        Alert.AlertType type = (title.equalsIgnoreCase("Gagal") || title.equalsIgnoreCase("Peringatan") || title.equalsIgnoreCase("Error"))
                ? Alert.AlertType.WARNING : Alert.AlertType.INFORMATION;
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
