package com.afreedshaik30.hotelparadiseinn.service;

import com.afreedshaik30.hotelparadiseinn.dto.BookingDTO;
import com.afreedshaik30.hotelparadiseinn.dto.Response;
import com.afreedshaik30.hotelparadiseinn.entity.Booking;
import com.afreedshaik30.hotelparadiseinn.exception.OurException;
import com.afreedshaik30.hotelparadiseinn.repository.BookingRepository;
import com.afreedshaik30.hotelparadiseinn.repository.RoomRepository;
import com.afreedshaik30.hotelparadiseinn.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.afreedshaik30.hotelparadiseinn.utils.Utils;

import java.util.List;
import java.util.UUID;

import com.afreedshaik30.hotelparadiseinn.entity.Room;
import  com.afreedshaik30.hotelparadiseinn.entity.User;

@Service
public class BookingServiceImpl implements BookingService{

    private final BookingRepository bookingRepository;
    private final RoomService roomService;
    private final RoomRepository roomRepository;
    private final UserRepository userRepository;

    @Autowired
    public BookingServiceImpl(BookingRepository bookingRepository, RoomService roomService, RoomRepository roomRepository, UserRepository userRepository) {
        this.bookingRepository = bookingRepository;
        this.roomService = roomService;
        this.roomRepository = roomRepository;
        this.userRepository = userRepository;
    }

    @Override
    public Response saveBooking(Long roomId, Long userId, Booking booking) {
        // This method handles saving a new booking for a given room and user.
        // It validates the room and user, generates a unique booking confirmation code,
        // saves the booking in the database, and returns a secure response with booking details.

        Response response = new Response(); // Step 1: Initialize the response object

        try {
            // Step 2: Fetch the Room by ID. If not found, throw custom exception.
            Room room = roomRepository.findById(roomId)
                    .orElseThrow(() -> new OurException("Room not found"));

            // Step 3: Fetch the User by ID. If not found, throw custom exception.
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new OurException("User not found"));

            // Step 4: Populate booking fields
            booking.setRoom(room); // Associate booking with the room
            booking.setUser(user); // Associate booking with the user
            booking.setBookingConfirmationCode(UUID.randomUUID().toString()); // Generate a unique confirmation code

            // Step 5: Persist the booking in the database
            Booking savedBooking = bookingRepository.save(booking);

            // Step 6: Convert entity to a safe DTO to avoid exposing sensitive fields
            BookingDTO bookingDTO = Utils.mapBookingEntityToBookingDTO(savedBooking);

            // Step 7: Populate the response with booking details
            response.setStatusCode(200);
            response.setMessage("Booking successful");
            response.setBooking(bookingDTO); // Attach the DTO to the response

        } catch (OurException e) {
            // Step 8: Handle known errors like user or room not found
            response.setStatusCode(404);
            response.setMessage(e.getMessage());

        } catch (Exception e) {
            // Step 9: Handle all other unexpected errors
            response.setStatusCode(500);
            response.setMessage("Error while saving booking: " + e.getMessage());
        }

        return response; // Step 10: Return final response object
    }

    @Override
    public Response findBookingByConfirmationCode(String confirmationCode) {
        // This method finds a booking using the unique confirmation code
        // and returns its details wrapped in a Response object.

        Response response = new Response(); // Step 1: Initialize the response

        try {
            // Step 2: Retrieve the booking from the database using the confirmation code.
            // If not found, throw a custom "Booking Not Found" exception.
            Booking booking = bookingRepository.findByBookingConfirmationCode(confirmationCode)
                    .orElseThrow(() -> new OurException("Booking Not Found"));

            // Step 3: Map the booking entity to a BookingDTO.
            // The boolean flag 'true' indicates that booked room details should be included.
            BookingDTO bookingDTO = Utils.mapBookingEntityToBookingDTOPlusBookedRooms(booking, true);

            // Step 4: Populate the response object with success status and booking data.
            response.setStatusCode(200);
            response.setMessage("successful");
            response.setBooking(bookingDTO);

        } catch (OurException e) {
            // If the booking is not found, return a 404 status with the exception message.
            response.setStatusCode(404);
            response.setMessage(e.getMessage());

        } catch (Exception e) {
            // Handle any other unexpected errors.
            response.setStatusCode(500);
            response.setMessage("Error Finding a booking: " + e.getMessage());
        }

        return response; // Step 5: Return the response object
    }

    @Override
    public Response getAllBookings() {
        // This method retrieves all bookings from the database,
        // converts them to DTOs, and returns them in a standardized Response object.

        Response response = new Response(); // Step 1: Initialize the response object

        try {
            // Step 2: Fetch all bookings sorted by descending ID (most recent first)
            List<Booking> bookingList = bookingRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));

            // Step 3: Convert list of Booking entities to a list of BookingDTOs
            List<BookingDTO> bookingDTOList = Utils.mapBookingListEntityToBookingListDTO(bookingList);

            // Step 4: Populate the response with the booking list and success status
            response.setStatusCode(200);
            response.setMessage("successful");
            response.setBookingList(bookingDTOList);

        } catch (OurException e) {
            // Handle any custom exceptions thrown within the method
            response.setStatusCode(404);
            response.setMessage(e.getMessage());

        } catch (Exception e) {
            // Handle unexpected exceptions
            response.setStatusCode(500);
            response.setMessage("Error Getting all bookings: " + e.getMessage());
        }

        return response; // Step 5: Return the populated response
    }

    @Override
    public Response cancelBooking(Long bookingId) {
        // This method cancels a booking if it exists, using the booking ID.

        Response response = new Response(); // Step 1: Initialize response object

        try {
            // Step 2: Check if booking exists; if not, throw custom exception
            bookingRepository.findById(bookingId)
                    .orElseThrow(() -> new OurException("Booking Does Not Exist"));

            // Step 3: Delete booking by ID
            bookingRepository.deleteById(bookingId);

            // Step 4: Prepare success response
            response.setStatusCode(200);
            response.setMessage("successful");

        } catch (OurException e) {
            // Handle case when booking is not found
            response.setStatusCode(404);
            response.setMessage(e.getMessage());

        } catch (Exception e) {
            // Handle any unexpected errors
            response.setStatusCode(500);
            response.setMessage("Error Cancelling a booking: " + e.getMessage());
        }

        return response; // Step 5: Return response
    }

    private boolean roomIsAvailable(Booking bookingRequest, List<Booking> existingBookings) {
        // This method checks whether the requested booking does NOT overlap
        // with any existing bookings for a room.

        return existingBookings.stream().noneMatch(existingBooking ->
                // Case 1: Exact check-in date match
                bookingRequest.getCheckInDate().equals(existingBooking.getCheckInDate()) ||

                        // Case 2: Check-out is before existing check-out but overlaps with existing stay
                        bookingRequest.getCheckOutDate().isBefore(existingBooking.getCheckOutDate()) ||

                        // Case 3: Check-in is between existing booking's check-in and check-out
                        (bookingRequest.getCheckInDate().isAfter(existingBooking.getCheckInDate()) &&
                                bookingRequest.getCheckInDate().isBefore(existingBooking.getCheckOutDate())) ||

                        // Case 4: Check-in is before and check-out is equal to existing check-out
                        (bookingRequest.getCheckInDate().isBefore(existingBooking.getCheckInDate()) &&
                                bookingRequest.getCheckOutDate().equals(existingBooking.getCheckOutDate())) ||

                        // Case 5: New booking range fully overlaps existing range
                        (bookingRequest.getCheckInDate().isBefore(existingBooking.getCheckInDate()) &&
                                bookingRequest.getCheckOutDate().isAfter(existingBooking.getCheckOutDate())) ||

                        // Case 6: Same-day back-to-back check-in/check-out conflicts
                        (bookingRequest.getCheckInDate().equals(existingBooking.getCheckOutDate()) &&
                                bookingRequest.getCheckOutDate().equals(existingBooking.getCheckInDate())) ||

                        // Case 7: Same check-in and check-out (single-day overlap)
                        (bookingRequest.getCheckInDate().equals(existingBooking.getCheckOutDate()) &&
                                bookingRequest.getCheckOutDate().equals(bookingRequest.getCheckInDate()))
        );
    }

}
