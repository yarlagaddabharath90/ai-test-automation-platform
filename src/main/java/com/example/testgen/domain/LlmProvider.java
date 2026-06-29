package com.example.testgen.domain;

/** Supported LLM providers. The config key matches the application.yml block. */
public enum LlmProvider {
    CLAUDE("claude"),
    GEMINI("gemini");

    private final String key;
    LlmProvider(String key) { this.key = key; }
    public String key() { return key; }

    public static LlmProvider from(String value) {
        if (value == null || value.isBlank()) return CLAUDE;
        return LlmProvider.valueOf(value.trim().toUpperCase());
    }
}
