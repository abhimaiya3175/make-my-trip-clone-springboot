package com.makemytrip.makemytrip.repositories;
import com.makemytrip.makemytrip.models.Hotel;

import org.springframework.data.mongodb.repository.MongoRepository;
public interface HotelRepository extends MongoRepository<Hotel,String>{
}