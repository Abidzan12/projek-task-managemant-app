package com.example.project.controller;

import com.example.project.App;
import com.example.project.model.Database;
import com.example.project.model.Task;
import javafx.application.HostServices;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.control.cell.TreeItemPropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
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
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class DashboardController {

    @FXML private TreeTableView<Task> taskTreeTable;
    @FXML private TreeTableColumn<Task, String> colName;
    @FXML private TreeTableColumn<Task, String> colDescription;
    @FXML private TreeTableColumn<Task, String> colCourse;
    @FXML private TreeTableColumn<Task, String> colDeadline;
    @FXML private TreeTableColumn<Task, String> colPriority;
    @FXML private TreeTableColumn<Task, String> colStatus;
    @FXML private TreeTableColumn<Task, Integer> colProgress;
    @FXML private TreeTableColumn<Task, String> colAttachment;
    @FXML private TreeTableColumn<Task, Void> colAction;

    @FXML private Label sidebarUserNameLabel;
    @FXML private Button navDashboardButton;
    @FXML private Button navListTasksButton;
    @FXML private Button navAddTaskButton;
    @FXML private Button navPendingTasksButton;
    @FXML private Button navCompletedTasksButton;

    @FXML private ImageView clockImageView;
    @FXML private Label greetingLabel;
    @FXML private Label taskSummaryLabel;

    private static Stage addTaskStage = null;
    private static Stage addSubtasksStage = null;
    private Integer currentUserId;
    private String currentUserName;
    private HostServices hostServices;

    @FXML
    public void initialize() {
        this.currentUserId = App.getCurrentUserId();
        this.hostServices = App.getHostServicesInstance();

        if (this.currentUserId == null) {
            showErrorDialog("Error Sesi Pengguna", "Tidak dapat memuat data tugas. Sesi pengguna tidak ditemukan. Silakan login ulang.");
            try {
                App.setRoot("login");
                Stage stage = (Stage) (navAddTaskButton != null && navAddTaskButton.getScene() != null ? navAddTaskButton.getScene().getWindow() : null);
                if (stage != null) {
                    stage.setTitle("Login");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }

        if (taskTreeTable != null) {
            taskTreeTable.setColumnResizePolicy(TreeTableView.CONSTRAINED_RESIZE_POLICY);
            taskTreeTable.setShowRoot(false);
        }

        loadUserInfo();
        setupHeaderInfo();
        setupColumns();
        loadTasksAndBuildTree();

        try {
            URL clockIconUrl = getClass().getResource("/com/example/project/images/clock_icon.png");
            if (clockImageView != null) {
                if (clockIconUrl != null) {
                    clockImageView.setImage(new Image(clockIconUrl.toExternalForm()));
                } else {
                    System.err.println("File ikon jam tidak ditemukan. Pastikan path benar: /com/example/project/images/clock_icon.png");
                }
            }
        } catch (Exception e) {
            System.err.println("Gagal memuat ikon jam: " + e.getMessage());
        }
    }

    private void loadUserInfo() {
        if (this.currentUserId != null) {
            String fetchedUserName = Database.getUserNameById(this.currentUserId);
            if (fetchedUserName != null && !fetchedUserName.isEmpty()) {
                this.currentUserName = fetchedUserName;
            } else {
                this.currentUserName = "Pengguna";
            }
        } else {
            this.currentUserName = "Pengguna";
        }
    }

    private void setupHeaderInfo() {
        if (sidebarUserNameLabel != null && this.currentUserName != null) {
            sidebarUserNameLabel.setText("Hi, " + this.currentUserName + "!");
        }

        LocalTime now = LocalTime.now();
        int hour = now.getHour();
        String greetingTextBase;

        if (hour >= 3 && hour < 11) {
            greetingTextBase = "Selamat Pagi";
        } else if (hour >= 11 && hour < 15) {
            greetingTextBase = "Selamat Siang";
        } else if (hour >= 15 && hour < 18) {
            greetingTextBase = "Selamat Sore";
        } else {
            greetingTextBase = "Selamat Malam";
        }

        if (greetingLabel != null && this.currentUserName != null) {
            greetingLabel.setText(greetingTextBase + ", " + this.currentUserName + "!");
        } else if (greetingLabel != null) {
            greetingLabel.setText(greetingTextBase + "!");
        }

        if (currentUserId != null) {
            updateTaskSummary(Database.getAllTasks(currentUserId));
        } else if (taskSummaryLabel != null) {
            taskSummaryLabel.setText("Silakan login untuk melihat tugas Anda.");
        }
    }

    private void setupColumns() {
        colName.setCellValueFactory(new TreeItemPropertyValueFactory<>("name"));
        colDescription.setCellValueFactory(new TreeItemPropertyValueFactory<>("description"));

        colCourse.setCellValueFactory(new TreeItemPropertyValueFactory<>("course"));
        colCourse.setCellFactory(column -> createInheritedDataCell(Task::getCourse));

        colDeadline.setCellValueFactory(cellData -> {
            Task task = cellData.getValue().getValue();
            if (task != null && task.getDate() != null && !task.getDate().isEmpty()) {
                String deadlineStr = task.getDate();
                if (task.getTime() != null && !task.getTime().isEmpty()) {
                    deadlineStr += " " + task.getTime();
                }
                return new SimpleStringProperty(deadlineStr);
            }
            return new SimpleStringProperty("");
        });
        colDeadline.setCellFactory(column -> createInheritedDataCell(task -> {
            if (task.getDate() != null && !task.getDate().isEmpty()) {
                String deadlineStr = task.getDate();
                if (task.getTime() != null && !task.getTime().isEmpty()) {
                    deadlineStr += " " + task.getTime();
                }
                return deadlineStr;
            }
            return "";
        }));

        colPriority.setCellValueFactory(new TreeItemPropertyValueFactory<>("priority"));
        colPriority.setCellFactory(column -> createInheritedDataCell(Task::getPriority));

        colProgress.setCellValueFactory(new TreeItemPropertyValueFactory<>("progress"));
        colProgress.setCellFactory(column -> new TreeTableCell<>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                TreeItem<Task> treeItem = getTreeTableRow() != null ? getTreeTableRow().getTreeItem() : null;

                if (empty || item == null || treeItem == null || treeItem.getValue() == null) {
                    setGraphic(null);
                    return;
                }

                if (treeItem.getParent() != null && treeItem.getParent().getParent() != null) {
                    setGraphic(null);
                } else {
                    ProgressBar progressBar = new ProgressBar(item / 100.0);
                    Label progressText = new Label(String.format("%d%%", item));
                    HBox progressPane = new HBox(5, progressBar, progressText);
                    progressPane.setAlignment(Pos.CENTER_LEFT);
                    progressBar.setMinWidth(60);
                    setGraphic(progressPane);
                }
            }
        });

        colStatus.setCellValueFactory(new TreeItemPropertyValueFactory<>("statusDisplay"));
        setupAttachmentColumn();
        setupActionButtonsWithIkonli();
    }

    private <T> TreeTableCell<Task, T> createInheritedDataCell(java.util.function.Function<Task, T> propertyExtractor) {
        return new TreeTableCell<>() {
            @Override
            protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);
                TreeItem<Task> treeItem = getTreeTableRow() != null ? getTreeTableRow().getTreeItem() : null;

                if (empty || treeItem == null || treeItem.getValue() == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    if (treeItem.getParent() != null && treeItem.getParent().getParent() != null) {
                        setText("—");
                        setGraphic(null);
                        setAlignment(Pos.CENTER);
                    } else {
                        setText(item != null ? item.toString() : "");
                        setGraphic(null);
                        setAlignment(Pos.CENTER_LEFT);
                    }
                }
            }
        };
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
                        setOnMouseClicked(null);
                        setCursor(Cursor.DEFAULT);
                    } else {
                        attachmentIcon.setIconSize(16);
                        attachmentIcon.setIconColor(Color.SLATEGRAY);
                        setGraphic(attachmentIcon);
                        setText(null);
                        setTooltip(new Tooltip("Lihat lampiran: " + item));
                        setCursor(Cursor.HAND);
                        setOnMouseClicked(event -> {
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
        updateTaskSummary(allTasks);
    }

    private void updateTaskSummary(List<Task> tasks) {
        if (tasks == null || taskSummaryLabel == null) return;
        long upcomingCount = tasks.stream()
                .filter(task -> !task.isCompleted() && task.getDate() != null && !task.getDate().isEmpty())
                .filter(task -> {
                    try {
                        LocalDate deadline = LocalDate.parse(task.getDate());
                        return !deadline.isBefore(LocalDate.now()) && deadline.isBefore(LocalDate.now().plusDays(4));
                    } catch (Exception e) { return false; }
                })
                .count();
        if (upcomingCount > 0) {
            taskSummaryLabel.setText("Anda memiliki " + upcomingCount + " tugas yang mendekati deadline. Segera selesaikan ya!");
        } else {
            taskSummaryLabel.setText("Tidak ada tugas yang mendekati deadline saat ini. Bagus!");
        }
    }

    private void setupActionButtonsWithIkonli() {
        Callback<TreeTableColumn<Task, Void>, TreeTableCell<Task, Void>> cellFactory = param -> {
            final TreeTableCell<Task, Void> cell = new TreeTableCell<>() {
                private final FontIcon editIcon = new FontIcon(FontAwesomeSolid.PENCIL_ALT);
                private final FontIcon deleteIcon = new FontIcon(FontAwesomeSolid.TRASH_ALT);
                private final FontIcon addSubtaskIcon = new FontIcon(FontAwesomeSolid.PLUS_CIRCLE);
                private final Button completeButton = new Button();
                private HBox pane;

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
                        } else {
                            completeButton.setText("Selesaikan");
                            completeButton.setStyle("-fx-background-color: #28a745; -fx-text-fill: white;");
                        }

                        boolean isSubtask = currentTask.getParentId() != null;
                        if (isSubtask) {
                            pane = new HBox(8, editIcon, deleteIcon, completeButton);
                            editIcon.setDisable(currentTask.isCompleted());
                            editIcon.setIconColor(currentTask.isCompleted() ? Color.LIGHTGRAY : Color.ROYALBLUE);
                        } else {
                            pane = new HBox(8, editIcon, deleteIcon, addSubtaskIcon, completeButton);
                            editIcon.setDisable(currentTask.isCompleted());
                            addSubtaskIcon.setDisable(currentTask.isCompleted());
                            editIcon.setIconColor(currentTask.isCompleted() ? Color.LIGHTGRAY : Color.ROYALBLUE);
                            addSubtaskIcon.setIconColor(currentTask.isCompleted() ? Color.LIGHTGRAY : Color.FORESTGREEN);
                        }

                        deleteIcon.setIconColor(Color.CRIMSON);
                        pane.setAlignment(javafx.geometry.Pos.CENTER);
                        setGraphic(pane);
                    }
                }
            };
            return cell;
        };
        colAction.setCellFactory(cellFactory);
    }

    private void handleAddSubTask(Task parentTask) {
        if (currentUserId == null) {
            showErrorDialog("Error Sesi", "Sesi pengguna tidak valid.");
            return;
        }
        try {
            if (addSubtasksStage == null || !addSubtasksStage.isShowing()) {
                FXMLLoader loader = new FXMLLoader(App.class.getResource("/com/example/project/fxml/add_subtasks_dialog.fxml"));
                Parent root = loader.load();

                AddSubtasksController controller = loader.getController();
                controller.setParentTask(parentTask);

                addSubtasksStage = new Stage();
                addSubtasksStage.initModality(Modality.APPLICATION_MODAL);
                addSubtasksStage.setTitle("Tambah Sub-Tugas");

                Scene scene = new Scene(root);
                URL cssUrl = App.class.getResource("/com/example/project/css/style.css");
                if (cssUrl != null) {
                    scene.getStylesheets().add(cssUrl.toExternalForm());
                }
                addSubtasksStage.setScene(scene);

                addSubtasksStage.setOnHiding(event -> {
                    checkAndUpdateParentTaskProgress(parentTask.getId());
                    loadTasksAndBuildTree();
                    addSubtasksStage = null;
                });
                addSubtasksStage.show();
            } else {
                addSubtasksStage.toFront();
            }
        } catch (IOException e) {
            e.printStackTrace();
            showErrorDialog("Gagal Memuat Form", "Tidak dapat memuat tampilan untuk menambah sub-tugas.");
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

        List<Task> descendants = new ArrayList<>();
        collectAllDescendants(task, descendants);
        descendants.add(task);

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Konfirmasi Hapus");
        alert.setHeaderText(null);
        alert.setContentText("Apakah Anda yakin ingin menghapus tugas '" + task.getName() + "' dan semua sub-tugasnya?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            Integer parentIdOfDeletedTask = task.getParentId();

            boolean success = Database.deleteTask(task.getId(), currentUserId);

            if (success) {
                for (Task descendant : descendants) {
                    String storedAttachmentName = descendant.getAttachmentStoredName();
                    if (storedAttachmentName != null && !storedAttachmentName.isEmpty()) {
                        try {
                            Files.deleteIfExists(Paths.get("data/attachments/" + storedAttachmentName));
                        } catch (IOException e) {
                            e.printStackTrace();
                            showErrorDialog("Error Hapus File", "Gagal menghapus file lampiran: " + descendant.getAttachmentOriginalName());
                        }
                    }
                }
                checkAndUpdateParentTaskProgress(parentIdOfDeletedTask);
                loadTasksAndBuildTree();
            } else {
                showErrorDialog("Gagal Hapus", "Tugas gagal dihapus dari database.");
            }
        }
    }

    private void collectAllDescendants(Task parent, List<Task> descendantList) {
        List<Task> children = Database.getSubTasks(parent.getId(), this.currentUserId);
        for (Task child : children) {
            descendantList.add(child);
            collectAllDescendants(child, descendantList);
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
    private void onAddTask(ActionEvent event) {
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

                addTaskStage.setOnHiding(e -> loadTasksAndBuildTree());
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
    private void onShowAll(ActionEvent event) {
        loadTasksAndBuildTree();
    }

    @FXML
    private void onShowPending(ActionEvent event) {
        if (currentUserId == null) return;
        List<Task> filteredList = Database.getTasksByCompletion(currentUserId, false);
        buildTreeFromList(filteredList);
    }

    @FXML
    private void onShowCompleted(ActionEvent event) {
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

    private void handleOpenAttachment(Task task) {
        if (hostServices == null) {
            showErrorDialog("Error Aplikasi", "Tidak dapat membuka file lampiran (HostServices tidak tersedia).");
            return;
        }
        if (task.getAttachmentStoredName() != null && !task.getAttachmentStoredName().isEmpty()) {
            File fileToOpen = new File("data/attachments/" + task.getAttachmentStoredName());
            if (fileToOpen.exists()) {
                this.hostServices.showDocument(fileToOpen.toURI().toString());
            } else {
                showErrorDialog("File Tidak Ditemukan", "File lampiran '" + task.getAttachmentOriginalName() + "' tidak ditemukan di folder attachments.");
            }
        } else {
            System.out.println("Tidak ada lampiran untuk tugas: " + task.getName());
        }
    }
}
