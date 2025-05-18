package com.Mathra.Salon.service;

import com.Mathra.Salon.filemanager.UserFileManager;
import com.Mathra.Salon.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * User file service
 * Handles user-related business logic with file storage
 */
@Service
public class UserFileService implements UserDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(UserFileService.class);
    private final UserFileManager userFileManager;
    private final PasswordEncoder passwordEncoder;

    /**
     * Constructor with dependency injection
     */
    @Autowired
    public UserFileService(UserFileManager userFileManager, PasswordEncoder passwordEncoder) {
        this.userFileManager = userFileManager;
        this.passwordEncoder = passwordEncoder;
        ensureAdminExists();
    }

    /**
     * Create default admin if no users exist
     */
    private void ensureAdminExists() {
        if (userFileManager.findAll().isEmpty()) {
            logger.info("No users found, creating admin user");
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin"));
            admin.setFullName("Admin User");
            admin.setEmail("admin@mathra.com");
            admin.setPhoneNumber("1234567890");
            admin.setRole(User.Role.ADMIN);
            userFileManager.save(admin);

            User staff = new User();
            staff.setUsername("staff");
            staff.setPassword(passwordEncoder.encode("staff"));
            staff.setFullName("Staff User");
            staff.setEmail("staff@mathra.com");
            staff.setPhoneNumber("1234567891");
            staff.setSpecialty("Hair Styling");
            staff.setRole(User.Role.STAFF);
            userFileManager.save(staff);

            User customer = new User();
            customer.setUsername("user");
            customer.setPassword(passwordEncoder.encode("user"));
            customer.setFullName("Regular User");
            customer.setEmail("user@example.com");
            customer.setPhoneNumber("1234567892");
            customer.setRole(User.Role.CUSTOMER);
            userFileManager.save(customer);

            logger.info("Created default users: admin, staff, user");
        }
    }

    /**
     * Spring Security UserDetailsService implementation
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userFileManager.findByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("User not found: " + username);
        }

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                Collections.singleton(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
        );
    }

    /**
     * Register a new user
     */
    public User registerNewUser(User user) {
        // Check if username already exists
        if (userFileManager.existsByUsername(user.getUsername())) {
            throw new RuntimeException("Username already exists");
        }

        // Check if email already exists
        if (userFileManager.existsByEmail(user.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        // Encode password
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // Save user
        boolean saved = userFileManager.save(user);
        if (!saved) {
            throw new RuntimeException("Failed to save user");
        }

        return user;
    }

    /**
     * Find all users
     */
    public List<User> findAllUsers() {
        return userFileManager.findAll();
    }

    /**
     * Find all staff members
     */
    public List<User> findAllStaff() {
        return userFileManager.findAll().stream()
                .filter(user -> user.getRole() == User.Role.STAFF || user.getRole() == User.Role.ADMIN)
                .collect(Collectors.toList());
    }

    /**
     * Find a user by username
     */
    public User findByUsername(String username) {
        User user = userFileManager.findByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("User not found: " + username);
        }
        return user;
    }

    /**
     * Alternative method name for finding user by username (for backward compatibility)
     */
    public User findUserByUsername(String username) {
        return findByUsername(username);
    }

    /**
     * Find a user by ID
     */
    public User findById(Long id) {
        return userFileManager.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
    }

    /**
     * Alternative method name for finding user by ID (for backward compatibility)
     */
    public User findUserById(Long id) {
        return findById(id);
    }

    /**
     * Check if a password matches
     */
    public boolean checkPassword(User user, String rawPassword) {
        return passwordEncoder.matches(rawPassword, user.getPassword());
    }

    /**
     * Update a user
     */
    public User updateUser(User user) {
        User existingUser = userFileManager.findById(user.getId())
                .orElseThrow(() -> new RuntimeException("User not found with id: " + user.getId()));

        // Check if updating to a username that already exists
        User userWithSameUsername = userFileManager.findByUsername(user.getUsername());
        if (userWithSameUsername != null && !userWithSameUsername.getId().equals(user.getId())) {
            throw new RuntimeException("Username already exists for another user");
        }

        // Check if updating to an email that already exists
        User userWithSameEmail = userFileManager.findByEmail(user.getEmail());
        if (userWithSameEmail != null && !userWithSameEmail.getId().equals(user.getId())) {
            throw new RuntimeException("Email already exists for another user");
        }

        boolean updated = userFileManager.save(user);
        if (!updated) {
            throw new RuntimeException("Failed to update user");
        }

        return user;
    }

    /**
     * Delete a user
     */
    public boolean deleteUser(Long id) {
        return userFileManager.deleteById(id);
    }
} 