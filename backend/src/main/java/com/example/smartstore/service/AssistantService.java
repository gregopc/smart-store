package com.example.smartstore.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.smartstore.ai.AIClient;
import com.example.smartstore.domain.ai.Suggestion;
import com.example.smartstore.domain.Product;
import com.example.smartstore.domain.ai.ChatHistoryEntry;
import com.example.smartstore.domain.ai.History;
import com.example.smartstore.dto.ai.ChatSuggestionResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AssistantService {

    private final ProductService productService;
    private final AIClient aiClient;

    public ChatSuggestionResponse chatSuggestion(List<ChatHistoryEntry> messages) {

        String lastMessage = messages.stream()
            .filter(e -> "user".equalsIgnoreCase(e.role()))
            .reduce((first, second) -> second)
            .map(ChatHistoryEntry::content)
            .orElseThrow(() -> new IllegalArgumentException("Nenhuma mensagem do usuário encontrada"));

        String rawKeywords = aiClient.extractKeywords(lastMessage);
        // System.out.println("rawKeywords");
        // System.out.println(rawKeywords);
        String searchQuery = cleanKeywords(rawKeywords);
        // System.out.println("searchQuery");
        // System.out.println(searchQuery);
        List<Product> relevantProducts = productService.findRelevantProductsForAssistant(searchQuery);
        // System.out.println("relevantProducts");
        // System.out.println(relevantProducts);

        if (relevantProducts.isEmpty()) {
            String emptyReply = "Não encontrei produtos disponíveis na nossa loja para essa necessidade no momento.";
            return ChatSuggestionResponse.builder()
                .reply(emptyReply)
                .suggestion(new Suggestion(List.of(), emptyReply))
                .build();
        }

        String selectionPrompt = buildSelectionPrompt(messages, relevantProducts);
        List<String> selectedNames = aiClient.extractProductNames(selectionPrompt);

        List<Product> suggestedProducts = relevantProducts.stream()
            .filter(p -> selectedNames.stream()
                .anyMatch(name -> normalize(p.getName()).contains(normalize(name))
                               || normalize(name).contains(normalize(p.getName()))))
            .toList();

        String reply = aiClient.generateReply(buildReplyPrompt(lastMessage, suggestedProducts));

        return ChatSuggestionResponse.builder()
            .reply(reply)
            .suggestion(new Suggestion(suggestedProducts, reply))
            .build();
    }

    public String chat(String message) {
        String rawKeywords = aiClient.extractKeywords(message);
        String searchQuery = cleanKeywords(rawKeywords);
        List<Product> products = productService.findRelevantProductsForAssistant(searchQuery);
        String prompt = buildPrompt(message, products);
        return aiClient.generateReply(prompt);
    }

    private String buildSelectionPrompt(List<ChatHistoryEntry> messages, List<Product> products) {
        StringBuilder sb = new StringBuilder();

        sb.append("Histórico da conversa:\n");
        for (ChatHistoryEntry entry : messages) {
            sb.append(entry.role()).append(": ").append(entry.content()).append("\n");
        }

        sb.append("\nProdutos disponíveis no estoque:\n");
        for (Product p : products) {
            sb.append("- ").append(p.getName()).append("\n");
        }

        sb.append("""

        Tarefa: Identifique quais produtos da lista acima o cliente vai precisar.

        REGRAS:
        - Retorne APENAS um array JSON com os nomes EXATOS da lista
        - NÃO inclua produtos que não estão na lista
        - NÃO adicione explicações, apenas o JSON
        - Se nenhum for relevante, retorne []

        Resposta (somente JSON):
        """);

        return sb.toString();
    }

    private String buildReplyPrompt(String lastMessage, List<Product> products) {
        StringBuilder sb = new StringBuilder();

        sb.append("O cliente perguntou: ").append(lastMessage).append("\n\n");
        sb.append("Produtos que ENCONTRAMOS na nossa loja:\n");

        for (Product p : products) {
            sb.append("- ").append(p.getName())
              .append(" | R$ ").append(p.getPrice())
              .append(" | ").append(p.getDescription())
              .append("\n");
        }

        sb.append("""

        Instruções:
        - Informe ao cliente que encontramos esses produtos na loja
        - Mencione apenas os produtos listados acima
        - NÃO invente ou mencione produtos que não estão na lista
        - Seja objetivo e amigável
        """);

        return sb.toString();
    }

    private String buildPrompt(String message, List<Product> products) {
        StringBuilder sb = new StringBuilder();
        sb.append("Usuário disse: ").append(message).append("\n\n");
        sb.append("Produtos disponíveis:\n");
        for (int i = 0; i < products.size(); i++) {
            Product p = products.get(i);
            sb.append(i + 1).append(". ")
              .append(p.getName()).append(" - ").append(p.getDescription()).append("\n");
        }
        sb.append("""

        Instruções:
        - Você é um assistente de compras de supermercado
        - Recomende produtos com base na lista
        - Não invente produtos
        - Seja claro e objetivo
        """);
        return sb.toString();
    }

    private String cleanKeywords(String raw) {
        return raw.toLowerCase()
            .replaceAll("[^a-z0-9 ]", " ")
            .replaceAll("\\s+", " ")
            .trim();
    }

    private String normalize(String text) {
        if (text == null) return "";
        return java.text.Normalizer.normalize(text, java.text.Normalizer.Form.NFD)
            .replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
            .toLowerCase()
            .replaceAll("[^a-z0-9 ]", "");
    }
}
