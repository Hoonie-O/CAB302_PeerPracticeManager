package com.cab302.peerpractice.Model;

public class Notifier {

    private IUserDAO userDAO;

    public Notifier(IUserDAO userDAO){
        this.userDAO = userDAO;
    }

    public void groupApprovalRequest(User user, Group group){
        String ownerUsername = group.getOwner();
        GroupApprovalNotification notification = new GroupApprovalNotification(user,ownerUsername,group);
        userDAO.addNotification(ownerUsername,notification);
    }

    //Method for approving a notification (should check if the user has permission to accept)
    public void approveNotification(User user, Notification notification){

    }

    //Method for denying a notification (should check if the user has permission to deny)
    public void denyNotification(User user, Notification notification){

    }

}
