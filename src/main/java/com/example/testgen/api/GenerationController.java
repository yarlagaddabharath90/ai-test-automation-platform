package com.example.testgen.api;

import com.example.testgen.domain.GenerationRequest;
import com.example.testgen.domain.GenerationResult;
import com.example.testgen.domain.LlmProvider;
import com.example.testgen.domain.TestType;
import com.example.testgen.service.GenerationOrchestrator;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * HTTP entry point. Upload a spec, choose a provider and the test types; the
 * platform scaffolds a multi-module Gradle project on disk and returns a summary.
 */
@RestController
public class GenerationController {

    private final GenerationOrchestrator orchestrator;

    public GenerationController(GenerationOrchestrator orchestrator) {
        this.orchestrator = orchestrator;
    }

    @GetMapping("/api/test-types")
    public List<TestType> testTypes() {
        return Arrays.asList(TestType.values());
    }

    @GetMapping("/api/providers")
    public List<LlmProvider> providers() {
        return Arrays.asList(LlmProvider.values());
    }

    @PostMapping(value = "/api/generate", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Map<String, Object> generate(
            @RequestPart("spec") MultipartFile spec,
            @RequestParam(value = "types", required = false) String types,
            @RequestParam(value = "providers", required = false) String providers) throws IOException {

        String content = new String(spec.getBytes(), StandardCharsets.UTF_8);
        String specName = spec.getOriginalFilename() == null ? "openapi.yaml" : spec.getOriginalFilename();

        // providers=claude or providers=gemini (first wins if a list is given)
        LlmProvider provider = LlmProvider.from(firstOf(providers));

        var request = new GenerationRequest(specName, content, provider, parseTypes(types), Map.of());
        GenerationResult result = orchestrator.generate(request);

        Path outputRoot = Path.of(orchestrator.defaultOutputDir(), "generated-test-suite");
        int filesWritten = orchestrator.scaffold(result, outputRoot);

        return Map.of(
                "spec", specName,
                "provider", provider.key(),
                "types", request.types().stream().map(Enum::name).toList(),
                "filesWritten", filesWritten,
                "outputDir", outputRoot.toString(),
                "warnings", result.warnings());
    }

    private String firstOf(String csv) {
        if (csv == null || csv.isBlank()) return null;
        return csv.split(",")[0].trim();
    }

    private List<TestType> parseTypes(String types) {
        if (types == null || types.isBlank() || types.equalsIgnoreCase("all")) {
            return Arrays.asList(TestType.values());
        }
        return Arrays.stream(types.split(","))
                .map(String::trim).filter(s -> !s.isEmpty())
                .map(s -> TestType.valueOf(s.toUpperCase()))
                .toList();
    }
}
