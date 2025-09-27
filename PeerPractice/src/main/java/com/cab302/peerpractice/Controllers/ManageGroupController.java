package com.cab302.peerpractice.Controllers;

import com.cab302.peerpractice.AppContext;
import com.cab302.peerpractice.Navigation;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;


public class ManageGroupController extends BaseController {
    @FXML private Button blockButton;
    @FXML private Button saveButton;

    @FXML private TableView<Member> membersTable;
    @FXML private TableColumn<Member, String> nameMemberColumn;
    @FXML private TableColumn<Member, String> roleColumn;
    @FXML private TableColumn<Member, Void> removeColumn;

    @FXML private TableView<MemberRequest> memberRequestsTable;
    @FXML private TableColumn<MemberRequest, String> nameRequestColumn;
    @FXML private TableColumn<MemberRequest, Void> approveColumn;
    @FXML private TableColumn<MemberRequest, Void> declineColumn;

    @FXML private TableView<BlockedUser> blockedUsersTable;
    @FXML private TableColumn<BlockedUser, String> nameBlockColumn;
    @FXML private TableColumn<BlockedUser, Void> unblockColumn;

    public ManageGroupController(AppContext ctx, Navigation nav) {
        super(ctx, nav);
    }

    public void initialize() {
        setupMembersTable();
        setupMemberRequestsTable();
        setupBlockedUsersTable();
    }


    // Temporary Member class
    public class Member {
        private String name;
        private String role;

        public Member(String name, String role) {
            this.name = name;
            this.role = role;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getRole() {
            return role;
        }

        public void setRole(String role) {
            this.role = role;
        }
    }

    private void setupMembersTable() {
        // Set up name column
        nameMemberColumn.setCellValueFactory(new PropertyValueFactory<>("name"));

        // Set up role column with ComboBox
        roleColumn.setCellValueFactory(new PropertyValueFactory<>("role"));
        roleColumn.setCellFactory(column -> {
            return new TableCell<Member, String>() {
                private ComboBox<String> comboBox = new ComboBox<>();
                {
                    comboBox.getItems().addAll("Admin", "Member");
                    comboBox.setMaxWidth(Double.MAX_VALUE);
                    comboBox.setOnAction(event -> {
                        if (getTableRow() != null && getTableRow().getItem() != null) {
                            Member member = getTableRow().getItem();
                            member.setRole(comboBox.getValue());
                        }
                    });
                }

                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                        setGraphic(null);
                    } else {
                        comboBox.setValue(item);
                        setGraphic(comboBox);
                        setAlignment(Pos.CENTER);
                    }
                }
            };
        });

        // Set up remove column with buttons
        removeColumn.setCellFactory(column ->
                new TableCell<Member, Void>() {
            private final Button removeBtn = new Button("Remove");
            {
                removeBtn.setMaxWidth(Double.MAX_VALUE);
                removeBtn.setOnAction(event -> {
                    Member member = getTableView().getItems().get(getIndex());
                    getTableView().getItems().remove(member);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(removeBtn);
                    setAlignment(Pos.CENTER);
                }
            }
        });

        // Temporary data
        ObservableList<Member> sampleMembers = FXCollections.observableArrayList(
                new Member("John Doe", "Admin"),
                new Member("Jane Doe", "Member")
        );

        membersTable.setItems(sampleMembers);
    }



    // Temporary MemberRequest Class
    public static class MemberRequest {
        private String name;

        public MemberRequest(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    private void setupMemberRequestsTable() {
        // Set up username column
        nameRequestColumn.setCellValueFactory(new PropertyValueFactory<>("name"));

        // Set up approve column with buttons
        approveColumn.setCellFactory(column ->
                new TableCell<MemberRequest, Void>() {
            private final Button approveBtn = new Button("Approve");

            {
                approveBtn.setMaxWidth(Double.MAX_VALUE);
                approveBtn.setOnAction(event -> {
                    MemberRequest request = getTableView().getItems().get(getIndex());
                    Member newMember = new Member(request.getName(), "Member");
                    membersTable.getItems().add(newMember);
                    getTableView().getItems().remove(request);
                    System.out.println("Approved member: " + request.getName());
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(approveBtn);
                    setAlignment(Pos.CENTER);
                }
            }
        });

        // Set up decline column with buttons
        declineColumn.setCellFactory(column ->
                new TableCell<MemberRequest, Void>() {
            private final Button declineBtn = new Button("Decline");
            {
                declineBtn.setMaxWidth(Double.MAX_VALUE);
                declineBtn.setOnAction(event -> {
                    MemberRequest request = getTableView().getItems().get(getIndex());
                    getTableView().getItems().remove(request);
                    System.out.println("Declined member request: " + request.getName());
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(declineBtn);
                    setAlignment(Pos.CENTER);
                }
            }
        });

        // Temporary data
        ObservableList<MemberRequest> sampleRequests = FXCollections.observableArrayList(
                new MemberRequest("John Doe"),
                new MemberRequest("Jane Doe")
        );

        memberRequestsTable.setItems(sampleRequests);
    }

    public static class BlockedUser {
        private String name;

        public BlockedUser(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    private void setupBlockedUsersTable() {
        // Set up username column
        nameBlockColumn.setCellValueFactory(new PropertyValueFactory<>("name"));

        // Set up unblock column with buttons
        unblockColumn.setCellFactory(column ->
                new TableCell<BlockedUser, Void>() {
            private final Button unblockBtn = new Button("Unblock");
            {
                unblockBtn.setMaxWidth(Double.MAX_VALUE);
                unblockBtn.setOnAction(event -> {
                    BlockedUser blockedUser = getTableView().getItems().get(getIndex());
                    getTableView().getItems().remove(blockedUser);
                    System.out.println("Unblocked user: " + blockedUser.getName());
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(unblockBtn);
                    setAlignment(Pos.CENTER);
                }
            }
        });

        // Temporary data
        ObservableList<BlockedUser> sampleBlockedUsers = FXCollections.observableArrayList(
                new BlockedUser("John Doe"),
                new BlockedUser("Jane Doe")
        );

        blockedUsersTable.setItems(sampleBlockedUsers);
    }

    @FXML
    private void onSave() {

    }

    @FXML
    private void onBlockUser() {

    }

    public void setStage(Stage dialog) {
    }
}
