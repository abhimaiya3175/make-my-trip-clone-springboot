package com.makemytrip.modules.recommendation.repository;

import com.makemytrip.modules.recommendation.model.UserEvent;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface UserEventRepository extends MongoRepository<UserEvent, String> {
    List<UserEvent> findByUserIdOrderByCreatedAtDesc(String userId);
    List<UserEvent> findByEntityIdAndEntityType(String entityId, String entityType);
}
