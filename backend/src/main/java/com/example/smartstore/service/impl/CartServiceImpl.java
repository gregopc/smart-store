package com.example.smartstore.service.impl;

import com.example.smartstore.domain.Cart;
import com.example.smartstore.domain.CartItem;
import com.example.smartstore.domain.Product;
import com.example.smartstore.domain.User;
import com.example.smartstore.dto.AddItemRequest;
import com.example.smartstore.dto.CartItemResponse;
import com.example.smartstore.dto.CartResponse;
import com.example.smartstore.exception.BusinessException;
import com.example.smartstore.exception.EntityNotFoundException;
import com.example.smartstore.repository.CartItemRepository;
import com.example.smartstore.repository.CartRepository;
import com.example.smartstore.repository.ProductRepository;
import com.example.smartstore.service.CartService;
import com.example.smartstore.service.PromotionEngine;
import com.example.smartstore.service.PromotionEvaluation;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final CartItemRepository cartItemRepository;
    private final PromotionEngine promotionEngine;

    @Override
    @Transactional
    public CartResponse createOrGetCart(User user) {
        Cart cart = cartRepository.findByUser(user).orElseGet(() -> {
            Cart newCart = new Cart();
            newCart.setUser(user);
            return cartRepository.save(newCart);
        });
        return mapToCartResponse(cart);
    }

    @Override
    @Transactional(readOnly = true)
    public CartResponse getCartByUser(User user) {
        Cart cart = findCartByUser(user);
        return mapToCartResponse(cart);
    }

    @Override
    @Transactional
    public CartResponse addItemToCart(User user, AddItemRequest request) {
        Cart cart = findCartByUser(user);
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new EntityNotFoundException("Product not found: " + request.getProductId()));

        if (request.getQuantity() <= 0) {
            throw new BusinessException("Quantity must be positive.");
        }

        CartItem item = cartItemRepository.findByCartIdAndProductId(cart.getId(), product.getId())
                .orElseGet(() -> {
                    CartItem newItem = new CartItem();
                    newItem.setCart(cart);
                    newItem.setProduct(product);
                    cart.getItems().add(newItem);
                    return newItem;
                });

        int newQuantity = item.getQuantity() == null ? request.getQuantity() : item.getQuantity() + request.getQuantity();

        if (product.getStock() < newQuantity) {
            throw new BusinessException("Not enough stock for product: " + product.getName());
        }

        item.setQuantity(newQuantity);
        cartItemRepository.save(item);

        return mapToCartResponse(cart);
    }

    @Override
    @Transactional
    public CartResponse removeItemFromCart(User user, UUID itemId) {
        Cart cart = findCartByUser(user);
        CartItem itemToRemove = cart.getItems().stream()
                .filter(item -> item.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Item not found in cart: " + itemId));

        cart.getItems().remove(itemToRemove);
        cartRepository.save(cart);

        return mapToCartResponse(cart);
    }

    @Override
    @Transactional
    public CartResponse updateItemQuantity(User user, UUID itemId, Integer quantity) {
        if (quantity <= 0) {
            return removeItemFromCart(user, itemId);
        }

        Cart cart = findCartByUser(user);
        CartItem itemToUpdate = cart.getItems().stream()
                .filter(item -> item.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Item not found in cart: " + itemId));

        if (itemToUpdate.getProduct().getStock() < quantity) {
            throw new BusinessException("Not enough stock for product: " + itemToUpdate.getProduct().getName());
        }

        itemToUpdate.setQuantity(quantity);
        cartItemRepository.save(itemToUpdate);

        return mapToCartResponse(cart);
    }

    private Cart findCartByUser(User user) {
        return cartRepository.findByUser(user)
                .orElseThrow(() -> new EntityNotFoundException("Cart not found for this user"));
    }

    private CartResponse mapToCartResponse(Cart cart) {
        List<CartItemResponse> itemResponses = cart.getItems().stream()
                .map(this::mapToCartItemResponse)
                .collect(Collectors.toList());

        BigDecimal subtotal = itemResponses.stream()
                .map(CartItemResponse::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        PromotionEvaluation promotionEvaluation = promotionEngine.evaluate(cart, cart.getUser());

        return CartResponse.builder()
                .cartId(cart.getId())
                .items(itemResponses)
                .subtotal(subtotal)
                .discountTotal(promotionEvaluation.getDiscountTotal())
                .finalTotal(promotionEvaluation.getFinalTotal())
                .total(promotionEvaluation.getFinalTotal())
                .appliedPromotion(promotionEvaluation.getAppliedPromotion())
                .suggestedPromotions(promotionEvaluation.getSuggestedPromotions())
                .build();
    }

    private CartItemResponse mapToCartItemResponse(CartItem item) {
        BigDecimal subtotal = item.getProduct().getPrice().multiply(new BigDecimal(item.getQuantity()));
        return CartItemResponse.builder()
                .itemId(item.getId())
                .productId(item.getProduct().getId())
                .productName(item.getProduct().getName())
                .quantity(item.getQuantity())
                .unitPrice(item.getProduct().getPrice())
                .subtotal(subtotal)
                .build();
    }
}
