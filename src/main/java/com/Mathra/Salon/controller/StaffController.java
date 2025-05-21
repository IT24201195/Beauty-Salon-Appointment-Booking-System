package com.Mathra.Salon.controller;

import com.Mathra.Salon.model.Booking;
import com.Mathra.Salon.model.User;
import com.Mathra.Salon.service.BookingFileService;
import com.Mathra.Salon.service.UserFileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller for staff dashboard and salon management functionality.
 *
 * The StaffController handles all requests related to the staff interface of the application.
 * This includes the staff dashboard, booking management, and customer management. The controller
 * is restricted to users with STAFF or ADMIN roles through Spring Security configuration.
 *
 * Key responsibilities:
 * - Displaying the staff dashboard with booking statistics
 * - Listing and filtering bookings for management
 * - Updating booking statuses (confirm, complete, cancel)
 * - Viewing and managing customer information
 *
 * All paths begin with "/staff" and are secured to require staff or admin role.
 *
 * @see com.Mathra.Salon.service.BookingFileService
 * @see com.Mathra.Salon.service.UserFileService
 */
@Controller
@RequestMapping("/staff")
public class StaffController {

    /**
     * Service for booking operations
     */
    private final BookingFileService bookingFileService;

    /**
     * Service for user operations
     */
    private final UserFileService userFileService;

    /**
     * Constructor with dependency injection.
     *
     * @param bookingFileService Service for booking operations
     * @param userFileService Service for user operations
     */
    @Autowired
    public StaffController(BookingFileService bookingFileService, UserFileService userFileService) {
        this.bookingFileService = bookingFileService;
        this.userFileService = userFileService;
    }

    /**
     * Displays the staff dashboard.
     *
     * This method prepares the dashboard view with:
     * - Pending bookings that need attention
     * - Confirmed bookings that are upcoming
     * - Customer count statistics
     * - Booking statistics (pending, confirmed, completed)
     *
     * The dashboard serves as a central hub for salon staff to monitor
     * daily operations and manage appointments.
     *
     * @param model The Spring MVC model for passing data to the view
     * @return The view name "staff/dashboard"
     */
    @GetMapping("/dashboard")
    public String showStaffDashboard(Model model) {
        try {
            // Get today's date
            LocalDate today = LocalDate.now();

            // Get sorted appointments for today only
            Booking[] sortedAppointments = bookingFileService.getSortedAppointments();
            Booking[] todayAppointments = java.util.Arrays.stream(sortedAppointments)
                    .filter(booking -> booking.getBookingDate().equals(today))
                    .toArray(Booking[]::new);
            model.addAttribute("sortedAppointments", todayAppointments);

            // Get the next appointment for today
            Booking nextAppointment = java.util.Arrays.stream(todayAppointments)
                    .findFirst()
                    .orElse(null);
            model.addAttribute("nextAppointment", nextAppointment);

            // Get all pending bookings
            List<Booking> pendingBookings = bookingFileService.findBookingsByStatus(Booking.Status.PENDING);
            model.addAttribute("pendingBookings", pendingBookings);

            // Get all confirmed bookings for today
            List<Booking> confirmedBookings = bookingFileService.findBookingsByStatus(Booking.Status.CONFIRMED)
                    .stream()
                    .filter(booking -> booking.getBookingDate().equals(today))
                    .toList();
            model.addAttribute("confirmedBookings", confirmedBookings);

            // Get customer count
            long customerCount = userFileService.findAllUsers().stream()
                    .filter(user -> user.getRole() == User.Role.CUSTOMER)
                    .count();
            model.addAttribute("customerCount", customerCount);

            // Get booking stats
            model.addAttribute("pendingCount", pendingBookings.size());
            model.addAttribute("confirmedCount", confirmedBookings.size());
            model.addAttribute("completedCount",
                    bookingFileService.findBookingsByStatus(Booking.Status.COMPLETED).size());

            return "staff/dashboard";
        } catch (Exception e) {
            model.addAttribute("error", "Error loading dashboard: " + e.getMessage());
            return "staff/dashboard";
        }
    }

    /**
     * Displays all bookings for management.
     *
     * This endpoint provides a comprehensive view of all salon bookings.
     * Staff can see and manage all appointments in the system from this interface.
     *
     * Bookings are retrieved from the service with complete customer information
     * loaded to enable proper display and management.
     *
     * @param model The Spring MVC model for passing data to the view
     * @return The view name "staff/bookings"
     */
    @GetMapping("/bookings")
    public String showAllBookings(Model model) {
        try {
            // Get all bookings and sort them by date and time
            List<Booking> allBookings = bookingFileService.findAllBookings();
            
            // Sort bookings by date and time
            allBookings.sort((b1, b2) -> {
                int dateComparison = b1.getBookingDate().compareTo(b2.getBookingDate());
                if (dateComparison != 0) {
                    return dateComparison;
                }
                return b1.getBookingTime().compareTo(b2.getBookingTime());
            });

            model.addAttribute("bookings", allBookings);
            return "staff/bookings";
        } catch (Exception e) {
            model.addAttribute("error", "Error loading bookings: " + e.getMessage());
            return "staff/bookings";
        }
    }

    /**
     * Updates the status of a booking.
     *
     * This endpoint handles state transitions for bookings:
     * - From PENDING to CONFIRMED or CANCELLED
     * - From CONFIRMED to COMPLETED or CANCELLED
     *
     * After updating the status, redirects back to the bookings page
     * with a success or error message.
     *
     * @param bookingId The ID of the booking to update
     * @param status The new status to set
     * @param redirectAttributes For adding flash messages to the redirect
     * @return Redirect to the bookings page
     */
    @PostMapping("/booking/{id}/status")
    public String updateBookingStatus(
            @PathVariable("id") Long bookingId,
            @RequestParam("status") Booking.Status status,
            RedirectAttributes redirectAttributes) {
        try {
            Booking booking = bookingFileService.findById(bookingId);
            if (booking == null) {
                throw new RuntimeException("Booking not found");
            }

            // Validate status transition
            if (!isValidStatusTransition(booking.getStatus(), status)) {
                throw new RuntimeException("Invalid status transition from " + booking.getStatus() + " to " + status);
            }

            bookingFileService.updateBookingStatus(bookingId, status);
            redirectAttributes.addFlashAttribute("success", "Booking status updated to " + status);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/staff/bookings";
    }

    private boolean isValidStatusTransition(Booking.Status currentStatus, Booking.Status newStatus) {
        if (currentStatus == null || newStatus == null) {
            return false;
        }

        return switch (currentStatus) {
            case PENDING -> newStatus == Booking.Status.CONFIRMED || newStatus == Booking.Status.CANCELLED;
            case CONFIRMED -> newStatus == Booking.Status.COMPLETED || newStatus == Booking.Status.CANCELLED;
            case COMPLETED, CANCELLED -> false;
        };
    }

    /**
     * Displays all customers for management.
     *
     * This endpoint provides a view of all customer accounts, along with
     * statistics about their booking history.
     *
     * For each customer, it displays:
     * - Basic profile information
     * - Number of bookings they've made
     *
     * This helps staff understand customer activity and manage customer relationships.
     *
     * @param model The Spring MVC model for passing data to the view
     * @return The view name "staff/customers"
     */
    @GetMapping("/customers")
    public String showAllCustomers(Model model) {
        try {
            List<User> users = userFileService.findAllUsers();
            model.addAttribute("users", users);

            Map<Long, Integer> userBookingCounts = new HashMap<>();
            List<Booking> allBookings = bookingFileService.findAllBookings();

            for (Booking booking : allBookings) {
                if (booking.getUser() != null) {
                    Long userId = booking.getUser().getId();
                    userBookingCounts.put(userId, userBookingCounts.getOrDefault(userId, 0) + 1);
                }
            }

            model.addAttribute("userBookingCounts", userBookingCounts);
            return "staff/customers";
        } catch (Exception e) {
            model.addAttribute("error", "Error loading customers: " + e.getMessage());
            return "staff/customers";
        }
    }
}
