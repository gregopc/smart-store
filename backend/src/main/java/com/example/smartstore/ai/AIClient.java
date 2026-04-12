package com.example.smartstore.ai;

public interface AIClient {
    
    String generateReply(String prompt);

    String extractKeywords(String message);
}