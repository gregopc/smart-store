package com.example.smartstore.service.impl;

import com.example.smartstore.service.PaymentService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class MockPaymentService implements PaymentService {

    @Override
    public PaymentResult processPayment(BigDecimal amount, String paymentMethod) {
        if (paymentMethod == null || paymentMethod.isBlank()) {
            return new PaymentResult(false, null);
        }

        String transactionId = UUID.randomUUID().toString();
        return new PaymentResult(true, transactionId);
    }
}
