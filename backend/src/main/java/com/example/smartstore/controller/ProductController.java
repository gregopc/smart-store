package com.example.smartstore.controller;

import com.example.smartstore.dto.ProductRequest;
import com.example.smartstore.dto.ProductUpdateRequest;
import com.example.smartstore.dto.ProductResponse;
import com.example.smartstore.service.ProductService;
import com.example.smartstore.mapper.ProductMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;
import java.util.List;

@RestController
@RequestMapping("/products")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final ProductMapper mapper;

    @PostMapping
    public ResponseEntity<ProductResponse> create(@Valid @RequestBody ProductRequest request) {
        ProductResponse created = productService.createProduct(request);
        return ResponseEntity.created(URI.create("/products/" + created.getId())).body(created);
    }

    @GetMapping
    public ResponseEntity<Page<ProductResponse>> list(@RequestParam(defaultValue = "0") int page,
                                                      @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ProductResponse> result = productService.getAllProducts(pageable);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    @GetMapping("/search")
    public List<ProductResponse> search(@RequestParam String query) {
        return productService.findRelevantProducts(query)
                .stream()
                .map(mapper::toResponse)
                .toList();
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
