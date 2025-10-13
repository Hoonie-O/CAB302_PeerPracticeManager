package com.cab302.peerpractice.Model.Managers;

import com.cab302.peerpractice.Model.Entities.*;
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
        userDAO.addNotification(notification.getFrom(), notification.getTo(), notification.getMsg());
    }

    /**
     * Factory method that builds the notification for a group-approval request.
     */
    protected GroupApprovalNotification buildGroupApprovalNotification(User requester, Group group) {
        User groupOwner = group.getOwner();
        return new GroupApprovalNotification(requester, groupOwner, group);
    }

    /**
     * Create a new friend request
     * @param sender The user sending the friend request
     * @param receiver The target user of the friend request
     * @throws SQLException SQLException
     */
    public FriendRequestNotification createFriendRequest(User sender, User receiver) throws SQLException {
        // Create object
        FriendRequestNotification notification = new FriendRequestNotification(sender, receiver);
        // Insert into table
        userDAO.addNotification(notification.getFrom(), notification.getTo(), notification.getMsg());

        return notification;
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
