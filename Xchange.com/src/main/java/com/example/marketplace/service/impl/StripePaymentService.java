package com.example.marketplace.service.impl;

import com.example.marketplace.dto.PaymentRequest;
import com.example.marketplace.dto.PaymentResponse;
import com.example.marketplace.service.PaymentMethod;
import com.example.marketplace.service.PaymentService;
import com.stripe.exception.*;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Stripe Payment Service Implementation
 * 
 * Implements PaymentService for Stripe card payments using Stripe Checkout (hosted payment page).
 * 
 * Features:
 * - Creates Checkout Sessions for secure hosted payment experience
 * - Automatic 3D Secure / SCA handling
 * - Mobile-optimized payment form
 * - Supports multiple payment methods (card, Apple Pay, Google Pay)
 * - Reduced PCI compliance burden (hosted by Stripe)
 * - Supports various currencies and amounts
 * - Handles Stripe API exceptions gracefully
 * - Logs all payment operations for audit trail
 * 
 * Payment Flow:
 * 1. Frontend initiates payment via POST /api/payments/stripe/checkout
 * 2. Backend creates Stripe Checkout Session with success/cancel URLs
 * 3. Frontend receives checkout URL and redirects user
 * 4. User enters payment details on Stripe's secure hosted page
 * 5. After payment, user redirected back to success URL
 * 6. Frontend verifies payment and creates order
 * 
 * Security Note:
 * - Never expose Stripe secret key to frontend
 * - All payment processing happens on Stripe's secure servers
 * - User data never enters your server
 * - Always validate payment session before confirming order
 */
@Service
@Slf4j
public class StripePaymentService implements PaymentService {

    /**
     * Is Stripe payment method enabled?
     * Can be toggled via application.properties
     */
    @Value("${stripe.enabled:true}")
    private boolean stripeEnabled;

    /**
     * Frontend base URL for redirect after payment
     * (Used to build success and cancel URLs)
     */
    @Value("${stripe.frontend.url:http://localhost:5173}")
    private String frontendUrl;

    /**
     * Optional: Stripe publishable key for logging/monitoring
     * (Not used for API calls, but useful for documentation)
     */
    @Value("${stripe.publishable.key:}")
    private String publishableKey;

    @Override
    public PaymentResponse processPayment(PaymentRequest paymentRequest) throws Exception {
        log.info("Creating Stripe Checkout Session - Amount: {}, Currency: {}, Email: {}", 
            paymentRequest.getAmount(), paymentRequest.getCurrency(), paymentRequest.getEmail());

        try {
            // Validate payment request
            validatePaymentRequest(paymentRequest);

            // Convert amount to smallest currency unit
            // Stripe expects amount as a long in smallest unit
            // For most currencies (USD, EUR, GBP, etc) multiply by 100 for cents
            // For zero-decimal currencies (LKR, JPY, KRW, etc) don't multiply
            long amountInSmallestUnit = convertAmountToSmallestUnit(
                paymentRequest.getAmount(),
                paymentRequest.getCurrency()
            );

            // Build Checkout Session parameters
            SessionCreateParams params = buildCheckoutSessionParams(
                amountInSmallestUnit, 
                paymentRequest
            );

            // Create Checkout Session with Stripe API
            Session session = Session.create(params);

            log.info("Checkout Session created successfully - ID: {}, URL: {}", 
                session.getId(), session.getUrl());

            // Build and return successful response
            return buildCheckoutSuccessResponse(session, paymentRequest);

        } catch (RateLimitException e) {
            // Too many requests to Stripe API
            log.warn("Rate limit exceeded: {}", e.getMessage());
            return buildErrorResponse(paymentRequest, "RATE_LIMIT", 
                "Too many payment requests. Please try again later.");

        } catch (AuthenticationException e) {
            // Invalid API key or authentication failure
            log.error("Stripe authentication failed: {}", e.getMessage());
            return buildErrorResponse(paymentRequest, "AUTH_FAILED", 
                "Payment service authentication failed. Please contact support.");

        } catch (ApiConnectionException e) {
            // Network error communicating with Stripe
            log.error("Connection error with Stripe: {}", e.getMessage());
            return buildErrorResponse(paymentRequest, "CONNECTION_ERROR", 
                "Unable to connect to payment service. Please try again later.");

        } catch (InvalidRequestException e) {
            // Invalid parameter or request format
            log.warn("Invalid request parameter: {}", e.getMessage());
            return buildErrorResponse(paymentRequest, "INVALID_REQUEST", e.getMessage());

        } catch (StripeException e) {
            // Generic Stripe exception
            log.error("Stripe API error: {}", e.getMessage(), e);
            return buildErrorResponse(paymentRequest, "STRIPE_ERROR", 
                "Payment processing error. Error ID: " + e.getStripeError().getCode());

        } catch (Exception e) {
            // Unexpected error
            log.error("Unexpected error during payment processing", e);
            return buildErrorResponse(paymentRequest, "UNKNOWN_ERROR", 
                "An unexpected error occurred during payment processing.");
        }
    }

    @Override
    public PaymentMethod getSupportedPaymentMethod() {
        return PaymentMethod.STRIPE;
    }

    @Override
    public boolean isAvailable() {
        return stripeEnabled;
    }

    /**
     * Convert amount to smallest currency unit based on currency type
     * 
     * Multiply by 100 to convert to cents/smallest unit for Stripe display.
     * Example: 1000 LKR → 100000 (displays as 1000.00 on Stripe checkout page)
     * 
     * @param amount the amount to convert (in currency's base unit)
     * @param currency the ISO 4217 currency code
     * @return amount multiplied by 100 for Stripe
     */
    private long convertAmountToSmallestUnit(BigDecimal amount, String currency) {
        // Always multiply by 100 for Stripe
        // Stripe will display: amount_in_smallest_unit / 100 = display_amount
        // Example: 100000 / 100 = 1000.00
        long result = amount.multiply(new BigDecimal("100")).longValue();
        
        log.info("Converting amount for currency {} - Original: {} → Stripe amount: {} (will display as {}.{})", 
            currency.toUpperCase(), 
            amount, 
            result,
            amount.longValue(),
            String.format("%02d", result % 100)
        );
        
        return result;
    }

    /**
     * Validate payment request data
     * Ensures all required fields are present and valid
     * 
     * @param paymentRequest the payment request to validate
     * @throws IllegalArgumentException if validation fails
     */
    private void validatePaymentRequest(PaymentRequest paymentRequest) {
        if (paymentRequest.getAmount() == null || paymentRequest.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Invalid payment amount");
        }

        if (paymentRequest.getCurrency() == null || paymentRequest.getCurrency().isEmpty()) {
            throw new IllegalArgumentException("Currency code is required");
        }

        if (paymentRequest.getDescription() == null || paymentRequest.getDescription().isEmpty()) {
            throw new IllegalArgumentException("Payment description is required");
        }

        if (paymentRequest.getEmail() == null || paymentRequest.getEmail().isEmpty()) {
            throw new IllegalArgumentException("Customer email is required");
        }

        // Validate amount is not too large (Stripe max is typically 99,999,999 in smallest unit)
        if (paymentRequest.getAmount().compareTo(new BigDecimal("999999.99")) > 0) {
            throw new IllegalArgumentException("Payment amount exceeds maximum limit");
        }
    }

    /**
     * Build SessionCreateParams for Stripe Checkout API
     * 
     * Stripe Checkout is the modern way to handle payments with:
     * - Hosted payment page (secure + professional looking)
     * - Built-in 3D Secure / SCA handling
     * - Mobile-optimized form
     * - Support for multiple payment methods (card, Apple Pay, Google Pay, etc)
     * 
     * @param amountInSmallestUnit amount in smallest currency unit
     * @param paymentRequest original payment request
     * @return configured SessionCreateParams
     */
    private SessionCreateParams buildCheckoutSessionParams(
        long amountInSmallestUnit, 
        PaymentRequest paymentRequest
    ) {
        // Build success URL (redirect after successful payment)
        String successUrl = frontendUrl + "/payment-success?session_id={CHECKOUT_SESSION_ID}";
        
        // Build cancel URL (redirect if user cancels payment)
        String cancelUrl = frontendUrl + "/payment-cancel?order_id=" + (paymentRequest.getOrderId() != null ? paymentRequest.getOrderId() : "");
        
        // Build metadata map for payment tracking
        Map<String, String> metadata = new HashMap<>();
        metadata.put("description", paymentRequest.getDescription());
        metadata.put("customer_email", paymentRequest.getEmail());
        
        if (paymentRequest.getOrderId() != null) {
            metadata.put("order_id", paymentRequest.getOrderId());
        }
        
        if (paymentRequest.getMetadata() != null) {
            metadata.put("custom_metadata", paymentRequest.getMetadata());
        }

        // Create Checkout Session parameters
        return SessionCreateParams.builder()
            .setMode(SessionCreateParams.Mode.PAYMENT)                // One-time payment
            .setSuccessUrl(successUrl)                               // Redirect URL after successful payment
            .setCancelUrl(cancelUrl)                                 // Redirect URL if user cancels
            .setCustomerEmail(paymentRequest.getEmail())             // Pre-populate customer email
            .putAllMetadata(metadata)                                // Attach metadata for tracking
            .addLineItem(                                            // Add product line item
                SessionCreateParams.LineItem.builder()
                    .setPriceData(
                        SessionCreateParams.LineItem.PriceData.builder()
                            .setUnitAmount(amountInSmallestUnit)      // Amount in smallest unit
                            .setCurrency(paymentRequest.getCurrency().toLowerCase())  // Currency (must be lowercase)
                            .setProductData(
                                SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                    .setName(paymentRequest.getDescription())  // Product name
                                    .build()
                            )
                            .build()
                    )
                    .setQuantity(1L)                                 // Quantity
                    .build()
            )
            .build();
    }

    /**
     * Build successful PaymentResponse from Checkout Session
     * Contains checkout URL where user should be redirected
     * 
     * @param session the created Checkout Session from Stripe
     * @param paymentRequest original payment request
     * @return PaymentResponse with checkout URL
     */
    private PaymentResponse buildCheckoutSuccessResponse(
        Session session, 
        PaymentRequest paymentRequest
    ) {
        return PaymentResponse.builder()
            .success(true)
            .status("pending")
            .transactionId(session.getId())
            .paymentIntentId(session.getPaymentIntentObject() != null ? session.getPaymentIntentObject().getId() : session.getId())
            .checkoutUrl(session.getUrl())                           // Frontend should redirect to this URL
            .paymentMethod(PaymentMethod.STRIPE.getDisplayName())
            .amount(paymentRequest.getAmount().toPlainString())
            .currency(paymentRequest.getCurrency())
            .timestamp(Instant.now().toString())
            .message("Checkout session created. Redirect user to the checkout URL.")
            .build();
    }

    /**
     * Build error PaymentResponse for failed payment processing
     * 
     * @param paymentRequest original payment request
     * @param errorCode Stripe error code
     * @param message error message for user
     * @return PaymentResponse with error details
     */
    private PaymentResponse buildErrorResponse(
        PaymentRequest paymentRequest, 
        String errorCode, 
        String message
    ) {
        return PaymentResponse.builder()
            .success(false)
            .status("FAILED")
            .errorCode(errorCode)
            .message(message)
            .paymentMethod(PaymentMethod.STRIPE.getDisplayName())
            .amount(paymentRequest.getAmount().toPlainString())
            .currency(paymentRequest.getCurrency())
            .timestamp(Instant.now().toString())
            .build();
    }
}
