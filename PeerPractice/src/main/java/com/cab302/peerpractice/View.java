package com.cab302.peerpractice;

import java.net.URL;

public enum View {
    Login("login-view.fxml", "Login"),
    Signup("signup-view.fxml", "Sign up"),
    ForgotPassword("forgotpassword-view.fxml", "Forgot password"),
    Default("default-view.fxml", "Main menu"),
    Calendar("calendar-view.fxml", "Calendar"),
    ResetPassword("resetpassword-view.fxml","ResetPassword"),
    Groups("groups-view.fxml","Groups"),
    EditProfile("editprofile-dialog.fxml", "Edit Profile"),
    SettingProfile("setting-dialog.fxml", "Setting Profile"),
    SessionTasks("session-tasks-view.fxml", "Session Tasks"),
    InviteMember("invitemember-dialog.fxml", "Invite Members"),
    ShareGroupID("sharegroupID-dialog.fxml", "Share Group ID"),
    ManageGroup("managegroup-dialog.fxml", "Mange Group");

    private final String fxml;
    private final String title;

    View(String fxml, String title) {
        this.fxml = fxml;
        this.title = title;
    }

    public String fxml() {
        return fxml;
    }

    public String title() {
        return title;
    }

    public URL url() {
        return View.class.getResource("/com/cab302/peerpractice/" + fxml);
    }
}
