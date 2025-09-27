package com.cab302.peerpractice.Model.managers;

import com.cab302.peerpractice.Model.entities.User;

public class UserSession {

    private User currentUser;

    public boolean isLoggedIn(){return currentUser != null;}
    public User getCurrentUser(){return currentUser;}
    public void setCurrentUser(User u){this.currentUser = u;}
    public void logout(){currentUser = null;}

}
