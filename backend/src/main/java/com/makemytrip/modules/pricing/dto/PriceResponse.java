package com.makemytrip.modules.pricing.dto;

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
public class PriceResponse {
    private String entityId;
    private String entityType;
    private double basePrice;
    private double finalPrice;
    private double totalMultiplier;
    private List<String> appliedRules;
    private LocalDateTime calculatedAt;

    // Freeze info (if any active freeze exists for this user)
    private Double frozenPrice;
    private LocalDateTime freezeExpiresAt;
}
