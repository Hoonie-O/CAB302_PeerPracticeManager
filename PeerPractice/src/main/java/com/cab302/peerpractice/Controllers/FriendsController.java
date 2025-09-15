package com.cab302.peerpractice.Controllers;

import com.cab302.peerpractice.AppContext;
import com.cab302.peerpractice.Model.Friend;
import com.cab302.peerpractice.Model.IUserDAO;
import com.cab302.peerpractice.Model.User;
import com.cab302.peerpractice.Navigation;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import javax.swing.*;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

public class FriendsController extends SidebarController{
    @FXML private Label friendsListLabel;
    @FXML private TextField friendsSearchBox;
    @FXML private Button addFriend;
    @FXML private Button removeFriend;
    @FXML private ListView<Friend> friendsListView;

    public FriendsController(AppContext ctx, Navigation nav) {
        super(ctx, nav);
    }

    @FXML
    public void initialize() {
        super.initialize();

        User currentUser = ctx.getUserSession().getCurrentUser();
        List<Friend> friendsList;
        try {
            friendsList = ctx.getFriendDao().getFriends(currentUser);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        friendsListView.setItems(FXCollections.observableArrayList(friendsList));
    }

    @FXML
    public void addFriend() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Add friend");
        dialog.setHeaderText("New friend");

        TextField identifier = new TextField();
        identifier.setPromptText("Username / email");

        Label feedback = new Label();

        VBox content = new VBox(10, identifier, feedback);
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
                        feedback.setText("Friend request sent!");
                    } else {
                        feedback.setText("Couldn't find user with that username or email!");
                    }

                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    @FXML
    public void removeFriend() {

    }
}
