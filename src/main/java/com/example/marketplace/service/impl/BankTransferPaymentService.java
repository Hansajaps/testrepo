package com.example.marketplace.service.impl;

import com.example.marketplace.dto.PaymentRequest;
import com.example.marketplace.dto.PaymentResponse;
import com.example.marketplace.service.PaymentMethod;
import com.example.marketplace.service.PaymentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

/**
 * Bank Transfer Payment Service Implementation
 * 
 * Implements PaymentService for bank transfer payments.
 * 
 * Features:
 * - Registers bank transfer payment intents
 * - Generates unique payment reference for tracking
 * - Provides bank details for payment instructions
 * - No automatic payment processing (manual confirmation via bank transfer)
 * - Maintains existing bank transfer logic without modifications
 * 
 * Payment Flow:
 * 1. Customer selects Bank Transfer at checkout
 * 2. Backend generates unique payment reference
 * 3. Customer receives bank details and payment reference
 * 4. Customer initiates bank transfer with payment reference
 * 5. Admin verifies payment receipt from bank
 * 6. Admin marks order as COMPLETED
 * 
 * Bank Details:
 * Configure in application.properties:
 * - bank.account.holder=Your Company Name
 * - bank.account.number=XXXX XXXX XXXX
 * - bank.routing.number=XXXXXX
 * - bank.name=Bank Name
 */
@Service
@Slf4j
public class BankTransferPaymentService implements PaymentService {

    /**
     * Bank account information (should be configured in application.properties)
     */
    @Value("${bank.account.holder:Xchange Marketplace}")
    private String accountHolder;

    @Value("${bank.account.number:XXXXXXXXXXXXXX}")
    private String accountNumber;

    @Value("${bank.routing.number:XXXXXX}")
    private String routingNumber;

    @Value("${bank.name:Your Bank}")
    private String bankName;

    @Override
    public PaymentResponse processPayment(PaymentRequest paymentRequest) throws Exception {
        log.info("Processing Bank Transfer payment - Amount: {}, Currency: {}, Email: {}",
            paymentRequest.getAmount(), paymentRequest.getCurrency(), paymentRequest.getEmail());

        try {
            // Validate payment request
            if (paymentRequest.getAmount() == null || paymentRequest.getAmount().doubleValue() <= 0) {
                throw new IllegalArgumentException("Invalid payment amount");
            }

            // Generate unique payment reference for this transfer
            // Customer must include this reference in bank transfer memo for matching
            String paymentReference = "BTR-" + UUID.randomUUID().toString().substring(0, 12).toUpperCase();

            log.info("Bank Transfer payment registered - Reference: {}", paymentReference);

            // Build payment instructions message
            String bankInstructions = buildBankInstructions(paymentRequest, paymentReference);

            // Return response with bank details
            return PaymentResponse.builder()
                .success(true)
                .status("PENDING_TRANSFER")
                .transactionId(paymentReference)
                .paymentMethod(PaymentMethod.BANK_TRANSFER.getDisplayName())
                .amount(paymentRequest.getAmount().toPlainString())
                .currency(paymentRequest.getCurrency())
                .timestamp(Instant.now().toString())
                .message(bankInstructions)
                .build();

        } catch (Exception e) {
            log.error("Error processing Bank Transfer payment", e);
            return PaymentResponse.builder()
                .success(false)
                .status("FAILED")
                .errorCode("BANK_ERROR")
                .message("Failed to process bank transfer: " + e.getMessage())
                .paymentMethod(PaymentMethod.BANK_TRANSFER.getDisplayName())
                .amount(paymentRequest.getAmount().toPlainString())
                .currency(paymentRequest.getCurrency())
                .timestamp(Instant.now().toString())
                .build();
        }
    }

    @Override
    public PaymentMethod getSupportedPaymentMethod() {
        return PaymentMethod.BANK_TRANSFER;
    }

    @Override
    public boolean isAvailable() {
        // Bank transfer is available if account details are configured
        return accountNumber != null && !accountNumber.equals("XXXXXXXXXXXXXX");
    }

    /**
     * Build bank transfer instructions for the customer
     * Includes payment reference, bank details, and amount due
     * 
     * @param paymentRequest the payment request
     * @param paymentReference unique reference for this transfer
     * @return formatted bank instructions string
     */
    private String buildBankInstructions(PaymentRequest paymentRequest, String paymentReference) {
        return String.format(
            "Bank Transfer Instructions:\n" +
            "==============================\n" +
            "Please transfer %s %s to the following account:\n\n" +
            "Bank Name: %s\n" +
            "Account Holder: %s\n" +
            "Account Number: %s\n" +
            "Routing Number: %s\n\n" +
            "IMPORTANT: Use this reference in the memo/description field:\n" +
            "Reference: %s\n\n" +
            "Please allow 2-3 business days for the transfer to complete.\n" +
            "Your order will be confirmed once payment is received.",
            paymentRequest.getAmount().toPlainString(),
            paymentRequest.getCurrency(),
            bankName,
            accountHolder,
            accountNumber,
            routingNumber,
            paymentReference
        );
    }
}
