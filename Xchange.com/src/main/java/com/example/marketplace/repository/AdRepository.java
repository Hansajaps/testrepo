package com.example.marketplace.repository;

import com.example.marketplace.model.Ad;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface AdRepository extends MongoRepository<Ad, String> {
    List<Ad> findByActiveTrue();
}
