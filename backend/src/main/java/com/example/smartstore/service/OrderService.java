package com.example.smartstore.service;

import com.example.smartstore.dto.CheckoutRequest;
import com.example.smartstore.dto.OrderResponse;
import com.example.smartstore.domain.User;

public interface OrderService {
    OrderResponse checkoutCart(User user, CheckoutRequest request);
}
