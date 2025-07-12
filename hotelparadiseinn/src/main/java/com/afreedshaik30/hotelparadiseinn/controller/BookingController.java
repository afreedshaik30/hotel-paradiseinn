package com.afreedshaik30.hotelparadiseinn.controller;

import com.afreedshaik30.hotelparadiseinn.dto.Response;
import com.afreedshaik30.hotelparadiseinn.entity.Booking;
import com.afreedshaik30.hotelparadiseinn.service.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/bookings")
public class BookingController {

    private final BookingService bookingService;

    @Autowired
    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    /**
     * 1. Book a room
     * POST /bookings/room/{roomId}/user/{userId}
     */
    @PostMapping("/room/{roomId}/user/{userId}")
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('USER')")
    public ResponseEntity<Response> saveBookings(@PathVariable Long roomId,
                                                 @PathVariable Long userId,
                                                 @RequestBody Booking bookingRequest) {
        Response response = bookingService.saveBooking(roomId, userId, bookingRequest);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    /**
     * 2. Get all bookings (admin only)
     * GET /bookings
     */
    @GetMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Response> getAllBookings() {
        Response response = bookingService.getAllBookings();
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    /**
     * 3. Get booking by confirmation code
     * GET /bookings/confirmation/{confirmationCode}
     */
    @GetMapping("/confirmation/{confirmationCode}")
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('USER')")
    public ResponseEntity<Response> getBookingByConfirmationCode(@PathVariable String confirmationCode) {
        Response response = bookingService.findBookingByConfirmationCode(confirmationCode);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    /**
     * 4. Cancel a booking
     * DELETE /bookings/{bookingId}
     */
    @DeleteMapping("/{bookingId}")
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('USER')")
    public ResponseEntity<Response> cancelBooking(@PathVariable Long bookingId) {
        Response response = bookingService.cancelBooking(bookingId);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }
}

/*
    ADMIN only
        GET - /bookings

    USER/ADMIN
        POST - /bookings/room/{roomId}/user/{userId}
        GET  - /bookings/confirmation/{confirmationCode}
        DELETE - /bookings/{bookingId}
*/

