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
 * <hr>
 * Controller for managing session tasks within study sessions.
 *
 * <p>This controller handles the creation, editing, deletion, and completion tracking
 * of tasks assigned to session participants. It provides a table-based interface for
 * task management with real-time validation and optimistic UI updates.
 *
 * <p> Key features include:
 * <ul>
 *   <li>Task creation with title, deadline, and assignee assignment</li>
 *   <li>Real-time form validation and error handling</li>
 *   <li>Optimistic UI updates for better user experience</li>
 *   <li>Task completion tracking and status management</li>
 *   <li>Role-based access control for task operations</li>
 * </ul>
 *
 * @see SessionTask
 * @see Session
 * @see SidebarController
 */
public class SessionTaskController extends SidebarController {

    /** <hr> Table view for displaying session tasks. */
    @FXML private TableView<SessionTask> taskTable;
    /** <hr> Table column for task titles. */
    @FXML private TableColumn<SessionTask, String> titleColumn;
    /** <hr> Table column for task assignees. */
    @FXML private TableColumn<SessionTask, String> assigneeColumn;
    /** <hr> Table column for task deadlines. */
    @FXML private TableColumn<SessionTask, LocalDateTime> deadlineColumn;
    /** <hr> Table column for task completion status. */
    @FXML private TableColumn<SessionTask, Boolean> completedColumn;

    /** <hr> Input field for task title. */
    @FXML private TextField taskTitleField;
    /** <hr> Input field for task deadline. */
    @FXML private TextField deadlineField;
    /** <hr> Combo box for selecting task assignee. */
    @FXML private ComboBox<User> assigneeComboBox;
    /** <hr> Button to navigate back from task management. */
    @FXML private Button backButton;
    /** <hr> Button to create new tasks. */
    @FXML private Button createTaskButton;
    /** <hr> Button to update existing tasks. */
    @FXML private Button updateTaskButton;
    /** <hr> Button to delete selected tasks. */
    @FXML private Button deleteTaskButton;
    /** <hr> Button to mark tasks as complete. */
    @FXML private Button markCompleteButton;
    /** <hr> Label for displaying error messages. */
    @FXML private Label errorLabel;
    /** <hr> Label for displaying session title. */
    @FXML private Label sessionTitleLabel;

    /** <hr> The current session ID being managed. */
    private String currentSessionId;
    /** <hr> The currently selected task in the table. */
    private SessionTask selectedTask;
    /** <hr> Observable list of tasks for the current session. */
    private ObservableList<SessionTask> taskList;

    /**
     * <hr>
     * Constructs a new SessionTaskController with the specified context and navigation.
     *
     * @param ctx the application context providing access to user session and managers
     * @param nav the navigation controller for screen transitions
     */
    public SessionTaskController(AppContext ctx, Navigation nav) { super(ctx, nav); }

    /**
     * <hr>
     * Initializes the controller after FXML loading is complete.
     *
     * <p>Sets up table columns, event handlers, and the task list observable collection.
     * Calls the parent class initialization for sidebar setup.
     */
    @FXML
    public void initialize() {
        super.initialize();
        setupTableColumns();
        setupEventHandlers();
        taskList = FXCollections.observableArrayList();
        taskTable.setItems(taskList);
    }

    /**
     * <hr>
     * Sets up the task table columns with proper cell value factories and formatters.
     *
     * <p>Configures each table column to display the appropriate task properties
     * with custom cell factories for dates and completion status.
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
     * <hr>
     * Sets up event handlers for buttons and table selection.
     *
     * <p>Configures click handlers for all action buttons and selection listeners
     * for the task table to enable interactive task management.
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
     * <hr>
     * Sets the current session and loads its tasks.
     *
     * <p>Initializes the controller for a specific session, loading all associated
     * tasks and populating the assignee selection with session participants.
     *
     * @param sessionId the ID of the session to manage tasks for
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
     * <hr>
     * Loads tasks for the current session from the data layer.
     *
     * <p>Retrieves all tasks associated with the current session and updates
     * the observable task list to refresh the table display.
     */
    private void loadSessionTasks() {
        if (currentSessionId != null) {
            List<SessionTask> tasks = ctx.getSessionTaskManager().getSessionTasks(currentSessionId);
            taskList.clear();
            taskList.addAll(tasks);
        }
    }

    /**
     * <hr>
     * Populates the assignee combo box with session participants.
     *
     * <p>Loads all participants of the current session into the assignee
     * selection dropdown with proper display formatting for usernames.
     *
     * @param participants the list of users participating in the session
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
     * <hr>
     * Creates a new task for the current session.
     *
     * <p>Validates input fields, creates a new task entity, and persists it
     * to the database. Uses optimistic UI updates for better user experience.
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
     * <hr>
     * Updates the selected task with modified information.
     *
     * <p>Validates input fields and updates the selected task entity in the database.
     * Uses optimistic UI updates and provides rollback functionality on errors.
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
     * <hr>
     * Deletes the selected task after confirmation.
     *
     * <p>Displays a confirmation dialog and removes the selected task from
     * the database if confirmed by the user.
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
     * <hr>
     * Marks the selected task as complete.
     *
     * <p>Updates the task's completion status in the database and refreshes
     * the UI to reflect the change. Uses optimistic UI updates.
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
     * <hr>
     * Populates form fields with selected task data for editing.
     *
     * <p>Loads the selected task's properties into the form fields to
     * enable editing of existing tasks.
     *
     * @param task the task to populate the form fields with
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
     * <hr>
     * Clears all form fields and resets selection state.
     *
     * <p>Resets the input form to its empty state and clears the current
     * task selection, updating button states accordingly.
     */
    private void clearFormFields() {
        taskTitleField.clear();
        deadlineField.clear();
        assigneeComboBox.setValue(null);
        selectedTask = null;
        updateButtonStates();
    }

    /**
     * <hr>
     * Updates button states based on selection and user permissions.
     *
     * <p>Enables or disables action buttons based on whether a task is
     * selected and the current user's permissions for task operations.
     */
    private void updateButtonStates() {
        boolean hasSelection = selectedTask != null;

        updateTaskButton.setDisable(!hasSelection);
        deleteTaskButton.setDisable(!hasSelection);
        markCompleteButton.setDisable(!hasSelection || (hasSelection && selectedTask.isCompleted()));
    }

    /**
     * <hr>
     * Shows an error message in the error label.
     *
     * @param message the error message to display
     */
    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }

    /**
     * <hr>
     * Clears the error message and hides the error label.
     */
    private void clearErrorMessage() {
        errorLabel.setText("");
        errorLabel.setVisible(false);
    }

    /**
     * <hr>
     * Gets the current user ID from the session context.
     *
     * @return the current user's ID, or null if no user is logged in
     */
    private String getCurrentUserId() {
        User currentUser = ctx.getUserSession().getCurrentUser();
        return currentUser != null ? currentUser.getUserId() : null;
    }

    /**
     * <hr>
     * Helper method to find a user by their ID.
     *
     * @param userId the ID of the user to find
     * @return the User object if found, null otherwise
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