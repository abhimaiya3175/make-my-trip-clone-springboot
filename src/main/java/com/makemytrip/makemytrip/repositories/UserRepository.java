package com.makemytrip.makemytrip.repositories;
import com.makemytrip.makemytrip.models.Users;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserRepository extends MongoRepository<Users,String>{
    Users findByEmail(String email);
}