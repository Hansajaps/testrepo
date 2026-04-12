package com.example.marketplace.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
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
public class VehicleDto {
    private String id;
    private String sellerId;

    @NotBlank
    private String title;

    @NotBlank
    private String description;

    @NotNull
    @Positive
    private BigDecimal price;

    @NotBlank
    private String categoryId;

    @NotBlank
    private String location;

    private List<String> images;
    private List<String> features;

    @NotNull
    private Double kms;

    @NotNull
    private Integer year;

    private String shopName;
    private LocalDateTime createdAt;
    private String status; // ACTIVE, SOLD, DRAFT
}
