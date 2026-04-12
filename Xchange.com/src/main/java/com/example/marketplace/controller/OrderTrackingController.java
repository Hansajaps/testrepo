package com.example.marketplace.controller;

import com.example.marketplace.dto.OrderTrackingDto;
import com.example.marketplace.dto.UpdateTrackingRequest;
import com.example.marketplace.service.OrderTrackingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping("/api/orders/{orderId}/tracking")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Slf4j
public class OrderTrackingController {

    private final OrderTrackingService trackingService;

    @PostMapping
    public ResponseEntity<OrderTrackingDto> createTracking(@PathVariable String orderId) {
        OrderTrackingDto trackingDto = trackingService.createTracking(orderId);
        return ResponseEntity.ok(trackingDto);
    }

    @GetMapping
    public ResponseEntity<OrderTrackingDto> getTracking(@PathVariable String orderId) {
        OrderTrackingDto trackingDto = trackingService.getTracking(orderId);
        return ResponseEntity.ok(trackingDto);
    }

    @PutMapping
    public ResponseEntity<OrderTrackingDto> updateTrackingStage(
            @PathVariable String orderId,
            @RequestBody UpdateTrackingRequest updateRequest,
            Authentication auth) {
        OrderTrackingDto trackingDto = trackingService.updateStage(orderId, updateRequest, auth.getName());
        return ResponseEntity.ok(trackingDto);
    }

    @RequestMapping(value = "/confirm-delivery", method = { RequestMethod.POST, RequestMethod.PUT })
    public ResponseEntity<?> confirmDelivery(
            @PathVariable String orderId,
            Authentication auth) {
        log.info("confirmDelivery request for orderId: {}", orderId);
        if (auth == null) {
            log.error("Authentication is null for confirmDelivery");
            return ResponseEntity.status(401).body(Map.of("error", "User must be authenticated"));
        }
        try {
            OrderTrackingDto result = trackingService.confirmDelivery(orderId, auth.getName());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error in confirmDelivery for orderId {}: {}", orderId, e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of(
                    "error", "Internal Server Error: " + e.getMessage(),
                    "path", "/api/orders/" + orderId + "/tracking/confirm-delivery"));
        }
    }

}
