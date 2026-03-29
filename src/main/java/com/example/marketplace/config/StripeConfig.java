package com.example.marketplace.config;

import com.stripe.Stripe;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import jakarta.annotation.PostConstruct;

/**
 * Stripe Configuration Class
 * 
 * Responsible for:
 * - Loading Stripe API key from application properties
 * - Initializing Stripe SDK on application startup
 * - Ensuring Stripe API key is available for all payment operations
 * 
 * Security Note:
 * - The Stripe secret key is read from application.properties (externalized config)
 * - In production, use environment variables or AWS Secrets Manager for sensitive keys
 * - Never commit API keys to version control
 * 
 * Usage:
 * - Add to application.properties: stripe.secret.key=sk_test_xxxxx
 * - This bean is automatically initialized on application startup via @PostConstruct
 */
@Configuration
public class StripeConfig {

    /**
     * Stripe Secret Key - injected from application.properties
     * This key should not be exposed in frontend or logs
     * 
     * In application.properties:
     * stripe.secret.key=sk_test_your_secret_key_here
     */
    @Value("${stripe.secret.key}")
    private String stripeSecretKey;

    /**
     * Initialize Stripe SDK with the secret key on application startup.
     * This must be called before any Stripe API operations.
     * 
     * @throws IllegalStateException if stripeSecretKey is not configured
     */
    @PostConstruct
    public void init() {
        if (stripeSecretKey == null || stripeSecretKey.isEmpty()) {
            throw new IllegalStateException(
                "Stripe secret key not configured! " +
                "Please add 'stripe.secret.key' to application.properties"
            );
        }

        // Initialize Stripe SDK with the secret key
        Stripe.apiKey = stripeSecretKey;
        
        System.out.println("[Stripe] SDK initialized successfully");
    }

    /**
     * Get the configured Stripe secret key
     * Warning: Only use this method internally; never expose to frontend
     * 
     * @return the Stripe secret key
     */
    public String getStripeSecretKey() {
        return stripeSecretKey;
    }
}
