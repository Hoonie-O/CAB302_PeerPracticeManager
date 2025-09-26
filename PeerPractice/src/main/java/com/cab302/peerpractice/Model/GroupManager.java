package com.cab302.peerpractice.Model;

import com.cab302.peerpractice.Exceptions.DuplicateGroupException;
import com.cab302.peerpractice.Exceptions.InsufficientPermissionsException;
import com.cab302.peerpractice.Exceptions.UserNotFoundException;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.regex.Pattern;

public class GroupManager {

    private IGroupDAO groupDAO;
    private Notifier notifier;
    private IUserDAO userDAO;


    public GroupManager(IGroupDAO groupDAO,Notifier notifier,IUserDAO userDAO){
        this.groupDAO = groupDAO;
        this.notifier = notifier;
        this.userDAO = userDAO;
    }

    public void createGroup(String name, String description, boolean require_approval, User user) throws Exception{

        if(user == null) throw new IllegalArgumentException("Creating user can't be null");

        name = ValidationUtils.validateAndCleanOthersName(name);
        description = ValidationUtils.validateAndCleanGroupDescription(description);

        Group group = new Group(name,description,require_approval,user.getUsername(), LocalDateTime.now());
        group.addMember(user);

        if(groupDAO.groupExists(group)) throw new DuplicateGroupException("Group already exists");

        int id = groupDAO.addGroup(group);
        group.setID(id);
    }


    public void requireApproval(Group group, boolean require_approval){
        group.setRequire_approval(require_approval);
        groupDAO.setRequireApproval(group.getID(),require_approval);
    }

    public void addMember(Group group,User user,String toAdd) throws SQLException {
        if(group == null) throw new IllegalArgumentException("Group can't be null");
        if(user == null) throw new IllegalArgumentException("User can't be null");
        if(toAdd == null || toAdd.isEmpty()) throw new IllegalArgumentException("User to add can't be null or empty");

        if(!group.getOwner().equals(user.getUsername())) throw new InsufficientPermissionsException("You are not the owner of the group");

        User userToAdd = userDAO.findUser("username", toAdd);
        if(userToAdd == null) throw new UserNotFoundException("User to add couldn't be found");

        group.addMember(userToAdd);
    }

    public void joinGroup(Group group, User user) throws SQLException {
        if(user == null) throw new IllegalArgumentException("User can't be null");
        if(group == null) throw new IllegalArgumentException("Group can't be null");
        if(group.isRequire_approval()){
            notifier.groupApprovalRequest(user,group);
        }
        else{
            group.addMember(user);
        }
    }

    public void approveRequest(Group group,User user, GroupApprovalNotification notification){
        if(group == null) throw  new IllegalArgumentException("Group can't be null");
        if(notification == null) throw new IllegalArgumentException("Notification can't be null");

        if(!group.getOwner().equals(user.getUsername())) throw new InsufficientPermissionsException("You are not the owner of the group");

        notifier.approveNotification(user,notification);

        if(!notification.isApproved()) throw new IllegalStateException("notification has not been approved");
        if(notification.isDenied()) throw new IllegalStateException("Notification can't be denied");

        User from = notification.getFrom();

        group.addMember(from);
        groupDAO.addToGroup(group.getID(),from);
    }

    public void denyRequest(Group group, User user, GroupApprovalNotification notification){
        if(group == null) throw new IllegalArgumentException("Group can't be null");
        if(notification == null) throw new IllegalArgumentException("Notification can't be null");

        if(!group.getOwner().equals(user.getUsername())) throw new InsufficientPermissionsException("You are not the owner of the group");

        notifier.denyNotification(user,notification);
    }

}
