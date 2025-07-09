package com.afreedshaik30.hotelparadiseinn.repository;

import com.afreedshaik30.hotelparadiseinn.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.time.LocalDate;

public interface RoomRepository extends JpaRepository<Room, Long> {
    // 1. Get unique room types
    @Query("SELECT DISTINCT r.roomType FROM Room r")
    List<String> findDistinctRoomTypes();

    // 2. Get available rooms by room type and date range
    @Query("""
    SELECT r FROM Room r 
    WHERE r.roomType LIKE %:roomType% 
    AND r.id NOT IN (
        SELECT b.room.id FROM Booking b 
        WHERE b.checkInDate <= :checkOutDate 
        AND b.checkOutDate >= :checkInDate
    )
    """)
    List<Room> findAvailableRoomsByDatesAndTypes(
            LocalDate checkInDate,
            LocalDate checkOutDate,
            String roomType
    );

    // 3. Get rooms that have no bookings at all
    @Query("SELECT r FROM Room r WHERE r.id NOT IN (SELECT b.room.id FROM Booking b)")
    List<Room> getAllAvailableRooms();

}
