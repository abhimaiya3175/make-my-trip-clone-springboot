package com.makemytrip.modules.booking.service;

import com.makemytrip.modules.auth.model.User;
import com.makemytrip.modules.auth.repository.UserRepository;
import com.makemytrip.modules.booking.dto.BookingPermissions;
import com.makemytrip.modules.booking.dto.CreateBookingRequest;
import com.makemytrip.modules.booking.model.*;
import com.makemytrip.modules.booking.repository.BookingRepository;
import com.makemytrip.modules.flights.model.Flight;
import com.makemytrip.modules.flights.repository.FlightRepository;
import com.makemytrip.modules.hotels.model.Hotel;
import com.makemytrip.modules.hotels.repository.HotelRepository;
import com.makemytrip.modules.seatroom.model.Room;
import com.makemytrip.modules.seatroom.repository.RoomRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@SuppressWarnings("null")
public class BookingService {
    private static final Logger log = LoggerFactory.getLogger(BookingService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FlightRepository flightRepository;

    @Autowired
    private HotelRepository hotelRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private com.makemytrip.modules.seatroom.service.SeatRoomService seatRoomService;

    /**
     * Create a new booking with date-based validation
     */
    public Booking createBooking(CreateBookingRequest request) {
        log.info("Creating booking for {} {} by user {}", 
                request.getEntityType(), request.getEntityId(), request.getUserId());

        // Validate travel date is in the future
        if (request.getTravelDate() == null || request.getTravelDate().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Travel date must be in the future");
        }

        // Validate numberOfNights for hotel bookings
        if (request.getEntityType() == EntityType.HOTEL && request.getNumberOfNights() <= 0) {
            throw new IllegalArgumentException("Number of nights must be at least 1 for hotel bookings");
        }

        Booking booking = new Booking();
        booking.setUserId(request.getUserId());
        booking.setUserName(request.getUserName());
        booking.setEntityId(request.getEntityId());
        booking.setEntityType(request.getEntityType());
        booking.setQuantity(request.getQuantity());
        booking.setTotalPrice(request.getTotalPrice());
        booking.setBookingDate(LocalDate.now());
        booking.setTravelDate(request.getTravelDate());
        
        // Set numberOfNights for hotel bookings (this also calculates checkOutDate)
        if (request.getEntityType() == EntityType.HOTEL) {
            booking.setNumberOfNights(request.getNumberOfNights());
        }
        
        booking.setBookingStatus(BookingStatus.PENDING);
        booking.setPaymentStatus(PaymentStatus.PENDING);
        booking.setCancellationAllowed(true);
        booking.setReviewSubmitted(false);

        return bookingRepository.save(booking);
    }

    /**
     * Get booking by ID and update status based on travel date
     */
    public Optional<Booking> getBookingById(String bookingId) {
        Optional<Booking> bookingOpt = bookingRepository.findById(bookingId);
        if (bookingOpt.isPresent()) {
            Booking booking = bookingOpt.get();
            updateBookingBasedOnDate(booking);
            return Optional.of(booking);
        }
        return bookingOpt;
    }

    /**
     * Get all bookings for a user
     */
    public List<Booking> getUserBookings(String userId) {
        List<Booking> bookings = bookingRepository.findByUserId(userId);
        bookings.forEach(this::updateBookingBasedOnDate);
        return bookings;
    }

    /**
     * Core business logic: Update booking status based on current date vs travel date
     * 
     * CASE 1: Before Travel Date
     * - Allow: Payment, Cancellation, Modification
     * - Status: PENDING or CONFIRMED
     * 
     * CASE 2: On or After Travel Date
     * - Disable: Cancellation, Modification
     * - Mark booking as COMPLETED
     * - Enable: Review submission
     */
    public void updateBookingBasedOnDate(Booking booking) {
        if (booking.getTravelDate() == null) {
            return;
        }

        LocalDate today = LocalDate.now();
        LocalDate travelDate = booking.getTravelDate();

        // CASE 1: Before travel date
        if (today.isBefore(travelDate)) {
            // Payment allowed if pending
            if (booking.getBookingStatus() == BookingStatus.PENDING) {
                booking.setCancellationAllowed(true);
            }
            // Cancellation allowed for confirmed bookings too
            if (booking.getBookingStatus() == BookingStatus.CONFIRMED) {
                booking.setCancellationAllowed(true);
            }
            log.debug("Booking {} is before travel date. Cancellation allowed.", booking.getId());
        }
        // CASE 2: On or after travel date
        else {
            // Disable cancellation
            booking.setCancellationAllowed(false);
            
            // Mark as completed if it was confirmed
            if (booking.getBookingStatus() == BookingStatus.CONFIRMED) {
                booking.setBookingStatus(BookingStatus.COMPLETED);
                log.info("Booking {} marked as COMPLETED (travel date passed)", booking.getId());
            }
            
            log.debug("Booking {} is on/after travel date. Cancellation disabled, review enabled.", 
                    booking.getId());
        }

        booking.setUpdatedAt(LocalDateTime.now());
    }

    /**
     * Check if booking can be cancelled
     */
    public boolean canCancelBooking(Booking booking) {
        if (booking.getBookingStatus() == BookingStatus.CANCELLED) {
            return false;
        }
        
        // Must be before travel date
        return booking.isBeforeTravelDate() && booking.isCancellationAllowed();
    }

    /**
     * Check if booking can be modified
     */
    public boolean canModifyBooking(Booking booking) {
        if (booking.getBookingStatus() == BookingStatus.CANCELLED) {
            return false;
        }
        
        // Must be before travel date
        return booking.isBeforeTravelDate();
    }

    /**
     * Check if review can be submitted
     */
    public boolean canSubmitReview(Booking booking) {
        // Can submit review if:
        // 1. Booking is completed
        // 2. Travel date has passed
        // 3. Review not already submitted
        return (booking.getBookingStatus() == BookingStatus.COMPLETED 
                || booking.isOnOrAfterTravelDate())
                && !booking.isReviewSubmitted();
    }

    /**
     * Get booking permissions based on date logic
     */
    public BookingPermissions getBookingPermissions(String bookingId) {
        Optional<Booking> bookingOpt = getBookingById(bookingId);
        
        if (!bookingOpt.isPresent()) {
            return new BookingPermissions(false, false, false, false, "Booking not found");
        }

        Booking booking = bookingOpt.get();
        
        boolean canPay = booking.getPaymentStatus() == PaymentStatus.PENDING 
                        && booking.isBeforeTravelDate();
        boolean canCancel = canCancelBooking(booking);
        boolean canModify = canModifyBooking(booking);
        boolean canReview = canSubmitReview(booking);

        String message = booking.isBeforeTravelDate()
                ? "Booking active. You can make changes or cancel."
                : "Travel date has passed. Please submit a review.";

        return new BookingPermissions(canPay, canCancel, canModify, canReview, message);
    }

    /**
     * Cancel booking (only allowed before travel date)
     */
    public Booking cancelBooking(String bookingId, String userId, String reason) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));

        if (!booking.getUserId().equals(userId)) {
            throw new IllegalArgumentException("User can only cancel their own bookings");
        }

        if (!canCancelBooking(booking)) {
            throw new IllegalStateException(
                    "Booking cannot be cancelled. Reason: " +
                    (booking.getBookingStatus() == BookingStatus.CANCELLED 
                            ? "Already cancelled" 
                            : "Travel date has passed")
            );
        }

        boolean hotelBooking = booking.getEntityType() == EntityType.HOTEL;
        List<String> allocatedRoomIds = booking.getSeatNumbers() != null
            ? new ArrayList<>(booking.getSeatNumbers())
            : List.of();

        booking.setBookingStatus(BookingStatus.CANCELLED);
        booking.setCancellationAllowed(false);
        booking.setCancelledAt(LocalDateTime.now());
        booking.setCancellationReason(reason);
        booking.setUpdatedAt(LocalDateTime.now());

        // Update payment status to refunded (if it was paid)
        if (booking.getPaymentStatus() == PaymentStatus.PAID) {
            booking.setPaymentStatus(PaymentStatus.REFUNDED);
        }

        // Unblock allocated hotel rooms if available on booking metadata.
        if (hotelBooking && booking.getTravelDate() != null && booking.getCheckOutDate() != null && !allocatedRoomIds.isEmpty()) {
            try {
                for (String roomId : allocatedRoomIds) {
                    seatRoomService.unblockRoomDates(roomId, booking.getTravelDate(), booking.getCheckOutDate(), bookingId);
                }
                log.info("Unblocked {} room(s) for cancelled booking {}", allocatedRoomIds.size(), bookingId);
            } catch (Exception e) {
                log.warn("Failed to unblock room dates for booking {}: {}", bookingId, e.getMessage());
            }
        }

        log.info("Booking {} cancelled by user {}. Reason: {}", bookingId, userId, reason);
        return bookingRepository.save(booking);
    }

    /**
     * Mark review as submitted (only allowed after travel date)
     */
    public Booking markReviewSubmitted(String bookingId, String userId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));

        if (!booking.getUserId().equals(userId)) {
            throw new IllegalArgumentException("User can only update their own bookings");
        }

        if (!canSubmitReview(booking)) {
            throw new IllegalStateException(
                    "Review cannot be submitted. Reason: " +
                    (booking.isReviewSubmitted() 
                            ? "Review already submitted" 
                            : "Travel date has not passed yet")
            );
        }

        booking.setReviewSubmitted(true);
        booking.setUpdatedAt(LocalDateTime.now());

        log.info("Review marked as submitted for booking {} by user {}", bookingId, userId);
        return bookingRepository.save(booking);
    }

    /**
     * Confirm payment and update booking status
     */
    public Booking confirmPayment(String bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));

        if (booking.getPaymentStatus() == PaymentStatus.PAID
                && booking.getBookingStatus() == BookingStatus.CONFIRMED) {
            return booking;
        }

        if (booking.getPaymentStatus() != PaymentStatus.PENDING) {
            throw new IllegalStateException("Payment cannot be confirmed from status: " + booking.getPaymentStatus());
        }

        if (!booking.isBeforeTravelDate()) {
            throw new IllegalStateException("Cannot pay for a booking after travel date");
        }

        if (booking.getEntityType() == EntityType.HOTEL) {
            allocateAndBlockHotelRooms(booking);
        }

        booking.setPaymentStatus(PaymentStatus.PAID);
        booking.setBookingStatus(BookingStatus.CONFIRMED);
        booking.setUpdatedAt(LocalDateTime.now());

        Booking savedBooking = bookingRepository.save(booking);

        log.info("Payment confirmed for booking {}", bookingId);
        return savedBooking;
    }

    private void allocateAndBlockHotelRooms(Booking booking) {
        if (booking.getTravelDate() == null || booking.getCheckOutDate() == null) {
            throw new IllegalStateException("Hotel booking requires valid check-in and check-out dates");
        }

        int roomsRequested = Math.max(1, booking.getQuantity());
        List<Room> rooms = roomRepository.findByHotelIdAndAvailable(booking.getEntityId(), true);

        List<String> selectedRoomIds = new ArrayList<>();
        for (Room room : rooms) {
            if (selectedRoomIds.size() >= roomsRequested) {
                break;
            }
            if (seatRoomService.isRoomAvailableForDateRange(room.getId(), booking.getTravelDate(), booking.getCheckOutDate())) {
                selectedRoomIds.add(room.getId());
            }
        }

        if (selectedRoomIds.size() < roomsRequested) {
            throw new IllegalStateException("Not enough rooms available for selected dates");
        }

        List<String> blockedRoomIds = new ArrayList<>();
        try {
            for (String roomId : selectedRoomIds) {
                seatRoomService.blockRoomDates(roomId, booking.getTravelDate(), booking.getCheckOutDate(), booking.getId());
                blockedRoomIds.add(roomId);
            }
            booking.setSeatNumbers(selectedRoomIds);
        } catch (Exception e) {
            for (String roomId : blockedRoomIds) {
                try {
                    seatRoomService.unblockRoomDates(roomId, booking.getTravelDate(), booking.getCheckOutDate(), booking.getId());
                } catch (Exception rollbackError) {
                    log.warn("Failed to rollback blocked room {} for booking {}: {}",
                            roomId, booking.getId(), rollbackError.getMessage());
                }
            }
            throw new IllegalStateException("Failed to reserve rooms for booking", e);
        }
    }

    // ========== Legacy Methods (for backward compatibility) ==========

    // TODO: Remove legacy method after all callers migrate to createBooking().
    public Booking bookFlight(String userId, String flightId, int seats, double price, String date, String seatNumbers) {
        Optional<User> usersOptional = userRepository.findById(userId);
        Optional<Flight> flightOptional = flightRepository.findById(flightId);
        if (flightOptional.isPresent()) {
            User user = usersOptional.orElse(null);
            Flight flight = flightOptional.get();
            if (flight.getAvailableSeats() >= seats) {
                flight.setAvailableSeats(flight.getAvailableSeats() - seats);
                flightRepository.save(flight);

                Booking booking = new Booking();
                booking.setType("Flight");
                booking.setBookingId(flightId);
                booking.setUserId(userId);
                booking.setUserName(user != null
                    ? (user.getFirstName() + " " + user.getLastName())
                    : "Guest User");
                booking.setEntityType(EntityType.FLIGHT);
                booking.setEntityId(flightId);
                
                if (date != null && !date.isEmpty()) {
                    booking.setDate(date.length() > 10 ? date.substring(0, 10) : date);
                } else if (flight.getDepartureTime() != null && !flight.getDepartureTime().isEmpty()) {
                    String depDate = flight.getDepartureTime().length() > 10
                        ? flight.getDepartureTime().substring(0, 10)
                        : flight.getDepartureTime();
                    booking.setDate(depDate);
                } else {
                    booking.setDate(LocalDate.now().toString());
                }
                
                booking.setQuantity(seats);
                if (seatNumbers != null && !seatNumbers.isBlank()) {
                    booking.setSeatNumbers(Arrays.stream(seatNumbers.split(","))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .collect(Collectors.toList()));
                }
                booking.setTotalPrice(price);
                booking.setBookingStatus(BookingStatus.CONFIRMED);
                booking.setPaymentStatus(PaymentStatus.PAID);
                
                // Save to new bookings collection
                Booking savedBooking = bookingRepository.save(booking);
                
                return savedBooking;
            } else {
                throw new RuntimeException("Not enough seats available");
            }
        }
        throw new RuntimeException("Flight not found");
    }

    // TODO: Remove legacy method after all callers migrate to createBooking().
    public Booking bookhotel(String userId, String hotelId, int rooms, double price, String date, String roomNumbers) {
        Optional<User> usersOptional = userRepository.findById(userId);
        Optional<Hotel> hotelOptional = hotelRepository.findById(hotelId);
        if (hotelOptional.isPresent()) {
            User user = usersOptional.orElse(null);
            Hotel hotel = hotelOptional.get();
            if (hotel.getAvailableRooms() >= rooms) {
                hotel.setAvailableRooms(hotel.getAvailableRooms() - rooms);
                hotelRepository.save(hotel);

                Booking booking = new Booking();
                booking.setType("Hotel");
                booking.setBookingId(hotelId);
                booking.setUserId(userId);
                booking.setUserName(user != null
                    ? (user.getFirstName() + " " + user.getLastName())
                    : "Guest User");
                booking.setEntityType(EntityType.HOTEL);
                booking.setEntityId(hotelId);
                
                if (date != null && !date.isEmpty()) {
                    booking.setDate(date.length() > 10 ? date.substring(0, 10) : date);
                } else {
                    booking.setDate(LocalDate.now().toString());
                }
                
                booking.setQuantity(rooms);
                if (roomNumbers != null && !roomNumbers.isBlank()) {
                    booking.setSeatNumbers(Arrays.stream(roomNumbers.split(","))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .collect(Collectors.toList()));
                }
                booking.setTotalPrice(price);
                booking.setBookingStatus(BookingStatus.CONFIRMED);
                booking.setPaymentStatus(PaymentStatus.PAID);
                
                // Save to new bookings collection
                Booking savedBooking = bookingRepository.save(booking);
                
                return savedBooking;
            } else {
                throw new RuntimeException("Not enough rooms available");
            }
        }
        throw new RuntimeException("Hotel not found");
    }
}
