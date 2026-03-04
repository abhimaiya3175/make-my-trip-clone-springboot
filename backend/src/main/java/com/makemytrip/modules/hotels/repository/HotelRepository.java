package com.makemytrip.modules.hotels.repository;

import com.makemytrip.modules.hotels.model.Hotel;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface HotelRepository extends MongoRepository<Hotel, String> {
}
