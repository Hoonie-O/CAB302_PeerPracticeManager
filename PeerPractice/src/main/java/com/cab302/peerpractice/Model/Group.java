package com.cab302.peerpractice.Model;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

public class Group {

    private List<User> members;
    private String name;
    private int ID;
    private String description;
    private int owner_id;
    private boolean require_approval;
    LocalDateTime created_at;

    public Group(String name, String description,boolean require_approval){
        this.name = name;
        this.description = description;
        this.require_approval = require_approval;
    }

    public List<User> getMembers() { return Collections.unmodifiableList(members); }
    public void setMembers(List<User> members) { this.members = members; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getID() { return ID; }
    public void setID(int ID) { this.ID = ID; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getOwner_id() { return owner_id; }
    public void setOwner_id(int owner_id) { this.owner_id = owner_id; }

    public boolean isRequire_approval() { return require_approval; }
    public void setRequire_approval(boolean require_approval) { this.require_approval = require_approval; }

    public LocalDateTime getCreated_at() { return created_at; }
    public void setCreated_at(LocalDateTime created_at) { this.created_at = created_at; }
}
