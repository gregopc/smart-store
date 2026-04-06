package com.example.smartstore.service;

import com.example.smartstore.domain.Product;
import com.example.smartstore.dto.ProductRequest;
import com.example.smartstore.dto.ProductUpdateRequest;
import com.example.smartstore.dto.ProductResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;
import java.util.List;

public interface ProductService {
    ProductResponse createProduct(ProductRequest request);

    Page<ProductResponse> getAllProducts(Pageable pageable);

    ProductResponse getProductById(UUID id);

    ProductResponse updateProduct(UUID id, ProductRequest request);

    ProductResponse partialUpdateProduct(UUID id, ProductUpdateRequest request);

    void deleteProduct(UUID id);

    List<Product> findRelevantProducts(String query);
}
