package com.example.marketplace.repository;

import com.example.marketplace.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;

public interface UserRepository extends MongoRepository<User, String> {
    Optional<User> findByEmail(String email);
    Optional<User> findByShopId(String shopId);

    Boolean existsByEmail(String email);
}
