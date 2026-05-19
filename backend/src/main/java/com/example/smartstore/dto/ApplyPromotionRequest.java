package com.example.smartstore.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class ApplyPromotionRequest {
    private UUID promotionId;
}
