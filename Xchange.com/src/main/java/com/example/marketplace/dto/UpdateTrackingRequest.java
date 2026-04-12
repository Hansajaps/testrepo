package com.example.marketplace.dto;

import com.example.marketplace.model.TrackingStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateTrackingRequest {
    private TrackingStatus newStage;
    private String courierName;
    private String trackingNumber;
}
