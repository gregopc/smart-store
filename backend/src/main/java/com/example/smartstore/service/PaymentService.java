package com.example.smartstore.service;

import java.math.BigDecimal;

public interface PaymentService {
    PaymentResult processPayment(BigDecimal amount, String paymentMethod);

    class PaymentResult {
        private final boolean success;
        private final String transactionId;

        public PaymentResult(boolean success, String transactionId) {
            this.success = success;
            this.transactionId = transactionId;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getTransactionId() {
            return transactionId;
        }
    }
}
