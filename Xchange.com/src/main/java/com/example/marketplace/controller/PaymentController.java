package com.example.marketplace.controller;

import com.example.marketplace.dto.PaymentRequest;
import com.example.marketplace.dto.PaymentResponse;
import com.example.marketplace.service.PaymentMethod;
import com.example.marketplace.service.PaymentService;
import com.example.marketplace.service.PaymentServiceFactory;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Payment Controller - REST API endpoints for payment processing
 * 
 * Provides endpoints to:
 * - Process payments using different payment methods
 * - Retrieve available payment methods
 * - Handle payment requests through the Strategy pattern
 * 
 * Security:
 * - All endpoints require authentication via JWT token
 * - Payment requests are validated before processing
 * - Sensitive data (API keys) are never exposed in responses
 * 
 * API Endpoints:
 * - POST /api/payments/process - Process payment via selected method
 * - POST /api/payments/stripe - Process Stripe payment (convenience endpoint)
 * - GET /api/payments/methods - Get available payment methods
 */
@RestController
@RequestMapping("/api/payments")
@Slf4j
public class PaymentController {

    @Autowired
    private PaymentServiceFactory paymentServiceFactory;

    /**
     * Process payment using the selected payment method
     * 
     * This endpoint accepts a payment request with all necessary details
     * and routes it to the appropriate payment service based on the payment method.
     * 
     * Request body structure:
     * {
     *   "amount": 99.99,
     *   "currency": "USD",
     *   "paymentMethod": "STRIPE",
     *   "description": "Order payment for product ABC",
     *   "email": "customer@example.com",
     *   "orderId": "order-123",
     *   "metadata": "optional-data"
     * }
     * 
     * @param paymentRequest validated payment request with all required fields
     * @param auth Spring Security Authentication object (contains user email)
     * @return PaymentResponse with payment processing result and Stripe clientSecret if applicable
     * 
     * @throws IllegalArgumentException if payment method is not supported
     * @throws Exception if payment processing fails
     */
    @PostMapping("/process")
    public ResponseEntity<PaymentResponse> processPayment(
        @Valid @RequestBody PaymentRequest paymentRequest,
        Authentication auth
    ) {
        try {
            log.info("Payment request received - Method: {}, Amount: {}, User: {}",
                paymentRequest.getPaymentMethod(),
                paymentRequest.getAmount(),
                auth.getName()
            );

            // Get the appropriate payment service based on payment method
            PaymentService paymentService = paymentServiceFactory
                .getPaymentService(paymentRequest.getPaymentMethod());

            // Process the payment
            PaymentResponse response = paymentService.processPayment(paymentRequest);

            // Log the result
            if (response.getSuccess()) {
                log.info("Payment processed successfully - ID: {}", response.getTransactionId());
                return ResponseEntity.ok(response);
            } else {
                log.warn("Payment processing failed - Error: {}", response.getErrorCode());
                return ResponseEntity.badRequest().body(response);
            }

        } catch (IllegalArgumentException e) {
            log.error("Invalid payment request: {}", e.getMessage());
            return ResponseEntity.badRequest().body(
                PaymentResponse.builder()
                    .success(false)
                    .status("FAILED")
                    .errorCode("INVALID_REQUEST")
                    .message(e.getMessage())
                    .build()
            );

        } catch (Exception e) {
            log.error("Error processing payment", e);
            return ResponseEntity.internalServerError().body(
                PaymentResponse.builder()
                    .success(false)
                    .status("FAILED")
                    .errorCode("PAYMENT_ERROR")
                    .message("An error occurred while processing the payment: " + e.getMessage())
                    .build()
            );
        }
    }

    /**
     * Convenience endpoint for Stripe payments
     * 
     * This endpoint simplifies Stripe payment processing by setting the payment method.
     * Client only needs to provide: amount, currency, description, email, orderId (optional)
     * 
     * Request body structure:
     * {
     *   "amount": 99.99,
     *   "currency": "USD",
     *   "description": "Order payment for product ABC",
     *   "email": "customer@example.com",
     *   "orderId": "order-123"
     * }
     * 
     * Response includes:
     * - clientSecret: Required by frontend to confirm payment with Stripe.js
     * - paymentIntentId: Stripe's PaymentIntent ID for tracking
     * - transactionId: Same as paymentIntentId for consistency
     * 
     * Frontend usage with Stripe.js:
     * ```javascript
     * const {error} = await stripe.confirmCardPayment(clientSecret, {
     *   payment_method: {
     *     card: cardElement,
     *     billing_details: {name: 'Customer Name'}
     *   }
     * });
     * ```
     * 
     * @param paymentRequest payment request (without paymentMethod, will be set to STRIPE)
     * @param auth Spring Security Authentication object
     * @return PaymentResponse with Stripe clientSecret and PaymentIntent details
     */
    @PostMapping("/stripe")
    public ResponseEntity<PaymentResponse> processStripePayment(
        @Valid @RequestBody PaymentRequest paymentRequest,
        Authentication auth
    ) {
        try {
            log.info("Stripe payment request received - Amount: {}, User: {}",
                paymentRequest.getAmount(),
                auth.getName()
            );

            // Set payment method to Stripe
            paymentRequest.setPaymentMethod(PaymentMethod.STRIPE.name());

            // Use the main payment processing endpoint
            return processPayment(paymentRequest, auth);

        } catch (Exception e) {
            log.error("Error processing Stripe payment", e);
            return ResponseEntity.internalServerError().body(
                PaymentResponse.builder()
                    .success(false)
                    .status("FAILED")
                    .errorCode("STRIPE_ERROR")
                    .message("An error occurred while processing Stripe payment: " + e.getMessage())
                    .build()
            );
        }
    }

    /**
     * Get list of available payment methods
     * 
     * Returns all active payment methods that customers can use.
     * Each payment method includes its display name and availability status.
     * 
     * Response structure:
     * {
     *   "availableMethods": [
     *     {
     *       "name": "STRIPE",
     *       "displayName": "Stripe Card Payment"
     *     },
     *     {
     *       "name": "CASH_ON_DELIVERY",
     *       "displayName": "Cash on Delivery"
     *     },
     *     ...
     *   ]
     * }
     * 
     * @return HTTP 200 with list of available payment methods
     */
    @GetMapping("/methods")
    public ResponseEntity<Map<String, Object>> getAvailablePaymentMethods() {
        try {
            log.info("Fetching available payment methods");

            // Get all available payment methods
            Map<PaymentMethod, PaymentService> availableMethods = 
                paymentServiceFactory.getAllAvailablePaymentServices();

            // Format response
            List<Map<String, String>> methodsList = availableMethods.entrySet().stream()
                .map(entry -> {
                    Map<String, String> method = new HashMap<>();
                    method.put("name", entry.getKey().name());
                    method.put("displayName", entry.getKey().getDisplayName());
                    return method;
                })
                .collect(Collectors.toList());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("count", methodsList.size());
            response.put("availableMethods", methodsList);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error fetching payment methods", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error fetching payment methods: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * Check if a specific payment method is available
     * 
     * @param paymentMethod the payment method to check
     * @return HTTP 200 with availability status
     */
    @GetMapping("/methods/{paymentMethod}/available")
    public ResponseEntity<Map<String, Object>> isPaymentMethodAvailable(
        @PathVariable String paymentMethod
    ) {
        try {
            log.info("Checking availability of payment method: {}", paymentMethod);

            PaymentMethod method = PaymentMethod.fromString(paymentMethod);
            boolean available = paymentServiceFactory.isPaymentMethodAvailable(method);

            Map<String, Object> response = new HashMap<>();
            response.put("paymentMethod", method.name());
            response.put("displayName", method.getDisplayName());
            response.put("available", available);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.warn("Invalid payment method: {}", paymentMethod);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("available", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);

        } catch (Exception e) {
            log.error("Error checking payment method availability", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("available", false);
            errorResponse.put("error", "Error checking availability: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * Verify Stripe Checkout Session (after payment redirect)
     * 
     * Called by frontend after user is redirected from Stripe Checkout.
     * Verifies the session and returns payment details for order creation.
     * 
     * Request body:
     * {
     *   "session_id": "cs_test_..."
     * }
     * 
     * Response:
     * {
     *   "success": true,
     *   "status": "paid",
     *   "payment_intent": "pi_...",
     *   "metadata": {...},
     *   "message": "Payment verified successfully"
     * }
     * 
     * @param request request containing session_id
     * @param auth Spring Security Authentication object
     * @return verified payment info or error
     */
    @PostMapping("/stripe/verify")
    public ResponseEntity<Map<String, Object>> verifyStripeCheckoutSession(
        @RequestBody Map<String, String> request,
        Authentication auth
    ) {
        try {
            String sessionId = request.get("session_id");
            
            if (sessionId == null || sessionId.isEmpty()) {
                throw new IllegalArgumentException("session_id is required");
            }

            log.info("Verifying Stripe Checkout Session: {}, User: {}", sessionId, auth.getName());

            // Retrieve session from Stripe API
            com.stripe.model.checkout.Session session = com.stripe.model.checkout.Session.retrieve(sessionId);

            if (session == null) {
                throw new Exception("Session not found");
            }

            // Check payment status
            String paymentStatus = session.getPaymentStatus();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", "paid".equals(paymentStatus));
            response.put("status", paymentStatus);
            response.put("session_id", session.getId());
            response.put("payment_intent", session.getPaymentIntent() != null ? 
                session.getPaymentIntent() : session.getId());
            
            // Include metadata if present
            if (session.getMetadata() != null) {
                response.put("metadata", session.getMetadata());
            }
            
            if ("paid".equals(paymentStatus)) {
                response.put("message", "Payment verified successfully");
                log.info("Stripe session verified - Status: {}, PaymentIntent: {}", 
                    paymentStatus, session.getPaymentIntent());
                return ResponseEntity.ok(response);
            } else {
                response.put("message", "Payment status: " + paymentStatus);
                log.warn("Stripe session status not paid - Status: {}", paymentStatus);
                return ResponseEntity.badRequest().body(response);
            }

        } catch (com.stripe.exception.StripeException e) {
            log.error("Stripe API error during session verification", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Stripe error: " + e.getMessage());
            errorResponse.put("error_code", e.getStripeError().getCode());
            return ResponseEntity.badRequest().body(errorResponse);

        } catch (Exception e) {
            log.error("Error verifying Stripe checkout session", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error verifying session: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
}
