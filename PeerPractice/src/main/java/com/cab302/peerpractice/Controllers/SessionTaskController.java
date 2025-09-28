package com.cab302.peerpractice.Controllers;

import com.cab302.peerpractice.AppContext;
import com.cab302.peerpractice.Model.Entities.Session;
import com.cab302.peerpractice.Model.Entities.SessionTask;
import com.cab302.peerpractice.Model.Entities.User;
import com.cab302.peerpractice.Navigation;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * Controller for managing session tasks. Handles creating, editing, and deleting
 * tasks within study sessions with proper validation.
 */
public class SessionTaskController extends SidebarController {
    
    @FXML private TableView<SessionTask> taskTable;
    @FXML private TableColumn<SessionTask, String> titleColumn;
    @FXML private TableColumn<SessionTask, String> assigneeColumn;
    @FXML private TableColumn<SessionTask, LocalDateTime> deadlineColumn;
    @FXML private TableColumn<SessionTask, Boolean> completedColumn;
    
    @FXML private TextField taskTitleField;
    @FXML private TextField deadlineField;
    @FXML private ComboBox<User> assigneeComboBox;
    @FXML private Button backButton;
    @FXML private Button createTaskButton;
    @FXML private Button updateTaskButton;
    @FXML private Button deleteTaskButton;
    @FXML private Button markCompleteButton;
    @FXML private Label errorLabel;
    @FXML private Label sessionTitleLabel;
    
    private String currentSessionId;
    private SessionTask selectedTask;
    private ObservableList<SessionTask> taskList;
    
    public SessionTaskController(AppContext ctx, Navigation nav) { super(ctx, nav); }
    
    @FXML
    public void initialize() {
        super.initialize();
        setupTableColumns();
        setupEventHandlers();
        taskList = FXCollections.observableArrayList();
        taskTable.setItems(taskList);
    }
    
    /**
     * Sets up the task table columns with proper cell value factories
     */
    private void setupTableColumns() {
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        deadlineColumn.setCellValueFactory(new PropertyValueFactory<>("deadline"));
        completedColumn.setCellValueFactory(new PropertyValueFactory<>("completed"));

        assigneeColumn.setCellValueFactory(cellData -> {
            String assigneeId = cellData.getValue().getAssigneeId();
            User assignee = findUserById(assigneeId);
            return new javafx.beans.property.SimpleStringProperty(
                assignee != null ? assignee.getUsername() : "Unknown User"
            );
        });
        
        // format the deadline column nicely
        deadlineColumn.setCellFactory(column -> new TableCell<SessionTask, LocalDateTime>() {
            @Override
            protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
                }
            }
        });
        
        // checkbox for completed column
        completedColumn.setCellFactory(column -> new TableCell<SessionTask, Boolean>() {
            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    CheckBox checkBox = new CheckBox();
                    checkBox.setSelected(item);
                    checkBox.setDisable(true); // read-only, use button to mark complete
                    setGraphic(checkBox);
                }
            }
        });
    }
    
    /**
     * Sets up event handlers for buttons and table selection
     */
    private void setupEventHandlers() {
        // table selection handler
        taskTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            selectedTask = newSelection;
            updateButtonStates();
            if (newSelection != null) {
                populateFormFields(newSelection);
            } else {
                clearFormFields();
            }
        });
        
        // button handlers
        createTaskButton.setOnAction(e -> createTask());
        updateTaskButton.setOnAction(e -> updateTask());
        deleteTaskButton.setOnAction(e -> deleteTask());
        markCompleteButton.setOnAction(e -> markTaskComplete());
        
        // enable/disable buttons based on selection
        updateButtonStates();
    }

    /**
     * Sets the current session and loads its tasks
     */
    public void setSession(String sessionId) {
        this.currentSessionId = sessionId;
        
        Session session = ctx.getSessionManager().findSessionById(sessionId);
        if (session != null) {
            sessionTitleLabel.setText("Tasks for: " + session.getTitle());
            populateAssigneeComboBox(session.getParticipants());
            loadSessionTasks();
        }
    }
    
    /**
     * Loads tasks for the current session
     */
    private void loadSessionTasks() {
        if (currentSessionId != null) {
            List<SessionTask> tasks = ctx.getSessionTaskManager().getSessionTasks(currentSessionId);
            taskList.clear();
            taskList.addAll(tasks);
        }
    }
    
    /**
     * Populates the assignee combo box with session participants
     */
    private void populateAssigneeComboBox(List<User> participants) {
        assigneeComboBox.getItems().clear();
        assigneeComboBox.getItems().addAll(participants);
        
        // custom cell factory to show username
        assigneeComboBox.setCellFactory(listView -> new ListCell<User>() {
            @Override
            protected void updateItem(User user, boolean empty) {
                super.updateItem(user, empty);
                setText(empty || user == null ? null : user.getUsername());
            }
        });
        
        // also set the button cell to show username
        assigneeComboBox.setButtonCell(new ListCell<User>() {
            @Override
            protected void updateItem(User user, boolean empty) {
                super.updateItem(user, empty);
                setText(empty || user == null ? null : user.getUsername());
            }
        });
    }
    
    /**
     * Creates a new task
     */
    @FXML
    private void createTask() {
        clearErrorMessage();
        
        try {
            String title = taskTitleField.getText().trim();
            String deadlineStr = deadlineField.getText().trim();
            User assignee = assigneeComboBox.getValue();
            
            // validation
            if (title.isEmpty()) {
                showError("Task title is required");
                return;
            }
            
            if (deadlineStr.isEmpty()) {
                showError("Deadline is required");
                return;
            }
            
            if (assignee == null) {
                showError("Please select an assignee");
                return;
            }
            
            // parse deadline
            LocalDateTime deadline;
            try {
                deadline = LocalDateTime.parse(deadlineStr, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
            } catch (DateTimeParseException e) {
                showError("Invalid deadline format. Use: yyyy-MM-dd HH:mm");
                return;
            }
            
            // create task with optimistic UI update
            String currentUserId = getCurrentUserId();

            // create temporary task for optimistic update
            SessionTask tempTask = new SessionTask(currentSessionId, title, deadline, assignee.getUserId(), currentUserId);

            // disable button to prevent double submission
            createTaskButton.setDisable(true);
            createTaskButton.setText("Saving...");

            // optimistic add to UI
            taskList.add(tempTask);

            try {
                SessionTask savedTask = ctx.getSessionTaskManager().createTask(
                    currentSessionId, title, deadline, assignee.getUserId(), currentUserId
                );

                // replace temp task with saved task
                int tempIndex = taskList.indexOf(tempTask);
                if (tempIndex >= 0) {
                    taskList.set(tempIndex, savedTask);
                }

                clearFormFields();
            } catch (Exception ex) {
                // rollback optimistic update on error
                taskList.remove(tempTask);
                showError("Failed to create task: " + ex.getMessage());
            } finally {
                // re-enable button
                createTaskButton.setDisable(false);
                createTaskButton.setText("Create Task");
            }
            
        } catch (IllegalArgumentException e) {
            showError(e.getMessage());
        } catch (Exception e) {
            showError("Failed to create task: " + e.getMessage());
        }
    }
    
    /**
     * Updates the selected task
     */
    @FXML
    private void updateTask() {
        if (selectedTask == null) return;
        
        clearErrorMessage();
        
        try {
            String title = taskTitleField.getText().trim();
            String deadlineStr = deadlineField.getText().trim();
            User assignee = assigneeComboBox.getValue();
            
            // validation
            if (title.isEmpty()) {
                showError("Task title is required");
                return;
            }
            
            if (deadlineStr.isEmpty()) {
                showError("Deadline is required");
                return;
            }
            
            if (assignee == null) {
                showError("Please select an assignee");
                return;
            }
            
            // parse deadline
            LocalDateTime deadline;
            try {
                deadline = LocalDateTime.parse(deadlineStr, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
            } catch (DateTimeParseException e) {
                showError("Invalid deadline format. Use: yyyy-MM-dd HH:mm");
                return;
            }
            
            // update task with optimistic UI update
            String currentUserId = getCurrentUserId();

            // store original values for rollback
            String originalTitle = selectedTask.getTitle();
            LocalDateTime originalDeadline = selectedTask.getDeadline();
            String originalAssigneeId = selectedTask.getAssigneeId();

            // disable button to prevent double submission
            updateTaskButton.setDisable(true);
            updateTaskButton.setText("Updating...");

            // optimistic update in UI
            int taskIndex = taskList.indexOf(selectedTask);
            if (taskIndex >= 0) {
                SessionTask updatedTask = new SessionTask(
                    selectedTask.getTaskId(),
                    currentSessionId,
                    title,
                    deadline,
                    assignee.getUserId(),
                    currentUserId,
                    selectedTask.getCreatedAt(),
                    selectedTask.isCompleted()
                );
                taskList.set(taskIndex, updatedTask);
                selectedTask = updatedTask;
            }

            try {
                SessionTask savedTask = ctx.getSessionTaskManager().updateTask(
                    selectedTask.getTaskId(), title, deadline, assignee.getUserId(), currentUserId
                );

                // replace with server response
                if (taskIndex >= 0) {
                    taskList.set(taskIndex, savedTask);
                }

                clearFormFields();
            } catch (Exception ex) {
                // rollback optimistic update on error
                if (taskIndex >= 0) {
                    SessionTask rolledBackTask = new SessionTask(
                        selectedTask.getTaskId(),
                        currentSessionId,
                        originalTitle,
                        originalDeadline,
                        originalAssigneeId,
                        selectedTask.getCreatedBy(), // use original createdBy
                        selectedTask.getCreatedAt(),
                        selectedTask.isCompleted()
                    );
                    taskList.set(taskIndex, rolledBackTask);
                    selectedTask = rolledBackTask;
                }
                showError("Failed to update task: " + ex.getMessage());
            } finally {
                // re-enable button
                updateTaskButton.setDisable(false);
                updateTaskButton.setText("Update Task");
            }
            
        } catch (IllegalArgumentException e) {
            showError(e.getMessage());
        } catch (Exception e) {
            showError("Failed to update task: " + e.getMessage());
        }
    }
    
    /**
     * Deletes the selected task
     */
    @FXML
    private void deleteTask() {
        if (selectedTask == null) return;
        
        // confirm deletion
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Task");
        alert.setHeaderText("Delete Task");
        alert.setContentText("Are you sure you want to delete this task?");
        
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    String currentUserId = getCurrentUserId();
                    boolean deleted = ctx.getSessionTaskManager().deleteTask(selectedTask.getTaskId(), currentUserId);
                    
                    if (deleted) {
                        loadSessionTasks();
                        clearFormFields();
                    } else {
                        showError("Failed to delete task");
                    }
                } catch (IllegalArgumentException e) {
                    showError(e.getMessage());
                } catch (Exception e) {
                    showError("Failed to delete task: " + e.getMessage());
                }
            }
        });
    }
    
    /**
     * Marks the selected task as complete
     */
    @FXML
    private void markTaskComplete() {
        if (selectedTask == null) return;
        
        try {
            String currentUserId = getCurrentUserId();

            // disable button to prevent double submission
            markCompleteButton.setDisable(true);
            markCompleteButton.setText("Completing...");

            // optimistic update in UI
            int taskIndex = taskList.indexOf(selectedTask);
            SessionTask originalTask = selectedTask;
            if (taskIndex >= 0) {
                // create updated task with completed status using 8-parameter constructor
                SessionTask completedTask = new SessionTask(
                    selectedTask.getTaskId(),
                    currentSessionId,
                    selectedTask.getTitle(),
                    selectedTask.getDeadline(),
                    selectedTask.getAssigneeId(),
                    selectedTask.getCreatedBy(),
                    selectedTask.getCreatedAt(),
                    true // mark as completed
                );

                taskList.set(taskIndex, completedTask);
                selectedTask = completedTask;
            }

            boolean completed = ctx.getSessionTaskManager().markTaskCompleted(selectedTask.getTaskId(), currentUserId);

            if (completed) {
                clearFormFields();
            } else {
                // rollback optimistic update
                if (taskIndex >= 0) {
                    taskList.set(taskIndex, originalTask);
                    selectedTask = originalTask;
                }
                showError("Failed to mark task as complete");
            }
        } catch (IllegalArgumentException e) {
            showError(e.getMessage());
        } catch (Exception e) {
            showError("Failed to mark task as complete: " + e.getMessage());
        } finally {
            // re-enable button
            markCompleteButton.setDisable(false);
            markCompleteButton.setText("Mark Complete");
        }
    }
    
    /**
     * Populates form fields with selected task data
     */
    private void populateFormFields(SessionTask task) {
        taskTitleField.setText(task.getTitle());
        deadlineField.setText(task.getDeadline().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
        
        // find and select the assignee
        User assignee = findUserById(task.getAssigneeId());
        if (assignee != null) {
            assigneeComboBox.setValue(assignee);
        }
    }
    
    /**
     * Clears all form fields
     */
    private void clearFormFields() {
        taskTitleField.clear();
        deadlineField.clear();
        assigneeComboBox.setValue(null);
        selectedTask = null;
        updateButtonStates();
    }
    
    /**
     * Updates button states based on selection and user permissions
     */
    private void updateButtonStates() {
        boolean hasSelection = selectedTask != null;
        
        updateTaskButton.setDisable(!hasSelection);
        deleteTaskButton.setDisable(!hasSelection);
        markCompleteButton.setDisable(!hasSelection || (hasSelection && selectedTask.isCompleted()));
    }
    
    /**
     * Shows an error message
     */
    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }
    
    /**
     * Clears the error message
     */
    private void clearErrorMessage() {
        errorLabel.setText("");
        errorLabel.setVisible(false);
    }

    

    /**
     * Gets the current user ID from the session
     */
    private String getCurrentUserId() {
        User currentUser = ctx.getUserSession().getCurrentUser();
        return currentUser != null ? currentUser.getUserId() : null;
    }

    /**
     * Helper method to find a user by ID
     */
    private User findUserById(String userId) {
        if (userId == null) return null;
        try {
            return ctx.getUserDAO().findUserById(userId);
        } catch (Exception e) {
            return null;
        }
    }
}
