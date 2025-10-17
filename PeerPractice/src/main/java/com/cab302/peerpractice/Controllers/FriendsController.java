package com.cab302.peerpractice.Controllers;

import com.cab302.peerpractice.Exceptions.DuplicateFriendException;
import com.cab302.peerpractice.Model.Managers.Notifier;
import com.cab302.peerpractice.AppContext;
import com.cab302.peerpractice.Model.Entities.Friend;
import com.cab302.peerpractice.Model.DAOs.IFriendDAO;
import com.cab302.peerpractice.Model.DAOs.IUserDAO;
import com.cab302.peerpractice.Model.Entities.User;
import com.cab302.peerpractice.Navigation;
import javafx.animation.FadeTransition;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.sql.SQLException;
import java.util.List;

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
     * <hr>
     * Handles friend search functionality.
     *
     * <p>Processes search queries entered in the search box and filters
     * the friends list based on the search criteria. Currently displays
     * a placeholder message indicating the feature is under development.
     */
    @FXML
    public void searchFriends() {
        feedbackMsg.setText("TO-DO: Search friends"); ft.playFromStart();
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
     * or declining incoming friend requests. Currently displays a placeholder
     * message indicating the feature is under development.
     */
    @FXML
    public void viewRequests() {
        feedbackMsg.setText("TO-DO: View requests"); ft.playFromStart();
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