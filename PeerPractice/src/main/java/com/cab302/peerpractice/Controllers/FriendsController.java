package com.cab302.peerpractice.Controllers;

import com.cab302.peerpractice.Exceptions.DuplicateFriendException;
import com.cab302.peerpractice.Model.Managers.Notifier;
import com.cab302.peerpractice.AppContext;
import com.cab302.peerpractice.Model.Entities.Friend;
import com.cab302.peerpractice.Model.DAOs.IFriendDAO;
import com.cab302.peerpractice.Model.DAOs.IUserDAO;
import com.cab302.peerpractice.Model.Entities.User;
import com.cab302.peerpractice.Model.Entities.FriendRequestNotification;
import com.cab302.peerpractice.Navigation;
import javafx.animation.FadeTransition;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * <hr>
 * Controller for managing user friendships and social connections.
 *
 * <p>This controller handles the complete friend management lifecycle including
 * sending friend requests, removing friends, viewing friend lists, and searching
 * for users. It provides a comprehensive interface for social networking within
 * the application.
 *
 * <p>Key features include:
 * <ul>
 *   <li>Friend list display with detailed user information</li>
 *   <li>Friend request sending and management</li>
 *   <li>User search by username or email</li>
 *   <li>Interactive table view with selection support</li>
 *   <li>Animated feedback messages</li>
 * </ul>
 *
 * @see Friend
 * @see IFriendDAO
 * @see SidebarController
 */
public class FriendsController extends SidebarController{
    /** <hr> Label for displaying feedback and status messages to the user. */
    @FXML private Label feedbackMsg;
    /** <hr> Label for the friends list section header. */
    @FXML private Label friendsListLabel;
    /** <hr> Text field for searching friends by username or other criteria. */
    @FXML private TextField friendsSearchBox;
    /** <hr> Button for initiating the add friend process. */
    @FXML private Button addFriend;
    /** <hr> Button for removing selected friends. */
    @FXML private Button removeFriend;
    /** <hr> Button for viewing and managing friend requests. */
    @FXML private Button viewRequests;
    /** <hr> Table view for displaying the list of friends. */
    @FXML private TableView<Friend> friendsTable;

    /**
     * <hr>
     * The currently logged-in user for friend operations.
     */
    User currentUser = ctx.getUserSession().getCurrentUser();
    /**
     * <hr>
     * Data access object for user-related database operations.
     */
    IUserDAO userDAO = ctx.getUserDAO();
    /**
     * <hr>
     * Data access object for friend-related database operations.
     */
    IFriendDAO friendDAO = ctx.getFriendDAO();
    /**
     * <hr>
     * Notifier to show friend request notifications
     */
    PopupController popups = new PopupController(ctx, nav);
    /**
     * <hr>
     * Notifier manager for handling friend request notifications
     */
    Notifier notifier = ctx.getNotifier();

    /**
     * <hr>
     * Fade transition for animating feedback messages.
     */
    private final FadeTransition ft = new FadeTransition(Duration.millis(4000));

    /**
     * <hr>
     * Constructs a new FriendsController with the specified context and navigation.
     *
     * @param ctx the application context providing access to user session and managers
     * @param nav the navigation controller for screen transitions
     */
    public FriendsController(AppContext ctx, Navigation nav) {
        super(ctx, nav);
    }

    /**
     * <hr>
     * Initializes the controller after FXML loading is complete.
     *
     * <p>Sets up the friends list table view, configures search functionality,
     * and initializes the feedback message animation system. Calls parent class
     * initialization for common sidebar setup.
     */
    @FXML
    public void initialize() {
        super.initialize();
        try {
            refreshFriendsList();
            updatePendingRequestBadge();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        // setup feedback label to fade-out
        ft.setNode(feedbackMsg);
        ft.setFromValue(1.0);
        ft.setToValue(0.0);
        ft.setCycleCount(1);
        ft.setAutoReverse(false);

        // configure searchbox to listen for enter key presses
        friendsSearchBox.setOnKeyReleased(keyPress -> {
            if (keyPress.getCode() == KeyCode.ENTER) {
                searchFriends();
            }
        });
    }

    /**
     * Updates the badge on the viewRequests button showing pending request count
     */
    private void updatePendingRequestBadge() {
        try {
            int pendingCount = notifier.getPendingFriendRequestCount(currentUser);
            if (pendingCount > 0) {
                viewRequests.setText("View Requests (" + pendingCount + ")");
                viewRequests.setStyle("-fx-font-weight: bold; -fx-padding: 8px; -fx-font-size: 12px;");
            } else {
                viewRequests.setText("View Requests");
                viewRequests.setStyle("-fx-padding: 8px; -fx-font-size: 12px;");
            }
        } catch (Exception e) {
            viewRequests.setText("View Requests");
        }
    }

    /**
     * <hr>
     * Handles friend search functionality.
     *
     * <p>Processes search queries entered in the search box and filters
     * the friends list based on the search criteria (username, first name, last name).
     */
    @FXML
    public void searchFriends() {
        try {
            String query = friendsSearchBox.getText().trim().toLowerCase();

            if (query.isEmpty()) {
                refreshFriendsList();
                return;
            }

            User currentUser = ctx.getUserSession().getCurrentUser();
            List<Friend> allFriends = ctx.getFriendDAO().getFriends(currentUser);

            // Filter friends by search query
            List<Friend> filteredFriends = allFriends.stream()
                    .filter(f -> {
                        User friend = f.getUser2();
                        return friend.getUsername().toLowerCase().contains(query) ||
                               friend.getFirstName().toLowerCase().contains(query) ||
                               friend.getLastName().toLowerCase().contains(query);
                    })
                    .toList();

            buildTableView(filteredFriends);

            if (filteredFriends.isEmpty()) {
                feedbackMsg.setText("No friends match your search");
                ft.playFromStart();
            } else {
                feedbackMsg.setText("Found " + filteredFriends.size() + " friend(s)");
                ft.playFromStart();
            }
        } catch (SQLException e) {
            feedbackMsg.setText("Error searching friends");
            ft.playFromStart();
        }
    }

    /**
     * <hr>
     * Handles the friend addition process.
     *
     * <p>Displays a dialog for users to enter friend identifiers (username or email),
     * validates the target user's existence, and sends friend requests. Provides
     * appropriate feedback for successful requests, failures, and invalid user entries.
     */
    @FXML
    public void addFriend() {
        // set up display window
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Add new friend");

        TextField identifier = new TextField();
        identifier.setPromptText("Username / email");

        VBox content = new VBox(10, identifier);
        dialog.getDialogPane().setContent(content);

        ButtonType request = new ButtonType("Send friend request", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(request, ButtonType.CANCEL);

        // wait until button press
        dialog.showAndWait().ifPresent(response -> {
            if(response == request) {
                try {
                    // check if target user exists, if so, send friend request
                    User friend = userDAO.findUser("username", identifier.getText());
                    // if username returned no users, search using email
                    if (friend == null) { friend = userDAO.findUser("email", identifier.getText());}
                    if (friend != null) {
                        boolean success = friendDAO.addFriend(currentUser, friend);

                        // (un)successful message
                        if (success) {
                            feedbackMsg.setText("Friend request sent!"); ft.playFromStart();
                            popups.friendPopup(currentUser, friend);
                        } else {
                            feedbackMsg.setText("Friend request failed! Maybe already on friends list?"); ft.playFromStart();
                        }
                    } else {
                        feedbackMsg.setText("Couldn't find user with that username or email"); ft.playFromStart();
                    }

                    refreshFriendsList();

                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    /**
     * <hr>
     * Handles friend removal process.
     *
     * <p>Removes the selected friend from the user's friend list after confirmation.
     * Validates selection existence and provides feedback on removal success or failure.
     *
     * @throws SQLException if database access errors occur during friend removal
     */
    @FXML
    public void removeFriend() throws SQLException {
        // exit if nothing selected
        if (getSelection() == null) {
            feedbackMsg.setText("No row selected"); ft.playFromStart();
            return;
        }

        User user = getSelection().getUser1();
        User friend = getSelection().getUser2();

        ctx.getFriendDAO().removeFriend(user, friend);

        // check if friend successfully removed
        boolean friendExists = friendDAO.getFriends(user).contains(getSelection());
        if (friendExists) {
            // (un)successful message
            feedbackMsg.setText("Couldn't remove friend"); ft.playFromStart();
        } else {
            feedbackMsg.setText("Friend removed!"); ft.playFromStart();
        }

        refreshFriendsList();
    }

    /**
     * <hr>
     * Handles friend request viewing functionality.
     *
     * <p>Displays pending friend requests and provides interface for accepting
     * or declining incoming friend requests. Shows a dialog with all pending requests.
     */
    @FXML
    public void viewRequests() {
        try {
            List<FriendRequestNotification> pendingRequests = notifier.getPendingFriendRequests(currentUser);

            if (pendingRequests.isEmpty()) {
                feedbackMsg.setText("No pending friend requests");
                ft.playFromStart();
                return;
            }

            // Create dialog
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("Friend Requests (" + pendingRequests.size() + ")");
            dialog.setHeaderText("Pending Friend Requests");

            // Create table for requests
            TableView<FriendRequestNotification> requestsTable = new TableView<>();
            TableColumn<FriendRequestNotification, String> usernameCol = new TableColumn<>("Username");
            usernameCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getFrom().getUsername()));
            usernameCol.setPrefWidth(100);

            TableColumn<FriendRequestNotification, String> nameCol = new TableColumn<>("Name");
            nameCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getFrom().getFirstName() + " " +
                    cellData.getValue().getFrom().getLastName()));
            nameCol.setPrefWidth(120);

            TableColumn<FriendRequestNotification, String> institutionCol = new TableColumn<>("Institution");
            institutionCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getFrom().getInstitution()));
            institutionCol.setPrefWidth(100);

            TableColumn<FriendRequestNotification, String> timestampCol = new TableColumn<>("Sent");
            timestampCol.setCellValueFactory(cellData -> {
                java.time.LocalDateTime createdAt = cellData.getValue().getCreatedAt();
                if (createdAt != null) {
                    java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("MMM dd, HH:mm");
                    return new SimpleStringProperty(createdAt.format(formatter));
                }
                return new SimpleStringProperty("Unknown");
            });
            timestampCol.setPrefWidth(120);

            requestsTable.getColumns().addAll(usernameCol, nameCol, institutionCol, timestampCol);
            requestsTable.setItems(FXCollections.observableArrayList(pendingRequests));
            requestsTable.setPrefHeight(300);

            // Create action buttons
            HBox buttonBox = new HBox(10);
            buttonBox.setPrefWidth(400);

            Button acceptBtn = new Button("Accept");
            acceptBtn.setPrefWidth(100);
            acceptBtn.setOnAction(e -> {
                FriendRequestNotification selected = requestsTable.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    acceptFriendRequest(selected);
                    pendingRequests.remove(selected);
                    requestsTable.setItems(FXCollections.observableArrayList(pendingRequests));
                    feedbackMsg.setText("Friend request accepted!");
                    ft.playFromStart();
                }
            });

            Button denyBtn = new Button("Deny");
            denyBtn.setPrefWidth(100);
            denyBtn.setOnAction(e -> {
                FriendRequestNotification selected = requestsTable.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    denyFriendRequest(selected);
                    pendingRequests.remove(selected);
                    requestsTable.setItems(FXCollections.observableArrayList(pendingRequests));
                    feedbackMsg.setText("Friend request denied!");
                    ft.playFromStart();
                }
            });

            Button blockBtn = new Button("Block");
            blockBtn.setPrefWidth(100);
            blockBtn.setOnAction(e -> {
                FriendRequestNotification selected = requestsTable.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    blockUser(selected);
                    pendingRequests.remove(selected);
                    requestsTable.setItems(FXCollections.observableArrayList(pendingRequests));
                    feedbackMsg.setText("User blocked!");
                    ft.playFromStart();
                }
            });

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            buttonBox.getChildren().addAll(acceptBtn, denyBtn, blockBtn, spacer);

            VBox content = new VBox(10, new Label("Select a request and click Accept, Deny, or Block:"),
                                     requestsTable, buttonBox);
            VBox.setVgrow(requestsTable, Priority.ALWAYS);
            dialog.getDialogPane().setContent(content);
            dialog.getDialogPane().setPrefWidth(500);
            dialog.getDialogPane().setPrefHeight(400);

            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.CLOSE);
            dialog.showAndWait();

            refreshFriendsList();
        } catch (Exception e) {
            feedbackMsg.setText("Error loading friend requests");
            ft.playFromStart();
        }
    }

    /**
     * <hr>
     * Accepts a pending friend request.
     */
    private void acceptFriendRequest(FriendRequestNotification notification) {
        try {
            notifier.approveNotification(currentUser, notification);
            notifier.clearNotification(currentUser, notification);
        } catch (SQLException e) {
            feedbackMsg.setText("Error accepting request");
            ft.playFromStart();
        }
    }

    /**
     * <hr>
     * Denies a pending friend request.
     */
    private void denyFriendRequest(FriendRequestNotification notification) {
        try {
            notifier.denyNotification(currentUser, notification);
            notifier.clearNotification(currentUser, notification);
        } catch (SQLException e) {
            feedbackMsg.setText("Error denying request");
            ft.playFromStart();
        }
    }

    /**
     * <hr>
     * Blocks a user and denies their friend request.
     */
    private void blockUser(FriendRequestNotification notification) {
        try {
            User requester = notification.getFrom();
            friendDAO.blockUser(currentUser, requester);
            notifier.denyNotification(currentUser, notification);
            notifier.clearNotification(currentUser, notification);
        } catch (SQLException e) {
            feedbackMsg.setText("Error blocking user");
            ft.playFromStart();
        }
    }

    /**
     * <hr>
     * Refreshes the friends list display.
     *
     * <p>Retrieves the current user's friends list from the database and updates
     * the table view. Clears any existing selection after refresh.
     *
     * @throws SQLException if database access errors occur during data retrieval
     */
    private void refreshFriendsList() throws SQLException {
        User currentUser = ctx.getUserSession().getCurrentUser();
        List<Friend> friendsList = ctx.getFriendDAO().getFriends(currentUser);
        buildTableView(friendsList);

        // select last row
        friendsTable.getSelectionModel().select(-1);

        // Update the pending request badge
        updatePendingRequestBadge();
    }

    /**
     * <hr>
     * Builds and configures the friends table view.
     *
     * <p>Sets up table columns for displaying friend information including
     * username, first name, last name, and friendship status. Configures
     * placeholder text for empty lists.
     *
     * @param friendsList the list of friends to display in the table
     */
    private void buildTableView(List<Friend> friendsList) {
        friendsTable.setItems(FXCollections.observableArrayList(friendsList));

        // setup table columns
        TableColumn<Friend,String> friendUsernameCol = new TableColumn<Friend,String>("Username");
        friendUsernameCol.setCellValueFactory(cellValue -> new SimpleStringProperty(cellValue.getValue().getUser2().getUsername()));
        TableColumn<Friend,String> friendFirstnameCol = new TableColumn<Friend,String>("First name");
        friendFirstnameCol.setCellValueFactory(cellValue -> new SimpleStringProperty(cellValue.getValue().getUser2().getFirstName()));
        TableColumn<Friend,String> friendLastnameCol = new TableColumn<Friend,String>("Last name");
        friendLastnameCol.setCellValueFactory(cellValue -> new SimpleStringProperty(cellValue.getValue().getUser2().getLastName()));
        TableColumn<Friend,String> friendStatusCol = new TableColumn<Friend,String>("Status");
        friendStatusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        //TODO Add column to display friend's online/offline status

        // setup placeholder and fill values
        friendsTable.setPlaceholder(new Label("No friends to display"));
        friendsTable.getColumns().setAll(friendUsernameCol, friendFirstnameCol, friendLastnameCol, friendStatusCol);
    }

    /**
     * <hr>
     * Gets the currently selected friend from the table view.
     *
     * @return the currently selected Friend object, or null if no selection
     */
    private Friend getSelection() {
        return friendsTable.getSelectionModel().selectedItemProperty().get();
    }
}