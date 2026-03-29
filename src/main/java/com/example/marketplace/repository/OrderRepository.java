package com.example.marketplace.repository;

import com.example.marketplace.model.Order;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface OrderRepository extends MongoRepository<Order, String> {
    List<Order> findByBuyerId(String buyerId);
    List<Order> findBySellerIdOrderByCreatedAtDesc(String sellerId);
}
