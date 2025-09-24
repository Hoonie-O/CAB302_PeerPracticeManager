package com.cab302.peerpractice.Controllers;

import com.cab302.peerpractice.AppContext;
import com.cab302.peerpractice.Model.*;
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

public class GroupCalendarController extends BaseController {
    @FXML private Label monthYearLabel;
    @FXML private GridPane calendarGrid;
    @FXML private Button prevButton;
    @FXML private Button nextButton;

    private final SessionCalendarManager sessionCalendarManager;
    private YearMonth currentYearMonth;
    private Group currentGroup;

    public GroupCalendarController(AppContext ctx, Navigation nav) {
        super(ctx, nav);
        this.sessionCalendarManager = ctx.getSessionCalendarManager();
    }

    public void setGroup(Group group) {
        this.currentGroup = group;
        updateCalendarView();
    }

    @FXML
    public void initialize() {
        currentYearMonth = YearMonth.now();
        updateCalendarView();
    }

    @FXML
    private void onPreviousMonth() {
        currentYearMonth = currentYearMonth.minusMonths(1);
        updateCalendarView();
    }

    @FXML
    private void onNextMonth() {
        currentYearMonth = currentYearMonth.plusMonths(1);
        updateCalendarView();
    }

    private void updateCalendarView() {
        if (monthYearLabel != null) {
            monthYearLabel.setText(currentYearMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")));
        }
        populateCalendarGrid();
    }

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

    private Color getColorForLabel(String colorLabel) {
        return switch (colorLabel.toUpperCase()) {
            case "RED" -> Color.RED;
            case "GREEN" -> Color.GREEN;
            case "ORANGE" -> Color.ORANGE;
            case "PURPLE" -> Color.PURPLE;
            default -> Color.BLUE;
        };
    }

    private void showItemDialog(LocalDate date) {
        if (currentGroup == null) return;
        List<Session> sessions = sessionCalendarManager.getSessionsForDateAndGroup(date, currentGroup);
        if (sessions.isEmpty()) {
            showAddSessionDialog(date);
        } else {
            showSessionListDialog(date, sessions);
        }
    }

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
                    LocalDateTime startTime = LocalDateTime.of(date, LocalTime.of(startHour.getValue(), startMinute.getValue()));
                    LocalDateTime endTime = LocalDateTime.of(date, LocalTime.of(endHour.getValue(), endMinute.getValue()));
                    if (endTime.isAfter(startTime)) {
                        System.out.println("[DEBUG] Creating session object: " + title + " for group " + currentGroup.getID());
                        Session session = new Session(title, currentUser, startTime, endTime, currentGroup);
                        session.setDescription(descriptionField.getText());
                        session.setColorLabel(colorCombo.getValue());
                        return session;
                    }
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(session -> {
            System.out.println("[DEBUG] Adding session to calendar manager: " + session.getSessionId());
            sessionCalendarManager.addSession(session, currentGroup);
            updateCalendarView();
        });
    }

    private void showSessionListDialog(LocalDate date, List<Session> sessions) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Study Sessions for " + date.format(DateTimeFormatter.ofPattern("MMMM d, yyyy")));
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        VBox content = new VBox(10);
        content.setPadding(new Insets(20));

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

            Label descriptionLabel = new Label(
                    session.getDescription().isEmpty() ? "No description" : session.getDescription()
            );
            descriptionLabel.setWrapText(true);

            HBox buttons = new HBox(8);
            Button createTaskButton = new Button("Create Task");
            createTaskButton.setOnAction(e -> {
                dialog.close();
                javafx.application.Platform.runLater(() -> openCreateTaskDialog(session));
            });
            Button deleteButton = new Button("Delete");
            deleteButton.setOnAction(e -> {
                dialog.close();
                javafx.application.Platform.runLater(() -> showDeleteSessionDialog(session));
            });

            buttons.getChildren().addAll(createTaskButton, deleteButton);
            sessionBox.getChildren().addAll(titleLabel, timeLabel, descriptionLabel, buttons);
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

    private void openCreateTaskDialog(Session session) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Create Task");
        dialog.setHeaderText("Create task for " + session.getTitle());
        TextField titleField = new TextField();
        titleField.setPromptText("Task title");
        TextField deadlineField = new TextField();
        deadlineField.setPromptText("yyyy-MM-dd HH:mm");
        ComboBox<User> assigneeBox = new ComboBox<>();
        var participants = session.getParticipants();
        if (participants != null && !participants.isEmpty()) assigneeBox.getItems().addAll(participants);
        var cellFactory = new javafx.util.Callback<ListView<User>, ListCell<User>>() {
            @Override public ListCell<User> call(ListView<User> lv) { return new ListCell<>() { @Override protected void updateItem(User u, boolean empty) { super.updateItem(u, empty); setText(empty||u==null?null:u.getUsername()); } }; }
        };
        assigneeBox.setButtonCell(cellFactory.call(null));
        assigneeBox.setCellFactory(cellFactory);
        VBox content = new VBox(10);
        content.setPadding(new Insets(20, 20, 10, 20));
        content.getChildren().addAll(new Label("Title"), titleField, new Label("Deadline"), deadlineField, new Label("Assignee"), assigneeBox);
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().setPrefWidth(420);
        ButtonType createBtn = new ButtonType("Create", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(createBtn, ButtonType.CANCEL);
        dialog.showAndWait().ifPresent(bt -> {
            if (bt == createBtn) {
                String title = titleField.getText() == null ? "" : titleField.getText().trim();
                String dl = deadlineField.getText() == null ? "" : deadlineField.getText().trim();
                User assignee = assigneeBox.getValue();
                if (!title.isEmpty() && !dl.isEmpty() && assignee != null) {
                    try {
                        LocalDateTime deadline = LocalDateTime.parse(dl, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
                        String createdBy = ctx.getUserSession().getCurrentUser() != null ? ctx.getUserSession().getCurrentUser().getUserId() : assignee.getUserId();
                        ctx.getSessionTaskManager().createTask(session.getSessionId(), title, deadline, assignee.getUserId(), createdBy);
                        new Alert(Alert.AlertType.INFORMATION, "Task created").showAndWait();
                    } catch (Exception ex) {
                        new Alert(Alert.AlertType.ERROR, "Failed to create task").showAndWait();
                    }
                }
            }
        });
    }

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
}