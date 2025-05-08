package com.Mathra.Salon.service;

import com.mathra.salon.model.Booking;
import com.mathra.salon.model.User;
import com.mathra.salon.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BookingService {

    private static final Logger logger = LoggerFactory.getLogger(BookingService.class);
    private final BookingRepository bookingRepository;

    @Transactional
    public Booking createBooking(Booking booking) {
        // Validate booking data
        if (booking.getUser() == null) {
            logger.error("Cannot create booking: User is null");
            throw new IllegalArgumentException("User is required for booking");
        }

        if (booking.getBookingDate() == null) {
            logger.error("Cannot create booking: Booking date is null");
            throw new IllegalArgumentException("Booking date is required");
        }

        if (booking.getBookingTime() == null) {
            logger.error("Cannot create booking: Booking time is null");
            throw new IllegalArgumentException("Booking time is required");
        }

        if (booking.getServiceType() == null) {
            logger.error("Cannot create booking: Service type is null");
            throw new IllegalArgumentException("Service type is required");
        }

        logger.info("Creating new booking: Date={}, Time={}, Service={}, User={}",
                booking.getBookingDate(), booking.getBookingTime(), booking.getServiceType(),
                booking.getUser().getUsername());

        try {
            Booking savedBooking = bookingRepository.save(booking);
            logger.info("Successfully created booking with ID: {}", savedBooking.getId());
            return savedBooking;
        } catch (Exception e) {
            logger.error("Error saving booking to database", e);
            throw new RuntimeException("Failed to save booking: " + e.getMessage(), e);
        }
    }

    public List<Booking> findAllBookings() {
        return bookingRepository.findAll();
    }

    public List<Booking> findBookingsByUser(User user) {
        return bookingRepository.findByUser(user);
    }

    public List<Booking> findBookingsByStaff(User staff) {
        return bookingRepository.findByAssignedStaff(staff);
    }

    public List<Booking> findBookingsByDate(LocalDate date) {
        return bookingRepository.findByBookingDate(date);
    }

    public List<Booking> findBookingsByStatus(Booking.Status status) {
        return bookingRepository.findByStatus(status);
    }

    public Booking findById(Long id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found with id: " + id));
    }

    public Booking findBookingById(Long id) {
        return findById(id);
    }

    @Transactional
    public Booking updateBookingStatus(Long bookingId, Booking.Status status) {
        logger.info("Updating booking status: ID={}, Status={}", bookingId, status);
        Booking booking = findById(bookingId);
        booking.setStatus(status);
        return bookingRepository.save(booking);
    }

    @Transactional
    public Booking assignStaffToBooking(Long bookingId, User staff) {
        logger.info("Assigning staff to booking: ID={}, Staff={}", bookingId, staff.getUsername());
        Booking booking = findById(bookingId);
        booking.setAssignedStaff(staff);
        booking.setStatus(Booking.Status.CONFIRMED);
        return bookingRepository.save(booking);
    }

    @Transactional
    public void deleteBooking(Long bookingId) {
        logger.info("Deleting booking: ID={}", bookingId);
        bookingRepository.deleteById(bookingId);
    }

    @Transactional
    public Booking saveBooking(Booking booking) {
        logger.info("Saving booking: ID={}", booking.getId());
        return bookingRepository.save(booking);
    }
}