package com.example.marketplace.repository;

import com.example.marketplace.model.Vehicle;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

public interface VehicleRepository extends MongoRepository<Vehicle, String> {
    Page<Vehicle> findByCategoryId(String categoryId, Pageable pageable);

    Page<Vehicle> findBySellerId(String sellerId, Pageable pageable);

    Page<Vehicle> findByStatus(String status, Pageable pageable);

    // Custom query for filtering - simple version for now
    @Query("{'title': {$regex: ?0, $options: 'i'}, 'status': 'ACTIVE'}")
    Page<Vehicle> searchByTitle(String title, Pageable pageable);
}
