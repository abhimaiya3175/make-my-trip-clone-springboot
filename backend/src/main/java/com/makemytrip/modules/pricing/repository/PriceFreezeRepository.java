package com.makemytrip.modules.pricing.repository;

import com.makemytrip.modules.pricing.model.PriceFreeze;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface PriceFreezeRepository extends MongoRepository<PriceFreeze, String> {
    Optional<PriceFreeze> findByUserIdAndEntityIdAndUsedFalse(String userId, String entityId);
    List<PriceFreeze> findByUserIdAndUsedFalse(String userId);
}
