package com.example.smartstore.service.impl;

import com.example.smartstore.domain.*;
import com.example.smartstore.dto.*;
import com.example.smartstore.exception.BusinessException;
import com.example.smartstore.repository.OrderRepository;
import com.example.smartstore.repository.ProductRepository;
import com.example.smartstore.repository.CartRepository;
import com.example.smartstore.service.OrderService;
import com.example.smartstore.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final CartRepository cartRepository;
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final PaymentService paymentService;

    @Override
    @Transactional
    public OrderResponse checkoutCart(User user, CheckoutRequest request) {
        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() -> new BusinessException("Cart not found for this user."));

        if (cart.getItems().isEmpty()) {
            throw new BusinessException("Cart is empty.");
        }

        if (request.getPaymentMethod() == null || request.getPaymentMethod().isBlank()) {
            throw new BusinessException("Payment method is required.");
        }

        Order order = new Order();
        order.setUser(user);
        order.setPaymentMethod(request.getPaymentMethod());
        order.setStatus(OrderStatus.PENDING);
        order.setShippingStreet(request.getShippingAddress() != null ? request.getShippingAddress().getStreet() : null);
        order.setShippingCity(request.getShippingAddress() != null ? request.getShippingAddress().getCity() : null);
        order.setShippingState(request.getShippingAddress() != null ? request.getShippingAddress().getState() : null);
        order.setShippingZip(request.getShippingAddress() != null ? request.getShippingAddress().getZip() : null);
        order.setShippingCountry(request.getShippingAddress() != null ? request.getShippingAddress().getCountry() : null);

        BigDecimal total = BigDecimal.ZERO;

        for (CartItem cartItem : cart.getItems()) {
            Product product = cartItem.getProduct();

            if (product.getStock() < cartItem.getQuantity()) {
                throw new BusinessException("Not enough stock for product: " + product.getName());
            }

            BigDecimal itemSubtotal = product.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity()));
            total = total.add(itemSubtotal);

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProductId(product.getId());
            orderItem.setProductName(product.getName());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setUnitPrice(product.getPrice());
            orderItem.setSubtotal(itemSubtotal);
            order.getItems().add(orderItem);
        }

        order.setTotal(total);
        order = orderRepository.save(order);

        PaymentService.PaymentResult paymentResult = paymentService.processPayment(total, request.getPaymentMethod());
        if (!paymentResult.isSuccess()) {
            order.setStatus(OrderStatus.FAILED);
            orderRepository.save(order);
            throw new BusinessException("Payment failed for order.");
        }

        order.setStatus(OrderStatus.PAID);
        orderRepository.save(order);

        cart.getItems().forEach(cartItem -> {
            Product product = cartItem.getProduct();
            product.setStock(product.getStock() - cartItem.getQuantity());
            productRepository.save(product);
        });

        cart.getItems().clear();
        cartRepository.save(cart);

        return mapToResponse(order);
    }

    private OrderResponse mapToResponse(Order order) {
        return OrderResponse.builder()
                .orderId(order.getId())
                .total(order.getTotal())
                .status(order.getStatus().name())
                .paymentMethod(order.getPaymentMethod())
                .shippingStreet(order.getShippingStreet())
                .shippingCity(order.getShippingCity())
                .shippingState(order.getShippingState())
                .shippingZip(order.getShippingZip())
                .shippingCountry(order.getShippingCountry())
                .createdAt(order.getCreatedAt())
                .items(order.getItems().stream()
                        .map(item -> OrderItemResponse.builder()
                                .itemId(item.getId())
                                .productId(item.getProductId())
                                .productName(item.getProductName())
                                .quantity(item.getQuantity())
                                .unitPrice(item.getUnitPrice())
                                .subtotal(item.getSubtotal())
                                .build())
                        .collect(Collectors.toList()))
                .build();
    }
}
