package com.example.smartstore.service;

import com.example.smartstore.dto.AddItemRequest;
import com.example.smartstore.dto.CartResponse;

import java.util.UUID;

public interface CartService {
    CartResponse createCart();
    CartResponse getCartById(UUID cartId);
    CartResponse addItemToCart(UUID cartId, AddItemRequest request);
    CartResponse removeItemFromCart(UUID cartId, UUID itemId);
    CartResponse updateItemQuantity(UUID cartId, UUID itemId, Integer quantity);
}