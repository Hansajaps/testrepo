package com.example.marketplace.repository;

import com.example.marketplace.model.Product;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends MongoRepository<Product, String> {
    List<Product> findBySellerId(String sellerId);

    List<Product> findByShopId(String shopId);

    List<Product> findByCategory(String category);
}
