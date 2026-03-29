package com.example.marketplace.service.impl;

import com.example.marketplace.dto.PaymentRequest;
import com.example.marketplace.dto.PaymentResponse;
import com.example.marketplace.service.PaymentMethod;
import com.example.marketplace.service.PaymentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

/**
 * Cash On Delivery Payment Service Implementation
 * 
 * Implements PaymentService for COD payments.
 * 
 * Features:
 * - Registers cash on delivery orders
 * - No actual payment processing (payment collected at delivery)
 * - Generates transaction reference for tracking
 * - Maintains existing COD logic without modifications
 * 
 * Payment Flow:
 * 1. Customer selects Cash on Delivery at checkout
 * 2. Backend registers payment intent (no actual charge)
 * 3. Order is placed in PENDING status
 * 4. Delivery agent collects payment upon delivery
 * 5. Seller marks order as COMPLETED after payment received
 */
@Service
@Slf4j
public class CashOnDeliveryPaymentService implements PaymentService {

    @Override
    public PaymentResponse processPayment(PaymentRequest paymentRequest) throws Exception {
        log.info("Processing Cash On Delivery payment - Amount: {}, Currency: {}, Email: {}",
            paymentRequest.getAmount(), paymentRequest.getCurrency(), paymentRequest.getEmail());

        try {
            // Validate payment request
            if (paymentRequest.getAmount() == null || paymentRequest.getAmount().doubleValue() <= 0) {
                throw new IllegalArgumentException("Invalid payment amount");
            }

            // Generate unique transaction reference for COD tracking
            String transactionId = "COD-" + UUID.randomUUID().toString();

            log.info("Cash On Delivery payment registered - ID: {}", transactionId);

            // Return success response
            return PaymentResponse.builder()
                .success(true)
                .status("REGISTERED")
                .transactionId(transactionId)
                .paymentMethod(PaymentMethod.CASH_ON_DELIVERY.getDisplayName())
                .amount(paymentRequest.getAmount().toPlainString())
                .currency(paymentRequest.getCurrency())
                .timestamp(Instant.now().toString())
                .message("Cash on Delivery payment registered. Payment will be collected at delivery.")
                .build();

        } catch (Exception e) {
            log.error("Error processing Cash On Delivery payment", e);
            return PaymentResponse.builder()
                .success(false)
                .status("FAILED")
                .errorCode("COD_ERROR")
                .message("Failed to register Cash on Delivery payment: " + e.getMessage())
                .paymentMethod(PaymentMethod.CASH_ON_DELIVERY.getDisplayName())
                .amount(paymentRequest.getAmount().toPlainString())
                .currency(paymentRequest.getCurrency())
                .timestamp(Instant.now().toString())
                .build();
        }
    }

    @Override
    public PaymentMethod getSupportedPaymentMethod() {
        return PaymentMethod.CASH_ON_DELIVERY;
    }

    @Override
    public boolean isAvailable() {
        // COD is always available (no configuration needed)
        return true;
    }
}
