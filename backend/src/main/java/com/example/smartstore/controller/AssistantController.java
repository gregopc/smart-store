package com.example.smartstore.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.smartstore.domain.User;
import com.example.smartstore.domain.ai.ChatHistoryEntry;
import com.example.smartstore.dto.ai.*;
import com.example.smartstore.event.UserActionEventPublisher;
import com.example.smartstore.event.UserActionType;
import com.example.smartstore.service.AssistantService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;
import java.util.Map;

@Tag(name = "Assistant", description = "Assistente inteligente de compras")
@RestController
@RequestMapping("/assistant")
public class AssistantController {

    private final AssistantService assistantService;
    private final UserActionEventPublisher userActionEventPublisher;

    public AssistantController(AssistantService assistantService, UserActionEventPublisher userActionEventPublisher) {
        this.assistantService = assistantService;
        this.userActionEventPublisher = userActionEventPublisher;
    }

    @Operation(summary = "Chat com assistente", description = "Recebe mensagem do usuário e retorna recomendação de produtos")
    @PostMapping("/chat")
    public ChatResponse chat(@RequestBody ChatRequest request, @AuthenticationPrincipal User user) {

        String reply = assistantService.chat(request.getMessage());
        userActionEventPublisher.publish(userActionEventPublisher
                .newEvent(UserActionType.ASSISTANT_MESSAGE_SENT, user, "POST /assistant/chat")
                .assistantMessage(request.getMessage())
                .metadata(Map.of("messageLength", messageLength(request.getMessage())))
                .build());

        return new ChatResponse(reply);
    }

    @Operation(summary = "Sugestões do assistente", description = "Recebe histórico de mensagens e retorna resposta + lista de produtos recomendados do banco")
    @PostMapping("/suggestion")
    public ChatSuggestionResponse suggestion(@RequestBody ChatSuggestionRequest request, @AuthenticationPrincipal User user) {
        ChatSuggestionResponse response = assistantService.chatSuggestion(request.getMessages());
        String lastUserMessage = lastUserMessage(request.getMessages());
        int suggestionCount = response.getSuggestion() == null
                || response.getSuggestion().suggestedProducts() == null
                ? 0
                : response.getSuggestion().suggestedProducts().size();

        userActionEventPublisher.publish(userActionEventPublisher
                .newEvent(UserActionType.ASSISTANT_SUGGESTION_REQUESTED, user, "POST /assistant/suggestion")
                .assistantMessage(lastUserMessage)
                .metadata(Map.of(
                        "messageLength", messageLength(lastUserMessage),
                        "suggestionCount", suggestionCount))
                .build());
        return response;
    }

    private String lastUserMessage(List<ChatHistoryEntry> messages) {
        if (messages == null) {
            return null;
        }

        return messages.stream()
                .filter(entry -> "user".equalsIgnoreCase(entry.role()))
                .reduce((first, second) -> second)
                .map(ChatHistoryEntry::content)
                .orElse(null);
    }

    private int messageLength(String message) {
        return message == null ? 0 : message.length();
    }
}
