package com.example.project.controller;

import com.example.project.App;
import com.example.project.model.Database;
import com.example.project.model.Task;
import javafx.application.HostServices;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.TreeItemPropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

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

    @FXML private HBox welcomeSection;
    @FXML private Label avatarInitialLabel;
    @FXML private Label greetingLabel;
    @FXML private Label welcomeMessageLabel;

    @FXML private HBox statsCardBox;
    @FXML private Label totalStatLabel;
    @FXML private Label doneStatLabel;
    @FXML private Label activeStatLabel;
    @FXML private Label nextTaskLabel;

    @FXML private VBox sidebar;
    @FXML private Button navDashboardButton;
    @FXML private Button navListTasksButton;
    @FXML private Button navAddTaskButton;
    @FXML private Button navCompletedTasksButton;

    private static Stage addTaskStage = null;
    private static Stage addSubtasksStage = null;
    private Integer currentUserId;
    private String currentUserName;
    private HostServices hostServices;
    private String activeView = "dashboard";


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

        colDeadline.setCellValueFactory(cellData -> {
            Task task = cellData.getValue().getValue();
            if (task == null) {
                return new SimpleStringProperty("");
            }
            String date = task.getDate() != null ? task.getDate() : "";
            String time = task.getTime() != null && !task.getTime().isEmpty() ? " pukul " + task.getTime() : "";
            return new SimpleStringProperty(date + time);
        });

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
        activeView = "dashboard";
        updateViewVisibility(true);
        setActiveSidebarButton(navDashboardButton);
        if (currentUserId == null) return;

        List<Task> allTasks = Database.getAllTasks(currentUserId);
        updateDashboardOverview(allTasks);

        List<Task> incompleteTasks = allTasks.stream()
                .filter(task -> !task.isCompleted())
                .collect(Collectors.toList());

        buildTreeFromList(incompleteTasks);
    }

    @FXML
    private void onShowAll(ActionEvent event) {
        activeView = "all";
        updateViewVisibility(false);
        setActiveSidebarButton(navListTasksButton);
        loadAndBuildAllTasks();
    }

    @FXML
    private void onShowCompleted(ActionEvent event) {
        activeView = "completed";
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
            }
        }
        if (activeButton != null) {
            activeButton.getStyleClass().add("sidebar-button-active");
        }
    }

    private void loadAndBuildAllTasks() {
        if (currentUserId == null) return;
        List<Task> allTasks = Database.getAllTasks(currentUserId);
        updateDashboardOverview(allTasks);
        buildTreeFromList(allTasks);
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

        taskTreeTable.setRowFactory(tv -> new TreeTableRow<>() {
            @Override
            protected void updateItem(Task item, boolean empty) {
                super.updateItem(item, empty);
                getStyleClass().remove("parent-task-row");
                if (!empty && item != null && item.getParentId() == null) {
                    getStyleClass().add("parent-task-row");
                }
            }
        });
    }

    private void updateDashboardOverview(List<Task> tasks) {
        if (tasks == null) {
            return;
        }
        List<Task> mainTasks = tasks.stream().filter(t -> t.getParentId() == null).collect(Collectors.toList());
        long totalCount = mainTasks.size();
        long doneCount = mainTasks.stream().filter(Task::isCompleted).count();
        long activeCount = totalCount - doneCount;

        totalStatLabel.setText(String.valueOf(totalCount));
        doneStatLabel.setText(String.valueOf(doneCount));
        activeStatLabel.setText(String.valueOf(activeCount));

        List<Task> incompleteTasks = tasks.stream()
                .filter(t -> !t.isCompleted() && t.getDate() != null && !t.getDate().isEmpty())
                .collect(Collectors.toList());

        if (incompleteTasks.isEmpty()) {
            nextTaskLabel.setText("Tidak ada tugas mendatang.");
        } else {
            LocalDate nearestDate = incompleteTasks.stream()
                    .map(t -> LocalDate.parse(t.getDate()))
                    .min(LocalDate::compareTo)
                    .orElse(null);

            if (nearestDate != null) {
                List<Task> tasksOnNearestDate = incompleteTasks.stream()
                        .filter(t -> LocalDate.parse(t.getDate()).isEqual(nearestDate))
                        .collect(Collectors.toList());

                if (tasksOnNearestDate.size() > 1) {
                    String taskNames = tasksOnNearestDate.stream()
                            .map(Task::getName)
                            .collect(Collectors.joining(", "));
                    nextTaskLabel.setText(nearestDate.toString() + " - Tugas: " + taskNames);
                } else if (!tasksOnNearestDate.isEmpty()) {
                    nextTaskLabel.setText(nearestDate.toString() + " - " + tasksOnNearestDate.get(0).getName());
                } else {
                    nextTaskLabel.setText("Tidak ada tugas mendatang.");
                }
            } else {
                nextTaskLabel.setText("Tidak ada tugas mendatang.");
            }
        }
    }

    private void handleSessionError() {
        showErrorDialog("Error Sesi Pengguna", "Sesi pengguna tidak ditemukan. Silakan login ulang.");
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

                    Button completeButton = new Button(task.isCompleted() ? "Batal" : "Selesai");
                    completeButton.getStyleClass().add("complete-button");
                    completeButton.getStyleClass().add(task.isCompleted() ? "completed" : "pending");
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
        refreshActiveView();
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
                    refreshActiveView();
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
            if (task.getParentId() != null) {
                checkAndUpdateParentTaskProgress(task.getParentId());
            }
            refreshActiveView();
        } catch (IOException e) {
            e.printStackTrace();
            showErrorDialog("Gagal Memuat Form Edit", "Gagal memuat form edit tugas.");
        }
    }

    private void refreshActiveView() {
        switch(activeView) {
            case "dashboard":
                onShowDashboard(null);
                break;
            case "completed":
                onShowCompleted(null);
                break;
            case "all":
            default:
                loadAndBuildAllTasks();
                break;
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
                if (task.getParentId() != null) {
                    checkAndUpdateParentTaskProgress(task.getParentId());
                }
                refreshActiveView();
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
                    refreshActiveView();
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
