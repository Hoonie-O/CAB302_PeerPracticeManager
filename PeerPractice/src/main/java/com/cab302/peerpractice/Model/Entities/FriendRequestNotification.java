package com.cab302.peerpractice.Model.Entities;

public class FriendRequestNotification extends Notification {

    public FriendRequestNotification(User from, User to) {
        super(from, to);
    }

    @Override
    public String getMsg() {
        return String.format("%s (%s) has sent you a friend request!",getFrom().getFirstName(),getFrom().getUsername());
    }
}
