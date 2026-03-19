package com.makemytrip.modules.recommendation.repository;

import com.makemytrip.modules.recommendation.model.RecommendationFeedback;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface FeedbackRepository extends MongoRepository<RecommendationFeedback, String> {
    Optional<RecommendationFeedback> findByUserIdAndItemIdAndItemType(String userId, String itemId, String itemType);
    List<RecommendationFeedback> findByUserId(String userId);
}
