package com.example.project.controller;

import com.example.project.App;
import com.example.project.model.Database;
import com.example.project.model.Task;
import javafx.application.HostServices;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.control.cell.TreeItemPropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.util.Callback;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class DashboardController {

    @FXML private TreeTableView<Task> taskTreeTable;
    @FXML private TreeTableColumn<Task, String> colName;
    @FXML private TreeTableColumn<Task, String> colCourse;
    @FXML private TreeTableColumn<Task, String> colDate;
    @FXML private TreeTableColumn<Task, String> colTime;
    @FXML private TreeTableColumn<Task, String> colPriority;
    @FXML private TreeTableColumn<Task, String> colStatus;
    @FXML private TreeTableColumn<Task, Integer> colProgress;
    @FXML private TreeTableColumn<Task, Void> colAction;
    @FXML private TreeTableColumn<Task, String> colAttachment; // Kolom baru untuk indikator lampiran

    @FXML private Button btnAddTask;
    @FXML private Button btnShowAll;
    @FXML private Button btnShowPending;
    @FXML private Button btnShowCompleted;

    private static Stage addTaskStage = null;
    private Integer currentUserId;
    private HostServices hostServices;


    @FXML
    public void initialize() {
        this.currentUserId = App.getCurrentUserId();
        this.hostServices = App.getHostServicesInstance(); // Ambil HostServices dari App

        if (this.currentUserId == null) {
            showErrorDialog("Error Sesi Pengguna", "Tidak dapat memuat data tugas. Sesi pengguna tidak ditemukan. Silakan login ulang.");
            try {
                App.setRoot("login");
                Stage stage = (Stage) (btnAddTask != null ? btnAddTask.getScene().getWindow() : null);
                if (stage != null) stage.setTitle("Login");
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }
        setupColumns();
        loadTasksAndBuildTree();
        taskTreeTable.setShowRoot(false);
    }

    private void setupColumns() {
        colName.setCellValueFactory(new TreeItemPropertyValueFactory<>("name"));
        colCourse.setCellValueFactory(new TreeItemPropertyValueFactory<>("course"));
        colDate.setCellValueFactory(new TreeItemPropertyValueFactory<>("date"));
        colTime.setCellValueFactory(new TreeItemPropertyValueFactory<>("time"));
        colPriority.setCellValueFactory(new TreeItemPropertyValueFactory<>("priority"));
        colProgress.setCellValueFactory(new TreeItemPropertyValueFactory<>("progress"));
        colStatus.setCellValueFactory(new TreeItemPropertyValueFactory<>("statusDisplay"));
        setupAttachmentColumn();
        setupActionButtonsWithIkonli();
    }

    private void setupAttachmentColumn() {
        colAttachment.setCellValueFactory(new TreeItemPropertyValueFactory<>("attachmentOriginalName"));
        Callback<TreeTableColumn<Task, String>, TreeTableCell<Task, String>> cellFactory = param -> {
            final TreeTableCell<Task, String> cell = new TreeTableCell<>() {
                private final FontIcon attachmentIcon = new FontIcon(FontAwesomeSolid.PAPERCLIP);

                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null || item.isEmpty()) {
                        setGraphic(null);
                        setText(null);
                        setTooltip(null);
                    } else {
                        attachmentIcon.setIconSize(16);
                        attachmentIcon.setIconColor(Color.SLATEGRAY);
                        setGraphic(attachmentIcon);
                        setText(null); // Atau tampilkan nama file: setText(item)
                        setTooltip(new Tooltip("Lihat lampiran: " + item));
                        this.setCursor(Cursor.HAND);
                        this.setOnMouseClicked(event -> {
                            TreeItem<Task> treeItem = getTreeTableView().getTreeItem(getIndex());
                            if (treeItem != null && treeItem.getValue() != null) {
                                handleOpenAttachment(treeItem.getValue());
                            }
                        });
                    }
                }
            };
            return cell;
        };
        colAttachment.setCellFactory(cellFactory);
        colAttachment.setStyle("-fx-alignment: CENTER;");
    }


    private void loadTasksAndBuildTree() {
        if (currentUserId == null) return;
        List<Task> allTasks = Database.getAllTasks(currentUserId);
        buildTreeFromList(allTasks);
    }

    private void setupActionButtonsWithIkonli() {
        Callback<TreeTableColumn<Task, Void>, TreeTableCell<Task, Void>> cellFactory = param -> {
            final TreeTableCell<Task, Void> cell = new TreeTableCell<>() {
                private final FontIcon editIcon = new FontIcon(FontAwesomeSolid.PENCIL_ALT);
                private final FontIcon deleteIcon = new FontIcon(FontAwesomeSolid.TRASH_ALT);
                private final FontIcon addSubtaskIcon = new FontIcon(FontAwesomeSolid.PLUS_CIRCLE);
                private final Button completeButton = new Button();
                private final HBox pane = new HBox(8, editIcon, deleteIcon, addSubtaskIcon, completeButton);

                {
                    editIcon.setIconSize(18);
                    editIcon.setIconColor(Color.ROYALBLUE);
                    editIcon.setCursor(Cursor.HAND);
                    editIcon.setOnMouseClicked(event -> {
                        TreeItem<Task> treeItem = getTreeTableView().getTreeItem(getIndex());
                        if (treeItem != null && treeItem.getValue() != null) {
                            handleEditTask(treeItem.getValue());
                        }
                    });

                    deleteIcon.setIconSize(18);
                    deleteIcon.setIconColor(Color.CRIMSON);
                    deleteIcon.setCursor(Cursor.HAND);
                    deleteIcon.setOnMouseClicked(event -> {
                        TreeItem<Task> treeItem = getTreeTableView().getTreeItem(getIndex());
                        if (treeItem != null && treeItem.getValue() != null) {
                            handleDeleteTask(treeItem.getValue());
                        }
                    });

                    addSubtaskIcon.setIconSize(18);
                    addSubtaskIcon.setIconColor(Color.FORESTGREEN);
                    addSubtaskIcon.setCursor(Cursor.HAND);
                    addSubtaskIcon.setOnMouseClicked(event -> {
                        TreeItem<Task> parentTreeItem = getTreeTableView().getTreeItem(getIndex());
                        if (parentTreeItem != null && parentTreeItem.getValue() != null) {
                            handleAddSubTask(parentTreeItem.getValue());
                        }
                    });

                    completeButton.setOnAction(event -> {
                        TreeItem<Task> treeItem = getTreeTableView().getTreeItem(getIndex());
                        if (treeItem != null && treeItem.getValue() != null) {
                            handleToggleCompleteTask(treeItem.getValue());
                        }
                    });

                    pane.setAlignment(javafx.geometry.Pos.CENTER);
                }

                @Override
                protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || getTreeTableRow() == null || getTreeTableRow().getTreeItem() == null) {
                        setGraphic(null);
                    } else {
                        Task currentTask = getTreeTableRow().getTreeItem().getValue();
                        if (currentTask == null) {
                            setGraphic(null);
                            return;
                        }

                        if (currentTask.isCompleted()) {
                            completeButton.setText("Batal Selesai");
                            completeButton.setStyle("-fx-background-color: #ffc107; -fx-text-fill: black;");
                            editIcon.setDisable(true);
                            addSubtaskIcon.setDisable(true);
                            editIcon.setIconColor(Color.LIGHTGRAY);
                            addSubtaskIcon.setIconColor(Color.LIGHTGRAY);
                        } else {
                            completeButton.setText("Selesaikan");
                            completeButton.setStyle("-fx-background-color: #28a745; -fx-text-fill: white;");
                            editIcon.setDisable(false);
                            addSubtaskIcon.setDisable(false);
                            editIcon.setIconColor(Color.ROYALBLUE);
                            addSubtaskIcon.setIconColor(Color.FORESTGREEN);
                        }
                        deleteIcon.setIconColor(Color.CRIMSON);
                        setGraphic(pane);
                    }
                }
            };
            return cell;
        };
        colAction.setCellFactory(cellFactory);
    }

    private void handleOpenAttachment(Task task) {
        if (hostServices == null) {
            showErrorDialog("Error Aplikasi", "Tidak dapat membuka file lampiran (HostServices tidak tersedia).");
            return;
        }
        if (task.getAttachmentStoredName() != null && !task.getAttachmentStoredName().isEmpty()) {
            File fileToOpen = new File("data/attachments/" + task.getAttachmentStoredName());
            if (fileToOpen.exists()) {
                hostServices.showDocument(fileToOpen.toURI().toString());
            } else {
                showErrorDialog("File Tidak Ditemukan", "File lampiran '" + task.getAttachmentOriginalName() + "' tidak ditemukan di disk.");
            }
        } else {
            showErrorDialog("Tidak Ada Lampiran", "Tugas ini tidak memiliki lampiran.");
        }
    }

    private void handleAddSubTask(Task parentTask) {
        if (currentUserId == null) {
            showErrorDialog("Error Sesi", "Sesi pengguna tidak valid.");
            return;
        }
        try {
            if (addTaskStage == null || !addTaskStage.isShowing()) {
                FXMLLoader loader = new FXMLLoader(App.class.getResource("/com/example/project/fxml/add_task.fxml"));
                Parent root = loader.load();

                AddTaskController controller = loader.getController();
                controller.setParentTaskInfo(parentTask.getId(), parentTask.getName());

                addTaskStage = new Stage();
                addTaskStage.initModality(Modality.APPLICATION_MODAL);
                addTaskStage.setTitle("Tambah Sub-Tugas untuk: " + parentTask.getName());

                Scene scene = new Scene(root);
                URL cssUrl = App.class.getResource("/com/example/project/css/style.css");
                if (cssUrl != null) {
                    scene.getStylesheets().add(cssUrl.toExternalForm());
                }
                addTaskStage.setScene(scene);
                addTaskStage.setOnHiding(event -> loadTasksAndBuildTree());
                addTaskStage.show();
            } else {
                addTaskStage.toFront();
            }
        } catch (IOException e) {
            e.printStackTrace();
            showErrorDialog("Gagal Memuat Form Tambah Sub-Tugas", "Tidak dapat memuat tampilan untuk menambah sub-tugas.");
        }
    }

    private void handleEditTask(Task task) {
        if (currentUserId == null) {
            showErrorDialog("Error Sesi", "Sesi pengguna tidak valid.");
            return;
        }
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

            checkAndUpdateParentTaskProgress(task.getParentId());
            loadTasksAndBuildTree();
        } catch (IOException e) {
            e.printStackTrace();
            showErrorDialog("Gagal Memuat Form Edit", "Tidak dapat memuat tampilan untuk mengedit tugas.");
        }
    }

    private void handleDeleteTask(Task task) {
        if (currentUserId == null) {
            showErrorDialog("Error Sesi", "Sesi pengguna tidak valid.");
            return;
        }
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Konfirmasi Hapus");
        alert.setHeaderText(null);
        alert.setContentText("Apakah Anda yakin ingin menghapus tugas '" + task.getName() + "' dan semua sub-tugasnya (jika ada)?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            Integer parentIdOfDeletedTask = task.getParentId();
            String storedAttachmentName = task.getAttachmentStoredName();

            boolean success = Database.deleteTask(task.getId(), currentUserId);
            if (success) {
                if (storedAttachmentName != null && !storedAttachmentName.isEmpty()) {
                    try {
                        Files.deleteIfExists(Paths.get("data/attachments/" + storedAttachmentName));
                    } catch (IOException e) {
                        e.printStackTrace();
                        showErrorDialog("Error Hapus File", "Gagal menghapus file lampiran dari disk.");
                    }
                }
                checkAndUpdateParentTaskProgress(parentIdOfDeletedTask);
                loadTasksAndBuildTree();
            } else {
                showErrorDialog("Gagal Hapus", "Tugas gagal dihapus dari database.");
            }
        }
    }

    private void handleToggleCompleteTask(Task task) {
        if (currentUserId == null) {
            showErrorDialog("Error Sesi", "Sesi pengguna tidak valid.");
            return;
        }
        boolean newStatus = !task.isCompleted();
        int newProgress = newStatus ? 100 : (task.getProgress() == 100 ? 0 : task.getProgress());

        Database.updateTaskCompletion(task.getId(), newStatus, newProgress, currentUserId);
        checkAndUpdateParentTaskProgress(task.getParentId());
        loadTasksAndBuildTree();
    }

    private void checkAndUpdateParentTaskProgress(Integer parentId) {
        if (parentId == null || currentUserId == null) {
            return;
        }

        List<Task> subTasks = Database.getSubTasks(parentId, currentUserId);

        if (subTasks.isEmpty()) {
            Database.updateTaskCompletion(parentId, false, 0, currentUserId);
            return;
        }

        long completedSubTasksCount = subTasks.stream().filter(Task::isCompleted).count();
        int totalSubTasksCount = subTasks.size();

        int parentProgress = 0;
        if (totalSubTasksCount > 0) {
            parentProgress = (int) (((double) completedSubTasksCount / totalSubTasksCount) * 100);
        }

        boolean parentIsCompleted = (completedSubTasksCount == totalSubTasksCount);

        Database.updateTaskCompletion(parentId, parentIsCompleted, parentProgress, currentUserId);
    }

    @FXML
    private void onAddTask() {
        if (currentUserId == null) {
            showErrorDialog("Error Sesi", "Sesi pengguna tidak valid. Silakan login ulang.");
            return;
        }
        try {
            if (addTaskStage == null || !addTaskStage.isShowing()) {
                FXMLLoader loader = new FXMLLoader(App.class.getResource("/com/example/project/fxml/add_task.fxml"));
                Parent root = loader.load();

                AddTaskController controller = loader.getController();
                controller.setEditTask(null);
                controller.setParentTaskInfo(null, null);

                addTaskStage = new Stage();
                addTaskStage.initModality(Modality.APPLICATION_MODAL);
                addTaskStage.setTitle("Tambah Tugas Baru");

                Scene scene = new Scene(root);
                URL cssUrl = App.class.getResource("/com/example/project/css/style.css");
                if (cssUrl != null) {
                    scene.getStylesheets().add(cssUrl.toExternalForm());
                }
                addTaskStage.setScene(scene);

                addTaskStage.setOnHiding(event -> loadTasksAndBuildTree());
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
        loadTasksAndBuildTree();
    }

    @FXML
    private void onShowPending() {
        if (currentUserId == null) return;
        List<Task> filteredList = Database.getTasksByCompletion(currentUserId, false);
        buildTreeFromList(filteredList);
    }

    @FXML
    private void onShowCompleted() {
        if (currentUserId == null) return;
        List<Task> filteredList = Database.getTasksByCompletion(currentUserId, true);
        buildTreeFromList(filteredList);
    }

    private void buildTreeFromList(List<Task> taskList) {
        if (currentUserId == null) {
            taskTreeTable.setRoot(null);
            taskTreeTable.refresh();
            return;
        }
        Map<Integer, TreeItem<Task>> taskMap = new HashMap<>();
        TreeItem<Task> rootItem = new TreeItem<>();

        for (Task task : taskList) {
            TreeItem<Task> treeItem = new TreeItem<>(task);
            taskMap.put(task.getId(), treeItem);
        }

        for (Task task : taskList) {
            TreeItem<Task> currentItem = taskMap.get(task.getId());
            if (task.getParentId() != null && taskMap.containsKey(task.getParentId())) {
                TreeItem<Task> parentItem = taskMap.get(task.getParentId());
                if (parentItem != null) {
                    parentItem.getChildren().add(currentItem);
                } else {
                    rootItem.getChildren().add(currentItem);
                }
            } else {
                rootItem.getChildren().add(currentItem);
            }
        }
        taskTreeTable.setRoot(rootItem);
        taskTreeTable.refresh();
    }

    private void showErrorDialog(String title, String message) {
        Alert errorAlert = new Alert(Alert.AlertType.ERROR);
        errorAlert.setTitle(title);
        errorAlert.setHeaderText(null);
        errorAlert.setContentText(message);
        errorAlert.showAndWait();
    }
}