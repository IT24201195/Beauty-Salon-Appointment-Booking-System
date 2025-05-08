package com.Mathra.Salon.model;

import com.mathra.salon.filemanager.FileStorable;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "bookings")
public class Booking implements FileStorable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private LocalDate bookingDate;

    @Column(nullable = false)
    private LocalTime bookingTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ServiceType serviceType;

    private String specialRequests;

    @Enumerated(EnumType.STRING)
    private Status status = Status.PENDING;

    @ManyToOne
    @JoinColumn(name = "staff_id")
    private User assignedStaff;

    public Booking() {
        // Default constructor
    }

    public Booking(Long id, User user, LocalDate bookingDate, LocalTime bookingTime,
                   ServiceType serviceType, String specialRequests, Status status, User assignedStaff) {
        this.id = id;
        this.user = user;
        this.bookingDate = bookingDate;
        this.bookingTime = bookingTime;
        this.serviceType = serviceType;
        this.specialRequests = specialRequests;
        this.status = status;
        this.assignedStaff = assignedStaff;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public LocalDate getBookingDate() {
        return bookingDate;
    }

    public void setBookingDate(LocalDate bookingDate) {
        this.bookingDate = bookingDate;
    }

    public LocalTime getBookingTime() {
        return bookingTime;
    }

    public void setBookingTime(LocalTime bookingTime) {
        this.bookingTime = bookingTime;
    }

    public ServiceType getServiceType() {
        return serviceType;
    }

    public void setServiceType(ServiceType serviceType) {
        this.serviceType = serviceType;
    }

    public String getSpecialRequests() {
        return specialRequests;
    }

    public void setSpecialRequests(String specialRequests) {
        this.specialRequests = specialRequests;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public User getAssignedStaff() {
        return assignedStaff;
    }

    public void setAssignedStaff(User assignedStaff) {
        this.assignedStaff = assignedStaff;
    }

    public enum ServiceType {
        HAIRCUT("Haircut"),
        COLORING("Hair Coloring"),
        FACIAL("Facial Treatment"),
        MANICURE("Manicure"),
        PEDICURE("Pedicure"),
        MAKEUP("Makeup"),
        MASSAGE("Massage");

        private final String displayName;

        ServiceType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum Status {
        PENDING, CONFIRMED, COMPLETED, CANCELLED
    }
    
    @Override
    public String toFileString() {
        // Format: id|userId|staffId|bookingDate|bookingTime|serviceType|status|specialRequests
        return String.format("%d|%d|%s|%s|%s|%s|%s|%s",
                id,
                user != null ? user.getId() : 0,
                assignedStaff != null ? assignedStaff.getId() : "",
                bookingDate.toString(),
                bookingTime.toString(),
                serviceType.name(),
                status.name(),
                specialRequests != null ? specialRequests : "");
    }
    
    @Override
    public FileStorable fromFileString(String fileString) {
        if (fileString == null || fileString.trim().isEmpty()) {
            return null;
        }
        
        String[] parts = fileString.split("\\|");
        if (parts.length < 6) {
            // Minimum required fields: id, userId, staffId, bookingDate, bookingTime, serviceType
            return null;
        }
        
        try {
            this.id = Long.parseLong(parts[0]);
            
            // User and assigned staff are references, they will be set by the service layer
            // Store the IDs temporarily in User objects
            if (!parts[1].isEmpty()) {
                User user = new User();
                user.setId(Long.parseLong(parts[1]));
                this.user = user;
            }
            
            if (parts[2] != null && !parts[2].isEmpty()) {
                User staff = new User();
                staff.setId(Long.parseLong(parts[2]));
                this.assignedStaff = staff;
            }
            
            this.bookingDate = LocalDate.parse(parts[3]);
            this.bookingTime = LocalTime.parse(parts[4]);
            this.serviceType = ServiceType.valueOf(parts[5]);
            
            if (parts.length > 6) {
                this.status = Status.valueOf(parts[6]);
            } else {
                this.status = Status.PENDING;
            }
            
            if (parts.length > 7) {
                this.specialRequests = parts[7].isEmpty() ? null : parts[7];
            }
            
            return this;
        } catch (Exception e) {
            return null;
        }
    }
} 