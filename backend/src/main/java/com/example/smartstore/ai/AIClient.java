package com.example.smartstore.ai;

import java.util.List;

public interface AIClient {
    
    String generateSuggestion(String prompt);

    String generateReply(String prompt);

    String extractKeywords(String message);

    List<String> extractProductNames(String prompt);
}
