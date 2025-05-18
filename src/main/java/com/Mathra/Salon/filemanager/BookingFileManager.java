package com.Mathra.Salon.filemanager;

import com.Mathra.Salon.model.Booking;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;


@Component
public class BookingFileManager extends AbstractFileStorageManager<Booking> {

    private static final String DATA_DIRECTORY = "data";
    private static final String FILE_NAME = "bookings.txt";
    private static final Logger logger = LoggerFactory.getLogger(BookingFileManager.class);


    public BookingFileManager() {
        super(DATA_DIRECTORY, FILE_NAME);
    }


    @Override
    protected Long getIdFromItem(Booking booking) {
        return booking.getId();
    }


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

    @Override
    public boolean save(Booking booking) {
        if (booking.getId() == null) {
            booking.setId(getNextId());
        }
        return super.save(booking);
    }


    public List<Booking> findByUserId(Long userId) {
        if (userId == null) {
            return List.of();
        }

        return findAll().stream()
                .filter(booking -> booking.getUser() != null &&
                        userId.equals(booking.getUser().getId()))
                .collect(Collectors.toList());
    }


    public List<Booking> findByStaffId(Long staffId) {
        if (staffId == null) {
            return List.of();
        }

        return findAll().stream()
                .filter(booking -> booking.getAssignedStaff() != null &&
                        staffId.equals(booking.getAssignedStaff().getId()))
                .collect(Collectors.toList());
    }


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