package com.example.testgen.domain;

import java.time.Instant;
import java.util.List;

/** The outcome of a generation run across all requested test types. */
public record GenerationResult(
        String specName,
        Instant generatedAt,
        List<GeneratedArtifact> artifacts,
        List<String> warnings) {
}
