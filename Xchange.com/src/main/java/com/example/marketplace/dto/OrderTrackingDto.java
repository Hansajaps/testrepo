package com.example.marketplace.dto;

import com.example.marketplace.model.TrackingStage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderTrackingDto {
    // ❗ IMPORTANT CHANGE: Use DTO instead of entity in API response
    private String orderId;
    private String status;
    private List<TrackingStage> stages;
    private String courierName;
    private String trackingNumber;
    private boolean deliveryConfirmed;
}
