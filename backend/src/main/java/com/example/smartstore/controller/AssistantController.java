package com.example.smartstore.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.smartstore.dto.ai.*;
import com.example.smartstore.service.AssistantService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "Assistant", description = "Assistente inteligente de compras")
@RestController
@RequestMapping("/assistant")
@RequiredArgsConstructor
public class AssistantController {

    private final AssistantService assistantService;

    @Operation(summary = "Chat com assistente", description = "Recebe mensagem do usuário e retorna recomendação de produtos")
    @PostMapping("/chat")
    public ChatResponse chat(@RequestBody ChatRequest request) {

        String reply = assistantService.chat(request.getMessage());

        return new ChatResponse(reply);
    }

    @Operation(summary = "Sugestões do assistente", description = "Recebe histórico de mensagens e retorna resposta + lista de produtos recomendados do banco")
    @PostMapping("/suggestion")
    public ChatSuggestionResponse suggestion(@RequestBody ChatSuggestionRequest request) {
        return assistantService.chatSuggestion(request.getMessages());
    }
}
