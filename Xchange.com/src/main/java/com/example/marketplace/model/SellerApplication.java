package com.example.marketplace.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "seller_applications")
public class SellerApplication {
    @Id
    private String id;
    private String userId;
    private String userName; // name of the user who created the store
    private String shopName;
    private List<String> shopCategories;
    private String district;
    private String city;
    private String acceptedPaymentMethods;
    private String status; // PENDING, APPROVED, REJECTED
    private LocalDateTime appliedAt;
    private LocalDateTime reviewedAt;
    private String reviewedBy; // admin ID who reviewed
    private String rejectionReason; // reason if rejected
}
