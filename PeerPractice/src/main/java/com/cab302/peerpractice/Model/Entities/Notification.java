package com.cab302.peerpractice.Model.Entities;

import java.time.LocalDateTime;

public abstract class Notification {

    private final User from;
    private final User to;
    private String msg;
    private boolean approved;
    private LocalDateTime createdAt;
    private boolean isRead;

    protected Notification(User from, User to) {
        this.from = from;
        this.to = to;
        msg = "";
        approved = false;
        this.createdAt = LocalDateTime.now();
        this.isRead = false;
    }

    public User getFrom() { return from; }
    public User getTo() { return to; }

    public String getMsg() { return msg; }
    public void setMsg(String msg) { this.msg = msg; }

    public boolean isApproved() { return approved; }
    public void approve() { approved = true; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public boolean isRead() { return isRead; }
    public void markAsRead() { this.isRead = true; }
    public void markAsUnread() { this.isRead = false; }
}
