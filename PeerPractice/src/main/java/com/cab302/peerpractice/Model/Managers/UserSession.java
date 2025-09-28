package com.cab302.peerpractice.Model.Managers;

import com.cab302.peerpractice.Model.Entities.User;

public class UserSession {

    private User currentUser;

    public boolean isLoggedIn(){return currentUser != null;}
    public User getCurrentUser(){return currentUser;}
    public void setCurrentUser(User u){this.currentUser = u;}
    public void logout(){currentUser = null;}

}
