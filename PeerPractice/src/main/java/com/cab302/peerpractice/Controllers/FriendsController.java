package com.cab302.peerpractice.Controllers;

import com.cab302.peerpractice.AppContext;
import com.cab302.peerpractice.Model.Friend;
import com.cab302.peerpractice.Model.IFriendDAO;
import com.cab302.peerpractice.Model.IUserDAO;
import com.cab302.peerpractice.Model.User;
import com.cab302.peerpractice.Navigation;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import javax.security.auth.callback.Callback;
import javax.swing.*;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

public class FriendsController extends SidebarController{
    @FXML private Label friendsListLabel;
    @FXML private TextField friendsSearchBox;
    @FXML private Button addFriend;
    @FXML private Button removeFriend;
    @FXML private TableView<Friend> friendsTable;

    private ObservableList selected;

    public FriendsController(AppContext ctx, Navigation nav) {
        super(ctx, nav);
    }

    @FXML
    public void initialize() throws SQLException {
        super.initialize();
        refreshFriendsList();

        TableView.TableViewSelectionModel friendsTableSelection = friendsTable.getSelectionModel();
        friendsTableSelection.setSelectionMode(SelectionMode.SINGLE);
    }

    @FXML
    public void addFriend() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Add friend");
        dialog.setHeaderText("New friend");

        TextField identifier = new TextField();
        identifier.setPromptText("Username / email");

        VBox content = new VBox(10, identifier);
        dialog.getDialogPane().setContent(content);

        ButtonType request = new ButtonType("Send friend request", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(request, ButtonType.CANCEL);

        dialog.showAndWait().ifPresent(response -> {
            if(response == request) {
                User currentUser = ctx.getUserSession().getCurrentUser();
                IUserDAO userDAO = ctx.getUserDao();

                // Check if friend exists, if so, send friend request
                try {
                    User friend = userDAO.findUser("username", identifier.getText());
                    if (Objects.nonNull(friend)) {
                        ctx.getFriendDao().addFriend(currentUser, friend);

                        new Alert(Alert.AlertType.INFORMATION, "Friend request sent!").showAndWait();
                    } else {
                        new Alert(Alert.AlertType.INFORMATION, "Couldn't find user with that username or email!").showAndWait();
                    }

                    refreshFriendsList();

                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    @FXML
    public void removeFriend() {

    }

    private void refreshFriendsList() throws SQLException {
        User currentUser = ctx.getUserSession().getCurrentUser();
        List<Friend> friendsList = ctx.getFriendDao().getFriends(currentUser);
        friendsTable.setItems(FXCollections.observableArrayList(friendsList));

        TableColumn<Friend,String> friendNameCol = new TableColumn<Friend,String>("Name");
        friendNameCol.setCellValueFactory(new PropertyValueFactory("user2"));
        TableColumn<Friend,String> friendStatusCol = new TableColumn<Friend,String>("Status");
        friendStatusCol.setCellValueFactory(new PropertyValueFactory("status"));

        friendsTable.setPlaceholder(new Label("No friends to display"));
        friendsTable.getColumns().setAll(friendNameCol, friendStatusCol);
    }

    // Cell factory to build necessary columns for friend details
    private TableCell<Friend,User> makeFriendInfoCells() {
        return new TableCell<Friend,User>() {

        };
    }
}
