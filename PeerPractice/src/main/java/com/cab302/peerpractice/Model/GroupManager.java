package com.cab302.peerpractice.Model;

import com.cab302.peerpractice.Exceptions.DuplicateGroupException;
import com.cab302.peerpractice.Exceptions.InsufficientPermissionsException;
import com.cab302.peerpractice.Exceptions.UserNotFoundException;

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

        validateName(name);
        validateDescription(description);

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

    public void addMember(Group group,User user,String toAdd){
        if(!group.getOwner().equals(user.getUsername())) throw new InsufficientPermissionsException("You are not the owner of the group");

        User userToAdd = userDAO.searchByUsername(toAdd);
        if(userToAdd == null) throw new UserNotFoundException("User to add coldn't be found");

        group.addMember(userToAdd);
    }

    public void joinGroup(Group group, User user){
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

    //Validators
    private static void validateName(String name) {
        if (name == null) throw new IllegalArgumentException("Group name can't be null");
        name = name.trim();
        if (name.isEmpty()) throw new IllegalArgumentException("Group name can't be blank");
        if (name.length() > 20) throw new IllegalArgumentException("Group name can't be longer than 20 characters");
        if (!Pattern.compile("^[A-Za-z0-9 _.-]+$").matcher(name).matches()) {
            throw new IllegalArgumentException("Group name can only contain letters, numbers, spaces, dots, hyphens, or underscores");
        }
    }

    private static void validateDescription(String description) {
        if (description == null) throw new IllegalArgumentException("Description can't be null");
        description = description.trim();
        if (description.isEmpty()) throw new IllegalArgumentException("Description can't be blank");
        if (description.length() > 200) throw new IllegalArgumentException("Description can't be longer than 200 characters");
    }

}
