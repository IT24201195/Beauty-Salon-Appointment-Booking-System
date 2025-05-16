package com.Mathra.Salon.repository;

import com.Mathra.Salon.model.Booking;
import com.Mathra.Salon.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByUser(User user);
    List<Booking> findByAssignedStaff(User staff);
    List<Booking> findByBookingDate(LocalDate date);
    List<Booking> findByStatus(Booking.Status status);
    List<Booking> findByUserAndStatus(User user, Booking.Status status);
} 