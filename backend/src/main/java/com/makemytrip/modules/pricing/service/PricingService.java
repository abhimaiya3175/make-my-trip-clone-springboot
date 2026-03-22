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
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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
    private static final int MAX_FREEZE_WINDOW_DAYS = 7;
    private static final DateTimeFormatter HOLIDAY_MM_DD = DateTimeFormatter.ofPattern("MM-dd");
    private static final Set<String> FIXED_HOLIDAYS_MM_DD = Set.of("12-25", "01-01", "01-26", "08-15", "10-02");
    private static final double OCCUPANCY_PRICE_SENSITIVITY = 0.006;
    private static final double MIN_OCCUPANCY_MULTIPLIER = 0.75;
    private static final double MAX_OCCUPANCY_MULTIPLIER = 1.30;

    // Approximate capacities used to derive occupancy from available inventory.
    private static final int ASSUMED_FLIGHT_CAPACITY = 100;
    private static final int ASSUMED_HOTEL_CAPACITY = 40;

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
                    // Store holidays as MM-DD so they recur every year.
                    .holidays(List.of("12-25", "01-01", "01-26", "08-15", "10-02"))
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

    public PriceResponse calculatePrice(String entityId, String entityType, String userId, LocalDate travelDate) {
        double basePrice = getBasePrice(entityId, entityType);
        double totalMultiplier = 1.0;
        List<String> appliedRuleNames = new ArrayList<>();
        LocalDate today = LocalDate.now();
        LocalDate effectiveTravelDate = travelDate != null ? travelDate : today.plusDays(7);
        long daysUntilTravel = ChronoUnit.DAYS.between(today, effectiveTravelDate);
        double occupancy = calculateOccupancy(entityId, entityType);

        double occupancyMultiplier = calculateOccupancyMultiplier(occupancy);
        totalMultiplier *= occupancyMultiplier;
        if (occupancyMultiplier > 1.02) {
            appliedRuleNames.add(String.format("Occupancy Surge (%.0f%% full)", occupancy));
        } else if (occupancyMultiplier < 0.98) {
            appliedRuleNames.add(String.format("Low Occupancy Discount (%.0f%% full)", occupancy));
        }

        double departureMultiplier = calculateDepartureMultiplier(daysUntilTravel, occupancy);
        totalMultiplier *= departureMultiplier;
        if (departureMultiplier > 1.0) {
            appliedRuleNames.add("Near-Departure Demand Surge");
        } else if (departureMultiplier < 1.0) {
            appliedRuleNames.add("Near-Departure Empty Seat Discount");
        }

        String holidayMonthDay = effectiveTravelDate.format(HOLIDAY_MM_DD);
        if (FIXED_HOLIDAYS_MM_DD.contains(holidayMonthDay)) {
            totalMultiplier *= 1.15;
            appliedRuleNames.add("Holiday Premium");
        }

        DayOfWeek travelDay = effectiveTravelDate.getDayOfWeek();
        if (travelDay == DayOfWeek.SATURDAY || travelDay == DayOfWeek.SUNDAY) {
            totalMultiplier *= 1.07;
            appliedRuleNames.add("Weekend Demand");
        }

        // Early-bird discount on weekdays for low-demand inventory far in advance.
        if (daysUntilTravel >= 30 && occupancy <= 50.0
                && travelDay != DayOfWeek.SATURDAY
                && travelDay != DayOfWeek.SUNDAY) {
            totalMultiplier *= 0.95;
            appliedRuleNames.add("Early Bird Discount");
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
        if ("FLIGHT".equals(entityType)) {
            return flightRepository.findById(entityId)
                    .map(f -> occupancyFromAvailable(f.getAvailableSeats(), ASSUMED_FLIGHT_CAPACITY))
                    .orElse(50.0);
        }
        return hotelRepository.findById(entityId)
                .map(h -> occupancyFromAvailable(h.getAvailableRooms(), ASSUMED_HOTEL_CAPACITY))
                .orElse(40.0);
    }

    private double calculateOccupancyMultiplier(double occupancyPercent) {
        // Baseline at 50% occupancy, then scale smoothly with demand pressure.
        double centered = occupancyPercent - 50.0;
        return clamp(1.0 + centered * OCCUPANCY_PRICE_SENSITIVITY,
                MIN_OCCUPANCY_MULTIPLIER,
                MAX_OCCUPANCY_MULTIPLIER);
    }

    private double calculateDepartureMultiplier(long daysUntilTravel, double occupancyPercent) {
        // Prices react more strongly in the final days before departure/check-in.
        if (daysUntilTravel <= 2) {
            if (occupancyPercent <= 35.0) {
                return 0.85;
            }
            if (occupancyPercent >= 85.0) {
                return 1.20;
            }
            if (occupancyPercent >= 70.0) {
                return 1.10;
            }
            return 0.95;
        }

        if (daysUntilTravel <= 7) {
            if (occupancyPercent <= 35.0) {
                return 0.93;
            }
            if (occupancyPercent >= 90.0) {
                return 1.08;
            }
        }

        return 1.0;
    }

    private double occupancyFromAvailable(int availableUnits, int capacity) {
        if (capacity <= 0) {
            return 50.0;
        }
        int boundedAvailable = Math.max(0, Math.min(availableUnits, capacity));
        double occupancyRatio = 1.0 - ((double) boundedAvailable / (double) capacity);
        return occupancyRatio * 100.0;
    }

    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
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
            PriceResponse price = calculatePrice(flight.getId(), "FLIGHT", null, LocalDate.now().plusDays(7));
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
            PriceResponse price = calculatePrice(hotel.getId(), "HOTEL", null, LocalDate.now().plusDays(7));
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

        LocalDate travelDate = request.getTravelDate();
        LocalDate today = LocalDate.now();
        long daysUntilTravel = ChronoUnit.DAYS.between(today, travelDate);

        if (daysUntilTravel < 0) {
            throw new IllegalStateException("Cannot freeze a price for a past departure date");
        }

        if (daysUntilTravel > MAX_FREEZE_WINDOW_DAYS) {
            throw new IllegalStateException("Price freeze is available only within 7 days before departure");
        }

        PriceResponse currentPrice = calculatePrice(
            request.getEntityId(),
            request.getEntityType(),
            null,
            travelDate);
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
