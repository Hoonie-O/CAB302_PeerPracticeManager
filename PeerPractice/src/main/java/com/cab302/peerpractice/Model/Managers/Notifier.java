package com.cab302.peerpractice.Model.Managers;

import com.cab302.peerpractice.Model.DAOs.IFriendDAO;
import com.cab302.peerpractice.Model.Entities.*;
import com.cab302.peerpractice.Model.DAOs.IUserDAO;

import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

public class Notifier {

    private final IUserDAO userDAO;
    private final IFriendDAO friendDAO;

    public Notifier(IUserDAO userDAO, IFriendDAO friendDAO){
        this.userDAO = userDAO;
        this.friendDAO = friendDAO;
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
    public void approveNotification(User user, Notification notification) throws SQLException {
        onApprove(notification);
    }

    /**
     * Deny pending notification.
     */
    public void denyNotification(User user, Notification notification) throws SQLException {
        onDeny(notification);
    }

    /**
     * Implement onApprove requirement when approval is performed.
     */
    protected void onApprove(Notification notification) throws SQLException {
        friendDAO.acceptFriendRequest(notification.getFrom(), notification.getTo());
        notification.approve();
    }

    /**
     * Implement onDeny requirement when denial is performed.
     */
    protected void onDeny(Notification notification) throws SQLException {
        friendDAO.denyFriendRequest(notification.getFrom(), notification.getTo());
    }

    /**
     * Get all notifications for a user
     * @param user The user to get notifications for
     * @return List of notifications
     */
    public List<Notification> getNotificationsForUser(User user) {
        return userDAO.getNotificationsForUser(user);
    }

    /**
     * Get pending friend requests for a user
     * @param user The user to get friend requests for
     * @return List of pending friend request notifications
     */
    public List<FriendRequestNotification> getPendingFriendRequests(User user) {
        return userDAO.getNotificationsForUser(user).stream()
                .filter(n -> n instanceof FriendRequestNotification)
                .map(n -> (FriendRequestNotification) n)
                .collect(Collectors.toList());
    }

    /**
     * Get the count of pending friend requests for a user
     * @param user The user to get count for
     * @return Number of pending friend requests
     */
    public int getPendingFriendRequestCount(User user) {
        return getPendingFriendRequests(user).size();
    }

    /**
     * Clear a friend request notification after acceptance/denial
     * @param notification The notification to clear
     * @param user The user receiving the notification
     * @return true if successfully removed
     */
    public boolean clearNotification(User user, Notification notification) {
        return userDAO.removeNotification(user, notification);
    }

    /**
     * Get count of unread notifications for a user
     * @param user The user
     * @return Number of unread notifications
     */
    public int getUnreadNotificationCount(User user) {
        return userDAO.getUnreadNotificationCount(user);
    }

    /**
     * Get count of unread friend requests for a user
     * @param user The user
     * @return Number of unread friend requests
     */
    public int getUnreadFriendRequestCount(User user) {
        return (int) getPendingFriendRequests(user).stream()
                .filter(n -> !n.isRead())
                .count();
    }

    /**
     * Mark a notification as read
     * @param user The user
     * @param notification The notification to mark as read
     * @return true if successful
     */
    public boolean markNotificationAsRead(User user, Notification notification) {
        return userDAO.markNotificationAsRead(user, notification);
    }

    /**
     * Mark all notifications as read for a user
     * @param user The user
     * @return true if successful
     */
    public boolean markAllNotificationsAsRead(User user) {
        return userDAO.markAllNotificationsAsRead(user);
    }
}
