package com.example.marketplace.service;

import com.example.marketplace.service.impl.BankTransferPaymentService;
import com.example.marketplace.service.impl.CashOnDeliveryPaymentService;
import com.example.marketplace.service.impl.StripePaymentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * PaymentServiceFactory - Factory Pattern for payment services
 * 
 * Responsibilities:
 * - Maintains registry of all available payment services
 * - Selects appropriate payment service based on payment method
 * - Implements Strategy Pattern for extensible payment processing
 * - Follows Dependency Inversion Principle
 * 
 * Benefits:
 * - Easy to add new payment methods without modifying existing code
 * - Decouples payment selection logic from business logic
 * - Supports dynamic payment method selection at runtime
 * - Maintains centralized payment method configuration
 * 
 * Usage:
 * @Autowired
 * private PaymentServiceFactory paymentServiceFactory;
 * 
 * PaymentService service = paymentServiceFactory.getPaymentService("STRIPE");
 * PaymentResponse response = service.processPayment(request);
 */
@Component
@Slf4j
public class PaymentServiceFactory {

    /**
     * Registry of payment services
     * Maps payment method names to their service implementations
     */
    private final Map<PaymentMethod, PaymentService> paymentServices = new HashMap<>();

    /**
     * Constructor - Initializes factory with all payment service implementations
     * 
     * Autowiring dependencies ensures all payment services are registered
     * automatically when the application starts.
     */
    @Autowired
    public PaymentServiceFactory(
        StripePaymentService stripePaymentService,
        CashOnDeliveryPaymentService cashOnDeliveryPaymentService,
        BankTransferPaymentService bankTransferPaymentService
    ) {
        // Register all payment services with their corresponding payment methods
        paymentServices.put(PaymentMethod.STRIPE, stripePaymentService);
        paymentServices.put(PaymentMethod.CASH_ON_DELIVERY, cashOnDeliveryPaymentService);
        paymentServices.put(PaymentMethod.BANK_TRANSFER, bankTransferPaymentService);

        log.info("PaymentServiceFactory initialized with {} payment methods", paymentServices.size());
    }

    /**
     * Get payment service for a specific payment method
     * 
     * @param paymentMethod the payment method to get service for
     * @return PaymentService implementation for the specified method
     * @throws IllegalArgumentException if payment method is not supported or not available
     */
    public PaymentService getPaymentService(PaymentMethod paymentMethod) {
        if (paymentMethod == null) {
            throw new IllegalArgumentException("Payment method cannot be null");
        }

        PaymentService service = paymentServices.get(paymentMethod);

        if (service == null) {
            throw new IllegalArgumentException(
                "Payment service not found for method: " + paymentMethod.getDisplayName()
            );
        }

        if (!service.isAvailable()) {
            throw new IllegalArgumentException(
                "Payment method is not currently available: " + paymentMethod.getDisplayName()
            );
        }

        log.debug("Retrieved payment service for method: {}", paymentMethod.getDisplayName());
        return service;
    }

    /**
     * Get payment service by string payment method name
     * Convenience method that converts string to PaymentMethod enum
     * 
     * @param paymentMethodString the payment method as string
     * @return PaymentService implementation
     * @throws IllegalArgumentException if payment method string is invalid or service not found
     */
    public PaymentService getPaymentService(String paymentMethodString) {
        if (paymentMethodString == null || paymentMethodString.isEmpty()) {
            throw new IllegalArgumentException("Payment method cannot be null or empty");
        }

        try {
            PaymentMethod paymentMethod = PaymentMethod.fromString(paymentMethodString);
            return getPaymentService(paymentMethod);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid payment method: {}", paymentMethodString);
            throw e;
        }
    }

    /**
     * Get all available payment services
     * Useful for displaying available payment options to users
     * 
     * @return map of available payment methods and their service implementations
     */
    public Map<PaymentMethod, PaymentService> getAllAvailablePaymentServices() {
        Map<PaymentMethod, PaymentService> available = new HashMap<>();
        
        paymentServices.forEach((method, service) -> {
            if (service.isAvailable()) {
                available.put(method, service);
            }
        });

        return available;
    }

    /**
     * Check if a payment method is supported and available
     * 
     * @param paymentMethod the payment method to check
     * @return true if payment method is available for use
     */
    public boolean isPaymentMethodAvailable(PaymentMethod paymentMethod) {
        if (paymentMethod == null) {
            return false;
        }

        PaymentService service = paymentServices.get(paymentMethod);
        return service != null && service.isAvailable();
    }
}
