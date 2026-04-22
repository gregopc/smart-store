package com.example.smartstore.dto.ai;

import com.example.smartstore.domain.ai.Suggestion;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatSuggestionResponse {
    private Suggestion suggestion;
    private String reply;
}
