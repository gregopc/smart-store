package com.example.smartstore.service.impl;

import com.example.smartstore.domain.Product;
import com.example.smartstore.dto.ProductRequest;
import com.example.smartstore.dto.ProductUpdateRequest;
import com.example.smartstore.dto.ProductResponse;
import com.example.smartstore.exception.EntityNotFoundException;
import com.example.smartstore.mapper.ProductMapper;
import com.example.smartstore.repository.ProductRepository;
import com.example.smartstore.service.ProductService;
import com.example.smartstore.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

import java.util.function.Consumer;
import java.math.BigDecimal;

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
            throw new BusinessException("Stock cannot be negative");
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

    private <T> void updateIfPresent(T value, Consumer<T> setter) {
        if (value != null) setter.accept(value);
    }

    private void merge(Product existing, ProductUpdateRequest request) {
        updateIfPresent(request.getName(), existing::setName);
        updateIfPresent(request.getDescription(), existing::setDescription);
        updateIfPresent(request.getPrice(), existing::setPrice);
        updateIfPresent(request.getCategory(), existing::setCategory);
        updateIfPresent(request.getStock(), existing::setStock);
    }

    private void validateBusinessRulesPartial(ProductUpdateRequest request) {
        if (request.getPrice() != null &&
            request.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Price must be positive");
        }

        if (request.getStock() != null &&
            request.getStock() < 0) {
            throw new BusinessException("Stock cannot be negative");
        }
    }

    @Override
    public ProductResponse partialUpdateProduct(UUID id, ProductUpdateRequest request) {

        Product existing = repository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Product not found: " + id));

        merge(existing, request);

        validateBusinessRulesPartial(request);

        Product updated = repository.save(existing);
        return mapper.toResponse(updated);
    }

    @Override
    public void deleteProduct(UUID id) {
        if (!repository.existsById(id)) throw new EntityNotFoundException("Product not found: " + id);
        repository.deleteById(id);
    }
}
