package com.example.marketplace.service;

import com.example.marketplace.dto.OrderTrackingDto;
import com.example.marketplace.dto.UpdateTrackingRequest;
import com.example.marketplace.exception.ConflictException;
import com.example.marketplace.exception.InvalidStageTransitionException;
import com.example.marketplace.exception.TrackingNotFoundException;
import com.example.marketplace.model.*;
import com.example.marketplace.repository.OrderTrackingRepository;
import com.example.marketplace.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderTrackingService {

    private final OrderTrackingRepository orderTrackingRepository;
    private final OrderRepository orderRepository;

    public OrderTrackingDto createTracking(String orderId) {
        log.info("Creating tracking for orderId: {}", orderId);
        
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new TrackingNotFoundException("Order not found: " + orderId));

        OrderTracking existingTracking = orderTrackingRepository.findByOrderId(orderId);
        if (existingTracking != null) {
            throw new ConflictException("Tracking already exists for this order");
        }

        OrderTracking tracking = OrderTracking.builder()
                .orderId(orderId)
                .buyerId(order.getBuyerId())
                .sellerId(order.getSellerId())
                .build();
        
        tracking.initializeTracking();
        OrderTracking saved = orderTrackingRepository.save(tracking);
        return mapToDto(saved);
    }

    public OrderTrackingDto getTracking(String orderId) {
        log.info("Fetching tracking for orderId: {}", orderId);
        OrderTracking tracking = orderTrackingRepository.findByOrderId(orderId);
        
        if (tracking == null) {
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new TrackingNotFoundException("Order not found: " + orderId));
            
            tracking = OrderTracking.builder()
                    .orderId(orderId)
                    .buyerId(order.getBuyerId())
                    .sellerId(order.getSellerId())
                    .build();
            tracking.initializeTracking();
            tracking = orderTrackingRepository.save(tracking);
        }
        
        return mapToDto(tracking);
    }

    public OrderTrackingDto updateStage(String orderId, UpdateTrackingRequest request, String sellerEmail) {
        OrderTracking tracking = orderTrackingRepository.findByOrderId(orderId);
        if (tracking == null) {
            throw new TrackingNotFoundException("Tracking not found for order: " + orderId);
        }

        // Security Check
        if (!tracking.getSellerId().equalsIgnoreCase(sellerEmail)) {
            throw new RuntimeException("Unauthorized: Only the seller can update tracking");
        }

        // Status Lock: Cannot update after delivered/closed
        if (tracking.getTrackingStatus() == TrackingStatus.DELIVERED || tracking.getTrackingStatus() == TrackingStatus.CLOSED) {
            throw new ConflictException("Order is finalized. Tracking cannot be updated further.");
        }

        TrackingStatus currentStage = tracking.getTrackingStatus();
        TrackingStatus newStage = request.getNewStage();

        // Sequential Transition Check
        if (newStage.ordinal() != currentStage.ordinal() + 1) {
            log.error("Invalid transition: {} -> {}", currentStage, newStage);
            throw new InvalidStageTransitionException(
                    "Invalid stage transition. Cannot move from " + currentStage + " to " + newStage);
        }

        // Courier Info (Required for OUT_FOR_DELIVERY)
        if (newStage == TrackingStatus.OUT_FOR_DELIVERY) {
            if (request.getCourierName() == null || request.getTrackingNumber() == null) {
                throw new RuntimeException("Courier details required for OUT_FOR_DELIVERY");
            }
            tracking.setCourierName(request.getCourierName());
            tracking.setTrackingNumber(request.getTrackingNumber());
        }

        // Update stage completion
        for (TrackingStage stage : tracking.getTrackingStages()) {
            if (stage.getStage() == newStage) {
                stage.setCompleted(true);
                stage.setTimestamp(LocalDateTime.now());
                break;
            }
        }

        tracking.setTrackingStatus(newStage);
        tracking.setUpdatedAt(LocalDateTime.now());
        
        // Also update the main order status
        Order order = orderRepository.findById(orderId).orElse(null);
        if (order != null) {
            order.setTrackingStatus(newStage);
            if (newStage == TrackingStatus.DELIVERED) {
                order.setStatus("DELIVERED");
            }
            order.setUpdatedAt(LocalDateTime.now());
            orderRepository.save(order);
        }
        
        return mapToDto(orderTrackingRepository.save(tracking));
    }

    public OrderTrackingDto confirmDelivery(String orderId, String buyerEmail) {
        OrderTracking tracking = orderTrackingRepository.findByOrderId(orderId);
        if (tracking == null) {
            throw new TrackingNotFoundException("Tracking not found for order: " + orderId);
        }

        if (!tracking.getBuyerId().equalsIgnoreCase(buyerEmail)) {
            throw new RuntimeException("Unauthorized: Only the buyer can confirm delivery");
        }

        // Allow confirm if status is OUT_FOR_DELIVERY or if seller already marked DELIVERED
        if (tracking.getTrackingStatus() != TrackingStatus.OUT_FOR_DELIVERY && tracking.getTrackingStatus() != TrackingStatus.DELIVERED) {
            throw new InvalidStageTransitionException("Cannot confirm before OUT_FOR_DELIVERY");
        }

        if (tracking.isDeliveryConfirmed()) {
            throw new ConflictException("Delivery already confirmed");
        }

        tracking.setDeliveryConfirmed(true);
        tracking.setTrackingStatus(TrackingStatus.CLOSED);
        tracking.setUpdatedAt(LocalDateTime.now());

        for (TrackingStage stage : tracking.getTrackingStages()) {
            if (stage.getStage() == TrackingStatus.DELIVERED) {
                stage.setCompleted(true);
                stage.setTimestamp(LocalDateTime.now());
            }
        }

        // Also update the main order status
        Order order = orderRepository.findById(orderId).orElse(null);
        if (order != null) {
            order.setDeliveryConfirmed(true);
            order.setTrackingStatus(TrackingStatus.CLOSED);
            order.setStatus("COMPLETED");
            order.setUpdatedAt(LocalDateTime.now());
            orderRepository.save(order);
        }

        return mapToDto(orderTrackingRepository.save(tracking));
    }

    private OrderTrackingDto mapToDto(OrderTracking entity) {
        List<TrackingStage> sortedStages = entity.getTrackingStages().stream()
                .sorted(Comparator.comparing(stage -> stage.getStage().ordinal()))
                .collect(Collectors.toList());

        return OrderTrackingDto.builder()
                .orderId(entity.getOrderId())
                .status(entity.getTrackingStatus() != null ? entity.getTrackingStatus().name() : "")
                .stages(sortedStages)
                .courierName(entity.getCourierName())
                .trackingNumber(entity.getTrackingNumber())
                .deliveryConfirmed(entity.isDeliveryConfirmed())
                .build();
    }
}

