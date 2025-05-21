package com.Mathra.Salon.controller;

import com.Mathra.Salon.model.Booking;
import com.Mathra.Salon.model.User;
import com.Mathra.Salon.service.BookingFileService;
import com.Mathra.Salon.service.UserFileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;


@Controller
public class BookingController {

    private static final Logger logger = LoggerFactory.getLogger(BookingController.class);

    private final BookingFileService bookingFileService;
    private final UserFileService userFileService;

    @Autowired
    public BookingController(BookingFileService bookingFileService, UserFileService userFileService) {
        this.bookingFileService = bookingFileService;
        this.userFileService = userFileService;
    }


    @GetMapping("/booking")
    public String showBookingForm() {
        return "booking";
    }


    @PostMapping("/booking")
    public String createBooking(
            Authentication authentication,
            @RequestParam("serviceType") Booking.ServiceType serviceType,
            @RequestParam("bookingDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate bookingDate,
            @RequestParam("bookingTime") @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime bookingTime,
            @RequestParam(value = "specialRequests", required = false) String specialRequests,
            RedirectAttributes redirectAttributes
    ) {
        try {
            User user = userFileService.findByUsername(authentication.getName());
            if (user == null) {
                throw new RuntimeException("User not found");
            }

            Booking booking = new Booking();
            booking.setUser(user);
            booking.setServiceType(serviceType);
            booking.setBookingDate(bookingDate);
            booking.setBookingTime(bookingTime);
            booking.setSpecialRequests(specialRequests);
            booking.setStatus(Booking.Status.PENDING);

            bookingFileService.createBooking(booking);
            logger.info("Created booking: {}", booking);
            redirectAttributes.addAttribute("success", true);
            return "redirect:/booking";
        } catch (Exception e) {
            logger.error("Error creating booking: {}", e.getMessage());
            redirectAttributes.addAttribute("error", e.getMessage());
            return "redirect:/booking";
        }
    }

    /**
     * Show user's bookings
     */
    @GetMapping("/bookings")
    public String showUserBookings(Authentication authentication, Model model) {
        try {
            User user = userFileService.findByUsername(authentication.getName());
            if (user == null) {
                throw new RuntimeException("User not found");
            }

            // Only get bookings for the current user
            List<Booking> userBookings = bookingFileService.findBookingsByUser(user);
            model.addAttribute("bookings", userBookings);
            return "bookings";
        } catch (Exception e) {
            logger.error("Error fetching user bookings: {}", e.getMessage());
            model.addAttribute("error", "Failed to load bookings: " + e.getMessage());
            return "bookings";
        }
    }

    /**
     * Cancel a booking
     */
    @PostMapping("/booking/cancel")
    public String cancelBooking(
            Authentication authentication,
            @RequestParam("id") Long bookingId,
            RedirectAttributes redirectAttributes
    ) {
        try {
            User user = userFileService.findByUsername(authentication.getName());
            if (user == null) {
                throw new RuntimeException("User not found");
            }

            Booking booking = bookingFileService.findById(bookingId);
            
            // Verify that the booking belongs to the current user
            if (!booking.getUser().getId().equals(user.getId())) {
                throw new RuntimeException("You can only cancel your own bookings");
            }

            bookingFileService.cancelBooking(bookingId);
            redirectAttributes.addFlashAttribute("success", "Booking cancelled successfully");
        } catch (Exception e) {
            logger.error("Error cancelling booking: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/bookings";
    }
}
