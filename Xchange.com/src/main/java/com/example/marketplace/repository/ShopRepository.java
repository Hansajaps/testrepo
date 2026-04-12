package com.example.marketplace.repository;

import com.example.marketplace.model.Shop;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ShopRepository extends MongoRepository<Shop, String> {
    Optional<Shop> findByUserId(String userId);
    boolean existsByUserId(String userId);
}
