package com.Mathra.Salon.service;

import com.Mathra.Salon.filemanager.BookingFileManager;
import com.Mathra.Salon.model.Booking;
import com.Mathra.Salon.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BookingFileService {


    private static final Logger logger = LoggerFactory.getLogger(BookingFileService.class);


    private final BookingFileManager bookingFileManager;


    private final UserFileService userFileService;


    @Autowired
    public BookingFileService(BookingFileManager bookingFileManager, UserFileService userFileService) {
        this.bookingFileManager = bookingFileManager;
        this.userFileService = userFileService;
    }


    public Booking createBooking(Booking booking) {
        if (booking.getId() != null) {
            throw new IllegalArgumentException("New booking should not have an ID");
        }

        boolean saved = bookingFileManager.save(booking);
        if (!saved) {
            throw new RuntimeException("Failed to save booking");
        }

        logger.info("Created booking: {}", booking);
        return booking;
    }

    public Booking saveBooking(Booking booking) {
        if (booking.getId() == null) {
            throw new IllegalArgumentException("Cannot update booking without ID");
        }

        boolean saved = bookingFileManager.save(booking);
        if (!saved) {
            throw new RuntimeException("Failed to save booking");
        }

        logger.info("Updated booking: {}", booking);
        return booking;
    }

    public List<Booking> findAllBookings() {
        List<Booking> bookings = bookingFileManager.findAll();
        // Load full user objects for each booking
        for (Booking booking : bookings) {
            loadUserDetails(booking);
        }
        return bookings;
    }

    private void loadUserDetails(Booking booking) {
        if (booking.getUser() != null && booking.getUser().getId() != null) {
            try {
                User fullUser = userFileService.findUserById(booking.getUser().getId());
                booking.setUser(fullUser);
            } catch (Exception e) {
                logger.warn("Failed to load user for booking {}: {}", booking.getId(), e.getMessage());
                // Keep the user with just the ID
            }
        }

        if (booking.getAssignedStaff() != null && booking.getAssignedStaff().getId() != null) {
            try {
                User fullStaff = userFileService.findUserById(booking.getAssignedStaff().getId());
                booking.setAssignedStaff(fullStaff);
            } catch (Exception e) {
                logger.warn("Failed to load staff for booking {}: {}", booking.getId(), e.getMessage());
                // Keep the staff with just the ID
            }
        }
    }

    public List<Booking> findBookingsByUser(User user) {
        List<Booking> bookings = bookingFileManager.findAll().stream()
                .filter(booking -> booking.getUser() != null &&
                        booking.getUser().getId().equals(user.getId()))
                .collect(Collectors.toList());

        // Load full user objects for each booking
        for (Booking booking : bookings) {
            loadUserDetails(booking);
        }

        return bookings;
    }

    public List<Booking> findBookingsByStatus(Booking.Status status) {
        if (status == null) {
            return new ArrayList<>();
        }

        List<Booking> bookings = bookingFileManager.findAll().stream()
                .filter(booking -> booking.getStatus() != null && booking.getStatus() == status)
                .collect(Collectors.toList());

        // Load full user objects for each booking
        for (Booking booking : bookings) {
            loadUserDetails(booking);
        }

        return bookings;
    }

    public List<Booking> findBookingsByStaff(User staffMember) {
        List<Booking> bookings = bookingFileManager.findAll().stream()
                .filter(booking -> booking.getAssignedStaff() != null &&
                        booking.getAssignedStaff().getId().equals(staffMember.getId()))
                .collect(Collectors.toList());

        // Load full user objects for each booking
        for (Booking booking : bookings) {
            loadUserDetails(booking);
        }

        return bookings;
    }


    public Booking findById(Long id) {
        Booking booking = bookingFileManager.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found with id: " + id));

        loadUserDetails(booking);
        return booking;
    }


    public Booking updateBookingStatus(Long bookingId, Booking.Status status) {
        Booking booking = findById(bookingId);
        booking.setStatus(status);
        return saveBooking(booking);
    }


    public Booking assignStaff(Long bookingId, User staffMember) {
        Booking booking = findById(bookingId);
        booking.setAssignedStaff(staffMember);
        return saveBooking(booking);
    }


    public Booking cancelBooking(Long bookingId) {
        return updateBookingStatus(bookingId, Booking.Status.CANCELLED);
    }
}