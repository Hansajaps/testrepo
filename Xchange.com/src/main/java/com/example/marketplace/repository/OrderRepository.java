package com.example.marketplace.repository;

import com.example.marketplace.model.Order;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import com.example.marketplace.model.TrackingStatus;
import java.util.List;

@Repository
public interface OrderRepository extends MongoRepository<Order, String> {
    List<Order> findByBuyerIdOrderByCreatedAtDesc(String buyerId);
    List<Order> findBySellerIdOrderByCreatedAtDesc(String sellerId);
    List<Order> findByTrackingStatus(TrackingStatus status);
}

