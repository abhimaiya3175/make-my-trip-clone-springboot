package com.makemytrip.modules.recommendation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RecommendationResponse {
    private String id;
    private String userId;
    private String itemId;
    private String itemType;
    private double score;
    private String reason;
    private List<String> tags;
    private LocalDateTime createdAt;
}
