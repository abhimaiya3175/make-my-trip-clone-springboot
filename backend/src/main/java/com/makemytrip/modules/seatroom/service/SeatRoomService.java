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
import java.util.List;

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
        return seatRepository.findByFlightId(flightId).stream()
                .map(seat -> mapSeatResponse(seat, userId))
                .toList();
    }

    public List<SeatResponse> getAvailableSeats(String flightId, String userId) {
        return seatRepository.findByFlightIdAndAvailable(flightId, true).stream()
                .filter(seat -> !seat.isLocked() || userId.equals(seat.getLockedByUserId()))
                .map(seat -> mapSeatResponse(seat, userId))
                .toList();
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
        return roomRepository.findByHotelId(hotelId).stream()
                .map(room -> mapRoomResponse(room, userId))
                .toList();
    }

    public List<RoomResponse> getAvailableRooms(String hotelId, String userId) {
        return roomRepository.findByHotelIdAndAvailable(hotelId, true).stream()
                .filter(room -> !room.isLocked() || userId.equals(room.getLockedByUserId()))
                .map(room -> mapRoomResponse(room, userId))
                .toList();
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
