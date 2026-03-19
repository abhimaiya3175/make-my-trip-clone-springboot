package com.makemytrip.modules.reviews.model;

import java.time.LocalDateTime;

public class ReviewReply {
    private String userId;
    private String userName;
    private String text;
    private LocalDateTime createdAt;
    private boolean isOwner;

    public ReviewReply() {}

    public ReviewReply(String userId, String userName, String text, boolean isOwner) {
        this.userId = userId;
        this.userName = userName;
        this.text = text;
        this.isOwner = isOwner;
        this.createdAt = LocalDateTime.now();
    }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public boolean isOwner() { return isOwner; }
    public void setOwner(boolean owner) { isOwner = owner; }
}
