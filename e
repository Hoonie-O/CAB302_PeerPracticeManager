[1mdiff --git a/PeerPractice/PeerPracticeManager.db b/PeerPractice/PeerPracticeManager.db[m
[1mindex d6e7ed4..0d3deda 100644[m
Binary files a/PeerPractice/PeerPracticeManager.db and b/PeerPractice/PeerPracticeManager.db differ
[1mdiff --git a/PeerPractice/src/main/java/com/cab302/peerpractice/AppContext.java b/PeerPractice/src/main/java/com/cab302/peerpractice/AppContext.java[m
[1mindex b3cc935..f03bddd 100644[m
[1m--- a/PeerPractice/src/main/java/com/cab302/peerpractice/AppContext.java[m
[1m+++ b/PeerPractice/src/main/java/com/cab302/peerpractice/AppContext.java[m
[36m@@ -9,6 +9,7 @@[m [mpublic class AppContext {[m
     private final UserSession userSession = new UserSession();[m
     private final IUserDAO userDao = new UserDAO();[m
     private final IGroupDAO groupDao = new MockGroupDAO();[m
[32m+[m[32m    private final IFriendDAO friendDao = new FriendDAO();[m
     private final Notifier notifier = new Notifier(userDao);[m
     private final PasswordHasher passwordHasher = new BcryptHasher();[m
     private final UserManager userManager = new UserManager(userDao,passwordHasher);[m
[36m@@ -49,6 +50,7 @@[m [mpublic class AppContext {[m
     public MailService getMailService(){return mailService;}[m
     public GroupManager getGroupManager(){return  groupManager;}[m
     public IGroupDAO getGroupDao() {return groupDao;}[m
[32m+[m[32m    public IFriendDAO getFriendDao() {return friendDao;}[m
     public SessionManager getSessionManager(){return sessionManager;}[m
     public SessionTaskManager getSessionTaskManager(){return sessionTaskManager;}[m
     public SessionCalendarManager getSessionCalendarManager(){return sessionCalendarManager;}[m
[36m@@ -57,5 +59,5 @@[m [mpublic class AppContext {[m
     public void setMenuOpen(boolean value) { this.menuOpen = value; }[m
     public boolean isProfileOpen() { return profileOpen; }[m
     public void setProfileOpen(boolean value) { this.profileOpen = value; }[m
[31m-    [m
[32m+[m
 }[m
[1mdiff --git a/PeerPractice/src/main/java/com/cab302/peerpractice/Controllers/CalendarController.java b/PeerPractice/src/main/java/com/cab302/peerpractice/Controllers/CalendarController.java[m
[1mindex d69afa1..33a423c 100644[m
[1m--- a/PeerPractice/src/main/java/com/cab302/peerpractice/Controllers/CalendarController.java[m
[1m+++ b/PeerPractice/src/main/java/com/cab302/peerpractice/Controllers/CalendarController.java[m
[36m@@ -14,6 +14,7 @@[m [mimport javafx.scene.paint.Color;[m
 import javafx.scene.text.Font;[m
 import javafx.scene.text.FontWeight;[m
 [m
[32m+[m[32mimport java.sql.SQLException;[m
 import java.time.LocalDate;[m
 import java.time.LocalDateTime;[m
 import java.time.LocalTime;[m
[1mdiff --git a/PeerPractice/src/main/java/com/cab302/peerpractice/Controllers/FriendsController.java b/PeerPractice/src/main/java/com/cab302/peerpractice/Controllers/FriendsController.java[m
[1mnew file mode 100644[m
[1mindex 0000000..bddaae0[m
[1m--- /dev/null[m
[1m+++ b/PeerPractice/src/main/java/com/cab302/peerpractice/Controllers/FriendsController.java[m
[36m@@ -0,0 +1,178 @@[m
[32m+[m[32mpackage com.cab302.peerpractice.Controllers;[m
[32m+[m
[32m+[m[32mimport com.cab302.peerpractice.AppContext;[m
[32m+[m[32mimport com.cab302.peerpractice.Model.Friend;[m
[32m+[m[32mimport com.cab302.peerpractice.Model.IFriendDAO;[m
[32m+[m[32mimport com.cab302.peerpractice.Model.IUserDAO;[m
[32m+[m[32mimport com.cab302.peerpractice.Model.User;[m
[32m+[m[32mimport com.cab302.peerpractice.Navigation;[m
[32m+[m[32mimport javafx.animation.FadeTransition;[m
[32m+[m[32mimport javafx.beans.property.SimpleStringProperty;[m
[32m+[m[32mimport javafx.collections.FXCollections;[m
[32m+[m[32mimport javafx.fxml.FXML;[m
[32m+[m[32mimport javafx.scene.control.*;[m
[32m+[m[32mimport javafx.scene.control.cell.PropertyValueFactory;[m
[32m+[m[32mimport javafx.scene.input.KeyCode;[m
[32m+[m[32mimport javafx.scene.layout.VBox;[m
[32m+[m[32mimport javafx.util.Duration;[m
[32m+[m
[32m+[m[32mimport java.sql.SQLException;[m
[32m+[m[32mimport java.util.List;[m
[32m+[m[32mimport java.util.Objects;[m
[32m+[m
[32m+[m[32mpublic class FriendsController extends SidebarController{[m
[32m+[m[32m    @FXML private Label feedbackMsg;[m
[32m+[m[32m    @FXML private Label friendsListLabel;[m
[32m+[m[32m    @FXML private TextField friendsSearchBox;[m
[32m+[m[32m    @FXML private Button addFriend;[m
[32m+[m[32m    @FXML private Button removeFriend;[m
[32m+[m[32m    @FXML private Button viewRequests;[m
[32m+[m[32m    @FXML private TableView<Friend> friendsTable;[m
[32m+[m
[32m+[m[32m    User currentUser = ctx.getUserSession().getCurrentUser();[m
[32m+[m[32m    IUserDAO userDAO = ctx.getUserDao();[m
[32m+[m[32m    IFriendDAO friendDAO = ctx.getFriendDao();[m
[32m+[m
[32m+[m[32m    private final FadeTransition ft = new FadeTransition(Duration.millis(4000));[m
[32m+[m
[32m+[m[32m    public FriendsController(AppContext ctx, Navigation nav) {[m
[32m+[m[32m        super(ctx, nav);[m
[32m+[m[32m    }[m
[32m+[m
[32m+[m[32m    @FXML[m
[32m+[m[32m    public void initialize() {[m
[32m+[m[32m        super.initialize();[m
[32m+[m[32m        try {[m
[32m+[m[32m            refreshFriendsList();[m
[32m+[m[32m        } catch (SQLException e) {[m
[32m+[m[32m            throw new RuntimeException(e);[m
[32m+[m[32m        }[m
[32m+[m
[32m+[m[32m        // setup feedback label to fade-out[m
[32m+[m[32m        ft.setNode(feedbackMsg);[m
[32m+[m[32m        ft.setFromValue(1.0);[m
[32m+[m[32m        ft.setToValue(0.0);[m
[32m+[m[32m        ft.setCycleCount(1);[m
[32m+[m[32m        ft.setAutoReverse(false);[m
[32m+[m
[32m+[m[32m        // configure searchbox to listen for enter key presses[m
[32m+[m[32m        friendsSearchBox.setOnKeyReleased(keyPress -> {[m
[32m+[m[32m            if (keyPress.getCode() == KeyCode.ENTER) {[m
[32m+[m[32m                searchFriends();[m
[32m+[m[32m            }[m
[32m+[m[32m        });[m
[32m+[m[32m    }[m
[32m+[m
[32m+[m[32m    @FXML[m
[32m+[m[32m    public void searchFriends() {[m
[32m+[m[32m        feedbackMsg.setText("TO-DO: Search friends"); ft.playFromStart();[m
[32m+[m[32m    }[m
[32m+[m
[32m+[m[32m    @FXML[m
[32m+[m[32m    public void addFriend() {[m
[32m+[m[32m        // set up display window[m
[32m+[m[32m        Dialog<ButtonType> dialog = new Dialog<>();[m
[32m+[m[32m        dialog.setTitle("Add new friend");[m
[32m+[m
[32m+[m[32m        TextField identifier = new TextField();[m
[32m+[m[32m        identifier.setPromptText("Username / email");[m
[32m+[m
[32m+[m[32m        VBox content = new VBox(10, identifier);[m
[32m+[m[32m        dialog.getDialogPane().setContent(content);[m
[32m+[m
[32m+[m[32m        ButtonType request = new ButtonType("Send friend request", ButtonBar.ButtonData.OK_DONE);[m
[32m+[m[32m        dialog.getDialogPane().getButtonTypes().addAll(request, ButtonType.CANCEL);[m
[32m+[m
[32m+[m[32m        // wait until button press[m
[32m+[m[32m        dialog.showAndWait().ifPresent(response -> {[m
[32m+[m[32m            if(response == request) {[m
[32m+[m[32m                try {[m
[32m+[m[32m                    // check if target user exists, if so, send friend request[m
[32m+[m[32m                    User friend = userDAO.findUser("username", identifier.getText());[m
[32m+[m[32m                    // if username returned no users, search using email[m
[32m+[m[32m                    if (friend == null) { friend = userDAO.findUser("email", identifier.getText());}[m
[32m+[m[32m                    if (friend != null) {[m
[32m+[m[32m                        boolean success = friendDAO.addFriend(currentUser, friend);[m
[32m+[m
[32m+[m[32m                        // (un)successful message[m
[32m+[m[32m                        if (success) {[m
[32m+[m[32m                            feedbackMsg.setText("Friend r