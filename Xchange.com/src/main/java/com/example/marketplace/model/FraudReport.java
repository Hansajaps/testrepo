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
@Document(collection = "fraud_reports")
public class FraudReport {
    @Id
    private String id;
    private String reporterId;
    private String vehicleId;
    private String details;
    private List<String> evidenceUrls;
    private String status; // PENDING, INVESTIGATED, RESOLVED
    private LocalDateTime createdAt;
}
