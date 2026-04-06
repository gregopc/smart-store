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
import java.util.List;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.text.Normalizer;

import java.util.function.Consumer;
import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository repository;
    private final ProductMapper mapper;

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
    public List<Product> findRelevantProducts(String query) {
        List<Product> products = repository.findAll();

        String normalizedQuery = normalize(query);

        List<String> terms = Arrays.stream(normalizedQuery.split(" "))
                .filter(t -> !STOPWORDS.contains(t))
                .toList();

        return products.stream()
                .map(p -> Map.entry(p, score(p, terms)))
                .filter(entry -> entry.getValue() > 0)
                .sorted((a, b) -> Integer.compare(b.getValue(), a.getValue()))
                .limit(10)
                .map(Map.Entry::getKey)
                .toList();
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

    private void validateBusinessRules(ProductRequest request) {
        if (request.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Price must be positive");
        }

        if (request.getStock() < 0) {
            throw new BusinessException("Stock cannot be negative");
        }
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

    private static final Set<String> STOPWORDS = Set.of(
        "para", "com", "de", "o", "a", "e", "do", "da"
    );

    private int score(Product product, List<String> terms) {
        int score = 0;

        String name = normalize(product.getName());
        String description = normalize(product.getDescription());
        String category = normalize(product.getCategory());

        for (String term : terms) {
            if (name.contains(term)) score += 3;
            if (category.contains(term)) score += 2;
            if (description.contains(term)) score += 1;
        }

        return score;
    }

    private String normalize(String text) {
        if (text == null) return "";

        String normalized = Normalizer.normalize(text, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");

        return normalized
                .toLowerCase()
                .replaceAll("[^a-z0-9 ]", "");
    }
        
}
