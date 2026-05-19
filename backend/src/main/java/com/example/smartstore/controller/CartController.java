package com.example.smartstore.controller;

import com.example.smartstore.dto.AddItemRequest;
import com.example.smartstore.dto.CartItemResponse;
import com.example.smartstore.dto.CartResponse;
import com.example.smartstore.dto.CheckoutRequest;
import com.example.smartstore.dto.OrderResponse;
import com.example.smartstore.domain.User;
import com.example.smartstore.event.UserActionEventPublisher;
import com.example.smartstore.event.UserActionType;
import com.example.smartstore.service.CartService;
import com.example.smartstore.service.OrderService;
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
    private final OrderService orderService;
    private final UserActionEventPublisher userActionEventPublisher;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cria ou recupera o carrinho do usuário logado")
    public CartResponse createOrGetCart(@AuthenticationPrincipal User user) {
        CartResponse response = cartService.createOrGetCart(user);
        userActionEventPublisher.publish(userActionEventPublisher
                .newEvent(UserActionType.CART_OPENED, user, "POST /carts")
                .cartId(response.getCartId())
                .cartTotal(response.getTotal())
                .metadata(Map.of("itemCount", response.getItems().size()))
                .build());
        return response;
    }

    @GetMapping("/my-cart")
    @Operation(summary = "Obtém os detalhes do carrinho do usuário logado")
    public CartResponse getMyCart(@AuthenticationPrincipal User user) {
        CartResponse response = cartService.getCartByUser(user);
        userActionEventPublisher.publish(userActionEventPublisher
                .newEvent(UserActionType.CART_VIEWED, user, "GET /carts/my-cart")
                .cartId(response.getCartId())
                .cartTotal(response.getTotal())
                .metadata(Map.of("itemCount", response.getItems().size()))
                .build());
        return response;
    }

    @PostMapping("/my-cart/items")
    @Operation(summary = "Adiciona um item ao carrinho")
    public CartResponse addItem(@AuthenticationPrincipal User user, @RequestBody AddItemRequest request) {
        CartResponse response = cartService.addItemToCart(user, request);
        userActionEventPublisher.publish(userActionEventPublisher
                .newEvent(UserActionType.PRODUCT_ADDED_TO_CART, user, "POST /carts/my-cart/items")
                .productId(request.getProductId())
                .cartId(response.getCartId())
                .cartTotal(response.getTotal())
                .metadata(Map.of(
                        "quantity", request.getQuantity(),
                        "itemCount", response.getItems().size()))
                .build());
        return response;
    }

    @PostMapping("/my-cart/checkout")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Finaliza o checkout do carrinho, usando mockPayment como método de pagamento")
    public OrderResponse checkout(@AuthenticationPrincipal User user, @RequestBody CheckoutRequest request) {
        return orderService.checkoutCart(user, request);
    }

    @DeleteMapping("/my-cart/items/{itemId}")
    @Operation(summary = "Remove um item do carrinho")
    public CartResponse removeItem(@AuthenticationPrincipal User user, @PathVariable UUID itemId) {
        CartResponse beforeRemoval = cartService.getCartByUser(user);
        CartItemResponse removedItem = beforeRemoval.getItems().stream()
                .filter(item -> item.getItemId().equals(itemId))
                .findFirst()
                .orElse(null);

        CartResponse response = cartService.removeItemFromCart(user, itemId);
        userActionEventPublisher.publish(userActionEventPublisher
                .newEvent(UserActionType.PRODUCT_REMOVED_FROM_CART, user, "DELETE /carts/my-cart/items/{itemId}")
                .cartItemId(itemId)
                .productId(removedItem != null ? removedItem.getProductId() : null)
                .cartId(response.getCartId())
                .cartTotal(response.getTotal())
                .metadata(Map.of(
                        "quantity", removedItem != null ? removedItem.getQuantity() : 0,
                        "itemCount", response.getItems().size()))
                .build());
        return response;
    }
}
