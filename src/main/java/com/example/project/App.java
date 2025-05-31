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
// import org.controlsfx.control.Action; // Dihapus karena tidak digunakan

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class App extends Application {

    private static Scene scene;
    private static Stage primaryStageRef;
    private ScheduledExecutorService reminderScheduler;
    private static Integer currentUserId = null;
    private static HostServices hostServicesInstance;

    @Override
    public void start(Stage stage) throws IOException {
        primaryStageRef = stage;
        hostServicesInstance = getHostServices();

        Parent root = loadFXML("login");
        scene = new Scene(root);

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

        startReminderService();
    }

    public static HostServices getHostServicesInstance() {
        return hostServicesInstance;
    }

    public static void setCurrentUserId(Integer userId) {
        currentUserId = userId;
    }

    public static Integer getCurrentUserId() {
        return currentUserId;
    }

    public static void setRoot(String fxml) throws IOException {
        Parent newRoot = loadFXML(fxml);
        if (scene != null && newRoot != null) {
            scene.setRoot(newRoot);
        } else {
            System.err.println("Gagal mengubah root: scene atau FXML root baru adalah null.");
        }
    }

    private static Parent loadFXML(String fxml) throws IOException {
        String path = "/com/example/project/fxml/" + fxml + ".fxml";
        URL fxmlLocation = App.class.getResource(path);
        System.out.println("Mencoba memuat FXML: " + path);
        System.out.println("URL FXML yang ditemukan: " + fxmlLocation);

        if (fxmlLocation == null) {
            throw new IOException("File FXML tidak ditemukan pada path: " + path + ". Pastikan path benar dan file ada di direktori resources yang ter-build.");
        }

        FXMLLoader loader = new FXMLLoader(fxmlLocation);
        return loader.load();
    }

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
                    .position(Pos.BOTTOM_RIGHT)
                    .hideAfter(Duration.seconds(15)) // Durasi bisa disesuaikan
                    .onAction(event -> { // Aksi default saat notifikasi diklik
                        System.out.println("Notifikasi untuk '" + task.getName() + "' diklik!");
                        if (primaryStageRef != null) {
                            primaryStageRef.setIconified(false);
                            primaryStageRef.toFront();
                        }
                    });
            // Pemanggilan .actions(...) dihapus

            notificationBuilder.showInformation();
        });
    }

    public void startReminderService() {
        reminderScheduler = Executors.newSingleThreadScheduledExecutor();
        reminderScheduler.scheduleAtFixedRate(() -> {
            Platform.runLater(() -> {
                Integer userId = getCurrentUserId();
                if (userId != null) {
                    System.out.println("Scheduler: Pemeriksaan pengingat berjalan pada - " + java.time.LocalDateTime.now() + " untuk User ID: " + userId);
                    List<Task> tasksToRemind = Database.getTasksDueForReminderToday(userId);
                    if (tasksToRemind != null && !tasksToRemind.isEmpty()) {
                        for (Task taskToRemind : tasksToRemind) {
                            showDesktopNotification(taskToRemind);
                        }
                    } else {
                        System.out.println("Scheduler: Tidak ada tugas untuk diingatkan saat ini untuk User ID: " + userId);
                    }
                } else {
                    System.out.println("Scheduler: Tidak ada pengguna yang login, pemeriksaan pengingat dilewati.");
                }
            });
        }, 0, 1, TimeUnit.HOURS);
    }

    @Override
    public void stop() throws Exception {
        System.out.println("Aplikasi berhenti, mematikan scheduler pengingat.");
        if (reminderScheduler != null && !reminderScheduler.isShutdown()) {
            reminderScheduler.shutdownNow();
        }
        super.stop();
    }

    public static void testNotification() {
        Task testTask = new Task(999, "Meeting Proyek Penting", "Diskusi progres mingguan", "Proyek Akhir",
                LocalDate.now().plusDays(1).toString(), "14:00", "Tinggi", 0, false, 1, null, null, null);
        showDesktopNotification(testTask);
    }

    public static void main(String[] args) {
        launch(args);
    }
}