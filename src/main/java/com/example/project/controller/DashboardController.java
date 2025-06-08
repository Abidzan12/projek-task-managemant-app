package com.example.project.controller;

import com.example.project.App;
import com.example.project.model.Database;
import com.example.project.model.Task;
import javafx.application.HostServices;
import javafx.application.Platform; // <-- IMPORT BARU
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.TreeItemPropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class DashboardController {

    // --- FXML Fields ---
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

    // --- Elemen UI Header ---
    @FXML private HBox welcomeSection;
    @FXML private Label avatarInitialLabel;
    @FXML private Label greetingLabel;
    @FXML private Label welcomeMessageLabel;

    // --- Elemen UI Kartu Statistik ---
    @FXML private HBox statsCardBox;
    @FXML private Label totalStatLabel;
    @FXML private Label doneStatLabel;
    @FXML private Label activeStatLabel;
    @FXML private Label nextTaskLabel;

    // --- Tombol Sidebar ---
    @FXML private VBox sidebar;
    @FXML private Button navDashboardButton;
    @FXML private Button navListTasksButton;
    @FXML private Button navAddTaskButton;
    @FXML private Button navCompletedTasksButton;

    // --- Variabel Kelas ---
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
            handleSessionError();
            return;
        }

        if (taskTreeTable != null) {
            taskTreeTable.setColumnResizePolicy(TreeTableView.CONSTRAINED_RESIZE_POLICY);
            taskTreeTable.setShowRoot(false);
        }

        loadUserInfo();
        setupSidebarIcons();
        setupColumns();

        // ===== PERBAIKAN BUG #3 (DATA TIDAK LANGSUNG MUNCUL) =====
        // Membungkus pemanggilan data awal dengan Platform.runLater()
        // Ini memastikan UI sudah siap sepenuhnya sebelum mencoba memuat data.
        Platform.runLater(() -> onShowDashboard(null));
    }

    private void loadUserInfo() {
        this.currentUserName = Database.getUserNameById(this.currentUserId);
        if (this.currentUserName == null || this.currentUserName.isEmpty()) {
            this.currentUserName = "Pengguna";
        }

        if (avatarInitialLabel != null) {
            avatarInitialLabel.setText(String.valueOf(this.currentUserName.charAt(0)).toUpperCase());
        }
        if (greetingLabel != null) {
            greetingLabel.setText("Welcome, " + this.currentUserName + " !");
        }
        if (welcomeMessageLabel != null) {
            welcomeMessageLabel.setText("You're almost there. Keep it up!");
        }
    }

    private void setupSidebarIcons() {
        navDashboardButton.setGraphic(new FontIcon(FontAwesomeSolid.HOME));
        navListTasksButton.setGraphic(new FontIcon(FontAwesomeSolid.LIST_ALT));
        navAddTaskButton.setGraphic(new FontIcon(FontAwesomeSolid.PLUS_SQUARE));
        navCompletedTasksButton.setGraphic(new FontIcon(FontAwesomeSolid.CHECK_SQUARE));
    }

    private void setupColumns() {
        colName.setCellValueFactory(new TreeItemPropertyValueFactory<>("name"));
        colDescription.setCellValueFactory(new TreeItemPropertyValueFactory<>("description"));
        colCourse.setCellValueFactory(new TreeItemPropertyValueFactory<>("course"));
        colDeadline.setCellValueFactory(cellData -> new SimpleStringProperty((cellData.getValue().getValue() != null && cellData.getValue().getValue().getDate() != null) ? cellData.getValue().getValue().getDate() : ""));
        colStatus.setCellValueFactory(new TreeItemPropertyValueFactory<>("statusDisplay"));
        colProgress.setCellValueFactory(new TreeItemPropertyValueFactory<>("progress"));

        colPriority.setCellValueFactory(new TreeItemPropertyValueFactory<>("priority"));
        colPriority.setCellFactory(column -> new TreeTableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    Label priorityLabel = new Label(item);
                    priorityLabel.getStyleClass().add("priority-label");
                    priorityLabel.getStyleClass().removeAll("priority-high", "priority-medium", "priority-low");

                    switch (item.toLowerCase()) {
                        case "tinggi":
                            priorityLabel.getStyleClass().add("priority-high");
                            break;
                        case "sedang":
                            priorityLabel.getStyleClass().add("priority-medium");
                            break;
                        case "rendah":
                            priorityLabel.getStyleClass().add("priority-low");
                            break;
                    }
                    setText(null);
                    setGraphic(priorityLabel);
                }
            }
        });

        setupActionButtonsWithIkonli();
        setupAttachmentColumn();
    }

    @FXML
    private void onShowDashboard(ActionEvent event) {
        updateViewVisibility(true);
        setActiveSidebarButton(navDashboardButton);
        loadTasksAndBuildTree();
    }

    @FXML
    private void onShowAll(ActionEvent event) {
        updateViewVisibility(false);
        setActiveSidebarButton(navListTasksButton);
        loadTasksAndBuildTree();
    }

    @FXML
    private void onShowCompleted(ActionEvent event) {
        if (currentUserId == null) return;
        updateViewVisibility(false);
        setActiveSidebarButton(navCompletedTasksButton);
        List<Task> filteredList = Database.getTasksByCompletion(currentUserId, true);
        buildTreeFromList(filteredList);
    }

    private void updateViewVisibility(boolean isDashboard) {
        statsCardBox.setVisible(isDashboard);
        statsCardBox.setManaged(isDashboard);
        if (nextTaskLabel.getParent() != null) {
            nextTaskLabel.getParent().setVisible(isDashboard);
            nextTaskLabel.getParent().setManaged(isDashboard);
        }
    }

    private void setActiveSidebarButton(Button activeButton) {
        if (sidebar == null) return;
        for (Node node : sidebar.getChildren()) {
            if (node instanceof Button) {
                node.getStyleClass().remove("sidebar-button-active");
                if (!node.getStyleClass().contains("sidebar-button")) {
                    node.getStyleClass().add("sidebar-button");
                }
            }
        }
        if (activeButton != null) {
            activeButton.getStyleClass().remove("sidebar-button");
            activeButton.getStyleClass().add("sidebar-button-active");
        }
    }

    private void loadTasksAndBuildTree() {
        if (currentUserId == null) return;
        List<Task> allTasks = Database.getAllTasks(currentUserId);
        updateDashboardOverview(allTasks);
        buildTreeFromList(allTasks);
    }

    private void updateDashboardOverview(List<Task> tasks) {
        if (tasks == null) return;
        List<Task> mainTasks = tasks.stream().filter(t -> t.getParentId() == null).collect(Collectors.toList());
        long totalCount = mainTasks.size();
        long doneCount = mainTasks.stream().filter(Task::isCompleted).count();
        long activeCount = totalCount - doneCount;

        totalStatLabel.setText(String.valueOf(totalCount));
        doneStatLabel.setText(String.valueOf(doneCount));
        activeStatLabel.setText(String.valueOf(activeCount));

        Optional<Task> nextTask = tasks.stream()
                .filter(t -> !t.isCompleted() && t.getDate() != null && !t.getDate().isEmpty())
                .min(Comparator.comparing(t -> LocalDate.parse(t.getDate())));
        nextTaskLabel.setText(nextTask.map(task -> task.getDate() + " - " + task.getName()).orElse("Tidak ada tugas mendatang."));
    }

    private void buildTreeFromList(List<Task> taskList) {
        if (taskTreeTable == null) return;
        Map<Integer, TreeItem<Task>> taskMap = new HashMap<>();
        TreeItem<Task> rootItem = new TreeItem<>();
        rootItem.setExpanded(true);
        taskList.forEach(task -> taskMap.put(task.getId(), new TreeItem<>(task)));
        taskList.forEach(task -> {
            TreeItem<Task> currentItem = taskMap.get(task.getId());
            if (task.getParentId() != null && taskMap.containsKey(task.getParentId())) {
                taskMap.get(task.getParentId()).getChildren().add(currentItem);
            } else {
                rootItem.getChildren().add(currentItem);
            }
            currentItem.setExpanded(true);
        });
        taskTreeTable.setRoot(rootItem);
    }

    private void handleSessionError() {
        showErrorDialog("Error Sesi Pengguna", "Tidak dapat memuat data tugas. Sesi pengguna tidak ditemukan. Silakan login ulang.");
        try {
            App.setRoot("login");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setupAttachmentColumn() {
        colAttachment.setCellValueFactory(new TreeItemPropertyValueFactory<>("attachmentOriginalName"));
        colAttachment.setCellFactory(param -> new TreeTableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(null);
                if (empty || item == null || item.isEmpty()) {
                    setGraphic(null);
                } else {
                    FontIcon icon = new FontIcon(FontAwesomeSolid.PAPERCLIP);
                    icon.setIconSize(16);
                    icon.setIconColor(Color.SLATEGRAY);
                    setGraphic(icon);
                    setTooltip(new Tooltip("Lihat lampiran: " + item));
                    setCursor(Cursor.HAND);
                    setOnMouseClicked(e -> handleOpenAttachment(getTreeTableRow().getItem()));
                }
            }
        });
    }

    private void setupActionButtonsWithIkonli() {
        colAction.setCellFactory(param -> new TreeTableCell<>() {
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTreeTableRow() == null || getTreeTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    HBox pane = new HBox(8);
                    pane.setAlignment(Pos.CENTER_LEFT);

                    Task task = getTreeTableRow().getItem();

                    Button completeButton = new Button();
                    completeButton.getStyleClass().add("complete-button");
                    if (task.isCompleted()) {
                        completeButton.setText("Batal");
                        completeButton.getStyleClass().add("completed");
                    } else {
                        completeButton.setText("Selesai");
                        completeButton.getStyleClass().add("pending");
                    }
                    completeButton.setOnAction(event -> handleToggleCompleteTask(getTreeTableRow().getItem()));

                    FontIcon deleteIcon = createActionIcon(FontAwesomeSolid.TRASH_ALT, "delete-icon", "Hapus Tugas");
                    deleteIcon.setOnMouseClicked(event -> handleDeleteTask(getTreeTableRow().getItem()));

                    if (task.getParentId() != null) {
                        pane.getChildren().addAll(deleteIcon, completeButton);
                    } else {
                        FontIcon editIcon = createActionIcon(FontAwesomeSolid.PENCIL_ALT, "edit-icon", "Edit Tugas");
                        FontIcon addSubtaskIcon = createActionIcon(FontAwesomeSolid.PLUS_CIRCLE, "add-subtask-icon", "Tambah Sub-Tugas");

                        editIcon.setOnMouseClicked(event -> handleEditTask(getTreeTableRow().getItem()));
                        addSubtaskIcon.setOnMouseClicked(event -> handleAddSubTask(getTreeTableRow().getItem()));

                        editIcon.setDisable(task.isCompleted());
                        addSubtaskIcon.setDisable(task.isCompleted());

                        pane.getChildren().addAll(editIcon, deleteIcon, addSubtaskIcon, completeButton);
                    }

                    setGraphic(pane);
                }
            }

            private FontIcon createActionIcon(FontAwesomeSolid icon, String styleClass, String tooltipText) {
                FontIcon fontIcon = new FontIcon(icon);
                fontIcon.getStyleClass().addAll("action-icon", styleClass);
                Tooltip.install(fontIcon, new Tooltip(tooltipText));
                return fontIcon;
            }
        });
    }

    private void handleToggleCompleteTask(Task task) {
        if (currentUserId == null || task == null) return;
        boolean newStatus = !task.isCompleted();
        int newProgress = newStatus ? 100 : 0;
        Database.updateTaskCompletion(task.getId(), newStatus, newProgress, currentUserId);
        if (newStatus && task.getParentId() == null) {
            List<Task> descendants = new ArrayList<>();
            collectAllDescendants(task, descendants);
            for (Task descendant : descendants) {
                if (!descendant.isCompleted()) {
                    Database.updateTaskCompletion(descendant.getId(), true, 100, currentUserId);
                }
            }
        }
        if (task.getParentId() != null) {
            checkAndUpdateParentTaskProgress(task.getParentId());
        }
        loadTasksAndBuildTree();
    }

    private void collectAllDescendants(Task parent, List<Task> descendantList) {
        if (currentUserId == null) return;
        List<Task> children = Database.getSubTasks(parent.getId(), this.currentUserId);
        for (Task child : children) {
            descendantList.add(child);
            collectAllDescendants(child, descendantList);
        }
    }

    private void handleAddSubTask(Task parentTask) {
        if (currentUserId == null) return;
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
                scene.getStylesheets().add(App.class.getResource("/com/example/project/css/style.css").toExternalForm());
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
            showErrorDialog("Gagal Memuat Form", "Gagal memuat form tambah sub-tugas.");
        }
    }

    private void handleEditTask(Task task) {
        if (currentUserId == null) return;
        try {
            FXMLLoader loader = new FXMLLoader(App.class.getResource("/com/example/project/fxml/add_task.fxml"));
            Parent root = loader.load();
            AddTaskController addTaskController = loader.getController();
            addTaskController.setEditTask(task);
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Edit Tugas");
            Scene scene = new Scene(root);
            scene.getStylesheets().add(App.class.getResource("/com/example/project/css/style.css").toExternalForm());
            stage.setScene(scene);
            stage.showAndWait();
            checkAndUpdateParentTaskProgress(task.getParentId());
            loadTasksAndBuildTree();
        } catch (IOException e) {
            e.printStackTrace();
            showErrorDialog("Gagal Memuat Form Edit", "Gagal memuat form edit tugas.");
        }
    }

    private void handleDeleteTask(Task task) {
        if (currentUserId == null) return;
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                "Yakin ingin menghapus tugas '" + task.getName() + "'?", ButtonType.YES, ButtonType.NO);
        alert.setTitle("Konfirmasi Hapus");
        alert.setHeaderText(null);
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                List<Task> descendants = new ArrayList<>();
                collectAllDescendants(task, descendants);
                for (Task descendant : descendants) {
                    Database.deleteTask(descendant.getId(), currentUserId);
                }
                Database.deleteTask(task.getId(), currentUserId);
                checkAndUpdateParentTaskProgress(task.getParentId());
                loadTasksAndBuildTree();
            }
        });
    }

    private void checkAndUpdateParentTaskProgress(Integer parentId) {
        if (parentId == null || currentUserId == null) return;
        List<Task> subTasks = Database.getSubTasks(parentId, currentUserId);
        if (subTasks.isEmpty()) {
            Database.updateTaskCompletion(parentId, false, 0, currentUserId);
            return;
        }
        long completedSubTasksCount = subTasks.stream().filter(Task::isCompleted).count();
        int parentProgress = (int) (((double) completedSubTasksCount / subTasks.size()) * 100);
        Database.updateTaskCompletion(parentId, parentProgress == 100, parentProgress, currentUserId);
    }

    @FXML
    private void onAddTask(ActionEvent event) {
        if (currentUserId == null) return;
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
                scene.getStylesheets().add(App.class.getResource("/com/example/project/css/style.css").toExternalForm());
                addTaskStage.setScene(scene);
                addTaskStage.setOnHiding(e -> {
                    loadTasksAndBuildTree();
                    addTaskStage = null;
                });
                addTaskStage.show();
            } else {
                addTaskStage.toFront();
            }
        } catch (IOException e) {
            e.printStackTrace();
            showErrorDialog("Gagal Memuat Form Tambah", "Gagal memuat form tambah tugas.");
        }
    }

    private void showErrorDialog(String title, String message) {
        Alert errorAlert = new Alert(Alert.AlertType.ERROR);
        errorAlert.setTitle(title);
        errorAlert.setHeaderText(null);
        errorAlert.setContentText(message);
        errorAlert.showAndWait();
    }

    private void handleOpenAttachment(Task task) {
        if (hostServices == null) return;
        if (task.getAttachmentStoredName() != null && !task.getAttachmentStoredName().isEmpty()) {
            File fileToOpen = new File("data/attachments/" + task.getAttachmentStoredName());
            if (fileToOpen.exists()) {
                this.hostServices.showDocument(fileToOpen.toURI().toString());
            } else {
                showErrorDialog("File Tidak Ditemukan", "File lampiran '" + task.getAttachmentOriginalName() + "' tidak ditemukan.");
            }
        }
    }
}