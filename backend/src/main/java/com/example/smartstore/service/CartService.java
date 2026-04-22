package com.example.smartstore.service;

import com.example.smartstore.dto.AddItemRequest;
import com.example.smartstore.dto.CartResponse;
import com.example.smartstore.domain.User;

import java.util.UUID;

public interface CartService {
    CartResponse createOrGetCart(User user);
    CartResponse getCartByUser(User user);
    CartResponse addItemToCart(User user, AddItemRequest request);
    CartResponse removeItemFromCart(User user, UUID itemId);
    CartResponse updateItemQuantity(User user, UUID itemId, Integer quantity);
}