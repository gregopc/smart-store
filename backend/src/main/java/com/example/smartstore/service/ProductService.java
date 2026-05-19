package com.example.smartstore.service;

import com.example.smartstore.domain.Product;
import com.example.smartstore.dto.ProductRequest;
import com.example.smartstore.dto.ProductUpdateRequest;
import com.example.smartstore.dto.ProductResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;
import java.util.List;
import java.math.BigDecimal;

public interface ProductService {
    ProductResponse createProduct(ProductRequest request);

    Page<ProductResponse> getAllProducts(
        String category,
        BigDecimal minPrice,
        BigDecimal maxPrice,
        Boolean inStock,
        Pageable pageable
    );

    ProductResponse getProductById(UUID id);
    
    Page<ProductResponse> searchProducts(
        String query,
        String category,
        BigDecimal minPrice,
        BigDecimal maxPrice,
        Boolean inStock,
        String sortBy,
        String sortDir,
        Pageable pageable
    );

    List<Product> findRelevantProductsForAssistant(String query);

    ProductResponse updateProduct(UUID id, ProductRequest request);
    ProductResponse partialUpdateProduct(UUID id, ProductUpdateRequest request);

    void deleteProduct(UUID id);
}
