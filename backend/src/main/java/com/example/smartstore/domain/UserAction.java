package com.example.smartstore.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "user_actions")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserAction {

    @Id
    @Column(name = "event_id", nullable = false, updatable = false)
    private UUID eventId;

    @Column(name = "event_type", nullable = false, length = 80)
    private String eventType;

    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "user_email")
    private String userEmail;

    @Column(name = "product_id")
    private UUID productId;

    @Column(name = "cart_id")
    private UUID cartId;

    @Column(name = "cart_item_id")
    private UUID cartItemId;

    @Column(name = "search_query", columnDefinition = "text")
    private String searchQuery;

    @Column(name = "assistant_message", columnDefinition = "text")
    private String assistantMessage;

    @Column(name = "cart_total", precision = 19, scale = 2)
    private BigDecimal cartTotal;

    @Column(name = "route", length = 180)
    private String route;

    @Column(name = "metadata_json", columnDefinition = "text")
    private String metadataJson;

    @Column(name = "raw_event_json", nullable = false, columnDefinition = "text")
    private String rawEventJson;

    @Column(name = "occurred_at")
    private Instant occurredAt;

    @Column(name = "consumed_at", nullable = false, updatable = false)
    private Instant consumedAt;

    @PrePersist
    public void prePersist() {
        if (consumedAt == null) {
            consumedAt = Instant.now();
        }
    }
}
