package com.cab302.peerpractice.Controllers;

import com.cab302.peerpractice.AppContext;
import com.cab302.peerpractice.Model.Entities.Availability;
import com.cab302.peerpractice.Model.Entities.User;
import com.cab302.peerpractice.Model.Managers.AvailabilityManager;
import com.cab302.peerpractice.Navigation;
import com.cab302.peerpractice.Model.Utils.DateTimeFormatUtils;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
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
 * Controller for managing user availability calendar functionality.
 *
 * <p>This controller handles the display and management of user availability schedules
 * in a monthly calendar view. Users can view, add, edit, and delete availability
 * entries for specific dates and times.
 *
 * <p> Key features include:
 * <ul>
 *   <li>Monthly calendar grid display with navigation</li>
 *   <li>Color-coded availability indicators</li>
 *   <li>Dialog-based availability management</li>
 *   <li>Integration with AvailabilityManager for data persistence</li>
 * </ul>
 *
 * @see Availability
 * @see AvailabilityManager
 * @see SidebarController
 */
public class AvailabilityController extends SidebarController {
    /** <hr> Label displaying the current month and year. */
    @FXML private Label monthYearLabel;
    /** <hr> Grid layout container for displaying the calendar days. */
    @FXML private GridPane calendarGrid;
    /** <hr> Button for navigating to the previous month. */
    @FXML private Button prevButton;
    /** <hr> Button for navigating to the next month. */
    @FXML private Button nextButton;

    /** <hr> The currently displayed month and year. */
    private YearMonth currentYearMonth;

    /** <hr> Manager for handling availability data operations. */
    private final AvailabilityManager availabilityManager;

    /**
     * <hr>
     * Constructs a new AvailabilityController with the specified context and navigation.
     *
     * @param ctx the application context providing access to user session and managers
     * @param nav the navigation controller for screen transitions
     */
    public AvailabilityController(AppContext ctx, Navigation nav) {
        super(ctx, nav);
        this.availabilityManager = ctx.getAvailabilityManager();
    }

    /**
     * <hr>
     *
     * Initializes the controller after FXML loading is complete.
     *
     * <p>Sets up the initial calendar view with the current month and year,
     * and calls the parent class initialization.
     */
    @FXML
    public void initialize() {
        super.initialize();
        currentYearMonth = YearMonth.now();
        updateCalendarView();
    }

    /**
     * <hr>
     *
     * Rebuilds the calendar UI for the currently selected month.
     *
     * <p>Clears all cells, lays out day headers and week rows, and injects
     * availability indicators for the signed-in user.
     *
     */
    private void updateCalendarView() {
        monthYearLabel.setText(currentYearMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")));
        populateCalendarGrid();
    }

    /**
     * <hr>
     *
     * Navigates to the previous month and updates the calendar view.
     *
     * <p>Decrements the current month by one and refreshes the calendar display
     * to show the previous month's availability data.
     */
    @FXML
    private void onPreviousMonth() {
        currentYearMonth = currentYearMonth.minusMonths(1);
        updateCalendarView();
    }

    /**
     * <hr>
     *
     * Navigates to the next month and updates the calendar view.
     *
     * <p>Increments the current month by one and refreshes the calendar display
     * to show the next month's availability data.
     */
    @FXML
    private void onNextMonth() {
        currentYearMonth = currentYearMonth.plusMonths(1);
        updateCalendarView();
    }

    /**
     * <hr>
     *
     * Populates the calendar grid with day cells for the current month.
     *
     * <p>Creates and arranges day headers (Sun-Sat) and individual day cells
     * for the current month, including availability indicators for each date.
     */
    private void populateCalendarGrid() {
        calendarGrid.getChildren().clear();
        calendarGrid.getColumnConstraints().clear();
        calendarGrid.getRowConstraints().clear();

        for (int i = 0; i < 7; i++) {
            ColumnConstraints col = new ColumnConstraints();
            col.setPercentWidth(100.0 / 7);
            col.setHgrow(Priority.ALWAYS);
            calendarGrid.getColumnConstraints().add(col);
        }

        RowConstraints headerRow = new RowConstraints();
        headerRow.setMinHeight(40);
        headerRow.setVgrow(Priority.NEVER);
        calendarGrid.getRowConstraints().add(headerRow);

        for (int i = 0; i < 6; i++) {
            RowConstraints row = new RowConstraints();
            row.setMinHeight(120);
            row.setVgrow(Priority.ALWAYS);
            calendarGrid.getRowConstraints().add(row);
        }

        String[] dayHeaders = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
        for (int i = 0; i < dayHeaders.length; i++) {
            Label dayHeader = new Label(dayHeaders[i]);
            dayHeader.getStyleClass().add("calendar-day-header");
            dayHeader.setAlignment(Pos.CENTER);
            dayHeader.setMaxWidth(Double.MAX_VALUE);
            calendarGrid.add(dayHeader, i, 0);
        }

        LocalDate firstDayOfMonth = currentYearMonth.atDay(1);
        int dayOfWeek = firstDayOfMonth.getDayOfWeek().getValue() % 7;
        int daysInMonth = currentYearMonth.lengthOfMonth();

        int row = 1;
        int col = dayOfWeek;

        // Fill in days from previous month
        LocalDate prevMonthStart = firstDayOfMonth.minusDays(dayOfWeek);
        for (int i = 0; i < dayOfWeek; i++) {
            LocalDate date = prevMonthStart.plusDays(i);
            VBox dayCell = createDayCell(date, true);
            calendarGrid.add(dayCell, i, row);
        }

        // Fill in current month days
        for (int day = 1; day <= daysInMonth; day++) {
            LocalDate date = LocalDate.of(currentYearMonth.getYear(), currentYearMonth.getMonth(), day);
            VBox dayCell = createDayCell(date, false);
            calendarGrid.add(dayCell, col, row);
            col++;
            if (col > 6) {
                col = 0;
                row++;
            }
        }

        // Fill in days from next month
        while (col > 0 && col <= 6) {
            LocalDate date = currentYearMonth.atEndOfMonth().plusDays(col - dayOfWeek - daysInMonth + 1);
            VBox dayCell = createDayCell(date, true);
            calendarGrid.add(dayCell, col, row);
            col++;
        }
    }

    /**
     * <hr>
     * Creates a visual cell representing a single day in the calendar.
     *
     * <p>Each day cell displays the date number and any availability entries
     * for that date. Today's date is highlighted with special styling.
     *
     * @param date the date to create the cell for
     * @param isOtherMonth whether this day is from previous or next month
     * @return a VBox containing the day cell with date label and availability indicators
     */
    private VBox createDayCell(LocalDate date, boolean isOtherMonth) {
        VBox dayCell = new VBox(5);
        dayCell.setMaxWidth(Double.MAX_VALUE);
        dayCell.setMaxHeight(Double.MAX_VALUE);
        dayCell.setPadding(new Insets(12));
        dayCell.setAlignment(Pos.TOP_LEFT);
        GridPane.setHgrow(dayCell, Priority.ALWAYS);
        GridPane.setVgrow(dayCell, Priority.ALWAYS);
        GridPane.setFillWidth(dayCell, true);
        GridPane.setFillHeight(dayCell, true);

        dayCell.getStyleClass().add("calendar-day-cell");

        if (isOtherMonth) {
            dayCell.getStyleClass().add("calendar-day-other-month");
        }

        // Day number label
        Label dayLabel = new Label(String.valueOf(date.getDayOfMonth()));
        dayLabel.getStyleClass().add("calendar-day-number");

        // Check if there are availabilities
        boolean hasAvailability = false;
        User currentUser = ctx.getUserSession().getCurrentUser();
        if (currentUser != null && !isOtherMonth) {
            List<Availability> availabilities = availabilityManager.getAvailabilitiesForDate(date);
            availabilities = availabilities.stream()
                    .filter(avail -> avail.getUser().equals(currentUser))
                    .toList();
            hasAvailability = !availabilities.isEmpty();
        }

        // Apply special styling for today or days with availability
        if (date.equals(LocalDate.now())) {
            dayCell.getStyleClass().add("calendar-day-today");
        } else if (hasAvailability && !isOtherMonth) {
            dayCell.getStyleClass().add("calendar-day-has-session");
        }

        dayCell.getChildren().add(dayLabel);

        // Add availability indicators
        if (currentUser != null && !isOtherMonth) {
            List<Availability> availabilities = availabilityManager.getAvailabilitiesForDate(date);
            availabilities = availabilities.stream()
                    .filter(avail -> avail.getUser().equals(currentUser))
                    .toList();

            if (!availabilities.isEmpty()) {
                // Show availability count badge instead of listing all
                Label availBadge = new Label(availabilities.size() + " slot" + (availabilities.size() > 1 ? "s" : ""));
                availBadge.getStyleClass().add("calendar-session-badge");
                dayCell.getChildren().add(availBadge);
            }
        }

        dayCell.setOnMouseClicked(e -> {
            if (!isOtherMonth) {
                showItemDialog(date);
            }
        });

        return dayCell;
    }

    /**
     * Old createDayCell signature for backwards compatibility
     */
    private VBox createDayCell(LocalDate date) {
        return createDayCell(date, false);
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
     * <p>If the date has no availability entries, shows the add availability dialog.
     * If entries exist, shows the availability list dialog.
     *
     * @param date the date that was clicked
     */
    private void showItemDialog(LocalDate date) {
        User currentUser = ctx.getUserSession().getCurrentUser();
        if (currentUser != null) {
            List<Availability> availabilities = availabilityManager.getAvailabilitiesForDate(date)
                    .stream()
                    .filter(avail -> avail.getUser().equals(currentUser))
                    .toList();

            if (availabilities.isEmpty()) {
                showAddAvailabilityDialog(date);
            } else {
                showAvailabilityListDialog(date, availabilities);
            }
        }
    }

    /**
     * <hr>
     * Displays a dialog for adding new availability for a specific date.
     *
     * <p>Allows users to set title, description, start/end times, and color
     * for a new availability entry.
     *
     * @param date the date to add availability for
     */
    private void showAddAvailabilityDialog(LocalDate date) {
        User currentUser = ctx.getUserSession().getCurrentUser();
        if (currentUser == null) return;

        Dialog<Availability> dialog = new Dialog<>();
        dialog.setTitle("Add Availability");
        dialog.setHeaderText("Set availability for " + date.format(DateTimeFormatter.ofPattern("MMMM d, yyyy")));

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField titleField = new TextField("Available");
        TextArea descriptionField = new TextArea();
        descriptionField.setPromptText("Description optional");
        descriptionField.setPrefRowCount(2);

        Spinner<Integer> startHour = new Spinner<>(0, 23, 9);
        Spinner<Integer> startMinute = new Spinner<>(0, 59, 0, 15);
        Spinner<Integer> endHour = new Spinner<>(0, 23, 17);
        Spinner<Integer> endMinute = new Spinner<>(0, 59, 0, 15);

        ComboBox<String> colorCombo = new ComboBox<>();
        colorCombo.getItems().addAll("GREEN", "BLUE", "ORANGE", "PURPLE", "RED");
        colorCombo.setValue("GREEN");

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
                        Availability availability = new Availability(title, currentUser, startTime, endTime, colorCombo.getValue());
                        availability.setDescription(descriptionField.getText());
                        return availability;
                    }
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(availability -> {

            availabilityManager.createAvailability(
                    availability.getTitle(),
                    availability.getUser(),
                    availability.getStartTime(),
                    availability.getEndTime(),
                    availability.getColorLabel()
            );
            updateCalendarView();
        });
    }

    /**
     * <hr>
     * Displays a dialog listing all availability entries for a specific date.
     *
     * <p>Shows details of each availability entry and provides options to
     * delete entries or add more availability.
     *
     * @param date the date to show availability for
     * @param availabilities list of availability entries for the date
     */
    private void showAvailabilityListDialog(LocalDate date, List<Availability> availabilities) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Availability for " + date.format(DateTimeFormatter.ofPattern("MMMM d, yyyy")));
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        VBox content = new VBox(10);
        content.setPadding(new Insets(20));

        for (Availability availability : availabilities) {
            VBox availBox = new VBox(5);
            availBox.setStyle("-fx-border-color: #ccc; -fx-padding: 10; -fx-background-color: #f0f8f0;");

            Label titleLabel = new Label(availability.getTitle());
            titleLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
            titleLabel.setTextFill(getColorForLabel(availability.getColorLabel()));

            Label timeLabel = new Label(
                    availability.getStartTime().format(DateTimeFormatter.ofPattern("HH:mm")) +
                            " - " +
                            availability.getEndTime().format(DateTimeFormatter.ofPattern("HH:mm"))
            );

            Label descriptionLabel = new Label(
                    availability.getDescription().isEmpty() ? "No description" : availability.getDescription()
            );
            descriptionLabel.setWrapText(true);

            Button deleteButton = new Button("Delete");
            deleteButton.setOnAction(e -> {
                dialog.close();
                javafx.application.Platform.runLater(() -> showDeleteAvailabilityDialog(availability));
            });

            availBox.getChildren().addAll(titleLabel, timeLabel, descriptionLabel, deleteButton);
            content.getChildren().add(availBox);
        }

        Button addNewButton = new Button("Add More Availability");
        addNewButton.setOnAction(e -> {
            dialog.close();
            showAddAvailabilityDialog(date);
        });
        content.getChildren().add(addNewButton);

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setPrefSize(450, 350);
        scrollPane.setFitToWidth(true);

        dialog.getDialogPane().setContent(scrollPane);
        dialog.showAndWait();
    }

    /**
     * <hr>
     * Displays a confirmation dialog for deleting an availability entry.
     *
     * @param availability the availability entry to be deleted
     */
    private void showDeleteAvailabilityDialog(Availability availability) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Availability");
        alert.setHeaderText("Are you sure you want to delete \"" + availability.getTitle() + "\"?");
        alert.setContentText("This action cannot be undone.");

        ButtonType deleteButton = new ButtonType("Delete", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

        alert.getButtonTypes().setAll(deleteButton, cancelButton);

        alert.showAndWait().ifPresent(response -> {
            if (response == deleteButton) {
                availabilityManager.removeAvailability(availability);
                updateCalendarView();
            }
        });
    }
}
