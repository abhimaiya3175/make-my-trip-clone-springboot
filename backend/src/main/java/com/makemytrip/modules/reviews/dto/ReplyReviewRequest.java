package com.makemytrip.modules.reviews.dto;

public class ReplyReviewRequest {
    private String text;
    private String userName;
    private boolean isOwner;

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
    
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    
    public boolean isOwner() { return isOwner; }
    public void setOwner(boolean owner) { isOwner = owner; }
}
