package com.makemytrip.config;

import com.makemytrip.modules.booking.repository.BookingRepository;
import com.makemytrip.modules.cancellation.repository.CancellationRepository;
import com.makemytrip.modules.cancellation.repository.RefundTrackerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(1)
public class BookingDataResetRunner implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(BookingDataResetRunner.class);

    private final BookingRepository bookingRepository;
    private final CancellationRepository cancellationRepository;
    private final RefundTrackerRepository refundTrackerRepository;

    @Value("${app.dev.reset-booking-data-on-startup:${RESET_BOOKING_DATA_ON_STARTUP:false}}")
    private boolean resetBookingDataOnStartup;

    public BookingDataResetRunner(
            BookingRepository bookingRepository,
            CancellationRepository cancellationRepository,
            RefundTrackerRepository refundTrackerRepository) {
        this.bookingRepository = bookingRepository;
        this.cancellationRepository = cancellationRepository;
        this.refundTrackerRepository = refundTrackerRepository;
    }

    @Override
    public void run(String... args) {
        if (!resetBookingDataOnStartup) {
            return;
        }

        long bookingCount = bookingRepository.count();
        long cancellationCount = cancellationRepository.count();
        long refundTrackerCount = refundTrackerRepository.count();

        bookingRepository.deleteAll();
        cancellationRepository.deleteAll();
        refundTrackerRepository.deleteAll();

        log.warn("[DEV RESET] Cleared data on startup: bookings={}, cancellations={}, refund_trackers={}",
                bookingCount, cancellationCount, refundTrackerCount);
    }
}
