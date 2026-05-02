package com.example.smartstore.dto;

import jakarta.validation.constraints.Min;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductUpdateRequest {

    private String name;

    private String description;

    private BigDecimal price;

    private String category;

    private String imageUrl;

    @Min(0)
    private Integer stock;
}