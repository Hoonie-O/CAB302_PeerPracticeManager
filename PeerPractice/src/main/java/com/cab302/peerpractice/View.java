package com.cab302.peerpractice;

public enum View {
    Login("login-view.fxml", "Login"),
    Signup("signup-view.fxml", "Sign up"),
    ForgotPassword("forgotpassword-view.fxml", "Forgot password"),
    MainMenu("mainmenu-view.fxml", "Main menu"),
    Calendar("calendar-view.fxml", "Calendar");

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
}
