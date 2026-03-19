package com.makemytrip.modules.reviews.dto;

import com.makemytrip.modules.reviews.model.EntityType;
import java.util.List;

public class CreateReviewRequest {
    private String entityId;
    private EntityType entityType;
    private int rating;
    private String text;
    private List<String> photos;

    // Getters and Setters
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
}
