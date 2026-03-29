package com.example.marketplace.service;

/**
 * Enum representing all supported payment methods in the marketplace.
 * Uses Strategy Pattern for extensible payment processing.
 */
public enum PaymentMethod {
    CASH_ON_DELIVERY("Cash on Delivery"),
    BANK_TRANSFER("Bank Transfer"),
    STRIPE("Stripe Card Payment");

    private final String displayName;

    PaymentMethod(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * Get payment method by string value (case-insensitive)
     * @param value the string representation of the payment method
     * @return PaymentMethod enum value
     * @throws IllegalArgumentException if value doesn't match any payment method
     */
    public static PaymentMethod fromString(String value) {
        if (value == null) {
            throw new IllegalArgumentException("Payment method cannot be null");
        }
        try {
            return PaymentMethod.valueOf(value.toUpperCase().replace(" ", "_"));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unknown payment method: " + value);
        }
    }
}
