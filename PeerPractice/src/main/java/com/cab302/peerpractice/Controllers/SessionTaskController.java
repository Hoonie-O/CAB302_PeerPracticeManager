package com.cab302.peerpractice.Controllers;

import com.cab302.peerpractice.AppContext;
import com.cab302.peerpractice.Navigation;
import com.cab302.peerpractice.Model.*;
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
public class SessionTaskController extends BaseController {
    
    @FXML private TableView<SessionTask> taskTable;
    @FXML private TableColumn<SessionTask, String> titleColumn;
    @FXML private TableColumn<SessionTask, String> assigneeColumn;
    @FXML private TableColumn<SessionTask, LocalDateTime> deadlineColumn;
    @FXML private TableColumn<SessionTask, Boolean> completedColumn;
    
    @FXML private TextField taskTitleField;
    @FXML private TextField deadlineField;
    @FXML private ComboBox<User> assigneeComboBox;
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
    private void initialize() {
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
            
            // create task
            String currentUserId = getCurrentUserId();
            SessionTask task = ctx.getSessionTaskManager().createTask(
                currentSessionId, title, deadline, assignee.getUserId(), currentUserId
            );
            
            // refresh task list
            loadSessionTasks();
            clearFormFields();
            
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
            
            // update task
            String currentUserId = getCurrentUserId();
            ctx.getSessionTaskManager().updateTask(
                selectedTask.getTaskId(), title, deadline, assignee.getUserId(), currentUserId
            );
            
            // refresh task list
            loadSessionTasks();
            clearFormFields();
            
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
            boolean completed = ctx.getSessionTaskManager().markTaskCompleted(selectedTask.getTaskId(), currentUserId);
            
            if (completed) {
                loadSessionTasks();
                clearFormFields();
            } else {
                showError("Failed to mark task as complete");
            }
        } catch (IllegalArgumentException e) {
            showError(e.getMessage());
        } catch (Exception e) {
            showError("Failed to mark task as complete: " + e.getMessage());
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
            return ctx.getUserDao().findUserById(userId);
        } catch (Exception e) {
            return null;
        }
    }
}
