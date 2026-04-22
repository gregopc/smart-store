package com.example.smartstore.ai;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class GeminiClient implements AIClient {

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.model}")
    private String model;

    private final WebClient webClient = WebClient.create("https://generativelanguage.googleapis.com");
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String generateSuggestion(String prompt) {

        String response = getModelResponse(prompt);

        return response;
    }

    @Override
    public List<String> extractProductNames(String prompt) {
        String response = getModelResponse(prompt);
        String text = extractTextFromResponse(response);

        try {
            JsonNode node = objectMapper.readTree(text.trim());
            if (node.isArray()) {
                List<String> names = new ArrayList<>();
                node.forEach(n -> names.add(n.asText()));
                return names;
            }
        } catch (Exception ignored) {}

        return Arrays.stream(text.split("[,\n]"))
            .map(String::trim)
            .filter(s -> !s.isBlank())
            .toList();
    }

    @Override
    public String generateReply(String prompt) {

        String response = getModelResponse(prompt);

        return extractTextFromResponse(response);
    }

    private String getModelResponse(String prompt) {
        Map<String, Object> request = Map.of(
                "contents", List.of(
                    Map.of("parts", List.of(
                        Map.of("text", prompt)
                    ))
                )
            );

        return webClient.post()
            .uri("/v1/models/" + model + ":generateContent")
            .header("x-goog-api-key", apiKey)
            .bodyValue(request)
            .retrieve()
            .bodyToMono(String.class)
            .block();
    }

    @Override
    public String extractKeywords(String message) {

        String prompt = """
        Extraia os principais ingredientes ou termos de busca da frase abaixo.

        Regras:
        - Responda apenas com palavras separadas por espaço
        - Não explique nada
        - Foque em alimentos e ingredientes

        Frase:
        """ + message;

        return generateReply(prompt);
    }

    private String extractTextFromResponse(String response) {
        try {
            JsonNode root = objectMapper.readTree(response);

            JsonNode candidates = root.path("candidates");

            if (!candidates.isArray() || candidates.isEmpty()) {
                return "Não consegui gerar resposta no momento.";
            }

            JsonNode textNode = candidates.get(0)
                .path("content")
                .path("parts");

            if (!textNode.isArray() || textNode.isEmpty()) {
                return "Resposta inválida da IA.";
            }

            return textNode.get(0).path("text").asText();

        } catch (Exception e) {
            throw new RuntimeException("Erro ao processar resposta da IA", e);
        }
    }
}
