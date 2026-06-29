package com.example.testgen.config;

import com.example.testgen.domain.LlmProvider;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.util.Map;

/**
 * Typed config bound from the {@code testgen.*} keys. Each entry under
 * {@code testgen.providers.*} configures one LLM provider (claude, gemini, ...).
 */
@ConfigurationProperties(prefix = "testgen")
public record TestGenProperties(
        @DefaultValue("claude") String defaultProvider,
        Map<String, Provider> providers,
        @DefaultValue("generated") String outputDir) {

    public record Provider(
            @DefaultValue("") String apiKey,
            String baseUrl,
            String model,
            @DefaultValue("8000") int maxTokens,
            @DefaultValue("2023-06-01") String anthropicVersion) {
    }

    /** Look up the config block for a provider, failing clearly if it is missing. */
    public Provider provider(LlmProvider provider) {
        Provider p = providers == null ? null : providers.get(provider.key());
        if (p == null) {
            throw new IllegalStateException("No configuration for provider '" + provider.key()
                    + "'. Add a testgen.providers." + provider.key() + " block.");
        }
        return p;
    }
}
