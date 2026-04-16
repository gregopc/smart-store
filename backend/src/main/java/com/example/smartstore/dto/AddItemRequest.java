package com.example.smartstore.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class AddItemRequest {
    private UUID productId;
    private Integer quantity;
}