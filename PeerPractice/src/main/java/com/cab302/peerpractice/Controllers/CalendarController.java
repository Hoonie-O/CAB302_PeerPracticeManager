package com.cab302.peerpractice.Controllers;

import com.cab302.peerpractice.AppContext;
import com.cab302.peerpractice.Model.Event;
import com.cab302.peerpractice.Model.EventManager;
import com.cab302.peerpractice.Navigation;
import com.cab302.peerpractice.View;
import javafx.animation.TranslateTransition;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class CalendarController extends BaseController {
    @FXML private Label monthYearLabel;
    @FXML private GridPane calendarGrid;
    @FXML private Button prevButton;
    @FXML private Button nextButton;

    @FXML private BorderPane menu;
    @FXML private BorderPane profile;
    @FXML private ComboBox<String> availabilityStatus;
    @FXML private Button menuButton;
    @FXML private Button profileButton;

    private boolean menuOpen = false;
    private boolean profileOpen = false;

    private YearMonth currentYearMonth;
    private final EventManager eventManager;

    private static final Duration SLIDE = Duration.millis(180);

    public CalendarController(AppContext ctx, Navigation nav) {
        super(ctx, nav);
        this.eventManager = ctx.getEventManager();
    }

    @FXML
    private void initialize(){
        if (menu != null) {
            menu.setVisible(false);
            menu.setManaged(false);
        }
        if (profile != null) {
            profile.setVisible(false);
            profile.setManaged(false);
        }

        if (availabilityStatus != null) {
            if (availabilityStatus.getItems().isEmpty()) {
                availabilityStatus.setItems(FXCollections.observableArrayList("Online", "Away", "Busy", "Offline"));
            }
            availabilityStatus.getSelectionModel().select("Online");
            availabilityStatus.getSelectionModel().selectedItemProperty()
                    .addListener((obs, oldVal, newVal) ->
                            System.out.println("Status changed to: " + newVal));
        }

        currentYearMonth = YearMonth.now();
        updateCalendarView();
    }

    private void animate(Region sidebar, double targetX, Runnable onComplete) {
        TranslateTransition transition = new TranslateTransition(SLIDE, sidebar);
        transition.setToX(targetX);
        transition.setOnFinished(e -> {
            if (onComplete != null) onComplete.run();
        });
        transition.play();
    }

    @FXML
    private void onToggleMenu() {
        if (!menuOpen) openMenu();
        else closeMenu();
    }

    private void openMenu() {
        menu.setVisible(true);
        menu.setManaged(true);
        double width = menu.getPrefWidth();
        menu.setTranslateX(-width);
        animate(menu, 0, () -> menuOpen = true);
    }

    private void closeMenu() {
        double width = menu.getPrefWidth();
        animate(menu, -width, () -> {
            menuOpen = false;
            menu.setVisible(false);
            menu.setManaged(false);
            menu.setTranslateX(0);
        });
    }

    @FXML
    private void onToggleProfile() {
        if (profileOpen) closeProfile();
        else openProfile();
    }

    private void openProfile() {
        profile.setVisible(true);
        profile.setManaged(true);
        double width = profile.getPrefWidth();
        profile.setTranslateX(width);
        animate(profile, 0, () -> profileOpen = true);
    }

    private void closeProfile() {
        double width = profile.getPrefWidth();
        animate(profile, width, () -> {
            profileOpen = false;
            profile.setVisible(false);
            profile.setManaged(false);
            profile.setTranslateX(0);
        });
    }

    @FXML
    private void onOpenGroups() {
        nav.DisplayMainMenuOrGroup();
    }

    @FXML
    private void onOpenCalendar() {
        nav.Display(View.Calendar);
    }

    @FXML
    private void onOpenFriends() {
        System.out.println("meow!");
    }

    @FXML
    private void onBackToLogin() {
        nav.Display(View.Login);
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
        monthYearLabel.setText(currentYearMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")));
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

        List<Event> events = eventManager.getEventsForDate(date);
        for (Event event : events) {
            Label eventLabel = new Label(event.getTitle());
            eventLabel.setFont(Font.font("System", 8));
            eventLabel.setTextFill(getColorForLabel(event.getColorLabel()));
            eventLabel.setPrefWidth(75);
            eventLabel.setWrapText(true);
            dayCell.getChildren().add(eventLabel);
        }

        dayCell.setOnMouseClicked(e -> showEventDialog(date));
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

    private void showEventDialog(LocalDate date) {
        List<Event> events = eventManager.getEventsForDate(date);
        if (events.isEmpty()) {
            showAddEventDialog(date);
        } else {
            showEventListDialog(date, events);
        }
    }

    private void showAddEventDialog(LocalDate date) {
        Dialog<Event> dialog = new Dialog<>();
        dialog.setTitle("Add Event");
        dialog.setHeaderText("Add event for " + date.format(DateTimeFormatter.ofPattern("MMMM d, yyyy")));

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = buildEventForm(null, date);
        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                return extractEventFromForm(grid, date, null);
            }
            return null;
        });

        dialog.showAndWait().ifPresent(event -> {
            eventManager.createEvent(
                    event.getTitle(),
                    event.getDescription(),
                    event.getStartTime(),
                    event.getEndTime(),
                    event.getColorLabel()
            );
            updateCalendarView();
        });
    }

    private void showEditEventDialog(Event oldEvent) {
        LocalDate date = oldEvent.getStartTime().toLocalDate();

        Dialog<Event> dialog = new Dialog<>();
        dialog.setTitle("Edit Event");
        dialog.setHeaderText("Edit event for " + date.format(DateTimeFormatter.ofPattern("MMMM d, yyyy")));

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = buildEventForm(oldEvent, date);
        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                return extractEventFromForm(grid, date, oldEvent);
            }
            return null;
        });

        dialog.showAndWait().ifPresent(newEvent -> {
            eventManager.updateEvent(oldEvent, newEvent);
            updateCalendarView();
        });
    }

    private void showDeleteEventDialog(Event event) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Event");
        alert.setHeaderText("Are you sure you want to delete \"" + event.getTitle() + "\"?");
        alert.setContentText("This action cannot be undone.");

        ButtonType deleteButton = new ButtonType("Delete", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

        alert.getButtonTypes().setAll(deleteButton, cancelButton);

        alert.showAndWait().ifPresent(response -> {
            if (response == deleteButton) {
                eventManager.deleteEvent(event);
                updateCalendarView();
            }
        });
    }

    private void showEventListDialog(LocalDate date, List<Event> events) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Events for " + date.format(DateTimeFormatter.ofPattern("MMMM d, yyyy")));
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        VBox content = new VBox(10);
        content.setPadding(new Insets(20));

        for (Event event : events) {
            VBox eventBox = new VBox(5);
            eventBox.setStyle("-fx-border-color: #ccc; -fx-padding: 10; -fx-background-color: #f9f9f9;");

            Label titleLabel = new Label(event.getTitle());
            titleLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
            titleLabel.setTextFill(getColorForLabel(event.getColorLabel()));

            Label timeLabel = new Label(
                    event.getStartTime().format(DateTimeFormatter.ofPattern("HH:mm")) +
                            " - " +
                            event.getEndTime().format(DateTimeFormatter.ofPattern("HH:mm"))
            );

            Label descriptionLabel = new Label(
                    event.getDescription().isEmpty() ? "No description" : event.getDescription()
            );
            descriptionLabel.setWrapText(true);

            HBox buttons = new HBox(8);
            Button editButton = new Button("Edit");
            editButton.setOnAction(e -> {
                dialog.close();
                javafx.application.Platform.runLater(() -> showEditEventDialog(event));
            });

            Button deleteButton = new Button("Delete");
            deleteButton.setOnAction(e -> {
                dialog.close();
                javafx.application.Platform.runLater(() -> showDeleteEventDialog(event));
            });

            buttons.getChildren().addAll(editButton, deleteButton);

            eventBox.getChildren().addAll(titleLabel, timeLabel, descriptionLabel, buttons);
            content.getChildren().add(eventBox);
        }

        Button addNewButton = new Button("Add Another Event");
        addNewButton.setOnAction(e -> {
            dialog.close();
            showAddEventDialog(date);
        });
        content.getChildren().add(addNewButton);

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setPrefSize(400, 300);
        scrollPane.setFitToWidth(true);

        dialog.getDialogPane().setContent(scrollPane);
        dialog.showAndWait();
    }

    private GridPane buildEventForm(Event existing, LocalDate date) {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField titleField = new TextField(existing == null ? "" : existing.getTitle());
        titleField.setPromptText("Event title");

        TextArea descriptionField = new TextArea(existing == null ? "" : existing.getDescription());
        descriptionField.setPromptText("Description (optional)");
        descriptionField.setPrefRowCount(3);

        Spinner<Integer> startHour   = new Spinner<>(0, 23, existing == null ? 9 : existing.getStartTime().getHour());
        Spinner<Integer> startMinute = new Spinner<>(0, 59, existing == null ? 0 : existing.getStartTime().getMinute(), 15);
        Spinner<Integer> endHour     = new Spinner<>(0, 23, existing == null ? 10 : existing.getEndTime().getHour());
        Spinner<Integer> endMinute   = new Spinner<>(0, 59, existing == null ? 0 : existing.getEndTime().getMinute(), 15);

        ComboBox<String> colorCombo = new ComboBox<>();
        colorCombo.getItems().addAll("BLUE", "RED", "GREEN", "ORANGE", "PURPLE");
        colorCombo.setValue(existing == null ? "BLUE" : existing.getColorLabel().toUpperCase());

        grid.add(new Label("Title:"), 0, 0);
        grid.add(titleField, 1, 0);
        grid.add(new Label("Description:"), 0, 1);
        grid.add(descriptionField, 1, 1);
        grid.add(new Label("Start time:"), 0, 2);
        grid.add(new VBox(5, new Label("Hour"), startHour), 1, 2);
        grid.add(new VBox(5, new Label("Minute"), startMinute), 2, 2);
        grid.add(new Label("End time:"), 0, 3);
        grid.add(new VBox(5, new Label("Hour"), endHour), 1, 3);
        grid.add(new VBox(5, new Label("Minute"), endMinute), 2, 3);
        grid.add(new Label("Color:"), 0, 4);
        grid.add(colorCombo, 1, 4);

        grid.setUserData(new Object[]{titleField, descriptionField, startHour, startMinute, endHour, endMinute, colorCombo});
        return grid;
    }

    @SuppressWarnings("unchecked")
    private Event extractEventFromForm(GridPane grid, LocalDate date, Event existing) {
        Object[] data = (Object[]) grid.getUserData();
        TextField titleField = (TextField) data[0];
        TextArea descriptionField = (TextArea) data[1];
        Spinner<Integer> startHour = (Spinner<Integer>) data[2];
        Spinner<Integer> startMinute = (Spinner<Integer>) data[3];
        Spinner<Integer> endHour = (Spinner<Integer>) data[4];
        Spinner<Integer> endMinute = (Spinner<Integer>) data[5];
        ComboBox<String> colorCombo = (ComboBox<String>) data[6];

        String title = titleField.getText().trim();
        if (!title.isEmpty()) {
            LocalDateTime startTime = LocalDateTime.of(date, LocalTime.of(startHour.getValue(), startMinute.getValue()));
            LocalDateTime endTime   = LocalDateTime.of(date, LocalTime.of(endHour.getValue(), endMinute.getValue()));
            if (endTime.isAfter(startTime)) {
                return new Event(title, descriptionField.getText(), startTime, endTime, colorCombo.getValue());
            }
        }
        return null;
    }
}