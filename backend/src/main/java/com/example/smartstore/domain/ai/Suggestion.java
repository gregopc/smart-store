package com.example.smartstore.domain.ai;

import java.util.List;

import com.example.smartstore.domain.Product;

public record Suggestion (List<Product> suggestedProducts, String reply) {}
