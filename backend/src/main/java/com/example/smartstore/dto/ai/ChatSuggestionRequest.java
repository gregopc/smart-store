package com.example.smartstore.dto.ai;

import com.example.smartstore.domain.ai.ChatHistoryEntry;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatSuggestionRequest {
    private List<ChatHistoryEntry> messages;
    private String message;
}
