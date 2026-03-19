package com.makemytrip.modules.pricing.service;

import com.makemytrip.modules.flights.model.Flight;
import com.makemytrip.modules.flights.repository.FlightRepository;
import com.makemytrip.modules.hotels.model.Hotel;
import com.makemytrip.modules.hotels.repository.HotelRepository;
import com.makemytrip.modules.pricing.dto.*;
import com.makemytrip.modules.pricing.model.PriceFreeze;
import com.makemytrip.modules.pricing.model.PriceSnapshot;
import com.makemytrip.modules.pricing.model.PricingRule;
import com.makemytrip.modules.pricing.repository.PriceFreezeRepository;
import com.makemytrip.modules.pricing.repository.PriceSnapshotRepository;
import com.makemytrip.modules.pricing.repository.PricingRuleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
public class PricingService {

    private final PricingRuleRepository ruleRepository;
    private final PriceSnapshotRepository snapshotRepository;
    private final PriceFreezeRepository freezeRepository;
    private final FlightRepository flightRepository;
    private final HotelRepository hotelRepository;

    private static final int FREEZE_HOURS = 24;

    // ── Bootstrap default rules ──────────────────────────────────────

    @PostConstruct
    public void initDefaultRules() {
        if (ruleRepository.count() > 0) {
            log.info("Pricing rules already exist, skipping init");
            return;
        }

        List<PricingRule> defaults = List.of(
                PricingRule.builder()
                        .name("Weekend Surge")
                        .ruleType(PricingRule.RuleType.WEEKEND)
                        .multiplier(1.15)
                        .daysOfWeek(List.of("SATURDAY", "SUNDAY"))
                        .entityType(null) // applies to all
                        .active(true)
                        .build(),
                PricingRule.builder()
                        .name("Holiday Premium")
                        .ruleType(PricingRule.RuleType.HOLIDAY)
                        .multiplier(1.30)
                        .holidays(List.of("2025-12-25", "2025-01-01", "2025-01-26", "2025-08-15", "2025-10-02"))
                        .entityType(null)
                        .active(true)
                        .build(),
                PricingRule.builder()
                        .name("Last-Minute Booking")
                        .ruleType(PricingRule.RuleType.LAST_MINUTE)
                        .multiplier(1.20)
                        .bookingWindowDaysMax(3)
                        .entityType("FLIGHT")
                        .active(true)
                        .build(),
                PricingRule.builder()
                        .name("High Demand")
                        .ruleType(PricingRule.RuleType.HIGH_DEMAND)
                        .multiplier(1.25)
                        .demandThreshold(80.0)
                        .entityType(null)
                        .active(true)
                        .build(),
                PricingRule.builder()
                        .name("Early Bird Discount")
                        .ruleType(PricingRule.RuleType.EARLY_BIRD)
                        .multiplier(0.90)
                        .bookingWindowDaysMax(null)
                        .entityType(null)
                        .active(true)
                        .build());

        ruleRepository.saveAll(defaults);
        log.info("Loaded {} default pricing rules", defaults.size());
    }

    // ── Core pricing calculation ─────────────────────────────────────

    public PriceResponse calculatePrice(String entityId, String entityType, String userId) {
        double basePrice = getBasePrice(entityId, entityType);
        List<PricingRule> activeRules = ruleRepository.findByActiveTrue();

        double totalMultiplier = 1.0;
        List<String> appliedRuleNames = new ArrayList<>();
        LocalDate today = LocalDate.now();
        String dayOfWeek = today.getDayOfWeek().name();

        for (PricingRule rule : activeRules) {
            // Filter by entity type
            if (rule.getEntityType() != null && !rule.getEntityType().equals(entityType))
                continue;
            // Filter by specific entity IDs
            if (rule.getEntityIds() != null && !rule.getEntityIds().contains(entityId))
                continue;

            boolean applies = false;

            switch (rule.getRuleType()) {
                case WEEKEND:
                    if (rule.getDaysOfWeek() != null && rule.getDaysOfWeek().contains(dayOfWeek)) {
                        applies = true;
                    }
                    break;
                case HOLIDAY:
                    if (rule.getHolidays() != null && rule.getHolidays().contains(today.toString())) {
                        applies = true;
                    }
                    break;
                case LAST_MINUTE:
                    // Always applies if booking window is short — we treat "today" as booking date
                    if (rule.getBookingWindowDaysMax() != null && rule.getBookingWindowDaysMax() >= 1) {
                        applies = true; // simplified: assume travel is soon
                    }
                    break;
                case HIGH_DEMAND:
                    double occupancy = calculateOccupancy(entityId, entityType);
                    if (rule.getDemandThreshold() != null && occupancy >= rule.getDemandThreshold()) {
                        applies = true;
                    }
                    break;
                case EARLY_BIRD:
                    // Applies if booking is > 30 days in advance (simplified: weekdays that aren't
                    // last-minute)
                    DayOfWeek dow = today.getDayOfWeek();
                    boolean isWeekday = dow != DayOfWeek.SATURDAY && dow != DayOfWeek.SUNDAY;
                    if (isWeekday && (rule.getBookingWindowDaysMax() == null)) {
                        applies = true;
                    }
                    break;
            }

            if (applies) {
                totalMultiplier *= rule.getMultiplier();
                appliedRuleNames.add(rule.getName());
            }
        }

        double finalPrice = Math.round(basePrice * totalMultiplier * 100.0) / 100.0;

        PriceResponse response = PriceResponse.builder()
                .entityId(entityId)
                .entityType(entityType)
                .basePrice(basePrice)
                .finalPrice(finalPrice)
                .totalMultiplier(totalMultiplier)
                .appliedRules(appliedRuleNames)
                .calculatedAt(LocalDateTime.now())
                .build();

        // Attach freeze info if user has an active freeze
        if (userId != null) {
            freezeRepository.findByUserIdAndEntityIdAndUsedFalse(userId, entityId)
                    .filter(PriceFreeze::isActive)
                    .ifPresent(freeze -> {
                        response.setFrozenPrice(freeze.getFrozenPrice());
                        response.setFreezeExpiresAt(freeze.getExpiresAt());
                    });
        }

        return response;
    }

    private double getBasePrice(String entityId, String entityType) {
        if ("FLIGHT".equals(entityType)) {
            return flightRepository.findById(entityId)
                    .map(Flight::getPrice)
                    .orElse(5000.0);
        } else {
            return hotelRepository.findById(entityId)
                    .map(Hotel::getPricePerNight)
                    .orElse(3000.0);
        }
    }

    private double calculateOccupancy(String entityId, String entityType) {
        // Simplified: return a mock occupancy based on entity
        if ("FLIGHT".equals(entityType)) {
            return flightRepository.findById(entityId)
                    .map(f -> f.getAvailableSeats() < 30 ? 85.0 : 50.0)
                    .orElse(50.0);
        }
        return hotelRepository.findById(entityId)
                .map(h -> h.getAvailableRooms() < 5 ? 90.0 : 40.0)
                .orElse(40.0);
    }

    // ── Price history ────────────────────────────────────────────────

    public PriceHistoryResponse getHistory(String entityId, int days) {
        LocalDateTime from = LocalDateTime.now().minusDays(days);
        LocalDateTime to = LocalDateTime.now();

        List<PriceSnapshot> snapshots = snapshotRepository
                .findByEntityIdAndSnapshotTimeBetweenOrderBySnapshotTimeAsc(entityId, from, to);

        List<PriceHistoryResponse.SnapshotPoint> points = snapshots.stream()
                .map(s -> PriceHistoryResponse.SnapshotPoint.builder()
                        .time(s.getSnapshotTime())
                        .price(s.getFinalPrice())
                        .multiplier(s.getTotalMultiplier())
                        .rules(s.getAppliedRules())
                        .build())
                .toList();

        return PriceHistoryResponse.builder()
                .entityId(entityId)
                .history(points)
                .build();
    }

    // ── Scheduled snapshot capture ───────────────────────────────────

    @Scheduled(fixedRate = 3600000) // Every hour
    public void captureSnapshots() {
        log.debug("Capturing hourly price snapshots");

        List<Flight> flights = flightRepository.findAll();
        for (Flight flight : flights) {
            PriceResponse price = calculatePrice(flight.getId(), "FLIGHT", null);
            PriceSnapshot snapshot = PriceSnapshot.builder()
                    .entityId(flight.getId())
                    .entityType("FLIGHT")
                    .basePrice(price.getBasePrice())
                    .finalPrice(price.getFinalPrice())
                    .totalMultiplier(price.getTotalMultiplier())
                    .appliedRules(String.join(", ", price.getAppliedRules()))
                    .snapshotTime(LocalDateTime.now())
                    .build();
            snapshotRepository.save(snapshot);
        }

        List<Hotel> hotels = hotelRepository.findAll();
        for (Hotel hotel : hotels) {
            PriceResponse price = calculatePrice(hotel.getId(), "HOTEL", null);
            PriceSnapshot snapshot = PriceSnapshot.builder()
                    .entityId(hotel.getId())
                    .entityType("HOTEL")
                    .basePrice(price.getBasePrice())
                    .finalPrice(price.getFinalPrice())
                    .totalMultiplier(price.getTotalMultiplier())
                    .appliedRules(String.join(", ", price.getAppliedRules()))
                    .snapshotTime(LocalDateTime.now())
                    .build();
            snapshotRepository.save(snapshot);
        }

        log.info("Captured price snapshots for {} flights and {} hotels", flights.size(), hotels.size());
    }

    // ── Price freeze ─────────────────────────────────────────────────

    public FreezeResponse freezePrice(FreezeRequest request) {
        // Check if user already has an active freeze for this entity
        freezeRepository.findByUserIdAndEntityIdAndUsedFalse(request.getUserId(), request.getEntityId())
                .filter(PriceFreeze::isActive)
                .ifPresent(f -> {
                    throw new IllegalStateException("You already have an active price freeze for this item");
                });

        PriceResponse currentPrice = calculatePrice(request.getEntityId(), request.getEntityType(), null);
        LocalDateTime now = LocalDateTime.now();

        PriceFreeze freeze = PriceFreeze.builder()
                .userId(request.getUserId())
                .entityId(request.getEntityId())
                .entityType(request.getEntityType())
                .frozenPrice(currentPrice.getFinalPrice())
                .frozenAt(now)
                .expiresAt(now.plusHours(FREEZE_HOURS))
                .used(false)
                .build();

        freeze = freezeRepository.save(freeze);
        return mapFreezeResponse(freeze);
    }

    public List<FreezeResponse> getUserFreezes(String userId) {
        return freezeRepository.findByUserIdAndUsedFalse(userId).stream()
                .filter(PriceFreeze::isActive)
                .map(this::mapFreezeResponse)
                .toList();
    }

    private FreezeResponse mapFreezeResponse(PriceFreeze freeze) {
        return FreezeResponse.builder()
                .id(freeze.getId())
                .entityId(freeze.getEntityId())
                .entityType(freeze.getEntityType())
                .frozenPrice(freeze.getFrozenPrice())
                .frozenAt(freeze.getFrozenAt())
                .expiresAt(freeze.getExpiresAt())
                .active(freeze.isActive())
                .build();
    }
}
