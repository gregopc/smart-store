package com.example.smartstore.controller;

import com.example.smartstore.dto.AddItemRequest;
import com.example.smartstore.dto.CartResponse;
import com.example.smartstore.domain.User;
import com.example.smartstore.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@Tag(name = "Carts", description = "Operações do Carrinho de Compras")
@RestController
@RequestMapping("/carts")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cria ou recupera o carrinho do usuário logado")
    public CartResponse createOrGetCart(@AuthenticationPrincipal User user) {
        return cartService.createOrGetCart(user);
    }

    @GetMapping("/my-cart")
    @Operation(summary = "Obtém os detalhes do carrinho do usuário logado")
    public CartResponse getMyCart(@AuthenticationPrincipal User user) {
        return cartService.getCartByUser(user);
    }

    @PostMapping("/my-cart/items")
    @Operation(summary = "Adiciona um item ao carrinho")
    public CartResponse addItem(@AuthenticationPrincipal User user, @RequestBody AddItemRequest request) {
        return cartService.addItemToCart(user, request);
    }

    @DeleteMapping("/my-cart/items/{itemId}")
    @Operation(summary = "Remove um item do carrinho")
    public CartResponse removeItem(@AuthenticationPrincipal User user, @PathVariable UUID itemId) {
        return cartService.removeItemFromCart(user, itemId);
    }
}