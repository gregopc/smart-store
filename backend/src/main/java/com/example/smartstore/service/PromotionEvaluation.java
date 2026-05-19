package com.example.smartstore.service;

import com.example.smartstore.dto.PromotionResponse;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class PromotionEvaluation {
    private BigDecimal subtotal;
    private BigDecimal discountTotal;
    private BigDecimal finalTotal;
    private PromotionResponse appliedPromotion;
    private List<PromotionResponse> suggestedPromotions;
}
