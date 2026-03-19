package com.makemytrip.modules.seatroom.repository;

import com.makemytrip.modules.seatroom.model.Room;
import com.makemytrip.modules.seatroom.model.RoomType;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface RoomRepository extends MongoRepository<Room, String> {
    List<Room> findByHotelId(String hotelId);
    List<Room> findByHotelIdAndAvailable(String hotelId, boolean available);
    List<Room> findByHotelIdAndRoomType(String hotelId, RoomType roomType);
    boolean existsByHotelId(String hotelId);
}
