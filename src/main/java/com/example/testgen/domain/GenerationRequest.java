package com.example.testgen.domain;

import java.util.List;
import java.util.Map;

/** A request to generate test modules from an OpenAPI spec using a given provider. */
public record GenerationRequest(
        String specName,
        String specContent,
        LlmProvider provider,
        List<TestType> types,
        Map<String, String> options) {
}
