package com.cab302.peerpractice.Controllers;

import com.cab302.peerpractice.AppContext;
import com.cab302.peerpractice.Model.Entities.Group;
import com.cab302.peerpractice.Model.Entities.GroupMessage;
import com.cab302.peerpractice.Model.Managers.GroupMessageManager;
import com.cab302.peerpractice.Navigation;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Controller for displaying and sending group chat messages.
 */
public class GroupChatController extends BaseController {

    private final GroupMessageManager groupMessageManager;
    private Group currentGroup;

    @FXML
    private VBox messagesBox;

    @FXML
    private TextField messageInput;

    @FXML
    private Button sendButton;

    /**
     * Constructs a new GroupChatController.
     *
     * @param ctx the application context providing access to user session and managers
     * @param nav the navigation controller for screen transitions
     */
    protected GroupChatController(AppContext ctx, Navigation nav) {
        super(ctx, nav);
        this.groupMessageManager = ctx.getGroupMessageManager();
    }

    /**
     * Sets the group for this chat view and loads its messages.
     *
     * @param group the group to display messages for
     */
    public void setGroup(Group group) {
        this.currentGroup = group;
        loadMessages();
    }

    /**
     * Loads all messages for the current group and displays them.
     */
    private void loadMessages() {
        if (currentGroup == null) return;

        messagesBox.getChildren().clear();
        List<GroupMessage> messages = groupMessageManager.getMessages(currentGroup.getID());

        for (GroupMessage msg : messages) {
            addMessageToView(msg);
        }
    }

    /**
     * Handles sending a new message when the Send button is pressed.
     */
    @FXML
    private void onSendMessage() {
        String content = messageInput.getText().trim();
        if (content.isEmpty() || currentGroup == null) return;

        GroupMessage message = new GroupMessage(
                UUID.randomUUID().toString(),
                ctx.getUserSession().getCurrentUser().getUsername(),
                content,
                LocalDateTime.now(),
                currentGroup.getID()
        );

        groupMessageManager.sendMessage(message);
        addMessageToView(message);
        messageInput.clear();
    }

    /**
     * Adds a single message to the message box UI.
     *
     * @param message the message to display
     */
    private void addMessageToView(GroupMessage message) {
        Text senderText = new Text(message.getSenderId() + "\n");
        senderText.getStyleClass().add("sender-text");

        Text contentText = new Text(message.getContent());
        contentText.getStyleClass().add("message-text");

        TextFlow messageBubble = new TextFlow(senderText, contentText);
        messageBubble.getStyleClass().add("message-bubble");
        messageBubble.setMaxWidth(350);

        Platform.runLater(() -> messagesBox.getChildren().add(messageBubble));
    }
}
