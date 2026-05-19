package com.example.smartstore.service;

import com.example.smartstore.domain.Cart;
import com.example.smartstore.domain.CartItem;
import com.example.smartstore.domain.Promotion;
import com.example.smartstore.domain.User;
import com.example.smartstore.domain.promotion.DiscountType;
import com.example.smartstore.domain.promotion.PromotionTriggerType;
import com.example.smartstore.dto.PromotionResponse;
import com.example.smartstore.event.UserActionType;
import com.example.smartstore.repository.PromotionRepository;
import com.example.smartstore.repository.UserActionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PromotionEngine {

    private static final BigDecimal ONE_HUNDRED = new BigDecimal("100");

    private final PromotionRepository promotionRepository;
    private final UserActionRepository userActionRepository;

    public PromotionEvaluation evaluate(Cart cart, User user) {
        BigDecimal subtotal = calculateSubtotal(cart);
        List<PromotionResponse> eligiblePromotions = promotionRepository.findByActiveTrue().stream()
                .filter(this::isWithinActiveWindow)
                .map(promotion -> evaluatePromotion(promotion, cart, user, subtotal))
                .flatMap(Optional::stream)
                .sorted(Comparator.comparing(PromotionResponse::getEstimatedDiscount).reversed())
                .toList();

        PromotionResponse appliedPromotion = eligiblePromotions.stream()
                .filter(promotion -> promotion.getId().equals(cart.getAppliedPromotionId()))
                .findFirst()
                .orElse(null);

        BigDecimal discountTotal = appliedPromotion == null
                ? BigDecimal.ZERO
                : appliedPromotion.getEstimatedDiscount();
        BigDecimal finalTotal = subtotal.subtract(discountTotal).max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);

        List<PromotionResponse> suggestedPromotions = eligiblePromotions.stream()
                .filter(promotion -> appliedPromotion == null || !promotion.getId().equals(appliedPromotion.getId()))
                .limit(5)
                .toList();

        return PromotionEvaluation.builder()
                .subtotal(subtotal)
                .discountTotal(discountTotal)
                .finalTotal(finalTotal)
                .appliedPromotion(appliedPromotion)
                .suggestedPromotions(suggestedPromotions)
                .build();
    }

    public Optional<PromotionResponse> evaluatePromotion(Promotion promotion, Cart cart, User user, BigDecimal subtotal) {
        if (!isWithinActiveWindow(promotion)) {
            return Optional.empty();
        }

        BigDecimal discountBase = discountBaseFor(promotion, cart, subtotal);
        if (discountBase.compareTo(BigDecimal.ZERO) <= 0 || !isEligible(promotion, cart, user, subtotal)) {
            return Optional.empty();
        }

        BigDecimal discount = calculateDiscount(promotion, discountBase, subtotal);
        if (discount.compareTo(BigDecimal.ZERO) <= 0) {
            return Optional.empty();
        }

        return Optional.of(toResponse(promotion, discount, reasonFor(promotion)));
    }

    public BigDecimal calculateSubtotal(Cart cart) {
        return cart.getItems().stream()
                .map(item -> item.getProduct().getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }

    public PromotionResponse toResponse(Promotion promotion) {
        return toResponse(promotion, null, null);
    }

    private boolean isEligible(Promotion promotion, Cart cart, User user, BigDecimal subtotal) {
        if (promotion.getMinimumCartTotal() != null
                && subtotal.compareTo(promotion.getMinimumCartTotal()) < 0) {
            return false;
        }

        PromotionTriggerType triggerType = promotion.getTriggerType();
        return switch (triggerType) {
            case CART_TOTAL -> promotion.getMinimumCartTotal() == null
                    || subtotal.compareTo(promotion.getMinimumCartTotal()) >= 0;
            case CATEGORY -> cartContainsCategory(cart, promotion.getTargetCategory());
            case PRODUCT -> cartContainsProduct(cart, promotion.getTargetProductId());
            case REPEATED_PRODUCT_VIEW -> hasRepeatedProductViews(promotion, cart, user);
            case SEARCH_TERM -> hasSearchedForTerm(promotion, user);
            case HIGH_STOCK -> hasHighStockItem(promotion, cart);
        };
    }

    private BigDecimal discountBaseFor(Promotion promotion, Cart cart, BigDecimal subtotal) {
        return switch (promotion.getTriggerType()) {
            case CATEGORY -> categorySubtotal(cart, promotion.getTargetCategory());
            case PRODUCT -> productSubtotal(cart, promotion.getTargetProductId());
            case REPEATED_PRODUCT_VIEW -> promotion.getTargetProductId() == null
                    ? subtotal
                    : productSubtotal(cart, promotion.getTargetProductId());
            case HIGH_STOCK -> highStockSubtotal(cart, promotion);
            case CART_TOTAL, SEARCH_TERM -> subtotal;
        };
    }

    private BigDecimal calculateDiscount(Promotion promotion, BigDecimal discountBase, BigDecimal subtotal) {
        BigDecimal discount = promotion.getDiscountType() == DiscountType.PERCENTAGE
                ? discountBase.multiply(promotion.getDiscountValue()).divide(ONE_HUNDRED, 2, RoundingMode.HALF_UP)
                : promotion.getDiscountValue().min(discountBase);

        return discount.min(subtotal).setScale(2, RoundingMode.HALF_UP);
    }

    private boolean isWithinActiveWindow(Promotion promotion) {
        LocalDateTime now = LocalDateTime.now();
        return Boolean.TRUE.equals(promotion.getActive())
                && (promotion.getStartsAt() == null || !promotion.getStartsAt().isAfter(now))
                && (promotion.getEndsAt() == null || !promotion.getEndsAt().isBefore(now));
    }

    private boolean cartContainsCategory(Cart cart, String category) {
        if (category == null || category.isBlank()) {
            return false;
        }

        return cart.getItems().stream()
                .anyMatch(item -> category.equalsIgnoreCase(item.getProduct().getCategory()));
    }

    private boolean cartContainsProduct(Cart cart, UUID productId) {
        if (productId == null) {
            return false;
        }

        return cart.getItems().stream()
                .anyMatch(item -> productId.equals(item.getProduct().getId()));
    }

    private boolean hasRepeatedProductViews(Promotion promotion, Cart cart, User user) {
        if (user == null || user.getId() == null) {
            return false;
        }

        int minimumCount = promotion.getMinimumActionCount() == null ? 2 : promotion.getMinimumActionCount();

        if (promotion.getTargetProductId() != null) {
            return userActionRepository.countByUserIdAndEventTypeAndProductId(
                    user.getId(),
                    UserActionType.PRODUCT_VIEWED.name(),
                    promotion.getTargetProductId()) >= minimumCount;
        }

        return cart.getItems().stream()
                .anyMatch(item -> userActionRepository.countByUserIdAndEventTypeAndProductId(
                        user.getId(),
                        UserActionType.PRODUCT_VIEWED.name(),
                        item.getProduct().getId()) >= minimumCount);
    }

    private boolean hasSearchedForTerm(Promotion promotion, User user) {
        if (user == null || user.getId() == null
                || promotion.getSearchTerm() == null || promotion.getSearchTerm().isBlank()) {
            return false;
        }

        int minimumCount = promotion.getMinimumActionCount() == null ? 1 : promotion.getMinimumActionCount();
        return userActionRepository.countSearchesContaining(
                user.getId(),
                UserActionType.PRODUCT_SEARCHED.name(),
                promotion.getSearchTerm()) >= minimumCount;
    }

    private boolean hasHighStockItem(Promotion promotion, Cart cart) {
        int threshold = promotion.getStockThreshold() == null ? 50 : promotion.getStockThreshold();
        return cart.getItems().stream()
                .anyMatch(item -> itemMatchesPromotionTarget(item, promotion)
                        && item.getProduct().getStock() != null
                        && item.getProduct().getStock() >= threshold);
    }

    private BigDecimal categorySubtotal(Cart cart, String category) {
        if (category == null || category.isBlank()) {
            return BigDecimal.ZERO;
        }

        return cart.getItems().stream()
                .filter(item -> category.equalsIgnoreCase(item.getProduct().getCategory()))
                .map(this::itemSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal productSubtotal(Cart cart, UUID productId) {
        if (productId == null) {
            return BigDecimal.ZERO;
        }

        return cart.getItems().stream()
                .filter(item -> productId.equals(item.getProduct().getId()))
                .map(this::itemSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal highStockSubtotal(Cart cart, Promotion promotion) {
        int threshold = promotion.getStockThreshold() == null ? 50 : promotion.getStockThreshold();
        return cart.getItems().stream()
                .filter(item -> itemMatchesPromotionTarget(item, promotion))
                .filter(item -> item.getProduct().getStock() != null && item.getProduct().getStock() >= threshold)
                .map(this::itemSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private boolean itemMatchesPromotionTarget(CartItem item, Promotion promotion) {
        boolean productMatches = promotion.getTargetProductId() == null
                || promotion.getTargetProductId().equals(item.getProduct().getId());
        boolean categoryMatches = promotion.getTargetCategory() == null
                || promotion.getTargetCategory().isBlank()
                || promotion.getTargetCategory().equalsIgnoreCase(item.getProduct().getCategory());
        return productMatches && categoryMatches;
    }

    private BigDecimal itemSubtotal(CartItem item) {
        return item.getProduct().getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
    }

    private String reasonFor(Promotion promotion) {
        return switch (promotion.getTriggerType()) {
            case CART_TOTAL -> "Cart total promotion";
            case CATEGORY -> "Category promotion";
            case PRODUCT -> "Product promotion";
            case REPEATED_PRODUCT_VIEW -> "Based on repeated product views";
            case SEARCH_TERM -> "Based on previous searches";
            case HIGH_STOCK -> "Stock-based promotion";
        };
    }

    private PromotionResponse toResponse(Promotion promotion, BigDecimal estimatedDiscount, String reason) {
        return PromotionResponse.builder()
                .id(promotion.getId())
                .name(promotion.getName())
                .description(promotion.getDescription())
                .discountType(promotion.getDiscountType())
                .discountValue(promotion.getDiscountValue())
                .triggerType(promotion.getTriggerType())
                .targetProductId(promotion.getTargetProductId())
                .targetCategory(promotion.getTargetCategory())
                .minimumCartTotal(promotion.getMinimumCartTotal())
                .searchTerm(promotion.getSearchTerm())
                .minimumActionCount(promotion.getMinimumActionCount())
                .stockThreshold(promotion.getStockThreshold())
                .active(promotion.getActive())
                .startsAt(promotion.getStartsAt())
                .endsAt(promotion.getEndsAt())
                .estimatedDiscount(estimatedDiscount)
                .reason(reason)
                .build();
    }
}
