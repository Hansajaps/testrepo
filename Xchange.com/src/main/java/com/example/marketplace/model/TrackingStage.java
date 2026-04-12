package com.example.marketplace.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrackingStage {
    private TrackingStatus stage;
    // ✅ CHANGE: Add completed flag
    private boolean completed;
    // ✅ CHANGE: Ensure timestamp is nullable initially
    private LocalDateTime timestamp;
}
