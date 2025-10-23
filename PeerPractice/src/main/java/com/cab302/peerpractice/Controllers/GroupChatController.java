package com.cab302.peerpractice.Controllers;

import com.cab302.peerpractice.AppContext;
import com.cab302.peerpractice.Model.Entities.Group;
import com.cab302.peerpractice.Model.Entities.GroupMessage;
import com.cab302.peerpractice.Model.Managers.GroupMessageManager;
import com.cab302.peerpractice.Navigation;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
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
    private ScrollPane scrollPane;

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

        Platform.runLater(() -> messagesBox.getChildren().clear());
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
        // Sender
        Text senderText = new Text(message.getSenderId());
        senderText.getStyleClass().add("sender-text");

        // Content
        Text contentText = new Text(message.getContent() + " ");
        contentText.getStyleClass().add("message-text");

        // Timestamp
        String timeString = message.getTimestamp().toLocalTime().withNano(0).toString();
        Text timestampText = new Text(timeString);
        timestampText.getStyleClass().add("timestamp-text");

        // Combine content + timestamp in a flow
        TextFlow messageContentFlow = new TextFlow(contentText, timestampText);
        messageContentFlow.setLineSpacing(2); // small line spacing for readability

        // Bubble containing sender + message
        VBox messageBubble = new VBox();
        messageBubble.getChildren().addAll(senderText, messageContentFlow);
        messageBubble.getStyleClass().add("message-bubble");
        messageBubble.setMaxWidth(350);
        messageBubble.setSpacing(2); // space between username and message

        // Add a little margin below the bubble for spacing between messages
        VBox.setMargin(messageBubble, new Insets(0, 0, 6, 0)); // 6px gap to next message

        Platform.runLater(() -> {
            messagesBox.getChildren().add(messageBubble);
            scrollPane.layout();
            scrollPane.setVvalue(1.0);
        });
    }
}
