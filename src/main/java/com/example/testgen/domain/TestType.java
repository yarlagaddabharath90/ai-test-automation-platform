package com.example.testgen.domain;

/** The kinds of artifacts the platform can produce from an OpenAPI spec. */
public enum TestType {
    UNIT("Unit tests", "JUnit 5 unit tests for the generated models and handlers."),
    INTEGRATION("Integration tests", "Spring Boot WebTestClient tests that exercise endpoints end to end."),
    CUCUMBER("Cucumber BDD", "Gherkin .feature files plus JUnit-Platform step definitions."),
    CONTRACT("Contract tests", "Pact-style consumer + provider contract tests derived from the schema."),
    LOAD("Load tests", "k6 load-test scripts with stages and thresholds."),
    GATLING("Gatling perf tests", "Gatling (Java DSL) performance simulations with injection profiles."),
    SECURITY("Security tests", "REST-assured OWASP API Top-10 checks and parameter fuzzing."),
    MOCK("Mock service", "A runnable WireMock service that conforms to the spec."),
    COVERAGE("Coverage", "JaCoCo coverage wiring aggregated across the generated modules.");

    private final String label;
    private final String description;

    TestType(String label, String description) { this.label = label; this.description = description; }
    public String label() { return label; }
    public String description() { return description; }
}
