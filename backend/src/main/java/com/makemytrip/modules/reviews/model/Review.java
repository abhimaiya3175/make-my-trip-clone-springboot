package com.makemytrip.modules.reviews.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.index.CompoundIndex;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "reviews")
@CompoundIndex(name = "entity_user", def = "{'entityType': 1, 'entityId': 1, 'userId': 1}", unique = true)
@CompoundIndex(name = "entity_helpful", def = "{'entityType': 1, 'entityId': 1, 'helpfulCount': -1}")
@CompoundIndex(name = "entity_created", def = "{'entityType': 1, 'entityId': 1, 'createdAt': -1}")
@CompoundIndex(name = "entity_rating", def = "{'entityType': 1, 'entityId': 1, 'rating': -1}")
public class Review {
    @Id
    private String id;
    
    @Indexed
    private String userId;
    private String userName;
    
    @Indexed
    private String entityId; // FlightId or HotelId
    
    @Indexed
    private EntityType entityType; // FLIGHT or HOTEL
    
    private int rating; // 1-5
    private String text;
    private List<String> photos = new ArrayList<>(); // Photo URLs
    
    private int helpfulCount = 0;
    private List<String> helpfulVoters = new ArrayList<>(); // User IDs who voted helpful
    
    private boolean flagged = false;
    private int flagCount = 0;
    private List<String> flagReasons = new ArrayList<>();
    
    @Indexed
    private ModerationStatus moderationStatus = ModerationStatus.ACTIVE;
    
    private List<ReviewReply> replies = new ArrayList<>();
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Legacy fields for backward compatibility
    private String bookingId;
    private String bookingType;
    private String comment;

    public Review() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    
    public String getEntityId() { return entityId; }
    public void setEntityId(String entityId) { this.entityId = entityId; }
    
    public EntityType getEntityType() { return entityType; }
    public void setEntityType(EntityType entityType) { this.entityType = entityType; }
    
    public int getRating() { return rating; }
    public void setRating(int rating) { this.rating = rating; }
    
    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
    
    public List<String> getPhotos() { return photos; }
    public void setPhotos(List<String> photos) { this.photos = photos; }
    
    public int getHelpfulCount() { return helpfulCount; }
    public void setHelpfulCount(int helpfulCount) { this.helpfulCount = helpfulCount; }
    
    public List<String> getHelpfulVoters() { return helpfulVoters; }
    public void setHelpfulVoters(List<String> helpfulVoters) { this.helpfulVoters = helpfulVoters; }
    
    public boolean isFlagged() { return flagged; }
    public void setFlagged(boolean flagged) { this.flagged = flagged; }
    
    public int getFlagCount() { return flagCount; }
    public void setFlagCount(int flagCount) { this.flagCount = flagCount; }
    
    public List<String> getFlagReasons() { return flagReasons; }
    public void setFlagReasons(List<String> flagReasons) { this.flagReasons = flagReasons; }
    
    public ModerationStatus getModerationStatus() { return moderationStatus; }
    public void setModerationStatus(ModerationStatus moderationStatus) { this.moderationStatus = moderationStatus; }
    
    public List<ReviewReply> getReplies() { return replies; }
    public void setReplies(List<ReviewReply> replies) { this.replies = replies; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    // Legacy getters/setters
    public String getBookingId() { return bookingId != null ? bookingId : entityId; }
    public void setBookingId(String bookingId) { this.bookingId = bookingId; }
    
    public String getBookingType() { return bookingType != null ? bookingType : (entityType != null ? entityType.name() : null); }
    public void setBookingType(String bookingType) { this.bookingType = bookingType; }
    
    public String getComment() { return comment != null ? comment : text; }
    public void setComment(String comment) { this.comment = comment; }
}
