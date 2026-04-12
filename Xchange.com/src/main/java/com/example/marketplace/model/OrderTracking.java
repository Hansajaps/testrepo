package com.example.marketplace.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Arrays;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "order_tracking")
public class OrderTracking {
    @Id
    private String id;
    private String orderId;
    private String buyerId;
    private String sellerId;
    private TrackingStatus trackingStatus;
    
    @Builder.Default
    private List<TrackingStage> trackingStages = new java.util.ArrayList<>();
    
    private String courierName;
    private String trackingNumber;
    private boolean deliveryConfirmed;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public void initializeTracking() {
        this.trackingStatus = TrackingStatus.PLACED;
        this.trackingStages = Arrays.asList(
            new TrackingStage(TrackingStatus.PLACED, true, LocalDateTime.now()),
            new TrackingStage(TrackingStatus.PACKED, false, null),
            new TrackingStage(TrackingStatus.SHIPPED, false, null),
            new TrackingStage(TrackingStatus.OUT_FOR_DELIVERY, false, null),
            new TrackingStage(TrackingStatus.DELIVERED, false, null)
        );
        this.deliveryConfirmed = false;
        this.courierName = "";
        this.trackingNumber = "";
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
}
