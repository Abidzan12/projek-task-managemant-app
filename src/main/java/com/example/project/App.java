package com.example.project;

import com.example.project.model.Database;
import com.example.project.model.Task;
import javafx.application.Application;
import javafx.application.HostServices;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.controlsfx.control.Notifications;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Kelas `App` adalah kelas utama yang menjalankan aplikasi JavaFX.
 * Kelas ini menginisialisasi window (Stage), memuat scene awal (login),
 * dan mengelola state global seperti session user.
 */
public class App extends Application {

    // Variabel statis untuk menyimpan referensi ke Scene, Stage, dan data global
    private static Scene scene;
    private static Stage primaryStageRef;
    private ScheduledExecutorService reminderScheduler; // (Tidak digunakan saat ini)
    private static Integer currentUserId = null; // Menyimpan ID user yang sedang login (session)
    private static HostServices hostServicesInstance; // Service untuk interaksi dengan OS (misal: buka file)

    /**
     * Method `start` adalah entry point utama untuk semua aplikasi JavaFX.
     * Mirip seperti `main` di aplikasi konsol, method ini dipanggil saat aplikasi diluncurkan.
     * @param stage Objek `Stage` utama (window aplikasi) yang dibuat oleh JavaFX.
     */
    @Override
    public void start(Stage stage) throws IOException {
        primaryStageRef = stage;
        hostServicesInstance = getHostServices();

        // Memuat tampilan awal yaitu halaman login.fxml
        Parent root = loadFXML("login");
        scene = new Scene(root);

        // Menerapkan file CSS untuk styling ke seluruh aplikasi
        String cssPath = "/com/example/project/css/style.css";
        URL cssUrl = App.class.getResource(cssPath);

        if (cssUrl == null) {
            System.err.println("Peringatan: File CSS tidak ditemukan di: " + cssPath);
        } else {
            scene.getStylesheets().add(cssUrl.toExternalForm());
            System.out.println("CSS (" + cssPath + ") berhasil ditambahkan ke scene awal.");
        }

        stage.setScene(scene);
        stage.setTitle("Task Manager");
        stage.show();
    }

    /**
     * Method untuk mendapatkan instance dari HostServices.
     * HostServices digunakan untuk berinteraksi dengan environment desktop,
     * seperti membuka browser atau file explorer.
     * @return Instance HostServices.
     */
    public static HostServices getHostServicesInstance() {
        return hostServicesInstance;
    }

    /**
     * Method untuk mengatur ID user yang sedang login.
     * Ini adalah cara sederhana untuk mengelola "session" di aplikasi desktop.
     * @param userId ID user yang berhasil login.
     */
    public static void setCurrentUserId(Integer userId) {
        currentUserId = userId;
    }

    /**
     * Method untuk mendapatkan ID user yang sedang login dari "session".
     * @return `Integer` berisi ID user, atau `null` jika tidak ada yang login.
     */
    public static Integer getCurrentUserId() {
        return currentUserId;
    }

    /**
     * Method helper untuk mengganti tampilan (view) di window utama.
     * Ini dilakukan dengan mengganti root node dari scene yang sedang aktif.
     * @param fxml Nama file FXML yang akan dimuat (tanpa ekstensi .fxml).
     * @throws IOException Jika file FXML tidak ditemukan.
     */
    public static void setRoot(String fxml) throws IOException {
        Parent newRoot = loadFXML(fxml);
        if (scene != null && newRoot != null) {
            scene.setRoot(newRoot);
        } else {
            System.err.println("Gagal mengubah root: scene atau FXML root baru adalah null.");
        }
    }

    /**
     * Method helper privat untuk memuat file FXML.
     * `FXMLLoader` membaca file .fxml dan mengubahnya menjadi objek UI Java.
     * @param fxml Nama file FXML yang akan dimuat.
     * @return Objek `Parent` yang merupakan root dari layout FXML.
     * @throws IOException Jika file FXML tidak ditemukan.
     */
    private static Parent loadFXML(String fxml) throws IOException {
        String path = "/com/example/project/fxml/" + fxml + ".fxml";
        FXMLLoader loader = new FXMLLoader(App.class.getResource(path));
        return loader.load();
    }

    /**
     * Method untuk menampilkan notifikasi desktop asli (OS-native).
     * Menggunakan `Platform.runLater` untuk memastikan kode yang memanipulasi UI
     * dijalankan di JavaFX Application Thread.
     * @param task Objek tugas yang informasinya akan ditampilkan di notifikasi.
     */
    public static void showDesktopNotification(Task task) {
        Platform.runLater(() -> {
            String title = "Pengingat Tugas!";
            String message = "Tugas: \"" + task.getName() + "\"" +
                    (task.getCourse() != null && !task.getCourse().isEmpty() ? "\nMata Kuliah: " + task.getCourse() : "") +
                    "\nJatuh tempo: " + task.getDate() +
                    (task.getTime() != null && !task.getTime().isEmpty() ? " pukul " + task.getTime() : "");

            Notifications notificationBuilder = Notifications.create()
                    .title(title)
                    .text(message)
                    .graphic(null)
                    .position(Pos.TOP_RIGHT)
                    .hideAfter(Duration.seconds(15))
                    .onAction(event -> {
                        // Aksi saat notifikasi diklik: bawa window aplikasi ke depan.
                        if (primaryStageRef != null) {
                            primaryStageRef.setIconified(false);
                            primaryStageRef.toFront();
                        }
                    });

            notificationBuilder.showInformation();
        });
    }

    /**
     * Method `stop` dipanggil secara otomatis oleh JavaFX saat aplikasi ditutup.
     * Tempat yang baik untuk melakukan proses cleanup, seperti menutup koneksi database
     * atau menghentikan background thread.
     */
    @Override
    public void stop() throws Exception {
        // if (reminderScheduler != null && !reminderScheduler.isShutdown()) {
        //     reminderScheduler.shutdownNow();
        // }
        super.stop();
    }

    /**
     * Method `main` adalah entry point standar untuk setiap aplikasi Java.
     * Untuk aplikasi JavaFX, fungsinya hanya untuk memanggil `launch(args)`
     * yang akan memulai lifecycle JavaFX dan memanggil method `start`.
     */
    public static void main(String[] args) {
        launch(args);
    }
}
