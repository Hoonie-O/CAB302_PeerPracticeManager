package com.cab302.peerpractice.Model.Entities;

import java.time.LocalDateTime;

public class GroupJoinRequest {
    private int requestId;
    private int groupId;
    private String userId;
    private String status;
    private LocalDateTime requestedAt;
    private LocalDateTime processedAt;
    private String processedBy;
    private User user;
    private Group group;

    public GroupJoinRequest(int groupId, String userId) {
        this.groupId = groupId;
        this.userId = userId;
        this.status = "pending";
        this.requestedAt = LocalDateTime.now();
    }

    public GroupJoinRequest(int requestId, int groupId, String userId, String status, 
                           LocalDateTime requestedAt, LocalDateTime processedAt, String processedBy) {
        this.requestId = requestId;
        this.groupId = groupId;
        this.userId = userId;
        this.status = status;
        this.requestedAt = requestedAt;
        this.processedAt = processedAt;
        this.processedBy = processedBy;
    }

    public int getRequestId() { return requestId; }
    public void setRequestId(int requestId) { this.requestId = requestId; }
    
    public int getGroupId() { return groupId; }
    public void setGroupId(int groupId) { this.groupId = groupId; }
    
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public LocalDateTime getRequestedAt() { return requestedAt; }
    public void setRequestedAt(LocalDateTime requestedAt) { this.requestedAt = requestedAt; }
    
    public LocalDateTime getProcessedAt() { return processedAt; }
    public void setProcessedAt(LocalDateTime processedAt) { this.processedAt = processedAt; }
    
    public String getProcessedBy() { return processedBy; }
    public void setProcessedBy(String processedBy) { this.processedBy = processedBy; }
    
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    
    public Group getGroup() { return group; }
    public void setGroup(Group group) { this.group = group; }
    
    public boolean isPending() { return "pending".equals(status); }
    public boolean isApproved() { return "approved".equals(status); }
    public boolean isRejected() { return "rejected".equals(status); }
}