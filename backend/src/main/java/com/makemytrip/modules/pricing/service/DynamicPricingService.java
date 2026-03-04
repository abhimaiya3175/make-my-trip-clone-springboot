package com.makemytrip.modules.pricing.service;

import org.springframework.stereotype.Service;

@Service
public class DynamicPricingService {

    private static final double BASE_DEMAND_MULTIPLIER = 1.0;
    private static final double HIGH_DEMAND_MULTIPLIER = 1.5;
    private static final double LOW_DEMAND_MULTIPLIER = 0.85;

    /**
     * Calculate dynamic price based on demand and availability
     */
    public double calculateDynamicPrice(double basePrice, int availableUnits, int totalUnits) {
        if (totalUnits == 0) return basePrice;

        double occupancyRate = 1.0 - ((double) availableUnits / totalUnits);

        double demandMultiplier;
        if (occupancyRate > 0.8) {
            demandMultiplier = HIGH_DEMAND_MULTIPLIER;
        } else if (occupancyRate < 0.3) {
            demandMultiplier = LOW_DEMAND_MULTIPLIER;
        } else {
            demandMultiplier = BASE_DEMAND_MULTIPLIER;
        }

        return Math.round(basePrice * demandMultiplier * 100.0) / 100.0;
    }
}
