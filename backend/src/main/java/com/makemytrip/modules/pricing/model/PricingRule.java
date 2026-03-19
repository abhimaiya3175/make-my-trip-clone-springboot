package com.makemytrip.modules.pricing.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "pricing_rules")
public class PricingRule {
    @Id
    private String id;

    private String name;            // e.g. "Weekend Surge", "Holiday Premium"
    private RuleType ruleType;
    private double multiplier;      // e.g. 1.25 = 25% increase

    // Conditions (evaluated as AND)
    private List<String> entityIds;         // null = applies to all
    private String entityType;              // "FLIGHT" or "HOTEL"
    private List<String> daysOfWeek;        // "SATURDAY", "SUNDAY", etc.
    private List<String> holidays;          // "2025-12-25", "2025-01-01"
    private Integer bookingWindowDaysMax;   // e.g. 3 = within 3 days of travel
    private Double demandThreshold;         // occupancy % above which rule triggers

    private boolean active;

    public enum RuleType {
        WEEKEND,
        HOLIDAY,
        LAST_MINUTE,
        HIGH_DEMAND,
        EARLY_BIRD
    }
}
