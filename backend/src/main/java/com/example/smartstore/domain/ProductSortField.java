package com.example.smartstore.domain;

import com.example.smartstore.exception.BusinessException;

public enum ProductSortField {
    price, name, createdAt;

    public static ProductSortField fromString(String value) {
        try {
            return ProductSortField.valueOf(value);
        } catch (IllegalArgumentException e) {
            throw new BusinessException(
                "Invalid sort field: '" + value + "'. Allowed: price, name, createdAt"
            );
        }
    }
}