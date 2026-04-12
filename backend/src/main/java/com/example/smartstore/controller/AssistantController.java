package com.example.smartstore.controller;

import com.example.smartstore.dto.ChatRequest;
import com.example.smartstore.dto.ChatResponse;
import com.example.smartstore.service.AssistantService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

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
}