package com.example.smartstore.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Map;

@Getter
@AllArgsConstructor
public class ApiError {
    private final String message;
    private final Map<String, String> errors;
}
