package com.cab302.peerpractice.Model.Managers;

import com.cab302.peerpractice.Model.Entities.Group;
import com.cab302.peerpractice.Model.Entities.GroupApprovalNotification;
import com.cab302.peerpractice.Model.Entities.Notification;
import com.cab302.peerpractice.Model.Entities.User;
import com.cab302.peerpractice.Model.DAOs.IUserDAO;

import java.sql.SQLException;

public class Notifier {

    private final IUserDAO userDAO;

    public Notifier(IUserDAO userDAO){
        this.userDAO = userDAO;
    }

    /**
     * Sends a group approval request notification to the group owner.
     */
    public void groupApprovalRequest(User requester, Group group) throws SQLException {
        // Build the notification via a factory hook for easy overriding.
        GroupApprovalNotification notification = buildGroupApprovalNotification(requester, group);

        userDAO.addNotification(notification.getTo(), requester.getUsername(), notification.getMessage());
    }

    /**
     * Factory method that builds the notification for a group-approval request.
     */
    protected GroupApprovalNotification buildGroupApprovalNotification(User requester, Group group) {
        String ownerUsername = group.getOwner();
        return new GroupApprovalNotification(requester, ownerUsername, group);
    }

    /**
     * Approve pending notification
     */
    public void approveNotification(User user, Notification notification){
        onApprove(user, notification);
    }

    /**
     * Deny pending notification.
     */
    public void denyNotification(User user, Notification notification){
        onDeny(user, notification);
    }

    /**
     * Implement onApprove requirement when approval is performed.
     */
    protected void onApprove(User actor, Notification notification) {

    }

    /**
     * Implement onDeny requirement when denial is performed.
     */
    protected void onDeny(User actor, Notification notification) {

    }
}
