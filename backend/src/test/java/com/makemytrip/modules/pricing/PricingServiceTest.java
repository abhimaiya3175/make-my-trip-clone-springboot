package com.makemytrip.modules.pricing;

import com.makemytrip.modules.flights.model.Flight;
import com.makemytrip.modules.flights.repository.FlightRepository;
import com.makemytrip.modules.hotels.model.Hotel;
import com.makemytrip.modules.hotels.repository.HotelRepository;
import com.makemytrip.modules.pricing.dto.FreezeRequest;
import com.makemytrip.modules.pricing.dto.FreezeResponse;
import com.makemytrip.modules.pricing.dto.PriceHistoryResponse;
import com.makemytrip.modules.pricing.dto.PriceResponse;
import com.makemytrip.modules.pricing.model.PriceFreeze;
import com.makemytrip.modules.pricing.model.PriceSnapshot;
import com.makemytrip.modules.pricing.model.PricingRule;
import com.makemytrip.modules.pricing.repository.PriceFreezeRepository;
import com.makemytrip.modules.pricing.repository.PriceSnapshotRepository;
import com.makemytrip.modules.pricing.repository.PricingRuleRepository;
import com.makemytrip.modules.pricing.service.PricingService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
class PricingServiceTest {

    private static final Set<String> FIXED_HOLIDAYS_MM_DD = Set.of("12-25", "01-01", "01-26", "08-15", "10-02");

    @Mock private PricingRuleRepository ruleRepository;
    @Mock private PriceSnapshotRepository snapshotRepository;
    @Mock private PriceFreezeRepository freezeRepository;
    @Mock private FlightRepository flightRepository;
    @Mock private HotelRepository hotelRepository;
    @InjectMocks private PricingService service;

    private Flight flight(String id, double price, int seats) {
        Flight f = new Flight();
        f.setId(id);
        f.setPrice(price);
        f.setAvailableSeats(seats);
        return f;
    }

    private Hotel hotel(String id, double price, int rooms) {
        Hotel h = new Hotel();
        h.setId(id);
        h.setPricePerNight(price);
        h.setAvailableRooms(rooms);
        return h;
    }

    private LocalDate stableWeekdayDate() {
        LocalDate candidate = LocalDate.now().plusDays(10);
        for (int i = 0; i < 30; i++) {
            String mmdd = String.format("%02d-%02d", candidate.getMonthValue(), candidate.getDayOfMonth());
            boolean weekend = candidate.getDayOfWeek().getValue() >= 6;
            if (!weekend && !FIXED_HOLIDAYS_MM_DD.contains(mmdd)) {
                return candidate;
            }
            candidate = candidate.plusDays(1);
        }
        return LocalDate.now().plusDays(14);
    }

    // ── calculatePrice ───────────────────────────────────────────────

    @Test
    void calculatePrice_noRules_returnsBasePrice() {
        when(flightRepository.findById("fl-1")).thenReturn(Optional.of(flight("fl-1", 5000, 50)));
        LocalDate travelDate = stableWeekdayDate();

        PriceResponse result = service.calculatePrice("fl-1", "FLIGHT", null, travelDate);

        assertThat(result.getBasePrice()).isEqualTo(5000.0);
        assertThat(result.getFinalPrice()).isEqualTo(5000.0);
        assertThat(result.getTotalMultiplier()).isEqualTo(1.0);
        assertThat(result.getAppliedRules()).isEmpty();
    }

    @Test
    void calculatePrice_highOccupancy_increasesPrice() {
        when(flightRepository.findById("fl-1")).thenReturn(Optional.of(flight("fl-1", 5000, 10)));
        LocalDate travelDate = stableWeekdayDate();

        PriceResponse result = service.calculatePrice("fl-1", "FLIGHT", null, travelDate);

        assertThat(result.getTotalMultiplier()).isGreaterThan(1.0);
        assertThat(result.getFinalPrice()).isGreaterThan(5000.0);
        assertThat(result.getAppliedRules()).isNotEmpty();
    }

    @Test
    void calculatePrice_nearDepartureLowOccupancy_decreasesPrice() {
        when(flightRepository.findById("fl-1")).thenReturn(Optional.of(flight("fl-1", 5000, 95)));
        LocalDate travelDate = LocalDate.now().plusDays(1);

        PriceResponse result = service.calculatePrice("fl-1", "FLIGHT", null, travelDate);

        assertThat(result.getTotalMultiplier()).isLessThan(1.0);
        assertThat(result.getFinalPrice()).isLessThan(5000.0);
        assertThat(result.getAppliedRules()).contains("Near-Departure Empty Seat Discount");
    }

    @Test
    void calculatePrice_fallsBackToDefaultPrice_whenNotFound() {
        when(flightRepository.findById("unknown")).thenReturn(Optional.empty());

        PriceResponse result = service.calculatePrice("unknown", "FLIGHT", null, stableWeekdayDate());

        assertThat(result.getBasePrice()).isEqualTo(5000.0); // default fallback
    }

    @Test
    void calculatePrice_hotel_usesHotelRepo() {
        when(hotelRepository.findById("h-1")).thenReturn(Optional.of(hotel("h-1", 3500, 20)));
        LocalDate travelDate = stableWeekdayDate();

        PriceResponse result = service.calculatePrice("h-1", "HOTEL", null, travelDate);

        assertThat(result.getBasePrice()).isEqualTo(3500.0);
    }

    @Test
    void calculatePrice_attachesFreezeInfo() {
        when(flightRepository.findById("fl-1")).thenReturn(Optional.of(flight("fl-1", 5000, 50)));

        PriceFreeze freeze = PriceFreeze.builder()
                .userId("u1").entityId("fl-1").entityType("FLIGHT")
                .frozenPrice(4800).frozenAt(LocalDateTime.now().minusHours(1))
                .expiresAt(LocalDateTime.now().plusHours(23)).used(false).build();
        when(freezeRepository.findByUserIdAndEntityIdAndUsedFalse("u1", "fl-1"))
                .thenReturn(Optional.of(freeze));

        PriceResponse result = service.calculatePrice("fl-1", "FLIGHT", "u1", stableWeekdayDate());

        assertThat(result.getFrozenPrice()).isEqualTo(4800.0);
        assertThat(result.getFreezeExpiresAt()).isNotNull();
    }

    // ── freezePrice ──────────────────────────────────────────────────

    @Test
    void freezePrice_success() {
        when(freezeRepository.findByUserIdAndEntityIdAndUsedFalse("u1", "fl-1"))
                .thenReturn(Optional.empty());
        when(flightRepository.findById("fl-1")).thenReturn(Optional.of(flight("fl-1", 5000, 50)));
        when(freezeRepository.save(any(PriceFreeze.class))).thenAnswer(i -> {
            PriceFreeze f = i.getArgument(0);
            f.setId("frz-1");
            return f;
        });

        FreezeRequest req = new FreezeRequest();
        req.setUserId("u1");
        req.setEntityId("fl-1");
        req.setEntityType("FLIGHT");
        req.setTravelDate(LocalDate.now().plusDays(2));

        FreezeResponse result = service.freezePrice(req);

        assertThat(result.getFrozenPrice()).isGreaterThan(0.0);
        assertThat(result.isActive()).isTrue();
    }

    @Test
    void freezePrice_alreadyActive_throwsIllegalState() {
        PriceFreeze existing = PriceFreeze.builder()
                .userId("u1").entityId("fl-1")
                .frozenAt(LocalDateTime.now().minusHours(1))
                .expiresAt(LocalDateTime.now().plusHours(23))
                .used(false).build();
        when(freezeRepository.findByUserIdAndEntityIdAndUsedFalse("u1", "fl-1"))
                .thenReturn(Optional.of(existing));

        FreezeRequest req = new FreezeRequest();
        req.setUserId("u1");
        req.setEntityId("fl-1");
        req.setEntityType("FLIGHT");
        req.setTravelDate(LocalDate.now().plusDays(2));

        assertThatThrownBy(() -> service.freezePrice(req))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already have an active");
    }

        @Test
        void freezePrice_moreThanSevenDaysBeforeDeparture_rejected() {
        when(freezeRepository.findByUserIdAndEntityIdAndUsedFalse("u1", "fl-1"))
            .thenReturn(Optional.empty());

        FreezeRequest req = new FreezeRequest();
        req.setUserId("u1");
        req.setEntityId("fl-1");
        req.setEntityType("FLIGHT");
        req.setTravelDate(LocalDate.now().plusDays(10));

        assertThatThrownBy(() -> service.freezePrice(req))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("within 7 days");
        }

    // ── getHistory ───────────────────────────────────────────────────

    @Test
    void getHistory_returnsSnapshots() {
        PriceSnapshot snap = PriceSnapshot.builder()
                .entityId("fl-1").entityType("FLIGHT")
                .basePrice(5000).finalPrice(5750).totalMultiplier(1.15)
                .appliedRules("Weekend Surge")
                .snapshotTime(LocalDateTime.now().minusHours(2)).build();
        when(snapshotRepository.findByEntityIdAndSnapshotTimeBetweenOrderBySnapshotTimeAsc(
                eq("fl-1"), any(), any())).thenReturn(List.of(snap));

        PriceHistoryResponse result = service.getHistory("fl-1", 7);

        assertThat(result.getHistory()).hasSize(1);
        assertThat(result.getHistory().get(0).getPrice()).isEqualTo(5750);
    }

    @Test
    void getHistory_empty() {
        when(snapshotRepository.findByEntityIdAndSnapshotTimeBetweenOrderBySnapshotTimeAsc(
                eq("fl-1"), any(), any())).thenReturn(Collections.emptyList());

        PriceHistoryResponse result = service.getHistory("fl-1", 7);

        assertThat(result.getHistory()).isEmpty();
    }

    // ── getUserFreezes ───────────────────────────────────────────────

    @Test
    void getUserFreezes_filtersExpired() {
        PriceFreeze active = PriceFreeze.builder()
                .id("f1").userId("u1").entityId("fl-1").entityType("FLIGHT")
                .frozenPrice(5000).frozenAt(LocalDateTime.now().minusHours(1))
                .expiresAt(LocalDateTime.now().plusHours(23)).used(false).build();
        PriceFreeze expired = PriceFreeze.builder()
                .id("f2").userId("u1").entityId("fl-2").entityType("FLIGHT")
                .frozenPrice(6000).frozenAt(LocalDateTime.now().minusDays(2))
                .expiresAt(LocalDateTime.now().minusDays(1)).used(false).build();
        when(freezeRepository.findByUserIdAndUsedFalse("u1")).thenReturn(List.of(active, expired));

        List<FreezeResponse> result = service.getUserFreezes("u1");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getEntityId()).isEqualTo("fl-1");
    }
}
