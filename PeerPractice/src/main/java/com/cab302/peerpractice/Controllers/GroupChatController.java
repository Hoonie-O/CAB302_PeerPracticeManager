package com.cab302.peerpractice.Controllers;

import com.cab302.peerpractice.AppContext;
import com.cab302.peerpractice.Model.Entities.Group;
import com.cab302.peerpractice.Model.Entities.GroupMessage;
import com.cab302.peerpractice.Model.Managers.GroupMessageManager;
import com.cab302.peerpractice.Navigation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.util.Duration;

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

    /** Timeline for periodic auto-refresh of group messages */
    private Timeline autoRefreshTimeline;

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
        startAutoRefresh();
    }

    /**
     * Starts the auto-refresh timeline to reload messages every 3 seconds.
     */
    private void startAutoRefresh() {
        stopAutoRefresh(); // ensure only one timeline runs
        autoRefreshTimeline = new Timeline(
                new KeyFrame(Duration.seconds(3), e -> loadMessages())
        );
        autoRefreshTimeline.setCycleCount(Timeline.INDEFINITE);
        autoRefreshTimeline.play();
    }

    /**
     * Stops the auto-refresh timeline (should be called when leaving the chat).
     */
    public void stopAutoRefresh() {
        if (autoRefreshTimeline != null) {
            autoRefreshTimeline.stop();
            autoRefreshTimeline = null;
        }
    }

    /**
     * Loads all messages for the current group and displays them.
     */
    private void loadMessages() {
        if (currentGroup == null) return;

        List<GroupMessage> messages = groupMessageManager.getMessages(currentGroup.getID());

        Platform.runLater(() -> {
            messagesBox.getChildren().clear();
            for (GroupMessage msg : messages) {
                addMessageToView(msg);
            }
            scrollPane.layout();
            scrollPane.setVvalue(1.0);
        });
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
        Text senderText = new Text(message.getSenderId());
        senderText.getStyleClass().add("sender-text");

        Text contentText = new Text(message.getContent() + " ");
        contentText.getStyleClass().add("message-text");

        String timeString = message.getTimestamp().toLocalTime().withNano(0).toString();
        Text timestampText = new Text(timeString);
        timestampText.getStyleClass().add("timestamp-text");

        TextFlow messageContentFlow = new TextFlow(contentText, timestampText);
        messageContentFlow.setLineSpacing(2);

        VBox messageBubble = new VBox();
        messageBubble.getChildren().addAll(senderText, messageContentFlow);
        messageBubble.getStyleClass().add("message-bubble");
        messageBubble.setMaxWidth(350);
        messageBubble.setSpacing(2);
        VBox.setMargin(messageBubble, new Insets(0, 0, 6, 0));

        Platform.runLater(() -> {
            messagesBox.getChildren().add(messageBubble);
        });
    }

    /**
     * Should be called when this view is closed or navigated away from.
     * Prevents background refresh from running.
     */
    public void onClose() {
        stopAutoRefresh();
    }
}
