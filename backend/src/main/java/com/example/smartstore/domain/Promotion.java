package com.example.smartstore.domain;

import com.example.smartstore.domain.promotion.DiscountType;
import com.example.smartstore.domain.promotion.PromotionTriggerType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "promotions")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Promotion {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "text")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DiscountType discountType;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal discountValue;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PromotionTriggerType triggerType;

    @Column(name = "target_product_id")
    private UUID targetProductId;

    @Column(name = "target_category")
    private String targetCategory;

    @Column(name = "minimum_cart_total", precision = 19, scale = 2)
    private BigDecimal minimumCartTotal;

    @Column(name = "search_term")
    private String searchTerm;

    @Column(name = "minimum_action_count")
    private Integer minimumActionCount;

    @Column(name = "stock_threshold")
    private Integer stockThreshold;

    @Column(nullable = false)
    private Boolean active;

    @Column(name = "starts_at")
    private LocalDateTime startsAt;

    @Column(name = "ends_at")
    private LocalDateTime endsAt;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (active == null) {
            active = true;
        }
        createdAt = LocalDateTime.now();
    }
}
