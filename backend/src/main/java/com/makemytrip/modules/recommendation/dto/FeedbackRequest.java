package com.makemytrip.modules.recommendation.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FeedbackRequest {
    @NotBlank
    private String userId;
    @NotBlank
    private String itemId;
    @NotBlank
    private String itemType;
    @NotBlank
    private String feedbackType;  // LIKE, SAVE, NOT_INTERESTED
}
