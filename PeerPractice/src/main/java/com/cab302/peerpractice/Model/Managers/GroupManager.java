package com.cab302.peerpractice.Model.Managers;

import com.cab302.peerpractice.Exceptions.DuplicateGroupException;
import com.cab302.peerpractice.Exceptions.InsufficientPermissionsException;
import com.cab302.peerpractice.Exceptions.UserNotFoundException;
import com.cab302.peerpractice.Model.Entities.Group;
import com.cab302.peerpractice.Model.Entities.GroupApprovalNotification;
import com.cab302.peerpractice.Model.Entities.User;
import com.cab302.peerpractice.Model.DAOs.GroupDAO;
import com.cab302.peerpractice.Model.DAOs.IGroupDAO;
import com.cab302.peerpractice.Model.DAOs.IUserDAO;
import com.cab302.peerpractice.Model.Utils.ValidationUtils;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.regex.Pattern;

public class GroupManager {

    private final IGroupDAO groupDAO;
    private final Notifier notifier;
    private final IUserDAO userDAO;


    public GroupManager(IGroupDAO groupDAO,Notifier notifier,IUserDAO userDAO){
        this.groupDAO = groupDAO;
        this.notifier = notifier;
        this.userDAO = userDAO;
    }

    public void createGroup(String name, String description, boolean require_approval, User user) throws Exception{

        if(user == null) throw new IllegalArgumentException("Creating user can't be null");

        name = ValidationUtils.validateAndCleanOthersName(name);
        description = ValidationUtils.validateAndCleanGroupDescription(description);

        Group group = new Group(name,description,require_approval,user,LocalDateTime.now());
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
        groupDAO.addToGroup(group.getID(), userToAdd);
    }

    public void joinGroup(Group group, User user) throws SQLException {
        if(user == null) throw new IllegalArgumentException("User can't be null");
        if(group == null) throw new IllegalArgumentException("Group can't be null");
        if(group.isRequire_approval()){
            if (!groupDAO.hasUserRequestedToJoin(group.getID(), user.getUserId()) &&
                !groupDAO.isUserMemberOfGroup(group.getID(), user.getUserId())) {
                groupDAO.createJoinRequest(group.getID(), user.getUserId());
            }
        }
        else{
            group.addMember(user);
            groupDAO.addToGroup(group.getID(), user);
        }
    }

    public void approveRequest(Group group,User user, GroupApprovalNotification notification) throws SQLException {
        if(group == null) throw  new IllegalArgumentException("Group can't be null");
        if(notification == null) throw new IllegalArgumentException("Notification can't be null");

        if(!group.getOwner().equals(user.getUsername())) throw new InsufficientPermissionsException("You are not the owner of the group");

        notifier.approveNotification(user,notification);

        if(!notification.isApproved()) throw new IllegalStateException("notification has not been approved");

        User from = notification.getFrom();

        group.addMember(from);
        groupDAO.addToGroup(group.getID(),from);
    }

    public void denyRequest(Group group, User user, GroupApprovalNotification notification) throws SQLException {
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
        if (!Pattern.compile("^[A-Za-z0-9 '_.-]+$").matcher(name).matches()) {
            throw new IllegalArgumentException("Group name can only contain letters, numbers, spaces, dots, hyphens,underscores, or apostrophes");
        }
    }

    private static void validateDescription(String description) {
        if (description == null) throw new IllegalArgumentException("Description can't be null");
        description = description.trim();
        if (description.isEmpty()) throw new IllegalArgumentException("Description can't be blank");
        if (description.length() > 200) throw new IllegalArgumentException("Description can't be longer than 200 characters");
    }

    // Admin role management methods
    public boolean isAdmin(Group group, User user) {
        if (group == null || user == null) return false;

        // Check if user is the original owner
        if (group.getOwner().equals(user.getUsername())) {
            return true;
        }

        // Check if user has admin role in database via the interface method
        return groupDAO.isAdmin(group.getID(), user.getUserId());
    }

    public void promoteToAdmin(Group group, User promoter, User userToPromote) throws SQLException {
        if (group == null || promoter == null || userToPromote == null) {
            throw new IllegalArgumentException("Group, promoter, and user to promote cannot be null");
        }

        if (!isAdmin(group, promoter)) {
            throw new InsufficientPermissionsException("Only admins can promote members to admin");
        }

        boolean success = groupDAO.promoteToAdmin(
            group.getID(), userToPromote.getUserId(), promoter.getUserId()
        );
        if (!success) {
            throw new IllegalStateException("Failed to promote user to admin");
        }
        // Update local group object
        group.setMemberRole(userToPromote.getUserId(), "admin");
    }

    public void demoteAdmin(Group group, User demoter, User userToDemote) throws SQLException {
        if (group == null || demoter == null || userToDemote == null) {
            throw new IllegalArgumentException("Group, demoter, and user to demote cannot be null");
        }

        if (!isAdmin(group, demoter)) {
            throw new InsufficientPermissionsException("Only admins can demote other admins");
        }

        // Can't demote the original owner
        if (group.getOwner().equals(userToDemote.getUsername())) {
            throw new InsufficientPermissionsException("Cannot demote the group owner");
        }

        boolean success = groupDAO.demoteAdmin(
            group.getID(), userToDemote.getUserId(), demoter.getUserId()
        );
        if (!success) {
            throw new IllegalStateException("Failed to demote admin");
        }
        // Update local group object
        group.setMemberRole(userToDemote.getUserId(), "member");
    }

    public void kickMember(Group group, User admin, User memberToKick) throws SQLException {
        if (group == null || admin == null || memberToKick == null) {
            throw new IllegalArgumentException("Group, admin, and member to kick cannot be null");
        }

        if (!isAdmin(group, admin)) {
            throw new InsufficientPermissionsException("Only admins can kick members");
        }

        // Can't kick the original owner
        if (group.getOwner().equals(memberToKick.getUsername())) {
            throw new InsufficientPermissionsException("Cannot kick the group owner");
        }

        boolean success = groupDAO.removeMember(
            group.getID(), memberToKick.getUserId(), admin.getUserId()
        );
        if (!success) {
            throw new IllegalStateException("Failed to remove member from group");
        }
        // Update local group object - need to create new list since getMembers() returns unmodifiable
        java.util.List<User> updatedMembers = new java.util.ArrayList<>(group.getMembers());
        updatedMembers.remove(memberToKick);
        group.setMembers(updatedMembers);
    }

    public void deleteGroup(Group group, User user) throws SQLException {
        if (group == null || user == null) {
            throw new IllegalArgumentException("Group and user cannot be null");
        }

        // Only the original owner can delete the group
        if (!group.getOwner().equals(user.getUsername())) {
            throw new InsufficientPermissionsException("Only the group owner can delete the group");
        }

        boolean success = groupDAO.deleteGroup(group);
        if (!success) {
            throw new IllegalStateException("Failed to delete group");
        }
    }

    public void processJoinRequest(Group group, User admin, int requestId, boolean approve) throws SQLException {
        if (group == null || admin == null) {
            throw new IllegalArgumentException("Group and admin cannot be null");
        }

        if (!isAdmin(group, admin)) {
            throw new InsufficientPermissionsException("Only admins can process join requests");
        }

        String status = approve ? "approved" : "rejected";
        boolean success = groupDAO.processJoinRequest(requestId, status, admin.getUserId());

        if (!success) {
            throw new IllegalStateException("Failed to process join request");
        }

        // If approved, the DAO already added the user to the group, so reload members
        if (approve) {
            java.util.List<com.cab302.peerpractice.Model.Entities.GroupMemberEntity> members = groupDAO.getGroupMembers(group.getID());
            java.util.List<User> users = new java.util.ArrayList<>();
            for (com.cab302.peerpractice.Model.Entities.GroupMemberEntity member : members) {
                if (member.getUser() != null) {
                    users.add(member.getUser());
                    // Sync roles
                    group.setMemberRole(member.getUserId(), member.getRole());
                }
            }
            group.setMembers(users);
        }
    }

}
