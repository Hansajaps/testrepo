package com.example.marketplace.repository;

import com.example.marketplace.model.OrderTracking;
import com.example.marketplace.model.TrackingStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderTrackingRepository extends MongoRepository<OrderTracking, String> {
    OrderTracking findByOrderId(String orderId);
    List<OrderTracking> findByBuyerIdOrderByCreatedAtDesc(String buyerId);
    List<OrderTracking> findBySellerIdOrderByCreatedAtDesc(String sellerId);
    List<OrderTracking> findByTrackingStatus(TrackingStatus status);
}
