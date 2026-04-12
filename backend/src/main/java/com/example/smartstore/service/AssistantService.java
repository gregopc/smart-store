package com.example.smartstore.service;

import com.example.smartstore.ai.AIClient;
import com.example.smartstore.domain.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AssistantService {

    private final ProductService productService;
    private final AIClient aiClient;

    public String chat(String message) {

        String rawKeywords = aiClient.extractKeywords(message);

        String searchQuery = cleanKeywords(rawKeywords);

        List<Product> products = productService.findRelevantProducts(searchQuery);

        String prompt = buildPrompt(message, products);

        return aiClient.generateReply(prompt);
    }

    private String cleanKeywords(String raw) {
        return raw
            .toLowerCase()
            .replaceAll("[^a-z0-9 ]", " ")
            .replaceAll("\\s+", " ")
            .trim();
    }

    private String buildPrompt(String message, List<Product> products) {

        StringBuilder sb = new StringBuilder();

        sb.append("Usuário disse: ").append(message).append("\n\n");

        sb.append("Produtos disponíveis:\n");

        for (int i = 0; i < products.size(); i++) {
            Product p = products.get(i);

            sb.append(i + 1).append(". ")
              .append(p.getName())
              .append(" - ")
              .append(p.getDescription())
              .append("\n");
        }

        sb.append("""
        
        Instruções:
        - Você é um assistente de compras de supermercado
        - Recomende produtos com base na lista
        - Compare opções quando houver mais de uma
        - Não invente produtos
        - Seja claro e objetivo
        """);

        return sb.toString();
    }
}