package com.afreedshaik30.hotelparadiseinn.service;

import com.afreedshaik30.hotelparadiseinn.dto.Response;
import com.afreedshaik30.hotelparadiseinn.entity.Booking;

public interface BookingService {
    Response saveBooking(Long roomId, Long userId, Booking bookingRequest);

    Response findBookingByConfirmationCode(String confirmationCode);

    Response getAllBookings();

    Response cancelBooking(Long bookingId);
}
