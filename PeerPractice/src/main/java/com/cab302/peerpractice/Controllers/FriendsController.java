package com.cab302.peerpractice.Controllers;

import com.cab302.peerpractice.AppContext;
import com.cab302.peerpractice.Model.Friend;
import com.cab302.peerpractice.Model.IFriendDAO;
import com.cab302.peerpractice.Model.IUserDAO;
import com.cab302.peerpractice.Model.User;
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
import java.util.Objects;

public class FriendsController extends SidebarController{
    @FXML private Label feedbackMsg;
    @FXML private Label friendsListLabel;
    @FXML private TextField friendsSearchBox;
    @FXML private Button addFriend;
    @FXML private Button removeFriend;
    @FXML private Button viewRequests;
    @FXML private TableView<Friend> friendsTable;

    User currentUser = ctx.getUserSession().getCurrentUser();
    IUserDAO userDAO = ctx.getUserDao();
    IFriendDAO friendDAO = ctx.getFriendDao();

    private final FadeTransition ft = new FadeTransition(Duration.millis(4000));

    public FriendsController(AppContext ctx, Navigation nav) {
        super(ctx, nav);
    }

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

    @FXML
    public void searchFriends() {
        feedbackMsg.setText("TO-DO: Search friends"); ft.playFromStart();
    }

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

    @FXML
    public void removeFriend() throws SQLException {
        // exit if nothing selected
        if (getSelection() == null) {
            feedbackMsg.setText("No row selected"); ft.playFromStart();
            return;
        }

        User user = getSelection().getUser1();
        User friend = getSelection().getUser2();

        ctx.getFriendDao().removeFriend(user, friend);

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

    @FXML
    public void viewRequests() {
        feedbackMsg.setText("TO-DO: View requests"); ft.playFromStart();
    }

    private void refreshFriendsList() throws SQLException {
        User currentUser = ctx.getUserSession().getCurrentUser();
        List<Friend> friendsList = ctx.getFriendDao().getFriends(currentUser);
        buildTableView(friendsList);

        // select last row
        friendsTable.getSelectionModel().select(-1);
    }

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

    // get current selected row
    private Friend getSelection() {
        return friendsTable.getSelectionModel().selectedItemProperty().get();
    };
}