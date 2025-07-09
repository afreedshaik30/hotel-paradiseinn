package com.afreedshaik30.hotelparadiseinn.repository;

import com.afreedshaik30.hotelparadiseinn.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking,Long> {
    // 1. by bookingConfirmationCode
    Optional<Booking> findByBookingConfirmationCode(String confirmationCode);
}
