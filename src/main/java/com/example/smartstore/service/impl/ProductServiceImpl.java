package com.example.smartstore.service.impl;

import com.example.smartstore.domain.Product;
import com.example.smartstore.dto.ProductRequest;
import com.example.smartstore.dto.ProductResponse;
import com.example.smartstore.exception.EntityNotFoundException;
import com.example.smartstore.mapper.ProductMapper;
import com.example.smartstore.repository.ProductRepository;
import com.example.smartstore.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository repository;
    private final ProductMapper mapper;

    private void validateBusinessRules(ProductRequest request) {
        if (request.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Price must be positive");
        }

        if (request.getStock() < 0) {
            throw new IllegalArgumentException("Stock cannot be negative");
        }
    }

    @Override
    public ProductResponse createProduct(ProductRequest request) {
        validateBusinessRules(request);

        Product p = mapper.toEntity(request);
        Product saved = repository.save(p);
        return mapper.toResponse(saved);
    }

    @Override
    public Page<ProductResponse> getAllProducts(Pageable pageable) {
        return repository.findAll(pageable).map(mapper::toResponse);
    }

    @Override
    public ProductResponse getProductById(UUID id) {
        Product p = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("Product not found: " + id));
        return mapper.toResponse(p);
    }

    @Override
    public ProductResponse updateProduct(UUID id, ProductRequest request) {
        validateBusinessRules(request);

        Product existing = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("Product not found: " + id));

        existing.setName(request.getName());
        existing.setDescription(request.getDescription());
        existing.setPrice(request.getPrice());
        existing.setCategory(request.getCategory());
        existing.setStock(request.getStock());

        Product updated = repository.save(existing);
        return mapper.toResponse(updated);
    }

    @Override
    public void deleteProduct(UUID id) {
        if (!repository.existsById(id)) throw new EntityNotFoundException("Product not found: " + id);
        repository.deleteById(id);
    }
}
