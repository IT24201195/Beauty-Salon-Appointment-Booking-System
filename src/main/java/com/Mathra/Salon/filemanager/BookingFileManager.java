package com.Mathra.Salon.filemanager;

import com.Mathra.Salon.model.Booking;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * File storage manager for Booking objects
 * Handles reading and writing Booking objects to/from the bookings.txt file
 */
@Component
public class BookingFileManager extends AbstractFileStorageManager<Booking> {

    private static final String DATA_DIRECTORY = "data";
    private static final String FILE_NAME = "bookings.txt";
    private static final Logger logger = LoggerFactory.getLogger(BookingFileManager.class);

    /**
     * Constructor
     * Initializes with the data directory and file name
     */
    public BookingFileManager() {
        super(DATA_DIRECTORY, FILE_NAME);
    }

    /**
     * Get the ID from a Booking object
     * @param booking The Booking object
     * @return The ID of the booking
     */
    @Override
    protected Long getIdFromItem(Booking booking) {
        return booking.getId();
    }

    /**
     * Create a new Booking from a file string
     * @param fileString The string from the file
     * @return A new Booking with data from the string
     */
    @Override
    protected Booking createFromFileString(String fileString) {
        Booking booking = new Booking();
        try {
            booking.fromFileString(fileString);
            return booking;
        } catch (Exception e) {
            logger.error("Failed to parse booking from: {}", fileString, e);
            return null;
        }
    }

    /**
     * Save a new booking, assigning an ID if needed
     * @param booking The booking to save
     * @return true if successful, false otherwise
     */
    @Override
    public boolean save(Booking booking) {
        if (booking.getId() == null) {
            booking.setId(getNextId());
        }
        return super.save(booking);
    }

    /**
     * Find bookings by user ID
     * @param userId The user ID
     * @return List of bookings for the user
     */
    public List<Booking> findByUserId(Long userId) {
        if (userId == null) {
            return List.of();
        }

        return findAll().stream()
                .filter(booking -> booking.getUser() != null &&
                        userId.equals(booking.getUser().getId()))
                .collect(Collectors.toList());
    }

    /**
     * Find bookings by staff ID
     * @param staffId The staff ID
     * @return List of bookings assigned to the staff
     */
    public List<Booking> findByStaffId(Long staffId) {
        if (staffId == null) {
            return List.of();
        }

        return findAll().stream()
                .filter(booking -> booking.getAssignedStaff() != null &&
                        staffId.equals(booking.getAssignedStaff().getId()))
                .collect(Collectors.toList());
    }

    /**
     * Find bookings by date
     * @param date The date
     * @return List of bookings on the date
     */
    public List<Booking> findByDate(LocalDate date) {
        if (date == null) {
            return List.of();
        }

        return findAll().stream()
                .filter(booking -> {
                    try {
                        return date.equals(booking.getBookingDate());
                    } catch (Exception e) {
                        logger.error("Error comparing booking date for booking ID {}: {}",
                                booking.getId(), e.getMessage());
                        return false;
                    }
                })
                .collect(Collectors.toList());
    }

    /**
     * Find bookings by status
     * @param status The status
     * @return List of bookings with the status
     */
    public List<Booking> findByStatus(Booking.Status status) {
        if (status == null) {
            return List.of();
        }

        return findAll().stream()
                .filter(booking -> {
                    try {
                        return status.equals(booking.getStatus());
                    } catch (Exception e) {
                        logger.error("Error comparing booking status for booking ID {}: {}",
                                booking.getId(), e.getMessage());
                        return false;
                    }
                })
                .collect(Collectors.toList());
    }
}