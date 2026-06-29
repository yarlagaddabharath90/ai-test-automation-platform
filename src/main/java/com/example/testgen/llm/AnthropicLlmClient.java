package com.example.testgen.llm;

import com.example.testgen.config.TestGenProperties;
import com.example.testgen.domain.LlmProvider;
import org.springframework.resilience.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

/** Calls the Anthropic Messages API. Key comes from ANTHROPIC_API_KEY. */
@Component
public class AnthropicLlmClient implements LlmClient {

    private final TestGenProperties.Provider cfg;
    private final RestClient http;

    public AnthropicLlmClient(TestGenProperties props) {
        this.cfg = props.provider(LlmProvider.CLAUDE);
        this.http = RestClient.builder()
                .baseUrl(cfg.baseUrl())
                .defaultHeader("x-api-key", cfg.apiKey())
                .defaultHeader("anthropic-version", cfg.anthropicVersion())
                .defaultHeader("content-type", "application/json")
                .build();
    }

    @Override
    public LlmProvider provider() { return LlmProvider.CLAUDE; }

    @Override
    @Retryable(maxRetries = 2, delay = 1500, multiplier = 2)
    @SuppressWarnings("unchecked")
    public String complete(String systemPrompt, String userPrompt) {
        if (cfg.apiKey() == null || cfg.apiKey().isBlank()) {
            throw new IllegalStateException(
                    "ANTHROPIC_API_KEY is not set. Export it or add it as a GitHub Actions secret.");
        }
        Map<String, Object> body = Map.of(
                "model", cfg.model(),
                "max_tokens", cfg.maxTokens(),
                "system", systemPrompt,
                "messages", List.of(Map.of("role", "user", "content", userPrompt)));

        Map<String, Object> response = http.post().body(body).retrieve().body(Map.class);
        if (response == null) throw new IllegalStateException("Empty response from Claude");

        List<Map<String, Object>> content = (List<Map<String, Object>>) response.get("content");
        if (content == null) throw new IllegalStateException("Claude response had no content: " + response);
        StringBuilder sb = new StringBuilder();
        for (Map<String, Object> block : content) {
            if ("text".equals(block.get("type"))) sb.append(block.get("text"));
        }
        return sb.toString();
    }
}
