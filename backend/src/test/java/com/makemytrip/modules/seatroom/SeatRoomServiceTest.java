package com.makemytrip.modules.seatroom;

import com.makemytrip.modules.seatroom.dto.SeatResponse;
import com.makemytrip.modules.seatroom.dto.RoomResponse;
import com.makemytrip.modules.seatroom.dto.UserPreferenceRequest;
import com.makemytrip.modules.seatroom.model.*;
import com.makemytrip.modules.seatroom.repository.RoomRepository;
import com.makemytrip.modules.seatroom.repository.SeatRepository;
import com.makemytrip.modules.seatroom.repository.UserPreferenceRepository;
import com.makemytrip.modules.seatroom.service.SeatRoomService;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.OptimisticLockingFailureException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
class SeatRoomServiceTest {

    @Mock
    private SeatRepository seatRepository;
    @Mock
    private RoomRepository roomRepository;
    @Mock
    private UserPreferenceRepository preferenceRepository;
    @InjectMocks
    private SeatRoomService service;

    // ── Fixtures ─────────────────────────────────────────────────────

    private Seat availableSeat() {
        Seat s = new Seat();
        s.setId("seat-1");
        s.setFlightId("FL001");
        s.setSeatNumber("1A");
        s.setRow("1");
        s.setColumn("A");
        s.setSeatClass(SeatClass.FIRST);
        s.setAvailable(true);
        s.setBasePrice(15000);
        s.setPremiumSurcharge(2000);
        return s;
    }

    private Room availableRoom() {
        Room r = new Room();
        r.setId("room-1");
        r.setHotelId("HTL001");
        r.setRoomNumber("101");
        r.setRoomType(RoomType.DELUXE);
        r.setAvailable(true);
        r.setPricePerNight(5000);
        r.setMaxOccupancy(2);
        r.setAmenities(List.of("WiFi", "TV"));
        r.setImages(List.of("img1.jpg"));
        return r;
    }

    // ── Seat Tests ───────────────────────────────────────────────────

    @Nested
    class SeatOperations {

        @Test
        void getSeatsByFlightId_returnsAllSeats() {
            Seat seat = availableSeat();
            when(seatRepository.findByFlightId("FL001")).thenReturn(List.of(seat));

            List<SeatResponse> result = service.getSeatsByFlightId("FL001", "user-1");

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getSeatNumber()).isEqualTo("1A");
            assertThat(result.get(0).getEffectivePrice()).isEqualTo(17000); // 15000 + 2000
        }

        @Test
        void lockSeat_success() {
            Seat seat = availableSeat();
            when(seatRepository.findById("seat-1")).thenReturn(Optional.of(seat));
            when(seatRepository.save(any(Seat.class))).thenAnswer(i -> i.getArgument(0));

            SeatResponse result = service.lockSeat("seat-1", "user-1");

            assertThat(result.isLockedByMe()).isTrue();
            assertThat(result.isLocked()).isTrue();
            verify(seatRepository).save(argThat(s -> "user-1".equals(s.getLockedByUserId())));
        }

        @Test
        void lockSeat_alreadyBooked_throwsConflict() {
            Seat seat = availableSeat();
            seat.setAvailable(false);
            when(seatRepository.findById("seat-1")).thenReturn(Optional.of(seat));

            assertThatThrownBy(() -> service.lockSeat("seat-1", "user-1"))
                    .isInstanceOf(SeatRoomService.ConflictException.class)
                    .hasMessageContaining("already booked");
        }

        @Test
        void lockSeat_lockedByAnother_throwsConflict() {
            Seat seat = availableSeat();
            seat.setLockedByUserId("user-2");
            seat.setLockedUntil(LocalDateTime.now().plusMinutes(5));
            when(seatRepository.findById("seat-1")).thenReturn(Optional.of(seat));

            assertThatThrownBy(() -> service.lockSeat("seat-1", "user-1"))
                    .isInstanceOf(SeatRoomService.ConflictException.class)
                    .hasMessageContaining("locked by another");
        }

        @Test
        void lockSeat_optimisticLockFailure_throwsConflict() {
            Seat seat = availableSeat();
            when(seatRepository.findById("seat-1")).thenReturn(Optional.of(seat));
            when(seatRepository.save(any())).thenThrow(new OptimisticLockingFailureException("conflict"));

            assertThatThrownBy(() -> service.lockSeat("seat-1", "user-1"))
                    .isInstanceOf(SeatRoomService.ConflictException.class)
                    .hasMessageContaining("concurrently");
        }

        @Test
        void lockSeat_notFound_throwsNotFound() {
            when(seatRepository.findById("gone")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.lockSeat("gone", "user-1"))
                    .isInstanceOf(SeatRoomService.ResourceNotFoundException.class);
        }

        @Test
        void releaseSeat_success() {
            Seat seat = availableSeat();
            seat.setLockedByUserId("user-1");
            seat.setLockedUntil(LocalDateTime.now().plusMinutes(5));
            when(seatRepository.findById("seat-1")).thenReturn(Optional.of(seat));
            when(seatRepository.save(any(Seat.class))).thenAnswer(i -> i.getArgument(0));

            SeatResponse result = service.releaseSeat("seat-1", "user-1");

            assertThat(result.isLocked()).isFalse();
        }

        @Test
        void releaseSeat_wrongUser_throwsConflict() {
            Seat seat = availableSeat();
            seat.setLockedByUserId("user-2");
            seat.setLockedUntil(LocalDateTime.now().plusMinutes(5));
            when(seatRepository.findById("seat-1")).thenReturn(Optional.of(seat));

            assertThatThrownBy(() -> service.releaseSeat("seat-1", "user-1"))
                    .isInstanceOf(SeatRoomService.ConflictException.class);
        }

        @Test
        void confirmSeatBooking_success() {
            Seat seat = availableSeat();
            seat.setLockedByUserId("user-1");
            seat.setLockedUntil(LocalDateTime.now().plusMinutes(5));
            when(seatRepository.findById("seat-1")).thenReturn(Optional.of(seat));
            when(seatRepository.save(any(Seat.class))).thenAnswer(i -> i.getArgument(0));

            SeatResponse result = service.confirmSeatBooking("seat-1", "user-1");

            assertThat(result.isAvailable()).isFalse();
            verify(seatRepository).save(argThat(s -> !s.isAvailable() && s.getLockedByUserId() == null));
        }

        @Test
        void confirmSeatBooking_withoutLock_throwsConflict() {
            Seat seat = availableSeat();
            when(seatRepository.findById("seat-1")).thenReturn(Optional.of(seat));

            assertThatThrownBy(() -> service.confirmSeatBooking("seat-1", "user-1"))
                    .isInstanceOf(SeatRoomService.ConflictException.class)
                    .hasMessageContaining("must lock");
        }
    }

    // ── Room Tests ───────────────────────────────────────────────────

    @Nested
    class RoomOperations {

        @Test
        void getRoomsByHotelId_returnsMapped() {
            Room room = availableRoom();
            when(roomRepository.findByHotelId("HTL001")).thenReturn(List.of(room));

            List<RoomResponse> result = service.getRoomsByHotelId("HTL001", "user-1");

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getRoomType()).isEqualTo(RoomType.DELUXE);
        }

        @Test
        void lockRoom_success() {
            Room room = availableRoom();
            when(roomRepository.findById("room-1")).thenReturn(Optional.of(room));
            when(roomRepository.save(any(Room.class))).thenAnswer(i -> i.getArgument(0));

            RoomResponse result = service.lockRoom("room-1", "user-1");

            assertThat(result.isLockedByMe()).isTrue();
        }

        @Test
        void confirmRoomBooking_success() {
            Room room = availableRoom();
            room.setLockedByUserId("user-1");
            room.setLockedUntil(LocalDateTime.now().plusMinutes(5));
            when(roomRepository.findById("room-1")).thenReturn(Optional.of(room));
            when(roomRepository.save(any(Room.class))).thenAnswer(i -> i.getArgument(0));

            RoomResponse result = service.confirmRoomBooking("room-1", "user-1");

            assertThat(result.isAvailable()).isFalse();
        }
    }

    // ── Preference Tests ─────────────────────────────────────────────

    @Nested
    class Preferences {

        @Test
        void savePreferences_createsNew() {
            when(preferenceRepository.findByUserId("user-1")).thenReturn(Optional.empty());
            when(preferenceRepository.save(any())).thenAnswer(i -> i.getArgument(0));

            UserPreferenceRequest req = new UserPreferenceRequest();
            req.setUserId("user-1");
            req.setPreferredSeatClass(SeatClass.BUSINESS);
            req.setPreferredRoomType(RoomType.SUITE);

            UserPreference result = service.savePreferences(req);

            assertThat(result.getUserId()).isEqualTo("user-1");
        }

        @Test
        void getPreferences_returnsNull_whenMissing() {
            when(preferenceRepository.findByUserId("user-1")).thenReturn(Optional.empty());

            assertThat(service.getPreferences("user-1")).isNull();
        }
    }
}
