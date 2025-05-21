package com.Mathra.Salon.service;

import com.Mathra.Salon.datastructures.AppointmentQueue;
import com.Mathra.Salon.datastructures.AppointmentSorter;
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

    private final AppointmentQueue appointmentQueue;


    @Autowired
    public BookingFileService(BookingFileManager bookingFileManager, UserFileService userFileService) {
        this.bookingFileManager = bookingFileManager;
        this.userFileService = userFileService;
        this.appointmentQueue = new AppointmentQueue();
        initializeQueue();
    }

    private void initializeQueue() {
        List<Booking> allBookings = bookingFileManager.findAll();
        for (Booking booking : allBookings) {
            if (booking.getStatus() == Booking.Status.PENDING || booking.getStatus() == Booking.Status.CONFIRMED) {
                appointmentQueue.enqueue(booking);
            }
        }
    }

    public Booking createBooking(Booking booking) {
        if (booking.getId() != null) {
            throw new IllegalArgumentException("New booking should not have an ID");
        }

        boolean saved = bookingFileManager.save(booking);
        if (!saved) {
            throw new RuntimeException("Failed to save booking");
        }

        // Add to queue if it's a pending or confirmed booking
        if (booking.getStatus() == Booking.Status.PENDING || booking.getStatus() == Booking.Status.CONFIRMED) {
            appointmentQueue.enqueue(booking);
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

        // Update queue if status changed
        if (booking.getStatus() == Booking.Status.COMPLETED || booking.getStatus() == Booking.Status.CANCELLED) {
            // Remove from queue if completed or cancelled
            removeFromQueue(booking);
        }

        logger.info("Updated booking: {}", booking);
        return booking;
    }

    private void removeFromQueue(Booking booking) {
        AppointmentQueue tempQueue = new AppointmentQueue();
        while (!appointmentQueue.isEmpty()) {
            Booking current = appointmentQueue.dequeue();
            if (!current.getId().equals(booking.getId())) {
                tempQueue.enqueue(current);
            }
        }
        // Restore the queue
        while (!tempQueue.isEmpty()) {
            appointmentQueue.enqueue(tempQueue.dequeue());
        }
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
            User user = userFileService.findById(booking.getUser().getId());
            booking.setUser(user);
        }
        if (booking.getAssignedStaff() != null && booking.getAssignedStaff().getId() != null) {
            User staff = userFileService.findById(booking.getAssignedStaff().getId());
            booking.setAssignedStaff(staff);
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

    /**
     * Get all pending and confirmed appointments sorted by time slot
     * @return Array of sorted bookings
     */
    public Booking[] getSortedAppointments() {
        return AppointmentSorter.sortQueue(appointmentQueue);
    }

    /**
     * Get the next appointment in the queue
     * @return The next booking or null if queue is empty
     */
    public Booking getNextAppointment() {
        return appointmentQueue.peek();
    }
}
