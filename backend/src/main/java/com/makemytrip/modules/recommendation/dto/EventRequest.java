package com.makemytrip.modules.recommendation.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EventRequest {
    @NotBlank
    private String userId;
    @NotBlank
    private String eventType;   // VIEW, SEARCH, BOOK, WISHLIST, CLICK
    @NotBlank
    private String entityId;
    @NotBlank
    private String entityType;  // FLIGHT or HOTEL
    private String metadata;
}
