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
@Document(collection = "vehicles")
public class Vehicle {
    @Id
    private String id;
    private String sellerId;
    private String shopName; // Denormalized for display
    private String title;
    private String description;
    private BigDecimal price;
    private String categoryId;
    private String location;
    @Builder.Default
    private List<String> images = new ArrayList<>();
    @Builder.Default
    private List<String> features = new ArrayList<>();
    private Double kms;
    private Integer year;
    private LocalDateTime createdAt;
    private String status; // ACTIVE, SOLD, DRAFT
}
