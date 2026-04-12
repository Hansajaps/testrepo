package com.example.marketplace.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "orders")
public class Order {
    @Id
    private String id;
    private String buyerId; // email of the buyer
    private String sellerId; // email of the seller - for single product orders or per-item
    private List<OrderItem> items;
    private String productName; // For quick display
    private String productImage; // For quick display
    private BigDecimal totalPrice;
    @Builder.Default
    private String status = "PENDING"; // PENDING, ACCEPTED, DECLINED, COMPLETED, CANCELLED
    private String shippingAddress;
    private String buyerName;
    private String buyerPhone;
    private Integer rating; // 1-5 stars
    private String review;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // ===== Tracking Fields ("One Row" structure) =====
    private TrackingStatus trackingStatus;
    
    @Builder.Default
    private List<TrackingStage> trackingStages = new java.util.ArrayList<>();
    
    private String courierName;
    private String trackingNumber;
    private boolean deliveryConfirmed;

    // ===== Payment Tracking Fields =====
    /**
     * Payment method used for this order
     * Examples: STRIPE, CASH_ON_DELIVERY, BANK_TRANSFER
     */
    private String paymentMethod;

    /**
     * Current status of the payment
     * Examples: PENDING, SUCCESS, FAILED, COMPLETED
     * Note: Different from order status - tracks payment specifically
     */
    @Builder.Default
    private String paymentStatus = "PENDING"; // PENDING, SUCCESS, FAILED, COMPLETED

    /**
     * Unique transaction ID for payment tracking
     * Provided by payment provider (e.g., Stripe PaymentIntent ID)
     * Used for refunds, disputes, and reconciliation
     */
    private String transactionId;

    /**
     * Timestamp when payment was processed
     */
    private LocalDateTime paymentDate;

    /**
     * Additional payment metadata (for future use)
     * Can store payment-related details like receipt URLs, etc.
     */
    private String paymentMetadata;

    public void initTracking() {
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
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItem {
        private String productId;
        private String name;
        private BigDecimal price;
        private Integer quantity;
    }
}
