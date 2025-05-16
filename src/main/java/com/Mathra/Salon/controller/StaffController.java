package com.Mathra.Salon.controller;

import com.mathra.salon.model.Booking;
import com.Mathra.Salon.model.User;
import com.Mathra.Salon.service.BookingFileService;
import com.Mathra.Salon.service.UserFileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
 * @see BookingFileService
 * @see UserFileService
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
        // Get all pending bookings
        List<Booking> pendingBookings = bookingFileService.findBookingsByStatus(Booking.Status.PENDING);
        model.addAttribute("pendingBookings", pendingBookings);

        // Get all confirmed bookings
        List<Booking> confirmedBookings = bookingFileService.findBookingsByStatus(Booking.Status.CONFIRMED);
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
        List<Booking> allBookings = bookingFileService.findAllBookings();
        model.addAttribute("bookings", allBookings);

        return "staff/bookings";
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
            bookingFileService.updateBookingStatus(bookingId, status);
            redirectAttributes.addFlashAttribute("success",
                    "Booking status updated to " + status);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/staff/bookings";
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
        List<User> users = userFileService.findAllUsers();
        model.addAttribute("users", users);

        // Create a map of user ID to booking count
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
    }
}