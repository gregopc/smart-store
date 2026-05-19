package com.example.smartstore.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserActionEvent {
    private UUID eventId;
    private UserActionType eventType;
    private UUID userId;
    private String userEmail;
    private UUID productId;
    private UUID cartId;
    private UUID cartItemId;
    private String searchQuery;
    private String assistantMessage;
    private BigDecimal cartTotal;
    private String route;
    private Map<String, Object> metadata;
    private String occurredAt;
}
