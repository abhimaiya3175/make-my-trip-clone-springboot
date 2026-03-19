package com.makemytrip.modules.seatroom.repository;

import com.makemytrip.modules.seatroom.model.UserPreference;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface UserPreferenceRepository extends MongoRepository<UserPreference, String> {
    Optional<UserPreference> findByUserId(String userId);
}
