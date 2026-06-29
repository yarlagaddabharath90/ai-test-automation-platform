package com.example.testgen.domain;

/** A single produced file (e.g. a test class, a k6 script, a WireMock stub). */
public record GeneratedArtifact(TestType type, String relativePath, String content) {
}
