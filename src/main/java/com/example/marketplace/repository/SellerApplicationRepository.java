package com.example.marketplace.repository;

import com.example.marketplace.model.SellerApplication;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface SellerApplicationRepository extends MongoRepository<SellerApplication, String> {
    List<SellerApplication> findByStatus(String status);

    List<SellerApplication> findByUserId(String userId);
}
