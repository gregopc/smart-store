package com.example.smartstore.ai;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

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
    public String generateReply(String prompt) {

        Map<String, Object> request = Map.of(
            "contents", List.of(
                Map.of("parts", List.of(
                    Map.of("text", prompt)
                ))
            )
        );

        String response = webClient.post()
                .uri("/v1/models/" + model + ":generateContent")
                .header("x-goog-api-key", apiKey)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        return extractTextFromResponse(response);
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