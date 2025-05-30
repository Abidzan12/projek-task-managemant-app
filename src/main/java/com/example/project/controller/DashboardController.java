package com.example.project.controller;

import com.example.project.App;
import com.example.project.model.Database;
import com.example.project.model.Task;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.util.Callback;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Optional;

public class DashboardController {

    @FXML private TableView<Task> taskTable;
    @FXML private TableColumn<Task, String> colName, colCourse, colDate, colTime, colPriority, colStatus;
    @FXML private TableColumn<Task, Integer> colProgress;
    @FXML private TableColumn<Task, Void> colAction;

    @FXML private Button btnAddTask, btnShowAll, btnShowPending, btnShowCompleted;

    private ObservableList<Task> tasks;

    @FXML
    public void initialize() {
        tasks = FXCollections.observableArrayList();
        taskTable.setItems(tasks);
        setupColumns();
        loadTasks();
    }

    private void setupColumns() {
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colCourse.setCellValueFactory(new PropertyValueFactory<>("course"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        colTime.setCellValueFactory(new PropertyValueFactory<>("time"));
        colPriority.setCellValueFactory(new PropertyValueFactory<>("priority"));
        colProgress.setCellValueFactory(new PropertyValueFactory<>("progress"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("statusDisplay"));

        setupActionButtons();
    }

    private void loadTasks() {
        List<Task> taskList = Database.getAllTasks();
        tasks.setAll(taskList);
    }

    private void setupActionButtons() {
        Callback<TableColumn<Task, Void>, TableCell<Task, Void>> cellFactory = new Callback<>() {
            @Override
            public TableCell<Task, Void> call(final TableColumn<Task, Void> param) {
                return new TableCell<>() {
                    private final Button editButton = new Button("Edit");
                    private final Button deleteButton = new Button("Hapus");
                    private final Button completeButton = new Button();
                    private final HBox pane = new HBox(5, editButton, deleteButton, completeButton);

                    {
                        editButton.setOnAction((ActionEvent event) -> {
                            Task task = getTableView().getItems().get(getIndex());
                            handleEditTask(task);
                        });

                        deleteButton.setOnAction((ActionEvent event) -> {
                            Task task = getTableView().getItems().get(getIndex());
                            handleDeleteTask(task);
                        });

                        completeButton.setOnAction((ActionEvent event) -> {
                            Task task = getTableView().getItems().get(getIndex());
                            handleToggleCompleteTask(task);
                        });
                    }

                    @Override
                    public void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            Task currentTask = getTableView().getItems().get(getIndex());
                            if (currentTask.isCompleted()) {
                                completeButton.setText("Batal Selesai");
                                completeButton.setStyle("-fx-background-color: #ffc107; -fx-text-fill: white;");
                                editButton.setDisable(true);
                            } else {
                                completeButton.setText("Selesaikan");
                                completeButton.setStyle("-fx-background-color: #28a745; -fx-text-fill: white;");
                                editButton.setDisable(false);
                            }
                            setGraphic(pane);
                        }
                    }
                };
            }
        };
        colAction.setCellFactory(cellFactory);
    }

    private void handleEditTask(Task task) {
        try {
            FXMLLoader loader = new FXMLLoader(App.class.getResource("/com/example/project/fxml/add_task.fxml"));
            Parent root = loader.load();

            AddTaskController addTaskController = loader.getController();
            addTaskController.setEditTask(task);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Edit Tugas");

            Scene scene = new Scene(root);
            URL cssUrl = App.class.getResource("/com/example/project/css/style.css");
            if (cssUrl != null) {
                scene.getStylesheets().add(cssUrl.toExternalForm());
            }
            stage.setScene(scene);

            stage.showAndWait();
            loadTasks();
        } catch (IOException e) {
            e.printStackTrace();
            showErrorDialog("Gagal Memuat Form Edit", "Tidak dapat memuat tampilan untuk mengedit tugas.");
        }
    }

    private void handleDeleteTask(Task task) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Konfirmasi Hapus");
        alert.setHeaderText(null);
        alert.setContentText("Apakah Anda yakin ingin menghapus tugas '" + task.getName() + "'?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            boolean success = Database.deleteTask(task.getId());
            if (success) {
                loadTasks();
            } else {
                showErrorDialog("Gagal Hapus", "Tugas gagal dihapus dari database.");
            }
        }
    }

    private void handleToggleCompleteTask(Task task) {
        boolean newStatus = !task.isCompleted();
        int newProgress = newStatus ? 100 : (task.getProgress() == 100 ? 0 : task.getProgress());
        Database.updateTaskCompletion(task.getId(), newStatus, newProgress);
        loadTasks();
    }

    private static Stage addTaskStage = null;

    @FXML
    private void onAddTask() {
        try {
            if (addTaskStage == null || !addTaskStage.isShowing()) {
                FXMLLoader loader = new FXMLLoader(App.class.getResource("/com/example/project/fxml/add_task.fxml"));
                Parent root = loader.load();

                AddTaskController controller = loader.getController();
                controller.setEditTask(null);

                addTaskStage = new Stage();
                addTaskStage.initModality(Modality.APPLICATION_MODAL);
                addTaskStage.setTitle("Tambah Tugas Baru");

                Scene scene = new Scene(root);
                URL cssUrl = App.class.getResource("/com/example/project/css/style.css");
                if (cssUrl != null) {
                    scene.getStylesheets().add(cssUrl.toExternalForm());
                }
                addTaskStage.setScene(scene);

                addTaskStage.setOnHiding(event -> loadTasks());
                addTaskStage.show();
            } else {
                addTaskStage.toFront();
            }
        } catch (IOException e) {
            e.printStackTrace();
            showErrorDialog("Gagal Memuat Form Tambah", "Tidak dapat memuat tampilan untuk menambah tugas.");
        }
    }

    @FXML
    private void onShowAll() {
        loadTasks();
    }

    @FXML
    private void onShowPending() {
        tasks.setAll(Database.getTasksByCompletion(false));
    }

    @FXML
    private void onShowCompleted() {
        tasks.setAll(Database.getTasksByCompletion(true));
    }

    private void showErrorDialog(String title, String message) {
        Alert errorAlert = new Alert(Alert.AlertType.ERROR);
        errorAlert.setTitle(title);
        errorAlert.setHeaderText(null);
        errorAlert.setContentText(message);
        errorAlert.showAndWait();
    }
}