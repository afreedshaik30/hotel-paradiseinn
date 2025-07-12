package com.afreedshaik30.hotelparadiseinn.utils;

import com.afreedshaik30.hotelparadiseinn.dto.BookingDTO;
import com.afreedshaik30.hotelparadiseinn.dto.RoomDTO;
import com.afreedshaik30.hotelparadiseinn.dto.UserDTO;
import com.afreedshaik30.hotelparadiseinn.entity.Booking;
import com.afreedshaik30.hotelparadiseinn.entity.Room;
import com.afreedshaik30.hotelparadiseinn.entity.User;

import java.security.SecureRandom;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class Utils {

    private static final String ALPHANUMERIC_STRING = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final SecureRandom secureRandom = new SecureRandom();

    // Random Confirmation Code Generator
    public static String generateRandomConfirmationCode(int length) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < length; i++) {
            int randomIdx = secureRandom.nextInt(ALPHANUMERIC_STRING.length());
            char randomChar = ALPHANUMERIC_STRING.charAt(randomIdx);
            stringBuilder.append(randomChar);
        }
        return stringBuilder.toString();
    }

    // User -> UserDTO
    public static UserDTO mapUserEntityToUserDTO(User user) {
        if (user == null) return null;

        UserDTO userDTO = new UserDTO();
        userDTO.setId(user.getId());
        userDTO.setName(user.getName());
        userDTO.setEmail(user.getEmail());
        userDTO.setPhoneNumber(user.getPhoneNumber());
        userDTO.setRole(user.getRole());
        return userDTO;
    }

    // Room -> RoomDTO
    public static RoomDTO mapRoomEntityToRoomDTO(Room room) {
        if (room == null) return null;

        RoomDTO roomDTO = new RoomDTO();
        roomDTO.setId(room.getId());
        roomDTO.setRoomType(room.getRoomType());
        roomDTO.setRoomPrice(room.getRoomPrice());
        roomDTO.setRoomImgUrl(room.getRoomImgUrl());
        roomDTO.setRoomDescription(room.getRoomDescription());
        return roomDTO;
    }

    // Booking -> BookingDTO
    public static BookingDTO mapBookingEntityToBookingDTO(Booking booking) {
        if (booking == null) return null;

        BookingDTO bookingDTO = new BookingDTO();
        bookingDTO.setId(booking.getId());
        bookingDTO.setCheckInDate(booking.getCheckInDate());
        bookingDTO.setCheckOutDate(booking.getCheckOutDate());
        bookingDTO.setTotalNumOfGuest(booking.getTotalGuests());
        bookingDTO.setNumOfAdults(booking.getNumOfAdults());
        bookingDTO.setNumOfChildren(booking.getNumOfChildren());
        bookingDTO.setBookingConfirmationCode(booking.getBookingConfirmationCode());
        return bookingDTO;
    }

    // Room -> RoomDTO + Bookings
    public static RoomDTO mapRoomEntityToRoomDTOPlusBookings(Room room) {
        if (room == null) return null;

        RoomDTO roomDTO = mapRoomEntityToRoomDTO(room);

        if (room.getBookings() != null && !room.getBookings().isEmpty()) {
            roomDTO.setBookings(
                    room.getBookings()
                            .stream()
                            .map(Utils::mapBookingEntityToBookingDTO)
                            .collect(Collectors.toList())
            );
        }
        return roomDTO;
    }

    // Booking -> BookingDTO + Room (+ optionally User)
    public static BookingDTO mapBookingEntityToBookingDTOPlusBookedRooms(Booking booking, boolean mapUser) {
        if (booking == null) return null;

        BookingDTO bookingDTO = mapBookingEntityToBookingDTO(booking);

        if (mapUser && booking.getUser() != null) {
            bookingDTO.setUser(mapUserEntityToUserDTO(booking.getUser()));
        }

        if (booking.getRoom() != null) {
            bookingDTO.setRoom(mapRoomEntityToRoomDTO(booking.getRoom()));
        }

        return bookingDTO;
    }

    // User -> UserDTO + Bookings + Room
    public static UserDTO mapUserEntityToUserDTOPlusUserBookingsAndRoom(User user) {
        if (user == null) return null;

        UserDTO userDTO = mapUserEntityToUserDTO(user);

        if (user.getBookings() != null && !user.getBookings().isEmpty()) {
            userDTO.setBookings(
                    user.getBookings()
                            .stream()
                            .map(booking -> mapBookingEntityToBookingDTOPlusBookedRooms(booking, false))
                            .collect(Collectors.toList())
            );
        }

        return userDTO;
    }

    // ====== List Mapping Methods ======

    public static List<UserDTO> mapUserListEntityToUserListDTO(List<User> userList) {
        if (userList == null) return Collections.emptyList();
        return userList.stream()
                .map(Utils::mapUserEntityToUserDTO)
                .collect(Collectors.toList());
    }

    public static List<RoomDTO> mapRoomListEntityToRoomListDTO(List<Room> roomList) {
        if (roomList == null) return Collections.emptyList();
        return roomList.stream()
                .map(Utils::mapRoomEntityToRoomDTO)
                .collect(Collectors.toList());
    }

    public static List<BookingDTO> mapBookingListEntityToBookingListDTO(List<Booking> bookingList) {
        if (bookingList == null) return Collections.emptyList();
        return bookingList.stream()
                .map(Utils::mapBookingEntityToBookingDTO)
                .collect(Collectors.toList());
    }
}
