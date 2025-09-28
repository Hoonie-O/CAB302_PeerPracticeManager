package com.cab302.peerpractice.Model.Entities;

public class GroupApprovalNotification extends Notification {

    private final Group group;

    public GroupApprovalNotification(User from, String to, Group group) {
        super(from,to);
        this.group = group;
    }

    @Override
    public String getMessage() {
        return String.format("%s (%s) requested to join group: %s",from.getFirstName(),from.getUsername(),group.getName());
    }


}
