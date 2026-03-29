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
    private String businessName;
    private List<String> documents;
    private String status; // PENDING, APPROVED, REJECTED
    private LocalDateTime appliedAt;
}
