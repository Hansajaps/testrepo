package com.example.marketplace.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "products")
public class Product {
    @Id
    private String id;
    private String sellerId; // Link to the user (email)
    private String shopId; // Link to the shop
    private String shopName; // Denormalized for display
    private String name;
    private String description;
    private BigDecimal price;
    private String category;
    private Integer stockQuantity;
    @Builder.Default
    private List<String> images = new ArrayList<>();
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    @Builder.Default
    private String status = "ACTIVE"; // ACTIVE, SOLD, DRAFT, ARCHIVED
}
