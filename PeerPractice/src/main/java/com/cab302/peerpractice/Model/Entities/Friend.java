package com.cab302.peerpractice.Model.Entities;

import java.util.Objects;

public class Friend {
    private final User user1;
    private final User user2;
    private FriendStatus status;

    public Friend(User user1, User user2, FriendStatus status) {
        this.user1 = Objects.requireNonNull(user1, "Must include two users");
        this.user2 = Objects.requireNonNull(user2, "Must include two users");
        this.status = FriendStatus.PENDING;

        if (user1 ==  user2) {
            throw new IllegalArgumentException("Users cannot be the same");
        }
    }

    public User getUser1() { return user1; }
    public User getUser2() { return user2; }
    public FriendStatus getStatus() { return status; }

    public void setStatus(FriendStatus status) {
        this.status = status != null ? status : FriendStatus.PENDING;
    }
}
