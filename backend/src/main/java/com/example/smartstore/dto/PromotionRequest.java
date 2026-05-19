package com.example.smartstore.dto;

import com.example.smartstore.domain.promotion.DiscountType;
import com.example.smartstore.domain.promotion.PromotionTriggerType;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class PromotionRequest {
    private String name;
    private String description;
    private DiscountType discountType;
    private BigDecimal discountValue;
    private PromotionTriggerType triggerType;
    private UUID targetProductId;
    private String targetCategory;
    private BigDecimal minimumCartTotal;
    private String searchTerm;
    private Integer minimumActionCount;
    private Integer stockThreshold;
    private Boolean active;
    private LocalDateTime startsAt;
    private LocalDateTime endsAt;
}
