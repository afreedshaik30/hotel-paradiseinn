package com.afreedshaik30.hotelparadiseinn.service;

import com.afreedshaik30.hotelparadiseinn.dto.Response;
import com.afreedshaik30.hotelparadiseinn.dto.RoomDTO;
import com.afreedshaik30.hotelparadiseinn.entity.Room;
import com.afreedshaik30.hotelparadiseinn.exception.OurException;
import com.afreedshaik30.hotelparadiseinn.repository.BookingRepository;
import com.afreedshaik30.hotelparadiseinn.repository.RoomRepository;
import com.afreedshaik30.hotelparadiseinn.utils.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class RoomServiceImpl implements RoomService {

    private final RoomRepository roomRepository;
    private final BookingRepository bookingRepository;
    private final ImgBBService imgbbService;

    @Autowired
    public RoomServiceImpl(RoomRepository roomRepository, BookingRepository bookingRepository, ImgBBService imgbbService) {
        this.roomRepository = roomRepository;
        this.bookingRepository = bookingRepository;
        this.imgbbService = imgbbService;
    }

    // Add a new room to the database with image upload to ImgBB
    @Override
    public Response addNewRoom(MultipartFile photo, String roomType, BigDecimal roomPrice, String description) {
        Response response = new Response();
        try {
            // Upload photo to ImgBB and get back the public image URL
            String imageUrl = imgbbService.uploadImage(photo);

            // Create and populate Room entity
            Room room = new Room();
            room.setRoomImgUrl(imageUrl);
            room.setRoomType(roomType);
            room.setRoomPrice(roomPrice);
            room.setRoomDescription(description);

            // Save to database
            Room savedRoom = roomRepository.save(room);

            // Convert to RoomDTO to send in response
            RoomDTO roomDTO = Utils.mapRoomEntityToRoomDTO(savedRoom);

            response.setStatusCode(200);
            response.setMessage("successful");
            response.setRoom(roomDTO);
        } catch (Exception e) {
            response.setStatusCode(500);
            response.setMessage("Error saving a room: " + e.getMessage());
        }
        return response;
    }

    // Get all unique room types from DB
    @Override
    public List<String> getAllRoomTypes() {
        return roomRepository.findDistinctRoomTypes();
    }

    // Get all rooms sorted by ID (newest first)
    @Override
    public Response getAllRooms() {
        Response response = new Response();
        try {
            List<Room> roomList = roomRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));
            List<RoomDTO> roomDTOList = Utils.mapRoomListEntityToRoomListDTO(roomList);

            response.setStatusCode(200);
            response.setMessage("successful");
            response.setRoomList(roomDTOList);
        } catch (Exception e) {
            response.setStatusCode(500);
            response.setMessage("Error getting rooms: " + e.getMessage());
        }
        return response;
    }

    // Get room details by ID with booking info
    @Override
    public Response getRoomById(Long roomId) {
        Response response = new Response();
        try {
            // Fetch room by ID
            Room room = roomRepository.findById(roomId).orElseThrow(() -> new OurException("Room Not Found"));

            // Map room with associated bookings
            RoomDTO roomDTO = Utils.mapRoomEntityToRoomDTOPlusBookings(room);

            response.setStatusCode(200);
            response.setMessage("successful");
            response.setRoom(roomDTO);
        } catch (OurException e) {
            response.setStatusCode(404);
            response.setMessage(e.getMessage());
        } catch (Exception e) {
            response.setStatusCode(500);
            response.setMessage("Error retrieving room: " + e.getMessage());
        }
        return response;
    }

    // Get available rooms filtered by check-in/out dates and room type
    @Override
    public Response getAvailableRoomsByDataAndType(LocalDate checkInDate, LocalDate checkOutDate, String roomType) {
        Response response = new Response();
        try {
            List<Room> availableRooms = roomRepository.findAvailableRoomsByDatesAndTypes(checkInDate, checkOutDate, roomType);
            List<RoomDTO> roomDTOList = Utils.mapRoomListEntityToRoomListDTO(availableRooms);

            response.setStatusCode(200);
            response.setMessage("successful");
            response.setRoomList(roomDTOList);
        } catch (Exception e) {
            response.setStatusCode(500);
            response.setMessage("Error retrieving available rooms: " + e.getMessage());
        }
        return response;
    }

    // Get all available rooms (ignoring dates/type)
    @Override
    public Response getAllAvailableRooms() {
        Response response = new Response();
        try {
            List<Room> roomList = roomRepository.getAllAvailableRooms();
            List<RoomDTO> roomDTOList = Utils.mapRoomListEntityToRoomListDTO(roomList);

            response.setStatusCode(200);
            response.setMessage("successful");
            response.setRoomList(roomDTOList);
        } catch (OurException e) {
            response.setStatusCode(404);
            response.setMessage(e.getMessage());
        } catch (Exception e) {
            response.setStatusCode(500);
            response.setMessage("Error retrieving available rooms: " + e.getMessage());
        }
        return response;
    }
    // Update room details (description, type, price, optional image)

    @Override
    public Response updateRoom(Long roomId, String description, String roomType, BigDecimal roomPrice, MultipartFile photo) {
        Response response = new Response();
        try {
            String imageUrl = null;

            // If new image is provided, upload and get URL
            if (photo != null && !photo.isEmpty()) {
                imageUrl = imgbbService.uploadImage(photo);
            }

            // Fetch existing room
            Room room = roomRepository.findById(roomId).orElseThrow(() -> new OurException("Room Not Found"));

            // Update non-null fields
            if (roomType != null) room.setRoomType(roomType);
            if (roomPrice != null) room.setRoomPrice(roomPrice);
            if (description != null) room.setRoomDescription(description);
            if (imageUrl != null) room.setRoomImgUrl(imageUrl);

            // Save updated room
            Room updatedRoom = roomRepository.save(room);
            RoomDTO roomDTO = Utils.mapRoomEntityToRoomDTO(updatedRoom);

            response.setStatusCode(200);
            response.setMessage("successful");
            response.setRoom(roomDTO);
        } catch (OurException e) {
            response.setStatusCode(404);
            response.setMessage(e.getMessage());
        } catch (Exception e) {
            response.setStatusCode(500);
            response.setMessage("Error updating room: " + e.getMessage());
        }
        return response;
    }

    // Delete a room by ID
    @Override
    public Response deleteRoom(Long roomId) {
        Response response = new Response();
        try {
            // Check if room exists
            roomRepository.findById(roomId).orElseThrow(() -> new OurException("Room Not Found"));

            // Delete room
            roomRepository.deleteById(roomId);

            response.setStatusCode(200);
            response.setMessage("successful");
        } catch (OurException e) {
            response.setStatusCode(404);
            response.setMessage(e.getMessage());
        } catch (Exception e) {
            response.setStatusCode(500);
            response.setMessage("Error deleting room: " + e.getMessage());
        }
        return response;
    }

}
