package com.cab302.peerpractice.Model;

import java.sql.SQLException;

public class Notifier {

    private IUserDAO userDAO;

    public Notifier(IUserDAO userDAO){
        this.userDAO = userDAO;
    }

    public void groupApprovalRequest(User user, Group group) throws SQLException {
        String ownerUsername = group.getOwner();
        GroupApprovalNotification notification = new GroupApprovalNotification(user,ownerUsername,group);
        userDAO.addNotification(ownerUsername, notification.getTo(), notification.getMessage());
    }

    //Method for approving a notification (should check if the user has permission to accept)
    public void approveNotification(User user, Notification notification){

    }

    //Method for denying a notification (should check if the user has permission to deny)
    public void denyNotification(User user, Notification notification){

    }

}
