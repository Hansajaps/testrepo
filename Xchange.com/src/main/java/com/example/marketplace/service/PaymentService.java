package com.example.marketplace.service;

import com.example.marketplace.dto.PaymentRequest;
import com.example.marketplace.dto.PaymentResponse;

/**
 * PaymentService Interface - Defines the contract for payment processing strategies.
 * 
 * This interface follows:
 * - Strategy Pattern: Different payment implementations without modifying client code
 * - Open/Closed Principle: Open for extension (new payment methods), closed for modification
 * - Dependency Inversion: Depend on abstractions, not concrete implementations
 * 
 * All payment method implementations must process payments according to their specific requirements.
 */
public interface PaymentService {

    /**
     * Process a payment request using the specific payment method.
     * 
     * @param paymentRequest contains payment details (amount, currency, description, email)
     * @return PaymentResponse with status, transaction ID, and client details if needed
     * @throws Exception if payment processing fails
     */
    PaymentResponse processPayment(PaymentRequest paymentRequest) throws Exception;

    /**
     * Get the payment method this service handles.
     * 
     * @return PaymentMethod enum value
     */
    PaymentMethod getSupportedPaymentMethod();

    /**
     * Check if this payment method is available/active.
     * 
     * @return true if payment method is available for use
     */
    boolean isAvailable();
}
