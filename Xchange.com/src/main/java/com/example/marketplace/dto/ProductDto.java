package com.example.marketplace.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductDto {
    private String id;
    private String sellerId;
    private String shopId;
    private String name;
    private String description;
    private BigDecimal price;
    private String category;
    private Integer stockQuantity;
    private List<String> images;
    private String shopName;
    private LocalDateTime createdAt;
    private String status;
    private Double averageRating;
    private Integer reviewCount;
    private List<com.example.marketplace.model.Review> reviews;

    // --- Location-Based Filtering Fields ---
    private String district;
    private String city;
    private String area;
    private Double latitude;
    private Double longitude;
    private String googleMapsUrl;
    private String location; // Legacy location string
    private String deliveryType; // CITY_BASED, RADIUS_BASED
    private Double deliveryRadius;
}
