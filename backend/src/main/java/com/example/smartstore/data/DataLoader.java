package com.example.smartstore.data;

import com.example.smartstore.domain.Product;
import com.example.smartstore.repository.ProductRepository;
import com.example.smartstore.repository.CartRepository;
import com.example.smartstore.repository.CartItemRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

@Component
public class DataLoader implements CommandLineRunner {

    private final ProductRepository productRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ObjectMapper objectMapper;

    public DataLoader(
            ProductRepository productRepository,
            CartRepository cartRepository,
            CartItemRepository cartItemRepository,
            ObjectMapper objectMapper) {
        this.productRepository = productRepository;
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    public void run(String... args) throws Exception {

        cartItemRepository.deleteAll();
        cartRepository.deleteAll();
        productRepository.deleteAll();

        // if (repository.count() > 0) return;

        InputStream inputStream = getClass()
                .getResourceAsStream("/products.json");

        if (inputStream == null) {
            throw new RuntimeException("products.json not found");
        }

        List<Product> products = Arrays.asList(
                objectMapper.readValue(inputStream, Product[].class));

        products.forEach(p -> {
            if (p.getStock() == null) {
                System.out.println("Produto sem stock: " + p.getName());
            }
        });

        productRepository.saveAll(products);
    }
}
