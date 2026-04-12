package com.example.marketplace.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * PaymentResponse DTO - Contains the result of a payment processing request.
 * Response structure varies based on payment method and processing result.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {

    /**
     * Indicates whether the payment was processed successfully
     */
    private Boolean success;

    /**
     * Unique transaction ID/reference number for this payment
     * Used for tracking, refunds, and support inquiries
     */
    private String transactionId;

    /**
     * Current status of the payment
     * Examples: PENDING, SUCCESS, FAILED, REQUIRES_ACTION
     */
    private String status;

    /**
     * Message describing the result or any errors encountered
     */
    private String message;

    /**
     * For Stripe: Contains the client secret needed for frontend payment confirmation
     * Frontend uses this to complete the payment with Stripe.js
     * Null for other payment methods
     */
    @JsonProperty("client_secret")
    private String clientSecret;

    /**
     * For Stripe: PaymentIntent ID for tracking in Stripe dashboard
     * Can be used to retrieve payment status later
     */
    @JsonProperty("payment_intent_id")
    private String paymentIntentId;

    /**
     * Payment method used for this transaction
     */
    private String paymentMethod;

    /**
     * Amount processed (in smallest currency unit)
     */
    private String amount;

    /**
     * Currency code used for this payment
     */
    private String currency;

    /**
     * Timestamp when the payment was processed (ISO 8601 format)
     */
    private String timestamp;

    /**
     * For Stripe Checkout: URL where user should be redirected to complete payment
     * This is the hosted Stripe payment page
     * Null for other payment methods
     */
    @JsonProperty("checkout_url")
    private String checkoutUrl;

    /**
     * Additional information or error codes (for debugging/support)
     */
    private String errorCode;
}
