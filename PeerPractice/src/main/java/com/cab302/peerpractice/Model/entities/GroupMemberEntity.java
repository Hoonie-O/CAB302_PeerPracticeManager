package com.cab302.peerpractice.Model.entities;

import java.time.LocalDateTime;

public class GroupMemberEntity {
    private int groupId;
    private String userId;
    private String role;
    private LocalDateTime joinedAt;
    private User user;

    public GroupMemberEntity() {}

    public GroupMemberEntity(int groupId, String userId, String role, LocalDateTime joinedAt) {
        this.groupId = groupId;
        this.userId = userId;
        this.role = role;
        this.joinedAt = joinedAt;
    }

    public int getGroupId() { return groupId; }
    public void setGroupId(int groupId) { this.groupId = groupId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public LocalDateTime getJoinedAt() { return joinedAt; }
    public void setJoinedAt(LocalDateTime joinedAt) { this.joinedAt = joinedAt; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
}