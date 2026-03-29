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
    private BigDecimal totalPrice;
    @Builder.Default
    private String status = "PENDING"; // PENDING, ACCEPTED, DECLINED, COMPLETED, CANCELLED
    private String shippingAddress;
    private String buyerName;
    private String buyerPhone;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

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
