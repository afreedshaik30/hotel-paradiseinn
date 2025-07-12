package com.afreedshaik30.hotelparadiseinn.controller;

import com.afreedshaik30.hotelparadiseinn.dto.Response;
import com.afreedshaik30.hotelparadiseinn.service.BookingService;
import com.afreedshaik30.hotelparadiseinn.service.RoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/rooms")
public class RoomController {

    private final RoomService roomService;
    private final BookingService bookingService;

    @Autowired
    public RoomController(RoomService roomService, BookingService bookingService) {
        this.roomService = roomService;
        this.bookingService = bookingService;
    }

    // 1. Add a new room (ADMIN only)
    @PostMapping("/add")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Response> addNewRoom(
            @RequestParam(value = "photo", required = false) MultipartFile photo,
            @RequestParam(value = "roomType", required = false) String roomType,
            @RequestParam(value = "roomPrice", required = false) BigDecimal roomPrice,
            @RequestParam(value = "roomDescription", required = false) String roomDescription
    ) {
        Response response = new Response();

        if (photo == null || photo.isEmpty()
                || roomType == null || roomType.isBlank()
                || roomPrice == null
                || roomDescription == null || roomDescription.isBlank()) {

            response.setStatusCode(400);
            response.setMessage("Please provide values for all fields (photo, roomType, roomPrice, roomDescription)");
            return ResponseEntity.status(400).body(response);
        }

        response = roomService.addNewRoom(photo, roomType, roomPrice, roomDescription);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    // 2. Get all rooms (Public)
    @GetMapping("/all")
    public ResponseEntity<Response> getAllRooms() {
        Response response = roomService.getAllRooms();
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    // 3. Get all distinct room types (Public)
    @GetMapping("/types")
    public List<String> getRoomTypes() {
        return roomService.getAllRoomTypes();
    }

    // 4. Get room by ID (Public)
    @GetMapping("/{roomId}")
    public ResponseEntity<Response> getRoomById(@PathVariable("roomId") Long roomId) {
        Response response = roomService.getRoomById(roomId);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    // 5. Get all available rooms (Public)
    @GetMapping("/available")
    public ResponseEntity<Response> getAllAvailableRooms() {
        Response response = roomService.getAllAvailableRooms();
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    // 6. Get available rooms by check-in, check-out and roomType (Public)
    @GetMapping("/available-by-date-type")
    public ResponseEntity<Response> getAvailableRoomsByDateAndType(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkInDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOutDate,
            @RequestParam(required = false) String roomType
    ) {
        Response response = new Response();

        if (checkInDate == null || checkOutDate == null || roomType == null || roomType.isBlank()) {
            response.setStatusCode(400);
            response.setMessage("Missing required parameters: checkInDate, checkOutDate, or roomType");
            return ResponseEntity.status(400).body(response);
        }

        response = roomService.getAvailableRoomsByDataAndType(checkInDate, checkOutDate, roomType);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    // 7. Update room (ADMIN only)
    @PutMapping("/update/{roomId}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Response> updateRoom(
            @PathVariable Long roomId,
            @RequestParam(value = "photo", required = false) MultipartFile photo,
            @RequestParam(value = "roomType", required = false) String roomType,
            @RequestParam(value = "roomPrice", required = false) BigDecimal roomPrice,
            @RequestParam(value = "roomDescription", required = false) String roomDescription
    ) {
        Response response = roomService.updateRoom(roomId, roomDescription, roomType, roomPrice, photo);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    // 8. Delete room (ADMIN only)
    @DeleteMapping("/delete/{roomId}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Response> deleteRoom(@PathVariable Long roomId) {
        Response response = roomService.deleteRoom(roomId);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }
}

/*
   ADMIN
      POST   -  /rooms/add
      PUT    -  /rooms/update/{roomId}
      DELETE -  /rooms/delete/{roomId}

   Public
      GET    -  /rooms/all
                /rooms/types
                /rooms/{roomId}
                /rooms/available
                /rooms/available-by-date-type

*/