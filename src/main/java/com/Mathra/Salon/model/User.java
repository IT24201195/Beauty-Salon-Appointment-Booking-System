package com.Mathra.Salon.model;

import com.Mathra.Salon.filemanager.FileStorable;
import jakarta.persistence.*;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;


@Entity
@Table(name = "users")
public class User implements FileStorable, Serializable {

    /**
     * Serial version UID for serialization.
     * Required for the Serializable interface to ensure version compatibility.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Unique identifier for the user.
     * Generated automatically using identity strategy.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Username for authentication.
     * Must be unique in the system and cannot be null.
     */
    @Column(nullable = false, unique = true)
    private String username;

    /**
     * Hashed password for authentication.
     * Should never store plain text passwords.
     */
    @Column(nullable = false)
    private String password;

    /**
     * User's full name.
     * Used for display purposes in the UI.
     */
    @Column(nullable = false)
    private String fullName;

    /**
     * User's email address.
     * Must be unique and cannot be null.
     * Used for notifications and account recovery.
     */
    @Column(nullable = false, unique = true)
    private String email;

    /**
     * User's phone number.
     * Optional field primarily used for staff and customers.
     */
    private String phoneNumber;

    /**
     * Staff specialty/expertise.
     * Only applicable for users with STAFF role.
     * Indicates the services they can perform.
     */
    private String specialty;

    /**
     * User's role in the system.
     * Determines permissions and access levels.
     * Default role is CUSTOMER.
     */
    @Enumerated(EnumType.STRING)
    private Role role = Role.CUSTOMER;

    /**
     * Timestamp when the user account was created.
     * Automatically set to the current time when a new user is created.
     */
    @Column(name = "created_at")
    private java.time.LocalDateTime createdAt = java.time.LocalDateTime.now();

    /**
     * Collection of bookings associated with this user.
     * For customers, these are their appointments.
     * For staff, these are appointments they're assigned to.
     */
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Booking> bookings = new HashSet<>();

    /**
     * Default constructor.
     * Required for JPA and for creating new user instances.
     */
    public User() {
        // Default constructor
    }

  
    public User(Long id, String username, String password, String fullName, String email,
                String phoneNumber, String specialty, Role role, Set<Booking> bookings) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.fullName = fullName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.specialty = specialty;
        this.role = role;
        this.bookings = bookings;
    }

    
    public Long getId() {
        return id;
    }

   
    public void setId(Long id) {
        this.id = id;
    }

   
    public String getUsername() {
        return username;
    }

   
    public void setUsername(String username) {
        this.username = username;
    }


    public String getPassword() {
        return password;
    }

    
    public void setPassword(String password) {
        this.password = password;
    }

    
    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    /**
     * Gets the user's email.
     *
     * @return The email address
     */
    public String getEmail() {
        return email;
    }

    /**
     * Sets the user's email.
     *
     * @param email The email to set
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Gets the user's phone number.
     *
     * @return The phone number
     */
    public String getPhoneNumber() {
        return phoneNumber;
    }

    /**
     * Sets the user's phone number.
     *
     * @param phoneNumber The phone number to set
     */
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    /**
     * Gets the staff specialty.
     *
     * @return The specialty
     */
    public String getSpecialty() {
        return specialty;
    }

    /**
     * Sets the staff specialty.
     *
     * @param specialty The specialty to set
     */
    public void setSpecialty(String specialty) {
        this.specialty = specialty;
    }

    /**
     * Gets the user's role.
     *
     * @return The role
     */
    public Role getRole() {
        return role;
    }

    /**
     * Sets the user's role.
     *
     * @param role The role to set
     */
    public void setRole(Role role) {
        this.role = role;
    }

    /**
     * Gets the account creation timestamp.
     *
     * @return The creation timestamp
     */
    public java.time.LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * Sets the account creation timestamp.
     *
     * @param createdAt The timestamp to set
     */
    public void setCreatedAt(java.time.LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * Gets the user's bookings.
     *
     * @return The set of bookings
     */
    public Set<Booking> getBookings() {
        return bookings;
    }

    /**
     * Sets the user's bookings.
     *
     * @param bookings The bookings to set
     */
    public void setBookings(Set<Booking> bookings) {
        this.bookings = bookings;
    }

    /**
     * Enum representing the possible roles a user can have in the system.
     * Each role has different permissions and access levels.
     */
    public enum Role {
        /** Administrator role with full system access */
        ADMIN,
        /** Staff role with booking management capabilities */
        STAFF,
        /** Customer role with appointment booking abilities */
        CUSTOMER
    }

    
    @Override
    public String toFileString() {
        // Format: id|username|password|fullName|email|phoneNumber|specialty|role|createdAt
        return String.format("%d|%s|%s|%s|%s|%s|%s|%s|%s",
                id,
                username,
                password,
                fullName,
                email,
                phoneNumber != null ? phoneNumber : "",
                specialty != null ? specialty : "",
                role.name(),
                createdAt != null ? createdAt.toString() : "");
    }

  
    @Override
    public void fromFileString(String fileString) {
        if (fileString == null || fileString.trim().isEmpty()) {
            return;
        }

        String[] parts = fileString.split("\\|");
        if (parts.length < 5) {
            // Minimum required fields: id, username, password, fullName, email
            return;
        }

        try {
            this.id = Long.parseLong(parts[0]);
            this.username = parts[1];
            this.password = parts[2];
            this.fullName = parts[3];
            this.email = parts[4];

            if (parts.length > 5) {
                this.phoneNumber = parts[5].isEmpty() ? null : parts[5];
            }

            if (parts.length > 6) {
                this.specialty = parts[6].isEmpty() ? null : parts[6];
            }

            if (parts.length > 7) {
                try {
                    this.role = Role.valueOf(parts[7]);
                } catch (IllegalArgumentException e) {
                    this.role = Role.CUSTOMER; // Default to CUSTOMER if invalid
                }
            } else {
                this.role = Role.CUSTOMER; // Default
            }

            if (parts.length > 8 && !parts[8].isEmpty()) {
                try {
                    this.createdAt = java.time.LocalDateTime.parse(parts[8]);
                } catch (Exception e) {
                    this.createdAt = java.time.LocalDateTime.now();
                }
            } else {
                this.createdAt = java.time.LocalDateTime.now();
            }

            // Successfully parsed
        } catch (NumberFormatException e) {
            // Invalid ID format, do nothing
        }
    }
}
