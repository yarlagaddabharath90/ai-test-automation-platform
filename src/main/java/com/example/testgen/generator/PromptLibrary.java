package com.example.testgen.generator;

import com.example.testgen.domain.TestType;
import com.example.testgen.generator.ModuleCatalog.ModuleSpec;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Per-type prompt templates. The user prompt is parameterised with the target
 * module's source root + base package so the model emits files at module-relative
 * paths that drop straight into the generated submodule.
 */
@Component
public class PromptLibrary {

    private static final String SYSTEM = """
            You are a senior test engineer. You generate production-quality, runnable test
            artifacts from an OpenAPI specification. Output ONLY files, each wrapped in a
            fenced block whose info string is the MODULE-RELATIVE file path, like:

            ```path=src/test/java/gen/unit/PetModelTest.java
            <file contents>
            ```

            Rules:
            - Emit complete, compilable files. No placeholders, no TODOs, no prose outside blocks.
            - Use the exact source root and base package given in the task.
            - Cover every path + method + status code in the spec.
            - Prefer modern idioms (JUnit 5, AssertJ, Java 25).
            """;

    private final Map<TestType, String> instructions = Map.ofEntries(
            Map.entry(TestType.UNIT,
                    "JUnit 5 + AssertJ unit tests for request/response models and schema validation "
                            + "(required fields, formats, enums, bounds)."),
            Map.entry(TestType.INTEGRATION,
                    "Spring Boot integration tests using WebTestClient that call each endpoint and "
                            + "assert status codes, headers and bodies against the schema."),
            Map.entry(TestType.CUCUMBER,
                    "BDD suite: Gherkin .feature files under src/test/resources/features, matching step "
                            + "definitions in the base package using REST-assured, and a JUnit-Platform "
                            + "suite runner annotated with @Suite + @IncludeEngines(\"cucumber\")."),
            Map.entry(TestType.CONTRACT,
                    "Pact consumer contract tests plus the matching provider verification, derived from "
                            + "request/response schemas and examples."),
            Map.entry(TestType.LOAD,
                    "k6 JavaScript at src/k6/main.js: one scenario per endpoint group with stages "
                            + "(ramp-up/steady/ramp-down), realistic payloads, and p95/error-rate thresholds."),
            Map.entry(TestType.GATLING,
                    "Gatling simulations using the Java DSL (io.gatling.javaapi) under the source root. "
                            + "Build HTTP scenarios per endpoint with rampUsersPerSec injection and "
                            + "response-time + success-rate assertions."),
            Map.entry(TestType.SECURITY,
                    "REST-assured security tests covering the OWASP API Top 10 relevant to this spec: "
                            + "broken auth, BOLA/IDOR, injection, mass assignment, excessive data exposure, "
                            + "and input fuzzing per parameter."),
            Map.entry(TestType.MOCK,
                    "A runnable mock service: WireMock JSON stub mappings under src/main/resources/mappings "
                            + "for every operation (responses must conform to the response schemas), plus a "
                            + "MockServer class at {basePackage}.MockServer that starts WireMock on port 8089 "
                            + "loading those mappings."),
            Map.entry(TestType.COVERAGE,
                    "A short COVERAGE.md describing which modules feed JaCoCo and the gradle invocation "
                            + "that produces the reports."));

    public String system() {
        return SYSTEM;
    }

    public String userPrompt(TestType type, ModuleSpec module, String specName, String specContent) {
        String root = module == null ? "(root)" : module.sourceRoot();
        String pkg = module == null || module.basePackage().isBlank() ? "(none)" : module.basePackage();
        return """
                Test type: %s — %s

                Target module source root: %s
                Base package: %s
                Source spec file: %s

                OpenAPI specification:
                ----------------------------------------
                %s
                ----------------------------------------

                Task: %s
                Emit every file path relative to the module root, under the source root above.
                """.formatted(type.label(), type.description(), root, pkg, specName, specContent,
                instructions.get(type));
    }
}
