package com.cab302.peerpractice.Model.entities;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Group {

    private List<User> members;
    private Map<String, String> memberRoles; // userId -> role mapping
    private String name;
    private int ID;
    private String description;
    private String owner;
    private boolean require_approval;
    LocalDateTime created_at;

    public Group(String name, String description,boolean require_approval,String owner,LocalDateTime created_at){
        this.name = name;
        this.description = description;
        this.require_approval = require_approval;
        this.owner = owner;
        this.created_at = created_at;
        this.members = new ArrayList<>();
        this.memberRoles = new HashMap<>();
    }

    public List<User> getMembers() { return Collections.unmodifiableList(members); }
    public void setMembers(List<User> members) { this.members = members; }
    public void addMember(User user){members.add(user);}

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getID() { return ID; }
    public void setID(int ID) { this.ID = ID; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getOwner() { return owner; }
    public void setOwner(String owner) { this.owner = owner; }

    public boolean isRequire_approval() { return require_approval; }
    public void setRequire_approval(boolean require_approval) { this.require_approval = require_approval; }

    public LocalDateTime getCreated_at() { return created_at; }

    
    public boolean getIsPrivate() { 
        return require_approval; 
    }
    
    public String getCreatorUsername() { 
        return owner; 
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Group group = (Group) o;
        return ID == group.ID;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(ID);
    }

    // Admin role management methods
    public void setMemberRole(String userId, String role) {
        memberRoles.put(userId, role);
    }

    public String getMemberRole(String userId) {
        return memberRoles.getOrDefault(userId, "member");
    }

    public boolean isAdmin(String userId) {
        return "admin".equals(getMemberRole(userId));
    }

    public List<User> getAdmins() {
        List<User> admins = new ArrayList<>();
        for (User member : members) {
            if (isAdmin(member.getUserId())) {
                admins.add(member);
            }
        }
        return admins;
    }

    public boolean hasAdminRights(String userId) {
        return owner.equals(userId) || isAdmin(userId);
    }
}
