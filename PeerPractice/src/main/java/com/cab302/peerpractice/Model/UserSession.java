package com.cab302.peerpractice.Model;

public class UserSession {

    private User currentUser;

    public boolean isLoggedIn(){return currentUser != null;}
    public User getCurrentUser(){return currentUser;}
    void setCurrentUser(User u){this.currentUser = u;}
    public void logout(){currentUser = null;}

}
