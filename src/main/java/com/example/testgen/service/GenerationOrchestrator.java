package com.example.testgen.service;

import com.example.testgen.config.TestGenProperties;
import com.example.testgen.domain.GeneratedArtifact;
import com.example.testgen.domain.GenerationRequest;
import com.example.testgen.domain.GenerationResult;
import com.example.testgen.domain.TestType;
import com.example.testgen.generator.LlmTestGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Coordinates a full run: validate the spec, fan out across the requested test
 * types using the chosen LLM provider, then scaffold a multi-module project.
 */
@Service
public class GenerationOrchestrator {

    private static final Logger log = LoggerFactory.getLogger(GenerationOrchestrator.class);

    private final OpenApiSpecParser parser;
    private final LlmTestGenerator generator;
    private final ProjectScaffolder scaffolder;
    private final TestGenProperties props;

    public GenerationOrchestrator(OpenApiSpecParser parser, LlmTestGenerator generator,
                                  ProjectScaffolder scaffolder, TestGenProperties props) {
        this.parser = parser;
        this.generator = generator;
        this.scaffolder = scaffolder;
        this.props = props;
    }

    public GenerationResult generate(GenerationRequest request) {
        var parsed = parser.parse(request.specContent());
        log.info("Parsed spec '{}' ({} endpoints) — provider={}",
                parsed.title(), parsed.endpointCount(), request.provider());

        List<GeneratedArtifact> artifacts = new ArrayList<>();
        List<String> warnings = new ArrayList<>(parsed.warnings());

        for (TestType type : request.types()) {
            try {
                log.info("Generating {} via {} ...", type.label(), request.provider());
                artifacts.addAll(generator.generate(
                        request.provider(), type, request.specName(), request.specContent()));
            } catch (Exception e) {
                String msg = "Failed to generate %s: %s".formatted(type.label(), e.getMessage());
                log.warn(msg, e);
                warnings.add(msg);
            }
        }
        return new GenerationResult(request.specName(), Instant.now(), artifacts, warnings);
    }

    /** Scaffold the generated multi-module project on disk. Returns files written. */
    public int scaffold(GenerationResult result, Path projectRoot) throws IOException {
        return scaffolder.scaffold(result, projectRoot);
    }

    public String defaultOutputDir() {
        return props.outputDir();
    }
}
