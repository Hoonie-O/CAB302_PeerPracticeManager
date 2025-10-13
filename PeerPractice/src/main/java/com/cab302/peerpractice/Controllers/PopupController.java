package com.cab302.peerpractice.Controllers;

import com.cab302.peerpractice.AppContext;
import com.cab302.peerpractice.Model.Entities.FriendRequestNotification;
import com.cab302.peerpractice.Model.Entities.User;
import com.cab302.peerpractice.Model.Managers.Notifier;
import com.cab302.peerpractice.Navigation;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Popup;
import javafx.stage.PopupWindow;
import org.controlsfx.control.spreadsheet.Grid;

import java.sql.SQLException;

public class PopupController extends BaseController{

    private final Notifier notifier;
    /**
     * <hr>
     * Constructs a new BaseController with the specified context and navigation.
     *
     * @param ctx the application context providing access to user session and managers
     * @param nav the navigation controller for screen transitions
     */
    protected PopupController(AppContext ctx, Navigation nav) {
        super(ctx, nav);
        this.notifier = new Notifier(ctx.getUserDAO(), ctx.getFriendDAO());
    }

    /**
     * Displays a popup for a friend request with buttons to take actions
     * @param from The user sending the friend request
     * @param to The user receiving the friend request
     * @throws SQLException error
     */
    public void friendPopup(User from, User to) throws SQLException {
        FriendRequestNotification n = notifier.createFriendRequest(from, to);

        Popup popup = new Popup();
        Label msg = new Label(n.getMsg());
        Button accept = new Button("Accept");
        Button ignore = new Button("Ignore");
        Button deny = new Button("Block");

        accept.setOnAction(e -> {
            try {
                notifier.approveNotification(from, n);
                popup.hide();
                resultPopup("Accepted friend request!");
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        });
        ignore.setOnAction(e -> {
            try {
                notifier.denyNotification(from, n);
                popup.hide();
                resultPopup("Ignored friend request!");
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        });
        deny.setOnAction(e -> {
            try {
                notifier.denyNotification(from, n);
                popup.hide();
                resultPopup("Denied friend request!");
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        });

        FlowPane popupInfo = formatPopup();
        popupInfo.getChildren().addAll(msg, accept, ignore, deny);
        popup.getContent().add(popupInfo);

        // display appropriate popup depending on current user
        if (ctx.getUserSession().getCurrentUser() == from) {
            resultPopup("Friend request sent!");
        } else {
            nav.displayPopup(popup);
        }
    }

    /**
     * Displays a popup with a simple message
     * @param feedback string to display
     */
    public void resultPopup(String feedback) {
        Popup popup = new Popup();
        Label text = new Label(feedback);

        FlowPane popupInfo = formatPopup();
        popupInfo.getChildren().addAll(text);
        popup.getContent().add(popupInfo);

        popup.setAutoHide(true);
        nav.displayPopup(popup);
    }

    /**
     * Generates a blank popup
     * @return popup object
     */
    private FlowPane formatPopup() {
        FlowPane popupInfo = new FlowPane(20, 20);
        popupInfo.setMaxSize(40, 40);
        popupInfo.setPadding(new Insets(20));
        popupInfo.setStyle("-fx-opacity: 0.8; -fx-border-color: black; -fx-background-color: #f5f5f5");

        return popupInfo;
    }
}