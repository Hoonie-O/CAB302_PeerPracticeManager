package com.cab302.peerpractice.Controllers;

import com.cab302.peerpractice.AppContext;
import com.cab302.peerpractice.Model.Entities.Group;
import com.cab302.peerpractice.Model.Entities.GroupFile;
import com.cab302.peerpractice.Model.Managers.GroupFileManager;
import com.cab302.peerpractice.Navigation;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Controller for displaying and managing group file sharing.
 */
public class GroupFileController extends BaseController {

    private final GroupFileManager groupFileManager;
    private Group currentGroup;

    @FXML
    private VBox dropZone;

    @FXML
    private Button browseButton;

    @FXML
    private Label uploadStatusLabel;

    @FXML
    private ListView<GroupFile> filesListView;

    @FXML
    private Button downloadButton;

    @FXML
    private Button deleteButton;

    @FXML
    private Button refreshButton;

    /**
     * Constructs a new GroupFileController.
     *
     * @param ctx the application context providing access to user session and managers
     * @param nav the navigation controller for screen transitions
     */
    protected GroupFileController(AppContext ctx, Navigation nav) {
        super(ctx, nav);
        this.groupFileManager = ctx.getGroupFileManager();
    }

    /**
     * Initializes the controller after FXML injection.
     */
    @FXML
    public void initialize() {
        setupDragAndDrop();
        setupFileListView();
        setupSelectionListener();
    }

    /**
     * Sets the group for this file view and loads its files.
     *
     * @param group the group to display files for
     */
    public void setGroup(Group group) {
        this.currentGroup = group;
        loadFiles();
    }

    /**
     * Sets up drag and drop functionality for the drop zone.
     */
    private void setupDragAndDrop() {
        dropZone.setOnDragOver(this::handleDragOver);
        dropZone.setOnDragDropped(this::handleDragDropped);
        dropZone.setOnDragExited(event -> {
            dropZone.setStyle("-fx-border-color: #cccccc; -fx-border-width: 2; -fx-border-style: dashed; -fx-background-color: #f9f9f9; -fx-padding: 20;");
        });
        dropZone.setOnDragEntered(event -> {
            if (event.getGestureSource() != dropZone && event.getDragboard().hasFiles()) {
                dropZone.setStyle("-fx-border-color: #4CAF50; -fx-border-width: 2; -fx-border-style: dashed; -fx-background-color: #e8f5e9; -fx-padding: 20;");
            }
        });
    }

    /**
     * Handles drag over event.
     */
    private void handleDragOver(DragEvent event) {
        if (event.getGestureSource() != dropZone && event.getDragboard().hasFiles()) {
            event.acceptTransferModes(TransferMode.COPY);
        }
        event.consume();
    }

    /**
     * Handles file drop event.
     */
    private void handleDragDropped(DragEvent event) {
        Dragboard dragboard = event.getDragboard();
        boolean success = false;

        if (dragboard.hasFiles()) {
            success = true;
            List<File> files = dragboard.getFiles();
            for (File file : files) {
                uploadFile(file);
            }
        }

        event.setDropCompleted(success);
        event.consume();
    }

    /**
     * Sets up the ListView to display files with custom formatting.
     */
    private void setupFileListView() {
        filesListView.setCellFactory(param -> new ListCell<GroupFile>() {
            @Override
            protected void updateItem(GroupFile file, boolean empty) {
                super.updateItem(file, empty);

                if (empty || file == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    HBox container = new HBox(10);
                    container.setAlignment(Pos.CENTER_LEFT);
                    container.setPadding(new Insets(8));

                    // File icon/type indicator
                    Label iconLabel = new Label(getFileIcon(file.getMimeType()));
                    iconLabel.setStyle("-fx-font-size: 24px;");

                    // File details
                    VBox detailsBox = new VBox(2);
                    Label nameLabel = new Label(file.getFilename());
                    nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");

                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");
                    String details = String.format("Uploaded by %s • %s • %s",
                            file.getUploaderId(),
                            file.getUploadedAt().format(formatter),
                            file.getFormattedFileSize());

                    Label detailsLabel = new Label(details);
                    detailsLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #666666;");

                    detailsBox.getChildren().addAll(nameLabel, detailsLabel);

                    // Spacer
                    Region spacer = new Region();
                    HBox.setHgrow(spacer, Priority.ALWAYS);

                    container.getChildren().addAll(iconLabel, detailsBox, spacer);
                    setGraphic(container);
                }
            }
        });
    }

    /**
     * Returns an emoji icon based on file MIME type.
     */
    private String getFileIcon(String mimeType) {
        if (mimeType == null) return "\uD83D\uDCC4"; // Document

        if (mimeType.startsWith("audio/")) return "\uD83C\uDFB5"; // Musical note
        if (mimeType.startsWith("image/")) return "\uD83D\uDDBC"; // Picture
        if (mimeType.equals("application/pdf")) return "\uD83D\uDCC4"; // Document
        if (mimeType.contains("word") || mimeType.equals("text/plain")) return "\uD83D\uDCC4"; // Document
        if (mimeType.contains("zip") || mimeType.contains("archive")) return "\uD83D\uDCE6"; // Package

        return "\uD83D\uDCC4"; // Default document icon
    }

    /**
     * Sets up selection listener to enable/disable action buttons.
     */
    private void setupSelectionListener() {
        filesListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            boolean hasSelection = newVal != null;
            downloadButton.setDisable(!hasSelection);

            // Only allow deletion if user is the uploader
            if (hasSelection) {
                String currentUser = ctx.getUserSession().getCurrentUser().getUsername();
                deleteButton.setDisable(!newVal.getUploaderId().equals(currentUser));
            } else {
                deleteButton.setDisable(true);
            }
        });
    }

    /**
     * Loads all files for the current group.
     */
    private void loadFiles() {
        if (currentGroup == null) return;

        Platform.runLater(() -> {
            List<GroupFile> files = groupFileManager.getFilesForGroup(currentGroup.getID());
            filesListView.getItems().clear();
            filesListView.getItems().addAll(files);
        });
    }

    /**
     * Handles the browse files button click.
     */
    @FXML
    private void onBrowseFiles() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Files to Upload");

        // Add file filters
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("All Files", "*.*"),
                new FileChooser.ExtensionFilter("Audio Files", "*.mp3", "*.wav", "*.ogg", "*.m4a", "*.flac"),
                new FileChooser.ExtensionFilter("PDF Files", "*.pdf"),
                new FileChooser.ExtensionFilter("Documents", "*.doc", "*.docx", "*.txt", "*.rtf"),
                new FileChooser.ExtensionFilter("Images", "*.jpg", "*.jpeg", "*.png", "*.gif")
        );

        List<File> selectedFiles = fileChooser.showOpenMultipleDialog(browseButton.getScene().getWindow());

        if (selectedFiles != null) {
            for (File file : selectedFiles) {
                uploadFile(file);
            }
        }
    }

    /**
     * Uploads a file to the group.
     */
    private void uploadFile(File file) {
        if (currentGroup == null) {
            showError("No group selected");
            return;
        }

        // Validate file size (50MB limit)
        final long MAX_FILE_SIZE = 50 * 1024 * 1024; // 50 MB in bytes
        if (file.length() > MAX_FILE_SIZE) {
            showError("File too large. Maximum file size is 50 MB.");
            return;
        }

        // Validate file exists and is readable
        if (!file.exists() || !file.canRead()) {
            showError("Cannot read file: " + file.getName());
            return;
        }

        String currentUser = ctx.getUserSession().getCurrentUser().getUsername();
        uploadStatusLabel.setText("Uploading " + file.getName() + "...");

        try {
            GroupFile uploadedFile = groupFileManager.uploadFile(
                    currentGroup.getID(),
                    currentUser,
                    file,
                    "" // No description for now
            );

            if (uploadedFile != null) {
                uploadStatusLabel.setText("Successfully uploaded " + file.getName());
                Platform.runLater(() -> {
                    filesListView.getItems().add(0, uploadedFile); // Add to top of list

                    // Clear status after 3 seconds
                    new Thread(() -> {
                        try {
                            Thread.sleep(3000);
                            Platform.runLater(() -> uploadStatusLabel.setText(""));
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    }).start();
                });
            } else {
                showError("Failed to upload " + file.getName());
            }
        } catch (IOException e) {
            showError("Error uploading file: " + e.getMessage());
        }
    }

    /**
     * Handles the download file button click.
     */
    @FXML
    private void onDownloadFile() {
        GroupFile selectedFile = filesListView.getSelectionModel().getSelectedItem();
        if (selectedFile == null) return;

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save File");
        fileChooser.setInitialFileName(selectedFile.getFilename());

        File destinationFile = fileChooser.showSaveDialog(downloadButton.getScene().getWindow());

        if (destinationFile != null) {
            try {
                File sourceFile = groupFileManager.getPhysicalFile(selectedFile.getFileId());
                if (sourceFile != null && sourceFile.exists()) {
                    Files.copy(sourceFile.toPath(), destinationFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    showSuccess("File downloaded successfully");
                } else {
                    showError("File not found on server");
                }
            } catch (IOException e) {
                showError("Error downloading file: " + e.getMessage());
            }
        }
    }

    /**
     * Handles the delete file button click.
     */
    @FXML
    private void onDeleteFile() {
        GroupFile selectedFile = filesListView.getSelectionModel().getSelectedItem();
        if (selectedFile == null) return;

        // Confirm deletion
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Delete File");
        confirmAlert.setHeaderText("Delete " + selectedFile.getFilename() + "?");
        confirmAlert.setContentText("This action cannot be undone.");

        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                boolean deleted = groupFileManager.deleteFile(selectedFile.getFileId());
                if (deleted) {
                    Platform.runLater(() -> {
                        filesListView.getItems().remove(selectedFile);
                        showSuccess("File deleted successfully");
                    });
                } else {
                    showError("Failed to delete file");
                }
            }
        });
    }

    /**
     * Handles the refresh button click.
     */
    @FXML
    private void onRefresh() {
        loadFiles();
        uploadStatusLabel.setText("Files refreshed");
        new Thread(() -> {
            try {
                Thread.sleep(2000);
                Platform.runLater(() -> uploadStatusLabel.setText(""));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }

    /**
     * Shows an error message.
     */
    private void showError(String message) {
        Platform.runLater(() -> {
            uploadStatusLabel.setText("Error: " + message);
            uploadStatusLabel.setStyle("-fx-text-fill: #f44336;");

            new Thread(() -> {
                try {
                    Thread.sleep(5000);
                    Platform.runLater(() -> {
                        uploadStatusLabel.setText("");
                        uploadStatusLabel.setStyle("-fx-text-fill: #666666;");
                    });
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }).start();
        });
    }

    /**
     * Shows a success message.
     */
    private void showSuccess(String message) {
        Platform.runLater(() -> {
            uploadStatusLabel.setText(message);
            uploadStatusLabel.setStyle("-fx-text-fill: #4CAF50;");

            new Thread(() -> {
                try {
                    Thread.sleep(3000);
                    Platform.runLater(() -> {
                        uploadStatusLabel.setText("");
                        uploadStatusLabel.setStyle("-fx-text-fill: #666666;");
                    });
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }).start();
        });
    }
}
