package com.makemytrip.modules.recommendation.repository;

import com.makemytrip.modules.recommendation.model.Recommendation;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface RecommendationRepository extends MongoRepository<Recommendation, String> {
    List<Recommendation> findByUserIdOrderByScoreDesc(String userId);
    List<Recommendation> findByItemTypeAndItemId(String itemType, String itemId);
    boolean existsByUserId(String userId);
}
