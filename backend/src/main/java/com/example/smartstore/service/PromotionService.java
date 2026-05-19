package com.example.smartstore.service;

import com.example.smartstore.domain.Cart;
import com.example.smartstore.domain.Promotion;
import com.example.smartstore.domain.User;
import com.example.smartstore.domain.promotion.DiscountType;
import com.example.smartstore.domain.promotion.PromotionTriggerType;
import com.example.smartstore.dto.CartResponse;
import com.example.smartstore.dto.PromotionRequest;
import com.example.smartstore.dto.PromotionResponse;
import com.example.smartstore.exception.BusinessException;
import com.example.smartstore.exception.EntityNotFoundException;
import com.example.smartstore.repository.CartRepository;
import com.example.smartstore.repository.PromotionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PromotionService {

    private final PromotionRepository promotionRepository;
    private final CartRepository cartRepository;
    private final CartService cartService;
    private final PromotionEngine promotionEngine;

    @Transactional
    public PromotionResponse createPromotion(PromotionRequest request) {
        validate(request);

        Promotion promotion = Promotion.builder()
                .name(request.getName())
                .description(request.getDescription())
                .discountType(request.getDiscountType())
                .discountValue(request.getDiscountValue())
                .triggerType(request.getTriggerType())
                .targetProductId(request.getTargetProductId())
                .targetCategory(request.getTargetCategory())
                .minimumCartTotal(request.getMinimumCartTotal())
                .searchTerm(request.getSearchTerm())
                .minimumActionCount(request.getMinimumActionCount())
                .stockThreshold(request.getStockThreshold())
                .active(request.getActive() == null || request.getActive())
                .startsAt(request.getStartsAt())
                .endsAt(request.getEndsAt())
                .build();

        return promotionEngine.toResponse(promotionRepository.save(promotion));
    }

    @Transactional(readOnly = true)
    public List<PromotionResponse> getActivePromotions() {
        return promotionRepository.findByActiveTrue().stream()
                .map(promotionEngine::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PromotionResponse> getEligiblePromotions(User user) {
        Cart cart = findCartByUser(user);
        return promotionEngine.evaluate(cart, user).getSuggestedPromotions();
    }

    @Transactional(readOnly = true)
    public PromotionEvaluation evaluateCart(User user) {
        Cart cart = findCartByUser(user);
        return promotionEngine.evaluate(cart, user);
    }

    @Transactional
    public CartResponse applyPromotion(User user, UUID promotionId) {
        if (promotionId == null) {
            throw new BusinessException("Promotion id is required.");
        }

        Cart cart = findCartByUser(user);
        Promotion promotion = promotionRepository.findById(promotionId)
                .orElseThrow(() -> new EntityNotFoundException("Promotion not found: " + promotionId));

        BigDecimal subtotal = promotionEngine.calculateSubtotal(cart);
        boolean eligible = promotionEngine.evaluatePromotion(promotion, cart, user, subtotal).isPresent();
        if (!eligible) {
            throw new BusinessException("Promotion is not eligible for this cart.");
        }

        cart.setAppliedPromotionId(promotionId);
        cartRepository.save(cart);
        return cartService.getCartByUser(user);
    }

    @Transactional
    public CartResponse removeAppliedPromotion(User user) {
        Cart cart = findCartByUser(user);
        cart.setAppliedPromotionId(null);
        cartRepository.save(cart);
        return cartService.getCartByUser(user);
    }

    private Cart findCartByUser(User user) {
        return cartRepository.findByUser(user)
                .orElseThrow(() -> new EntityNotFoundException("Cart not found for this user"));
    }

    private void validate(PromotionRequest request) {
        if (request.getName() == null || request.getName().isBlank()) {
            throw new BusinessException("Promotion name is required.");
        }

        if (request.getDiscountType() == null) {
            throw new BusinessException("Discount type is required.");
        }

        if (request.getTriggerType() == null) {
            throw new BusinessException("Promotion trigger type is required.");
        }

        if (request.getDiscountValue() == null || request.getDiscountValue().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Discount value must be positive.");
        }

        if (request.getDiscountType() == DiscountType.PERCENTAGE
                && request.getDiscountValue().compareTo(new BigDecimal("100")) > 0) {
            throw new BusinessException("Percentage discount cannot be greater than 100.");
        }

        if (request.getMinimumCartTotal() != null && request.getMinimumCartTotal().compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("Minimum cart total cannot be negative.");
        }

        if (request.getMinimumActionCount() != null && request.getMinimumActionCount() <= 0) {
            throw new BusinessException("Minimum action count must be positive.");
        }

        if (request.getStockThreshold() != null && request.getStockThreshold() < 0) {
            throw new BusinessException("Stock threshold cannot be negative.");
        }

        if (request.getTriggerType() == PromotionTriggerType.CATEGORY
                && (request.getTargetCategory() == null || request.getTargetCategory().isBlank())) {
            throw new BusinessException("Target category is required for category promotions.");
        }

        if (request.getTriggerType() == PromotionTriggerType.PRODUCT
                && request.getTargetProductId() == null) {
            throw new BusinessException("Target product id is required for product promotions.");
        }

        if (request.getTriggerType() == PromotionTriggerType.SEARCH_TERM
                && (request.getSearchTerm() == null || request.getSearchTerm().isBlank())) {
            throw new BusinessException("Search term is required for search term promotions.");
        }
    }
}
