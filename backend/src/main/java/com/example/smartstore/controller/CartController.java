package com.example.smartstore.controller;

import com.example.smartstore.dto.AddItemRequest;
import com.example.smartstore.dto.CartResponse;
import com.example.smartstore.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@Tag(name = "Carts", description = "Operações do Carrinho de Compras")
@RestController
@RequestMapping("/api/carts")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cria um novo carrinho de compras")
    public CartResponse createCart() {
        return cartService.createCart();
    }

    @GetMapping("/{cartId}")
    @Operation(summary = "Obtém os detalhes de um carrinho")
    public CartResponse getCart(@PathVariable UUID cartId) {
        return cartService.getCartById(cartId);
    }

    @PostMapping("/{cartId}/items")
    @Operation(summary = "Adiciona um item ao carrinho")
    public CartResponse addItem(@PathVariable UUID cartId, @RequestBody AddItemRequest request) {
        return cartService.addItemToCart(cartId, request);
    }

    @DeleteMapping("/{cartId}/items/{itemId}")
    @Operation(summary = "Remove um item do carrinho")
    public CartResponse removeItem(@PathVariable UUID cartId, @PathVariable UUID itemId) {
        return cartService.removeItemFromCart(cartId, itemId);
    }
}