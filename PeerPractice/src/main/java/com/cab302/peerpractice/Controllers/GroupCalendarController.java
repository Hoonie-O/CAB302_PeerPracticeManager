package com.cab302.peerpractice.Controllers;

import com.cab302.peerpractice.AppContext;
import com.cab302.peerpractice.Model.Entities.Group;
import com.cab302.peerpractice.Model.Entities.Session;
import com.cab302.peerpractice.Model.Entities.User;
import com.cab302.peerpractice.Model.Managers.SessionCalendarManager;
import com.cab302.peerpractice.Navigation;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * <hr>
 * Controller for managing group study session calendar functionality.
 *
 * <p>This controller handles the display and management of study sessions
 * within groups using a monthly calendar view. Group members can view,
 * create, edit, and delete study sessions with detailed scheduling information.
 *
 * <p>Key features include:
 * <ul>
 *   <li>Monthly calendar grid display with group session indicators</li>
 *   <li>Color-coded session types and priorities</li>
 *   <li>Session creation with detailed metadata (priority, subject, etc.)</li>
 *   <li>Role-based session editing permissions</li>
 *   <li>Integration with SessionCalendarManager for data persistence</li>
 * </ul>
 *
 * @see Session
 * @see SessionCalendarManager
 * @see BaseController
 */
public class GroupCalendarController extends BaseController {
    /** <hr> Label displaying the current month and year. */
    @FXML private Label monthYearLabel;
    /** <hr> Grid layout container for displaying the calendar days. */
    @FXML private GridPane calendarGrid;
    /** <hr> Button for navigating to the previous month. */
    @FXML private Button prevButton;
    /** <hr> Button for navigating to the next month. */
    @FXML private Button nextButton;

    /**
     * <hr>
     * Manager for handling session calendar data operations.
     */
    private final SessionCalendarManager sessionCalendarManager;
    /**
     * <hr>
     * The currently displayed month and year.
     */
    private YearMonth currentYearMonth;
    /**
     * <hr>
     * The currently selected group for session management.
     */
    private Group currentGroup;

    /**
     * <hr>
     * Constructs a new GroupCalendarController with the specified context and navigation.
     *
     * @param ctx the application context providing access to user session and managers
     * @param nav the navigation controller for screen transitions
     */
    public GroupCalendarController(AppContext ctx, Navigation nav) {
        super(ctx, nav);
        this.sessionCalendarManager = ctx.getSessionCalendarManager();
    }

    /**
     * <hr>
     * Sets the current group for session management and updates the calendar view.
     *
     * @param group the group to set as current for session operations
     */
    public void setGroup(Group group) {
        this.currentGroup = group;
        updateCalendarView();
    }

    /**
     * <hr>
     * Initializes the controller after FXML loading is complete.
     *
     * <p>Sets up the initial calendar view with the current month and year,
     * preparing the interface for group session management.
     */
    @FXML
    public void initialize() {
        currentYearMonth = YearMonth.now();
        updateCalendarView();
    }

    /**
     * <hr>
     * Handles navigation to the previous month.
     *
     * <p>Decrements the current month by one and refreshes the calendar display
     * to show the previous month's session data.
     */
    @FXML
    private void onPreviousMonth() {
        currentYearMonth = currentYearMonth.minusMonths(1);
        updateCalendarView();
    }

    /**
     * <hr>
     * Handles navigation to the next month.
     *
     * <p>Increments the current month by one and refreshes the calendar display
     * to show the next month's session data.
     */
    @FXML
    private void onNextMonth() {
        currentYearMonth = currentYearMonth.plusMonths(1);
        updateCalendarView();
    }

    /**
     * <hr>
     * Updates the calendar view with current month and session data.
     *
     * <p>Refreshes the month/year label and repopulates the calendar grid
     * with the current month's days and session information.
     */
    private void updateCalendarView() {
        if (monthYearLabel != null) {
            monthYearLabel.setText(currentYearMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")));
        }
        populateCalendarGrid();
    }

    /**
     * <hr>
     * Populates the calendar grid with day cells for the current month.
     *
     * <p>Creates and arranges day headers (Sun-Sat) and individual day cells
     * for the current month, including session indicators for each date.
     */
    private void populateCalendarGrid() {
        calendarGrid.getChildren().clear();

        String[] dayHeaders = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
        for (int i = 0; i < dayHeaders.length; i++) {
            Label dayHeader = new Label(dayHeaders[i]);
            dayHeader.setFont(Font.font("System", FontWeight.BOLD, 12));
            dayHeader.setAlignment(Pos.CENTER);
            dayHeader.setPrefWidth(80);
            dayHeader.setPrefHeight(30);
            dayHeader.setStyle("-fx-background-color: #f0f0f0; -fx-border-color: #ccc;");
            calendarGrid.add(dayHeader, i, 0);
        }

        LocalDate firstDayOfMonth = currentYearMonth.atDay(1);
        int dayOfWeek = firstDayOfMonth.getDayOfWeek().getValue() % 7;
        int daysInMonth = currentYearMonth.lengthOfMonth();

        int row = 1;
        int col = dayOfWeek;

        for (int day = 1; day <= daysInMonth; day++) {
            LocalDate date = LocalDate.of(currentYearMonth.getYear(), currentYearMonth.getMonth(), day);
            VBox dayCell = createDayCell(date);
            calendarGrid.add(dayCell, col, row);
            col++;
            if (col > 6) {
                col = 0;
                row++;
            }
        }
    }

    /**
     * <hr>
     * Creates a visual cell representing a single day in the calendar.
     *
     * <p>Each day cell displays the date number and any study session entries
     * for that date. Today's date is highlighted with special styling.
     *
     * @param date the date to create the cell for
     * @return a VBox containing the day cell with date label and session indicators
     */
    private VBox createDayCell(LocalDate date) {
        VBox dayCell = new VBox();
        dayCell.setPrefWidth(80);
        dayCell.setPrefHeight(80);
        dayCell.setPadding(new Insets(2));
        dayCell.setAlignment(Pos.TOP_LEFT);
        dayCell.setStyle("-fx-border-color: #ccc; -fx-background-color: white;");

        Label dayLabel = new Label(String.valueOf(date.getDayOfMonth()));
        dayLabel.setFont(Font.font("System", FontWeight.BOLD, 12));

        if (date.equals(LocalDate.now())) {
            dayLabel.setTextFill(Color.BLUE);
            dayCell.setStyle("-fx-border-color: #ccc; -fx-background-color: #e6f3ff;");
        }

        dayCell.getChildren().add(dayLabel);

        if (currentGroup != null) {
            List<Session> sessions = sessionCalendarManager.getSessionsForDateAndGroup(date, currentGroup);
            for (Session session : sessions) {
                Label sessionLabel = new Label(session.getTitle());
                sessionLabel.setFont(Font.font("System", 8));
                sessionLabel.setTextFill(getColorForLabel(session.getColorLabel()));
                sessionLabel.setPrefWidth(75);
                sessionLabel.setWrapText(true);
                dayCell.getChildren().add(sessionLabel);
            }
        }

        dayCell.setOnMouseClicked(e -> showItemDialog(date));
        return dayCell;
    }

    /**
     * <hr>
     * Converts a color label string to a JavaFX Color object.
     *
     * @param colorLabel the color label string (e.g., "RED", "GREEN")
     * @return the corresponding Color object, defaults to BLUE if unknown
     */
    private Color getColorForLabel(String colorLabel) {
        return switch (colorLabel.toUpperCase()) {
            case "RED" -> Color.RED;
            case "GREEN" -> Color.GREEN;
            case "ORANGE" -> Color.ORANGE;
            case "PURPLE" -> Color.PURPLE;
            default -> Color.BLUE;
        };
    }

    /**
     * <hr>
     * Shows the appropriate dialog when a day cell is clicked.
     *
     * <p>If the date has no session entries, shows the add session dialog.
     * If entries exist, shows the session list dialog.
     *
     * @param date the date that was clicked
     */
    private void showItemDialog(LocalDate date) {
        if (currentGroup == null) return;
        List<Session> sessions = sessionCalendarManager.getSessionsForDateAndGroup(date, currentGroup);
        if (sessions.isEmpty()) {
            showAddSessionDialog(date);
        } else {
            showSessionListDialog(date, sessions);
        }
    }

    /**
     * <hr>
     * Displays a dialog for adding new study sessions for a specific date.
     *
     * <p>Allows users to set title, description, start/end times, priority,
     * subject, and color for a new study session entry.
     *
     * @param date the date to add session for
     */
    private void showAddSessionDialog(LocalDate date) {
        User currentUser = ctx.getUserSession().getCurrentUser();
        if (currentUser == null || currentGroup == null) return;

        Dialog<Session> dialog = new Dialog<>();
        dialog.setTitle("Add Study Session");
        dialog.setHeaderText("Create study session for " + date.format(DateTimeFormatter.ofPattern("MMMM d, yyyy")));

        ButtonType saveButtonType = new ButtonType("Create", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField titleField = new TextField();
        TextArea descriptionField = new TextArea();
        descriptionField.setPromptText("Description optional");
        descriptionField.setPrefRowCount(2);

        Spinner<Integer> startHour = new Spinner<>(0, 23, 9);
        Spinner<Integer> startMinute = new Spinner<>(0, 59, 0, 15);
        Spinner<Integer> endHour = new Spinner<>(0, 23, 10);
        Spinner<Integer> endMinute = new Spinner<>(0, 59, 0, 15);

        ComboBox<String> colorCombo = new ComboBox<>();
        colorCombo.getItems().addAll("BLUE", "RED", "GREEN", "ORANGE", "PURPLE");
        colorCombo.setValue("BLUE");

        ComboBox<String> priorityCombo = new ComboBox<>();
        priorityCombo.getItems().addAll("optional", "urgent", "important");
        priorityCombo.setValue("optional");

        ComboBox<String> subjectCombo = new ComboBox<>();
        subjectCombo.getItems().addAll("Science", "Technology", "Engineering", "Maths", "Humanities and Social Sciences");
        subjectCombo.setValue("Science");

        grid.add(new Label("Title:"), 0, 0);
        grid.add(titleField, 1, 0);
        grid.add(new Label("Description:"), 0, 1);
        grid.add(descriptionField, 1, 1);
        grid.add(new Label("Start time:"), 0, 2);
        grid.add(new HBox(5, new VBox(5, new Label("Hour"), startHour), new VBox(5, new Label("Minute"), startMinute)), 1, 2);
        grid.add(new Label("End time:"), 0, 3);
        grid.add(new HBox(5, new VBox(5, new Label("Hour"), endHour), new VBox(5, new Label("Minute"), endMinute)), 1, 3);
        grid.add(new Label("Priority:"), 0, 4);
        grid.add(priorityCombo, 1, 4);
        grid.add(new Label("Subject:"), 0, 5);
        grid.add(subjectCombo, 1, 5);
        grid.add(new Label("Color:"), 0, 6);
        grid.add(colorCombo, 1, 6);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                String title = titleField.getText().trim();
                if (!title.isEmpty()) {
                    LocalDateTime startTime = LocalDateTime.of(date, LocalTime.of(startHour.getValue(), startMinute.getValue()));
                    LocalDateTime endTime = LocalDateTime.of(date, LocalTime.of(endHour.getValue(), endMinute.getValue()));
                    if (endTime.isAfter(startTime)) {
                        Session session = new Session(title, currentUser, startTime, endTime, currentGroup);
                        session.setDescription(descriptionField.getText());
                        session.setColorLabel(colorCombo.getValue());
                        session.setSubject(subjectCombo.getValue());
                        session.setPriority(priorityCombo.getValue());
                        return session;
                    }
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(session -> {
            sessionCalendarManager.addSession(session, currentGroup);
            updateCalendarView();
        });
    }

    /**
     * <hr>
     * Displays a dialog listing all session entries for a specific date.
     *
     * <p>Shows details of each session entry and provides options to
     * edit sessions, view tasks, delete sessions, or add more sessions.
     *
     * @param date the date to show sessions for
     * @param sessions list of session entries for the date
     */
    private void showSessionListDialog(LocalDate date, List<Session> sessions) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Study Sessions for " + date.format(DateTimeFormatter.ofPattern("MMMM d, yyyy")));
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        VBox content = new VBox(10);
        content.setPadding(new Insets(20));

        User currentUser = ctx.getUserSession().getCurrentUser();

        for (Session session : sessions) {
            VBox sessionBox = new VBox(5);
            sessionBox.setStyle("-fx-border-color: #ccc; -fx-padding: 10; -fx-background-color: #f9f9f9;");

            Label titleLabel = new Label(session.getTitle());
            titleLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
            titleLabel.setTextFill(getColorForLabel(session.getColorLabel()));

            Label timeLabel = new Label(
                    session.getStartTime().format(DateTimeFormatter.ofPattern("HH:mm")) +
                            " - " +
                            session.getEndTime().format(DateTimeFormatter.ofPattern("HH:mm"))
            );

            Label descriptionLabel = new Label(session.getDescription().isEmpty() ? "No description" : session.getDescription());
            descriptionLabel.setWrapText(true);

            HBox buttonBox = new HBox(8);

            // Edit button - admins can edit all, members can edit own
            boolean canEdit = canUserEditSession(currentUser, session);
            if (canEdit) {
                Button editButton = new Button("Edit");
                editButton.setOnAction(e -> {
                    dialog.close();
                    javafx.application.Platform.runLater(() -> showEditSessionDialog(session));
                });
                buttonBox.getChildren().add(editButton);
            }

            // View Tasks button
            Button viewTasksButton = new Button("View Tasks");
            viewTasksButton.setOnAction(e -> {
                dialog.close();
                nav.openSessionTasks(session.getSessionId());
            });
            buttonBox.getChildren().add(viewTasksButton);

            // Delete button - same permissions as edit
            if (canEdit) {
                Button deleteButton = new Button("Delete");
                deleteButton.setOnAction(e -> {
                    dialog.close();
                    javafx.application.Platform.runLater(() -> showDeleteSessionDialog(session));
                });
                buttonBox.getChildren().add(deleteButton);
            }

            sessionBox.getChildren().addAll(titleLabel, timeLabel, descriptionLabel, buttonBox);
            content.getChildren().add(sessionBox);
        }

        Button addNewButton = new Button("Add Another Session");
        addNewButton.setOnAction(e -> {
            dialog.close();
            javafx.application.Platform.runLater(() -> showAddSessionDialog(date));
        });
        content.getChildren().add(addNewButton);

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setPrefSize(500, 400);
        scrollPane.setFitToWidth(true);

        dialog.getDialogPane().setContent(scrollPane);
        dialog.showAndWait();
    }

    /**
     * <hr>
     * Displays a confirmation dialog for deleting a session entry.
     *
     * @param session the session entry to be deleted
     */
    private void showDeleteSessionDialog(Session session) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Session");
        alert.setHeaderText("Are you sure you want to delete \"" + session.getTitle() + "\"?");
        alert.setContentText("This action cannot be undone.");

        ButtonType deleteButton = new ButtonType("Delete", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

        alert.getButtonTypes().setAll(deleteButton, cancelButton);

        alert.showAndWait().ifPresent(response -> {
            if (response == deleteButton) {
                sessionCalendarManager.deleteSession(session);
                updateCalendarView();
            }
        });
    }

    /**
     * <hr>
     * Checks if a user has permission to edit a specific session.
     *
     * <p>Group administrators can edit all sessions, while regular members
     * can only edit sessions they have created themselves.
     *
     * @param user the user to check permissions for
     * @param session the session to check edit permissions for
     * @return true if the user can edit the session, false otherwise
     */
    private boolean canUserEditSession(User user, Session session) {
        if (user == null || currentGroup == null) return false;

        // Admin check - admins can edit all sessions in the group
        if (ctx.getGroupManager().isAdmin(currentGroup, user)) {
            return true;
        }

        // Members can only edit their own sessions
        return session.getOrganiser().getUserId().equals(user.getUserId());
    }

    /**
     * <hr>
     * Displays a dialog for editing existing study sessions.
     *
     * <p>Allows users to modify session details including title, description,
     * start/end times, and color coding.
     *
     * @param session the session to be edited
     */
    private void showEditSessionDialog(Session session) {
        User currentUser = ctx.getUserSession().getCurrentUser();
        if (currentUser == null || currentGroup == null) return;

        Dialog<Session> dialog = new Dialog<>();
        dialog.setTitle("Edit Study Session");
        dialog.setHeaderText("Edit session: " + session.getTitle());

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField titleField = new TextField(session.getTitle());
        TextArea descriptionField = new TextArea(session.getDescription());
        descriptionField.setPrefRowCount(2);

        LocalDateTime startTime = session.getStartTime();
        LocalDateTime endTime = session.getEndTime();

        Spinner<Integer> startHour = new Spinner<>(0, 23, startTime.getHour());
        Spinner<Integer> startMinute = new Spinner<>(0, 59, startTime.getMinute(), 15);
        Spinner<Integer> endHour = new Spinner<>(0, 23, endTime.getHour());
        Spinner<Integer> endMinute = new Spinner<>(0, 59, endTime.getMinute(), 15);

        ComboBox<String> colorCombo = new ComboBox<>();
        colorCombo.getItems().addAll("BLUE", "RED", "GREEN", "ORANGE", "PURPLE");
        colorCombo.setValue(session.getColorLabel());

        grid.add(new Label("Title:"), 0, 0);
        grid.add(titleField, 1, 0);
        grid.add(new Label("Description:"), 0, 1);
        grid.add(descriptionField, 1, 1);
        grid.add(new Label("Start time:"), 0, 2);
        grid.add(new HBox(5, new VBox(5, new Label("Hour"), startHour), new VBox(5, new Label("Minute"), startMinute)), 1, 2);
        grid.add(new Label("End time:"), 0, 3);
        grid.add(new HBox(5, new VBox(5, new Label("Hour"), endHour), new VBox(5, new Label("Minute"), endMinute)), 1, 3);
        grid.add(new Label("Color:"), 0, 4);
        grid.add(colorCombo, 1, 4);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                String title = titleField.getText().trim();
                if (!title.isEmpty()) {
                    LocalDate sessionDate = startTime.toLocalDate();
                    LocalDateTime newStartTime = LocalDateTime.of(sessionDate,
                            java.time.LocalTime.of(startHour.getValue(), startMinute.getValue()));
                    LocalDateTime newEndTime = LocalDateTime.of(sessionDate,
                            java.time.LocalTime.of(endHour.getValue(), endMinute.getValue()));

                    if (newEndTime.isAfter(newStartTime)) {
                        Session editedSession = new Session(title, session.getOrganiser(), newStartTime, newEndTime, currentGroup);
                        editedSession.setDescription(descriptionField.getText());
                        editedSession.setColorLabel(colorCombo.getValue());
                        return editedSession;
                    }
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(editedSession -> {
            sessionCalendarManager.updateSession(session, editedSession);
            updateCalendarView();
        });
    }
}