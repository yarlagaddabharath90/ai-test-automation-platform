package com.example.testgen.llm;

import com.example.testgen.domain.LlmProvider;

/** Abstraction over an LLM provider so generators don't depend on a vendor. */
public interface LlmClient {

    /** Which provider this client serves. */
    LlmProvider provider();

    /**
     * Send a system + user prompt and return the model's text completion.
     */
    String complete(String systemPrompt, String userPrompt);
}
