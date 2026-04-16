package com.example.smartstore.service.impl;

import com.example.smartstore.domain.Cart;
import com.example.smartstore.domain.CartItem;
import com.example.smartstore.domain.Product;
import com.example.smartstore.dto.AddItemRequest;
import com.example.smartstore.dto.CartItemResponse;
import com.example.smartstore.dto.CartResponse;
import com.example.smartstore.exception.BusinessException;
import com.example.smartstore.exception.EntityNotFoundException;
import com.example.smartstore.repository.CartItemRepository;
import com.example.smartstore.repository.CartRepository;
import com.example.smartstore.repository.ProductRepository;
import com.example.smartstore.service.CartService;
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

    @Override
    @Transactional
    public CartResponse createCart() {
        Cart newCart = new Cart();
        Cart savedCart = cartRepository.save(newCart);
        return mapToCartResponse(savedCart);
    }

    @Override
    @Transactional(readOnly = true)
    public CartResponse getCartById(UUID cartId) {
        Cart cart = findCartById(cartId);
        return mapToCartResponse(cart);
    }

    @Override
    @Transactional
    public CartResponse addItemToCart(UUID cartId, AddItemRequest request) {
        Cart cart = findCartById(cartId);
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new EntityNotFoundException("Product not found: " + request.getProductId()));

        if (request.getQuantity() <= 0) {
            throw new BusinessException("Quantity must be positive.");
        }

        CartItem item = cartItemRepository.findByCartIdAndProductId(cartId, product.getId())
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
    public CartResponse removeItemFromCart(UUID cartId, UUID itemId) {
        Cart cart = findCartById(cartId);
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
    public CartResponse updateItemQuantity(UUID cartId, UUID itemId, Integer quantity) {
        if (quantity <= 0) {
            return removeItemFromCart(cartId, itemId);
        }

        Cart cart = findCartById(cartId);
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

    private Cart findCartById(UUID cartId) {
        return cartRepository.findById(cartId)
                .orElseThrow(() -> new EntityNotFoundException("Cart not found: " + cartId));
    }

    private CartResponse mapToCartResponse(Cart cart) {
        List<CartItemResponse> itemResponses = cart.getItems().stream()
                .map(this::mapToCartItemResponse)
                .collect(Collectors.toList());

        BigDecimal total = itemResponses.stream()
                .map(CartItemResponse::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return CartResponse.builder()
                .cartId(cart.getId())
                .items(itemResponses)
                .total(total)
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