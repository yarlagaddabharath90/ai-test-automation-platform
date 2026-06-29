package com.example.testgen.llm;

import com.example.testgen.config.TestGenProperties;
import com.example.testgen.domain.LlmProvider;
import org.springframework.resilience.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

/**
 * Calls the Google Gemini API ({@code generateContent}). The key is read from
 * the GEMINI_API_KEY environment variable and passed via the x-goog-api-key
 * header (never in the URL).
 *
 * Endpoint: {baseUrl}/models/{model}:generateContent
 * baseUrl defaults to https://generativelanguage.googleapis.com/v1beta
 */
@Component
public class GeminiLlmClient implements LlmClient {

    private final TestGenProperties.Provider cfg;
    private final RestClient http;

    public GeminiLlmClient(TestGenProperties props) {
        this.cfg = props.provider(LlmProvider.GEMINI);
        this.http = RestClient.builder()
                .baseUrl(cfg.baseUrl())
                .defaultHeader("x-goog-api-key", cfg.apiKey())
                .defaultHeader("content-type", "application/json")
                .build();
    }

    @Override
    public LlmProvider provider() { return LlmProvider.GEMINI; }

    @Override
    @Retryable(maxRetries = 2, delay = 1500, multiplier = 2)
    @SuppressWarnings("unchecked")
    public String complete(String systemPrompt, String userPrompt) {
        if (cfg.apiKey() == null || cfg.apiKey().isBlank()) {
            throw new IllegalStateException(
                    "GEMINI_API_KEY is not set. Export it or add it as a GitHub Actions secret.");
        }

        Map<String, Object> body = Map.of(
                "systemInstruction", Map.of("parts", List.of(Map.of("text", systemPrompt))),
                "contents", List.of(Map.of(
                        "role", "user",
                        "parts", List.of(Map.of("text", userPrompt)))),
                "generationConfig", Map.of("maxOutputTokens", cfg.maxTokens()));

        Map<String, Object> response = http.post()
                .uri("/models/{model}:generateContent", cfg.model())
                .body(body)
                .retrieve()
                .body(Map.class);

        if (response == null) throw new IllegalStateException("Empty response from Gemini");

        List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.get("candidates");
        if (candidates == null || candidates.isEmpty()) {
            throw new IllegalStateException("Gemini response had no candidates: " + response);
        }
        Map<String, Object> content = (Map<String, Object>) candidates.get(0).get("content");
        List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
        StringBuilder sb = new StringBuilder();
        for (Map<String, Object> part : parts) {
            Object text = part.get("text");
            if (text != null) sb.append(text);
        }
        return sb.toString();
    }
}
