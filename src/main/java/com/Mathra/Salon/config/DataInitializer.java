package com.Mathra.Salon.config;

import com.Mathra.Salon.model.Booking;
import com.Mathra.Salon.model.User;
import com.Mathra.Salon.filemanager.BookingFileManager;
import com.Mathra.Salon.filemanager.UserFileManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    private final PasswordEncoder passwordEncoder;
    private final UserFileManager userFileManager;
    private final BookingFileManager bookingFileManager;

    @Autowired
    public DataInitializer(PasswordEncoder passwordEncoder,
                           UserFileManager userFileManager,
                           BookingFileManager bookingFileManager) {
        this.passwordEncoder = passwordEncoder;
        this.userFileManager = userFileManager;
        this.bookingFileManager = bookingFileManager;

        logger.info("DataInitializer created with file storage only");
    }

    @Override
    public void run(String... args) {
        // Only initialize if no users exist yet
        if (userFileManager.findAll().isEmpty()) {
            initializeData();
        }
    }

    private void initializeData() {
        // Create admin user
        User admin = new User();
        admin.setUsername("admin");
        admin.setPassword(passwordEncoder.encode("admin"));
        admin.setFullName("Admin User");
        admin.setEmail("admin@mathra.com");
        admin.setRole(User.Role.ADMIN);

        // Create shared staff account
        User staff = new User();
        staff.setUsername("staff");
        staff.setPassword(passwordEncoder.encode("staff"));
        staff.setFullName("Salon Staff");
        staff.setEmail("staff@mathra.com");
        staff.setPhoneNumber("123-456-7890");
        staff.setRole(User.Role.STAFF);

        // Create customer users
        User customer1 = new User();
        customer1.setUsername("customer1");
        customer1.setPassword(passwordEncoder.encode("password"));
        customer1.setFullName("Alice Johnson");
        customer1.setEmail("alice@example.com");
        customer1.setPhoneNumber("555-123-4567");
        customer1.setRole(User.Role.CUSTOMER);

        User customer2 = new User();
        customer2.setUsername("customer2");
        customer2.setPassword(passwordEncoder.encode("password"));
        customer2.setFullName("Bob Brown");
        customer2.setEmail("bob@example.com");
        customer2.setPhoneNumber("555-987-6543");
        customer2.setRole(User.Role.CUSTOMER);

        // Save users to file storage
        userFileManager.save(admin);
        userFileManager.save(staff);
        userFileManager.save(customer1);
        userFileManager.save(customer2);
        logger.info("Saved users to file storage");

        // Create sample bookings
        Booking booking1 = new Booking();
        booking1.setUser(customer1);
        booking1.setBookingDate(LocalDate.now().plusDays(1));
        booking1.setBookingTime(LocalTime.of(10, 0));
        booking1.setServiceType(Booking.ServiceType.HAIRCUT);
        booking1.setStatus(Booking.Status.CONFIRMED);
        booking1.setAssignedStaff(staff);

        Booking booking2 = new Booking();
        booking2.setUser(customer2);
        booking2.setBookingDate(LocalDate.now().plusDays(2));
        booking2.setBookingTime(LocalTime.of(14, 30));
        booking2.setServiceType(Booking.ServiceType.COLORING);
        booking2.setStatus(Booking.Status.PENDING);

        Booking booking3 = new Booking();
        booking3.setUser(customer1);
        booking3.setBookingDate(LocalDate.now().plusDays(5));
        booking3.setBookingTime(LocalTime.of(16, 0));
        booking3.setServiceType(Booking.ServiceType.FACIAL);
        booking3.setStatus(Booking.Status.PENDING);

        // Save bookings to file storage
        bookingFileManager.save(booking1);
        bookingFileManager.save(booking2);
        bookingFileManager.save(booking3);
        logger.info("Saved bookings to file storage");

        logger.info("Sample data has been populated!");
    }
}
