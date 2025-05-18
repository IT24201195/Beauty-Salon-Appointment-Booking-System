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

/**
 * Service class for managing booking-related business logic with file-based storage.
 *
 * The BookingFileService acts as the primary interface between the controllers and the
 * booking data storage system. It handles creation, modification, retrieval, and filtering
 * of booking records, applying business rules and validation logic.
 *
 * This service is critical for the appointment management functionality of the salon application,
 * enabling customers to create bookings and staff to manage them. It maintains the state of
 * bookings throughout their lifecycle (pending, confirmed, completed, cancelled).
 *
 * The service uses a BookingFileManager for persistence operations and includes logic
 * to load complete User objects when retrieving bookings from storage.
 *
 * @see com.Mathra.Salon.model.Booking
 * @see com.Mathra.Salon.filemanager.BookingFileManager
 */
@Service
public class BookingFileService {

    /** Logger for this class */
    private static final Logger logger = LoggerFactory.getLogger(BookingFileService.class);

    /** File manager for booking data persistence */
    private final BookingFileManager bookingFileManager;

    /** User service for loading complete user information */
    private final UserFileService userFileService;

    /**
     * Constructor with dependency injection.
     *
     * Spring will automatically inject the required dependencies:
     * - BookingFileManager for file operations
     * - UserFileService to load complete user information
     *
     * @param bookingFileManager File manager for booking persistence
     * @param userFileService Service for user operations
     */
    @Autowired
    public BookingFileService(BookingFileManager bookingFileManager, UserFileService userFileService) {
        this.bookingFileManager = bookingFileManager;
        this.userFileService = userFileService;
    }

    /**
     * Creates a new booking.
     *
     * This method validates the booking data, assigns a new ID, and persists the booking.
     * A new booking should not have an ID already assigned.
     *
     * @param booking The booking to create
     * @return The saved booking with its assigned ID
     * @throws IllegalArgumentException If the booking already has an ID
     * @throws RuntimeException If saving fails
     */
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

    /**
     * Saves changes to an existing booking.
     *
     * Updates the booking in the file storage system. The booking must have
     * an existing ID to be updated.
     *
     * @param booking The booking to update
     * @return The updated booking
     * @throws IllegalArgumentException If the booking doesn't have an ID
     * @throws RuntimeException If saving fails
     */
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

    /**
     * Retrieves all bookings from storage.
     *
     * This method returns all bookings, regardless of status or ownership.
     * It also loads the complete User objects for each booking.
     *
     * @return List of all bookings
     */
    public List<Booking> findAllBookings() {
        List<Booking> bookings = bookingFileManager.findAll();
        // Load full user objects for each booking
        for (Booking booking : bookings) {
            loadUserDetails(booking);
        }
        return bookings;
    }

    /**
     * Helper method to load complete user details for a booking.
     *
     * This method retrieves the full User objects for both the customer
     * and assigned staff member in a booking, ensuring that all user
     * information is available when working with bookings.
     *
     * @param booking The booking to enhance with complete user information
     */
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

    /**
     * Finds all bookings for a specific user.
     *
     * Retrieves bookings where the specified user is the customer.
     * This is typically used to show a customer their booking history.
     *
     * @param user The user to find bookings for
     * @return List of bookings for the user
     */
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

    /**
     * Finds all bookings with a specific status.
     *
     * Filters bookings by their current status (PENDING, CONFIRMED, etc.).
     * This is typically used in the staff dashboard to categorize bookings.
     *
     * @param status The status to filter by
     * @return List of bookings with the specified status
     */
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

    /**
     * Finds all bookings assigned to a specific staff member.
     *
     * Retrieves bookings where the specified staff member is assigned.
     * This is typically used to show staff their assigned bookings.
     *
     * @param staffMember The staff member to find bookings for
     * @return List of bookings assigned to the staff member
     */
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

    /**
     * Finds a booking by its ID.
     *
     * Retrieves a specific booking by its unique identifier.
     * Throws an exception if the booking doesn't exist.
     *
     * @param id The booking ID to search for
     * @return The booking with the specified ID
     * @throws RuntimeException If no booking is found with the ID
     */
    public Booking findById(Long id) {
        Booking booking = bookingFileManager.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found with id: " + id));

        loadUserDetails(booking);
        return booking;
    }

    /**
     * Updates the status of a booking.
     *
     * Changes a booking's status (e.g., from PENDING to CONFIRMED).
     * This is the primary method for moving bookings through their lifecycle.
     *
     * @param bookingId The ID of the booking to update
     * @param status The new status to set
     * @return The updated booking
     */
    public Booking updateBookingStatus(Long bookingId, Booking.Status status) {
        Booking booking = findById(bookingId);
        booking.setStatus(status);
        return saveBooking(booking);
    }

    /**
     * Assigns a staff member to a booking.
     *
     * Sets or changes which staff member is responsible for a booking.
     *
     * @param bookingId The ID of the booking to update
     * @param staffMember The staff member to assign
     * @return The updated booking
     */
    public Booking assignStaff(Long bookingId, User staffMember) {
        Booking booking = findById(bookingId);
        booking.setAssignedStaff(staffMember);
        return saveBooking(booking);
    }

    /**
     * Cancels a booking.
     *
     * Convenience method that sets a booking's status to CANCELLED.
     *
     * @param bookingId The ID of the booking to cancel
     * @return The updated booking with CANCELLED status
     */
    public Booking cancelBooking(Long bookingId) {
        return updateBookingStatus(bookingId, Booking.Status.CANCELLED);
    }
}