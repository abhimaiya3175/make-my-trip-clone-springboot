package com.makemytrip.modules.recommendation.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "recommendation_feedback")
@CompoundIndex(name = "user_feedback_idx", def = "{'userId': 1, 'itemType': 1, 'itemId': 1}", unique = true)
public class RecommendationFeedback {
    @Id
    private String id;

    private String userId;
    private String itemId;
    private String itemType;
    private FeedbackType feedbackType;  // LIKE, SAVE, NOT_INTERESTED
    private LocalDateTime createdAt;

    public enum FeedbackType {
        LIKE,
        SAVE,
        NOT_INTERESTED
    }
}
