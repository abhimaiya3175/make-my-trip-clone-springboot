package com.makemytrip.modules.seatroom.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.makemytrip.modules.seatroom.dto.*;
import com.makemytrip.modules.seatroom.model.*;
import com.makemytrip.modules.seatroom.repository.RoomRepository;
import com.makemytrip.modules.seatroom.repository.SeatRepository;
import com.makemytrip.modules.seatroom.repository.UserPreferenceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.HashMap;

@Slf4j
@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
public class SeatRoomService {

    private final SeatRepository seatRepository;
    private final RoomRepository roomRepository;
    private final UserPreferenceRepository preferenceRepository;

    private static final int LOCK_DURATION_MINUTES = 10;

    // ── Mock data bootstrap ──────────────────────────────────────────

    @PostConstruct
    public void loadMockData() {
        loadMockSeats();
        loadMockRooms();
    }

    private void loadMockSeats() {
        if (seatRepository.count() > 0) {
            log.info("Seat data already loaded, skipping");
            return;
        }
        try {
            ObjectMapper mapper = new ObjectMapper();
            ClassPathResource resource = new ClassPathResource("mock/seats.mock.json");
            List<Seat> seats = mapper.readValue(resource.getInputStream(), new TypeReference<>() {
            });
            seatRepository.saveAll(seats);
            log.info("Loaded {} mock seats", seats.size());
        } catch (IOException e) {
            log.error("Failed to load mock seat data", e);
        }
    }

    private void loadMockRooms() {
        if (roomRepository.count() > 0) {
            log.info("Room data already loaded, skipping");
            return;
        }
        try {
            ObjectMapper mapper = new ObjectMapper();
            ClassPathResource resource = new ClassPathResource("mock/rooms.mock.json");
            List<Room> rooms = mapper.readValue(resource.getInputStream(), new TypeReference<>() {
            });
            roomRepository.saveAll(rooms);
            log.info("Loaded {} mock rooms", rooms.size());
        } catch (IOException e) {
            log.error("Failed to load mock room data", e);
        }
    }

    // ── Seat operations ──────────────────────────────────────────────

    public List<SeatResponse> getSeatsByFlightId(String flightId, String userId) {
        ensureSeatMapForFlight(flightId);
        return seatRepository.findByFlightId(flightId).stream()
                .map(seat -> mapSeatResponse(seat, userId))
                .toList();
    }

    public List<SeatResponse> getAvailableSeats(String flightId, String userId) {
        ensureSeatMapForFlight(flightId);
        return seatRepository.findByFlightIdAndAvailable(flightId, true).stream()
                .filter(seat -> !seat.isLocked() || userId.equals(seat.getLockedByUserId()))
                .map(seat -> mapSeatResponse(seat, userId))
                .toList();
    }

    private void ensureSeatMapForFlight(String flightId) {
        if (flightId == null || flightId.isBlank() || seatRepository.existsByFlightId(flightId)) {
            return;
        }

        try {
            List<Seat> generatedSeats = new ArrayList<>();
            String[] columns = {"A", "B", "C", "D", "E", "F"};

            for (int row = 1; row <= 20; row++) {
                for (String column : columns) {
                    generatedSeats.add(Seat.builder()
                            .flightId(flightId)
                            .seatNumber(row + column)
                            .row(String.valueOf(row))
                            .column(column)
                            .seatClass(resolveSeatClass(row))
                            .available(true)
                            .basePrice(resolveBasePrice(row))
                            .premiumSurcharge(resolveSurcharge(row, column))
                            .lockedByUserId(null)
                            .lockedUntil(null)
                            .build());
                }
            }

            seatRepository.saveAll(generatedSeats);
            log.info("Generated {} seats for flight {}", generatedSeats.size(), flightId);
        } catch (Exception ex) {
            // Another request may generate seats concurrently; unique index protects integrity.
            log.debug("Seat map generation skipped for flight {}: {}", flightId, ex.getMessage());
        }
    }

    private SeatClass resolveSeatClass(int row) {
        if (row <= 2) {
            return SeatClass.FIRST;
        }
        if (row <= 6) {
            return SeatClass.BUSINESS;
        }
        return SeatClass.ECONOMY;
    }

    private double resolveBasePrice(int row) {
        if (row <= 2) {
            return 4500.0;
        }
        if (row <= 6) {
            return 2500.0;
        }
        return 800.0;
    }

    private double resolveSurcharge(int row, String column) {
        boolean isWindow = "A".equals(column) || "F".equals(column);
        boolean isExitRow = row == 10 || row == 11;

        double surcharge = 0.0;
        if (isWindow) {
            surcharge += 200.0;
        }
        if (isExitRow) {
            surcharge += 300.0;
        }
        if (row <= 3) {
            surcharge += 250.0;
        }
        return surcharge;
    }

    /**
     * Lock a seat for the user (10-minute hold).
     * Uses @Version for optimistic locking — throws 409 on concurrent modification.
     */
    public SeatResponse lockSeat(String seatId, String userId) {
        Seat seat = seatRepository.findById(seatId)
                .orElseThrow(() -> new ResourceNotFoundException("Seat not found: " + seatId));

        if (!seat.isAvailable()) {
            throw new ConflictException("Seat is already booked");
        }
        if (seat.isLocked() && !userId.equals(seat.getLockedByUserId())) {
            throw new ConflictException("Seat is locked by another user");
        }

        seat.setLockedByUserId(userId);
        seat.setLockedUntil(LocalDateTime.now().plusMinutes(LOCK_DURATION_MINUTES));

        try {
            seat = seatRepository.save(seat);
        } catch (OptimisticLockingFailureException e) {
            throw new ConflictException("Seat was modified concurrently, please retry");
        }
        return mapSeatResponse(seat, userId);
    }

    /**
     * Release a seat lock (only the locking user or expired locks).
     */
    public SeatResponse releaseSeat(String seatId, String userId) {
        Seat seat = seatRepository.findById(seatId)
                .orElseThrow(() -> new ResourceNotFoundException("Seat not found: " + seatId));

        if (seat.getLockedByUserId() != null && !userId.equals(seat.getLockedByUserId())) {
            throw new ConflictException("You do not hold the lock on this seat");
        }

        seat.setLockedByUserId(null);
        seat.setLockedUntil(null);
        seat = seatRepository.save(seat);
        return mapSeatResponse(seat, userId);
    }

    /**
     * Confirm seat booking — marks it unavailable permanently.
     */
    public SeatResponse confirmSeatBooking(String seatId, String userId) {
        Seat seat = seatRepository.findById(seatId)
                .orElseThrow(() -> new ResourceNotFoundException("Seat not found: " + seatId));

        if (!seat.isAvailable()) {
            throw new ConflictException("Seat is already booked");
        }
        // Must hold the lock
        if (!userId.equals(seat.getLockedByUserId())) {
            throw new ConflictException("You must lock the seat before confirming");
        }

        seat.setAvailable(false);
        seat.setLockedByUserId(null);
        seat.setLockedUntil(null);

        try {
            seat = seatRepository.save(seat);
        } catch (OptimisticLockingFailureException e) {
            throw new ConflictException("Seat was modified concurrently, please retry");
        }
        return mapSeatResponse(seat, userId);
    }

    // ── Room operations ──────────────────────────────────────────────

    public List<RoomResponse> getRoomsByHotelId(String hotelId, String userId) {
        ensureRoomInventoryForHotel(hotelId);
        return roomRepository.findByHotelId(hotelId).stream()
                .map(room -> mapRoomResponse(room, userId))
                .toList();
    }

    public List<RoomResponse> getAvailableRooms(String hotelId, String userId) {
        ensureRoomInventoryForHotel(hotelId);
        return roomRepository.findByHotelIdAndAvailable(hotelId, true).stream()
                .filter(room -> !room.isLocked() || userId.equals(room.getLockedByUserId()))
                .map(room -> mapRoomResponse(room, userId))
                .toList();
    }

    private void ensureRoomInventoryForHotel(String hotelId) {
        if (hotelId == null || hotelId.isBlank() || roomRepository.existsByHotelId(hotelId)) {
            return;
        }

        try {
            List<Room> generatedRooms = new ArrayList<>();
            int seed = Math.abs(hotelId.hashCode());
            int floors = 3 + (seed % 4); // 3 to 6 floors

            for (int floor = 1; floor <= floors; floor++) {
                int roomsOnFloor = 4 + Math.abs((seed + floor * 31) % 3); // 4 to 6 rooms, irregular by floor

                for (int roomIndex = 1; roomIndex <= roomsOnFloor; roomIndex++) {
                    int roomNo = floor * 100 + roomIndex;
                    RoomType roomType = resolveRoomType(floor, roomIndex, floors);
                    double price = resolveRoomPrice(roomType) + (Math.abs(seed + floor * 17 + roomIndex * 13) % 6) * 250;
                    boolean available = ((seed + floor * roomIndex) % 9) != 0;

                    generatedRooms.add(Room.builder()
                            .hotelId(hotelId)
                            .roomNumber(String.valueOf(roomNo))
                            .roomType(roomType)
                            .available(available)
                            .pricePerNight(price)
                            .maxOccupancy(resolveMaxOccupancy(roomType))
                            .amenities(resolveAmenities(roomType))
                            .images(resolveImages(hotelId, roomNo))
                            .isPanorama(roomType != RoomType.STANDARD)
                            .lockedByUserId(null)
                            .lockedUntil(null)
                            .build());
                }
            }

            if (floors >= 5) {
                generatedRooms.add(Room.builder()
                        .hotelId(hotelId)
                        .roomNumber("PH01")
                        .roomType(RoomType.PENTHOUSE)
                        .available((seed % 4) != 0)
                        .pricePerNight(22000 + (seed % 5) * 1000)
                        .maxOccupancy(6)
                        .amenities(Arrays.asList("WiFi", "Balcony", "Living Room", "Jacuzzi", "Private Pool"))
                        .images(Arrays.asList(
                                "https://picsum.photos/seed/" + hotelId + "-PH01-1/1200/600",
                                "https://picsum.photos/seed/" + hotelId + "-PH01-2/1200/600"))
                        .isPanorama(true)
                        .lockedByUserId(null)
                        .lockedUntil(null)
                        .build());
            }

            roomRepository.saveAll(generatedRooms);
            log.info("Generated {} rooms for hotel {}", generatedRooms.size(), hotelId);
        } catch (Exception ex) {
            // Another request may generate rooms concurrently; unique index protects integrity.
            log.debug("Room inventory generation skipped for hotel {}: {}", hotelId, ex.getMessage());
        }
    }

    private RoomType resolveRoomType(int floor, int roomIndex, int totalFloors) {
        if (floor >= totalFloors - 1 && roomIndex <= 2) {
            return RoomType.SUITE;
        }
        if (floor >= 3 || roomIndex == 1) {
            return RoomType.DELUXE;
        }
        return RoomType.STANDARD;
    }

    private double resolveRoomPrice(RoomType roomType) {
        return switch (roomType) {
            case STANDARD -> 3200.0;
            case DELUXE -> 5400.0;
            case SUITE -> 9800.0;
            case PENTHOUSE -> 20000.0;
        };
    }

    private int resolveMaxOccupancy(RoomType roomType) {
        return switch (roomType) {
            case STANDARD -> 2;
            case DELUXE -> 3;
            case SUITE -> 4;
            case PENTHOUSE -> 6;
        };
    }

    private List<String> resolveAmenities(RoomType roomType) {
        List<String> base = new ArrayList<>(Arrays.asList("WiFi", "AC", "TV"));
        if (roomType == RoomType.DELUXE || roomType == RoomType.SUITE || roomType == RoomType.PENTHOUSE) {
            base.add("Mini Bar");
            base.add("Balcony");
        }
        if (roomType == RoomType.SUITE || roomType == RoomType.PENTHOUSE) {
            base.add("Living Room");
            base.add("Jacuzzi");
        }
        if (roomType == RoomType.PENTHOUSE) {
            base.add("Private Pool");
        }
        return base;
    }

    private List<String> resolveImages(String hotelId, int roomNo) {
        return Arrays.asList(
                "https://picsum.photos/seed/" + hotelId + "-" + roomNo + "-1/1200/600",
                "https://picsum.photos/seed/" + hotelId + "-" + roomNo + "-2/1200/600");
    }

    public RoomResponse lockRoom(String roomId, String userId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found: " + roomId));

        if (!room.isAvailable()) {
            throw new ConflictException("Room is already booked");
        }
        if (room.isLocked() && !userId.equals(room.getLockedByUserId())) {
            throw new ConflictException("Room is locked by another user");
        }

        room.setLockedByUserId(userId);
        room.setLockedUntil(LocalDateTime.now().plusMinutes(LOCK_DURATION_MINUTES));

        try {
            room = roomRepository.save(room);
        } catch (OptimisticLockingFailureException e) {
            throw new ConflictException("Room was modified concurrently, please retry");
        }
        return mapRoomResponse(room, userId);
    }

    public RoomResponse releaseRoom(String roomId, String userId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found: " + roomId));

        if (room.getLockedByUserId() != null && !userId.equals(room.getLockedByUserId())) {
            throw new ConflictException("You do not hold the lock on this room");
        }

        room.setLockedByUserId(null);
        room.setLockedUntil(null);
        room = roomRepository.save(room);
        return mapRoomResponse(room, userId);
    }

    public RoomResponse confirmRoomBooking(String roomId, String userId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found: " + roomId));

        if (!room.isAvailable()) {
            throw new ConflictException("Room is already booked");
        }
        if (!userId.equals(room.getLockedByUserId())) {
            throw new ConflictException("You must lock the room before confirming");
        }

        room.setAvailable(false);
        room.setLockedByUserId(null);
        room.setLockedUntil(null);

        try {
            room = roomRepository.save(room);
        } catch (OptimisticLockingFailureException e) {
            throw new ConflictException("Room was modified concurrently, please retry");
        }
        return mapRoomResponse(room, userId);
    }

    /**
     * Block room dates for a booking (called when booking is confirmed)
     */
    public void blockRoomDates(String roomId, LocalDate checkInDate, LocalDate checkOutDate, String bookingId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found: " + roomId));

        if (room.getBlockedDates() == null) {
            room.setBlockedDates(new ArrayList<>());
        }
        if (room.getDateBookingMap() == null) {
            room.setDateBookingMap(new HashMap<>());
        }

        // Block all dates from checkInDate (inclusive) to checkOutDate (exclusive)
        LocalDate currentDate = checkInDate;
        while (currentDate.isBefore(checkOutDate)) {
            if (!room.getBlockedDates().contains(currentDate)) {
                room.getBlockedDates().add(currentDate);
                room.getDateBookingMap().put(currentDate.toString(), bookingId);
            }
            currentDate = currentDate.plusDays(1);
        }

        try {
            roomRepository.save(room);
            log.info("Blocked room {} from {} to {} for booking {}", 
                    roomId, checkInDate, checkOutDate, bookingId);
        } catch (OptimisticLockingFailureException e) {
            throw new ConflictException("Room was modified concurrently, please retry");
        }
    }

    /**
     * Unblock room dates (called when booking is cancelled)
     */
    public void unblockRoomDates(String roomId, LocalDate checkInDate, LocalDate checkOutDate, String bookingId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found: " + roomId));

        if (room.getBlockedDates() == null || room.getDateBookingMap() == null) {
            return; // Already unblocked
        }

        // Unblock all dates from checkInDate (inclusive) to checkOutDate (exclusive)
        LocalDate currentDate = checkInDate;
        while (currentDate.isBefore(checkOutDate)) {
            String dateStr = currentDate.toString();
            // Only remove if it was blocked by this booking
            if (bookingId.equals(room.getDateBookingMap().get(dateStr))) {
                room.getBlockedDates().remove(currentDate);
                room.getDateBookingMap().remove(dateStr);
            }
            currentDate = currentDate.plusDays(1);
        }

        try {
            roomRepository.save(room);
            log.info("Unblocked room {} from {} to {} for booking {}", 
                    roomId, checkInDate, checkOutDate, bookingId);
        } catch (OptimisticLockingFailureException e) {
            throw new ConflictException("Room was modified concurrently, please retry");
        }
    }

    /**
     * Check if room is available for date range (not considering temporary locks)
     */
    public boolean isRoomAvailableForDateRange(String roomId, LocalDate checkInDate, LocalDate checkOutDate) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found: " + roomId));

        if (room.getBlockedDates() == null || room.getBlockedDates().isEmpty()) {
            return true;
        }

        // Check if any date in range is blocked
        LocalDate currentDate = checkInDate;
        while (currentDate.isBefore(checkOutDate)) {
            if (room.getBlockedDates().contains(currentDate)) {
                return false;
            }
            currentDate = currentDate.plusDays(1);
        }

        return true;
    }

    // ── User preferences ─────────────────────────────────────────────

    public UserPreference getPreferences(String userId) {
        return preferenceRepository.findByUserId(userId).orElse(null);
    }

    public UserPreference savePreferences(UserPreferenceRequest request) {
        UserPreference pref = preferenceRepository.findByUserId(request.getUserId())
                .orElse(UserPreference.builder().userId(request.getUserId()).build());

        if (request.getPreferredSeatClass() != null)
            pref.setPreferredSeatClass(request.getPreferredSeatClass());
        if (request.getPreferredSeatPosition() != null)
            pref.setPreferredSeatPosition(request.getPreferredSeatPosition());
        if (request.getPreferredRoomType() != null)
            pref.setPreferredRoomType(request.getPreferredRoomType());
        if (request.getPreferredMaxOccupancy() != null)
            pref.setPreferredMaxOccupancy(request.getPreferredMaxOccupancy());

        return preferenceRepository.save(pref);
    }

    // ── Mapping helpers ──────────────────────────────────────────────

    private SeatResponse mapSeatResponse(Seat seat, String userId) {
        return SeatResponse.builder()
                .id(seat.getId())
                .flightId(seat.getFlightId())
                .seatNumber(seat.getSeatNumber())
                .row(seat.getRow())
                .column(seat.getColumn())
                .seatClass(seat.getSeatClass())
                .available(seat.isAvailable())
                .basePrice(seat.getBasePrice())
                .premiumSurcharge(seat.getPremiumSurcharge())
                .effectivePrice(seat.getEffectivePrice())
                .locked(seat.isLocked())
                .lockedByMe(userId != null && userId.equals(seat.getLockedByUserId()))
                .build();
    }

    private RoomResponse mapRoomResponse(Room room, String userId) {
        return RoomResponse.builder()
                .id(room.getId())
                .hotelId(room.getHotelId())
                .roomNumber(room.getRoomNumber())
                .roomType(room.getRoomType())
                .available(room.isAvailable())
                .pricePerNight(room.getPricePerNight())
                .maxOccupancy(room.getMaxOccupancy())
                .amenities(room.getAmenities())
                .images(room.getImages())
                .isPanorama(room.isPanorama())
                .locked(room.isLocked())
                .lockedByMe(userId != null && userId.equals(room.getLockedByUserId()))
                .build();
    }

    // ── Custom exceptions ────────────────────────────────────────────

    public static class ResourceNotFoundException extends RuntimeException {
        public ResourceNotFoundException(String message) {
            super(message);
        }
    }

    public static class ConflictException extends RuntimeException {
        public ConflictException(String message) {
            super(message);
        }
    }
}
