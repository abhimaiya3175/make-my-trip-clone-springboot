package com.makemytrip.modules.pricing.repository;

import com.makemytrip.modules.pricing.model.PricingRule;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface PricingRuleRepository extends MongoRepository<PricingRule, String> {
    List<PricingRule> findByActiveTrue();
    List<PricingRule> findByEntityTypeAndActiveTrue(String entityType);
}
