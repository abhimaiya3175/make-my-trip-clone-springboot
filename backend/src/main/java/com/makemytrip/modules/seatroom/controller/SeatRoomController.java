package com.makemytrip.modules.seatroom.controller;

import com.makemytrip.common.api.ApiError;
import com.makemytrip.common.api.ApiResponse;
import com.makemytrip.modules.seatroom.dto.*;
import com.makemytrip.modules.seatroom.model.UserPreference;
import com.makemytrip.modules.seatroom.service.SeatRoomService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/seatroom")
@RequiredArgsConstructor
public class SeatRoomController {

    private final SeatRoomService seatRoomService;

    // ── Seat endpoints ──────────────────────────────────────────────

    @GetMapping("/seats/flight/{flightId}")
    public ResponseEntity<ApiResponse<List<SeatResponse>>> getSeatsByFlight(
            @PathVariable String flightId,
            @RequestParam(required = false) String userId) {
        var seats = seatRoomService.getSeatsByFlightId(flightId, userId);
        return ResponseEntity.ok(ApiResponse.ok(seats, reqId()));
    }

    @GetMapping("/seats/flight/{flightId}/available")
    public ResponseEntity<ApiResponse<List<SeatResponse>>> getAvailableSeats(
            @PathVariable String flightId,
            @RequestParam(required = false) String userId) {
        var seats = seatRoomService.getAvailableSeats(flightId, userId);
        return ResponseEntity.ok(ApiResponse.ok(seats, reqId()));
    }

    @PostMapping("/seats/{seatId}/lock")
    public ResponseEntity<ApiResponse<SeatResponse>> lockSeat(
            @PathVariable String seatId,
            @Valid @RequestBody LockRequest request) {
        var seat = seatRoomService.lockSeat(seatId, request.getUserId());
        return ResponseEntity.ok(ApiResponse.ok(seat, reqId()));
    }

    @PostMapping("/seats/{seatId}/release")
    public ResponseEntity<ApiResponse<SeatResponse>> releaseSeat(
            @PathVariable String seatId,
            @Valid @RequestBody LockRequest request) {
        var seat = seatRoomService.releaseSeat(seatId, request.getUserId());
        return ResponseEntity.ok(ApiResponse.ok(seat, reqId()));
    }

    @PostMapping("/seats/{seatId}/confirm")
    public ResponseEntity<ApiResponse<SeatResponse>> confirmSeat(
            @PathVariable String seatId,
            @Valid @RequestBody LockRequest request) {
        var seat = seatRoomService.confirmSeatBooking(seatId, request.getUserId());
        return ResponseEntity.ok(ApiResponse.ok(seat, reqId()));
    }

    // ── Room endpoints ──────────────────────────────────────────────

    @GetMapping("/rooms/hotel/{hotelId}")
    public ResponseEntity<ApiResponse<List<RoomResponse>>> getRoomsByHotel(
            @PathVariable String hotelId,
            @RequestParam(required = false) String userId) {
        var rooms = seatRoomService.getRoomsByHotelId(hotelId, userId);
        return ResponseEntity.ok(ApiResponse.ok(rooms, reqId()));
    }

    @GetMapping("/rooms/hotel/{hotelId}/available")
    public ResponseEntity<ApiResponse<List<RoomResponse>>> getAvailableRooms(
            @PathVariable String hotelId,
            @RequestParam(required = false) String userId) {
        var rooms = seatRoomService.getAvailableRooms(hotelId, userId);
        return ResponseEntity.ok(ApiResponse.ok(rooms, reqId()));
    }

    @PostMapping("/rooms/{roomId}/lock")
    public ResponseEntity<ApiResponse<RoomResponse>> lockRoom(
            @PathVariable String roomId,
            @Valid @RequestBody LockRequest request) {
        var room = seatRoomService.lockRoom(roomId, request.getUserId());
        return ResponseEntity.ok(ApiResponse.ok(room, reqId()));
    }

    @PostMapping("/rooms/{roomId}/release")
    public ResponseEntity<ApiResponse<RoomResponse>> releaseRoom(
            @PathVariable String roomId,
            @Valid @RequestBody LockRequest request) {
        var room = seatRoomService.releaseRoom(roomId, request.getUserId());
        return ResponseEntity.ok(ApiResponse.ok(room, reqId()));
    }

    @PostMapping("/rooms/{roomId}/confirm")
    public ResponseEntity<ApiResponse<RoomResponse>> confirmRoom(
            @PathVariable String roomId,
            @Valid @RequestBody LockRequest request) {
        var room = seatRoomService.confirmRoomBooking(roomId, request.getUserId());
        return ResponseEntity.ok(ApiResponse.ok(room, reqId()));
    }

    // ── User preferences ─────────────────────────────────────────────

    @GetMapping("/preferences/{userId}")
    public ResponseEntity<ApiResponse<UserPreference>> getPreferences(@PathVariable String userId) {
        var pref = seatRoomService.getPreferences(userId);
        return ResponseEntity.ok(ApiResponse.ok(pref, reqId()));
    }

    @PutMapping("/preferences")
    public ResponseEntity<ApiResponse<UserPreference>> savePreferences(
            @Valid @RequestBody UserPreferenceRequest request) {
        var pref = seatRoomService.savePreferences(request);
        return ResponseEntity.ok(ApiResponse.ok(pref, reqId()));
    }

    // ── Exception handlers ───────────────────────────────────────────

    @ExceptionHandler(SeatRoomService.ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotFound(SeatRoomService.ResourceNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.fail(new ApiError("NOT_FOUND", e.getMessage(), null), reqId()));
    }

    @ExceptionHandler(SeatRoomService.ConflictException.class)
    public ResponseEntity<ApiResponse<Void>> handleConflict(SeatRoomService.ConflictException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.fail(new ApiError("CONFLICT", e.getMessage(), null), reqId()));
    }

    private String reqId() {
        return UUID.randomUUID().toString().substring(0, 8);
    }
}
