package com.example.smartstore.controller;

import com.example.smartstore.domain.User;
import com.example.smartstore.mapper.ProductMapper;
import com.example.smartstore.dto.ProductRequest;
import com.example.smartstore.dto.ProductUpdateRequest;
import com.example.smartstore.dto.ProductResponse;
import com.example.smartstore.event.UserActionEventPublisher;
import com.example.smartstore.event.UserActionType;
import com.example.smartstore.service.ProductService;
import com.example.smartstore.domain.ProductSortField;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/products")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final ProductMapper mapper;
    private final UserActionEventPublisher userActionEventPublisher;

    @PostMapping
    public ResponseEntity<ProductResponse> create(@Valid @RequestBody ProductRequest request) {
        ProductResponse created = productService.createProduct(request);
        return ResponseEntity.created(URI.create("/products/" + created.getId())).body(created);
    }

    @GetMapping
    public ResponseEntity<Page<ProductResponse>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) Boolean inStock,
            @RequestParam(required = false) String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            @AuthenticationPrincipal User user) {
        Sort sort = buildSort(sortBy, sortDir);
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<ProductResponse> result = productService.getAllProducts(category, minPrice, maxPrice, inStock, pageable);

        userActionEventPublisher.publish(userActionEventPublisher
                .newEvent(UserActionType.PRODUCT_LIST_VIEWED, user, "GET /products")
                .metadata(Map.of(
                        "page", page,
                        "size", size,
                        "resultCount", result.getNumberOfElements()))
                .build());

        return ResponseEntity.ok(result);
    }

    private Sort buildSort(String sortBy, String sortDir) {
        if (sortBy == null || sortBy.isBlank())
            return Sort.unsorted();

        ProductSortField.fromString(sortBy);

        Sort.Direction direction = "desc".equalsIgnoreCase(sortDir)
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;

        return Sort.by(direction, sortBy);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getById(@PathVariable UUID id,
            @AuthenticationPrincipal User user) {
        ProductResponse response = productService.getProductById(id);
        userActionEventPublisher.publish(userActionEventPublisher
                .newEvent(UserActionType.PRODUCT_VIEWED, user, "GET /products/{id}")
                .productId(id)
                .metadata(Map.of(
                        "productName", response.getName(),
                        "category", response.getCategory()))
                .build());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search")
    public ResponseEntity<Page<ProductResponse>> search(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) Boolean inStock,
            @RequestParam(required = false) String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            @AuthenticationPrincipal User user) {

        Pageable pageable = PageRequest.of(page, size);
        Page<ProductResponse> results = productService.searchProducts(query, category, minPrice, maxPrice, inStock,
                sortBy, sortDir, pageable);
        userActionEventPublisher.publish(userActionEventPublisher
                .newEvent(UserActionType.PRODUCT_SEARCHED, user, "GET /products/search")
                .searchQuery(query)
                .metadata(Map.of("resultCount", results.getTotalElements()))
                .build());

        return ResponseEntity.ok(results);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductResponse> update(@PathVariable UUID id, @Valid @RequestBody ProductRequest request) {
        return ResponseEntity.ok(productService.updateProduct(id, request));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ProductResponse> partialUpdate(
            @PathVariable UUID id,
            @RequestBody ProductUpdateRequest request) {

        return ResponseEntity.ok(productService.partialUpdateProduct(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }
}
