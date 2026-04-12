package com.example.marketplace.repository;

import com.example.marketplace.model.Review;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface ReviewRepository extends MongoRepository<Review, String> {
    List<Review> findByProductIdOrderByCreatedAtDesc(String productId);
    List<Review> findByBuyerIdOrderByCreatedAtDesc(String buyerId);
    java.util.Optional<Review> findByOrderId(String orderId);
    java.util.Optional<Review> findByOrderIdAndBuyerId(String orderId, String buyerId);
    java.util.Optional<Review> findByProductIdAndBuyerId(String productId, String buyerId);
}
