package com.example.marketplace.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * PaymentRequest DTO - Contains all information needed to process a payment.
 * Includes validation constraints following Jakarta validation specification.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequest {

    /**
     * Payment amount in the specified currency (smallest unit, e.g., cents for USD)
     * Must be greater than 0
     */
    @NotNull(message = "Amount cannot be null")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;

    /**
     * ISO 4217 currency code (e.g., USD, EUR, GBP)
     * Required for payment processing
     */
    @NotBlank(message = "Currency code cannot be blank")
    @Size(min = 3, max = 3, message = "Currency code must be 3 characters (ISO 4217)")
    @Pattern(regexp = "^[A-Z]{3}$", message = "Currency code must be uppercase ISO 4217 code")
    private String currency;

    /**
     * Payment method to use
     * Must match one of the supported PaymentMethod values
     */
    @NotBlank(message = "Payment method cannot be blank")
    private String paymentMethod;

    /**
     * Description of the payment (for order/invoice reference)
     * Helps identify the transaction in payment records
     */
    @NotBlank(message = "Description cannot be blank")
    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    private String description;

    /**
     * Customer email address for payment confirmation and contact
     */
    @NotBlank(message = "Email cannot be blank")
    @Email(message = "Invalid email format")
    private String email;

    /**
     * Optional: Order ID reference for tracking
     */
    private String orderId;

    /**
     * Optional: Additional metadata for payment processing
     * Can be used for storing order details, tax info, etc.
     */
    private String metadata;
}
