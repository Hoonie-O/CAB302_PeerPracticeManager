package com.cab302.peerpractice.Model.Entities;

public abstract class Notification {

    private final User from;
    private final User to;
    private String msg;
    private boolean approved;

    protected Notification(User from, User to) {
        this.from = from;
        this.to = to;
        msg = "";
        approved = false;
    }

    public User getFrom() { return from; }
    public User getTo() { return to; }

    public String getMsg() { return msg; }
    public void setMsg(String msg) { this.msg = msg; }

    public boolean isApproved() { return approved; }
    public void approve() { approved = true; }
}
