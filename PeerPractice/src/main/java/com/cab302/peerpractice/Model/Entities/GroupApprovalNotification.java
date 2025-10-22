package com.cab302.peerpractice.Model.Entities;

public class GroupApprovalNotification extends Notification {

    private final Group group;

    public GroupApprovalNotification(User from, User to, Group group) {
        super(from,to);
        this.group = group;
    }

    @Override
    public String getMsg() {
        return String.format("%s (%s) requested to join group: %s",getFrom().getFirstName(),getFrom().getUsername(),group.getName());
    }
}
