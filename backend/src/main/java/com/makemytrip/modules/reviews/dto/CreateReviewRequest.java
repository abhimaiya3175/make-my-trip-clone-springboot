package com.makemytrip.modules.reviews.dto;

import com.makemytrip.modules.reviews.model.EntityType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public class CreateReviewRequest {
    @NotBlank(message = "Entity ID is required")
    private String entityId;

    @NotNull(message = "Entity type is required")
    private EntityType entityType;

    @Min(value = 1, message = "Rating must be between 1 and 5")
    @Max(value = 5, message = "Rating must be between 1 and 5")
    private int rating;

    @NotBlank(message = "Review text is required")
    @Size(max = 2000, message = "Review text cannot exceed 2000 characters")
    private String text;

    @Size(max = 5, message = "Cannot upload more than 5 photos")
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
