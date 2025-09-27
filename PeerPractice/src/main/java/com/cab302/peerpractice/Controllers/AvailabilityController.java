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

public class AvailabilityController extends SidebarController {
    @FXML private Label monthYearLabel;
    @FXML private GridPane calendarGrid;
    @FXML private Button prevButton;
    @FXML private Button nextButton;

    private YearMonth currentYearMonth;
    private AvailabilityManager availabilityManager;

    public AvailabilityController(AppContext ctx, Navigation nav) {
        super(ctx, nav);
        this.availabilityManager = ctx.getAvailabilityManager();
    }

    @FXML
    public void initialize() {
        super.initialize();
        currentYearMonth = YearMonth.now();
        updateCalendarView();
    }

    private void updateCalendarView() {
        monthYearLabel.setText(currentYearMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")));
        populateCalendarGrid();
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

        User currentUser = ctx.getUserSession().getCurrentUser();
        if (currentUser != null) {
            List<Availability> availabilities = availabilityManager.getAvailabilitiesForDate(date);

            availabilities = availabilities.stream()
                    .filter(avail -> avail.getUser().equals(currentUser))
                    .toList();

            for (Availability availability : availabilities) {
                Label availLabel = new Label(availability.getTitle());
                availLabel.setFont(Font.font("System", 8));
                availLabel.setTextFill(getColorForLabel(availability.getColorLabel()));
                availLabel.setPrefWidth(75);
                availLabel.setWrapText(true);
                dayCell.getChildren().add(availLabel);
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
