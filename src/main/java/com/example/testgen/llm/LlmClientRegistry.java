package com.example.testgen.llm;

import com.example.testgen.domain.LlmProvider;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Collects every {@link LlmClient} bean and hands out the right one for a
 * requested provider. This is what makes "providers=claude" vs "providers=gemini"
 * select the correct API + key at request time.
 */
@Component
public class LlmClientRegistry {

    private final Map<LlmProvider, LlmClient> byProvider = new EnumMap<>(LlmProvider.class);

    public LlmClientRegistry(List<LlmClient> clients) {
        for (LlmClient c : clients) {
            byProvider.put(c.provider(), c);
        }
    }

    public LlmClient resolve(LlmProvider provider) {
        LlmClient client = byProvider.get(provider);
        if (client == null) {
            throw new IllegalArgumentException("No LLM client registered for provider: " + provider);
        }
        return client;
    }
}
