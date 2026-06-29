package com.example.testgen.service;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.parser.OpenAPIV3Parser;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Parses and validates an OpenAPI document. We keep the raw text around because
 * the LLM works best from the original spec, but we validate first so we fail
 * fast on a broken document and can surface a short summary.
 */
@Service
public class OpenApiSpecParser {

    public record ParsedSpec(OpenAPI model, String title, int endpointCount, List<String> warnings) {}

    public ParsedSpec parse(String specContent) {
        SwaggerParseResult result = new OpenAPIV3Parser().readContents(specContent);
        OpenAPI api = result.getOpenAPI();
        if (api == null) {
            throw new IllegalArgumentException(
                    "Could not parse OpenAPI spec: " + result.getMessages());
        }
        String title = api.getInfo() != null ? api.getInfo().getTitle() : "Untitled API";
        int endpoints = api.getPaths() == null ? 0 : api.getPaths().size();
        List<String> warnings = result.getMessages() == null ? List.of() : result.getMessages();
        return new ParsedSpec(api, title, endpoints, warnings);
    }
}
