package com.example.testgen;

import com.example.testgen.domain.GeneratedArtifact;
import com.example.testgen.domain.LlmProvider;
import com.example.testgen.domain.TestType;
import com.example.testgen.generator.LlmTestGenerator;
import com.example.testgen.generator.ModuleCatalog;
import com.example.testgen.generator.PromptLibrary;
import com.example.testgen.llm.LlmClient;
import com.example.testgen.llm.LlmClientRegistry;
import com.example.testgen.service.OpenApiSpecParser;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GenerationSmokeTest {

    private static final String SPEC = """
            openapi: 3.0.3
            info: { title: Demo, version: 1.0.0 }
            paths:
              /ping:
                get:
                  responses:
                    "200": { description: ok }
            """;

    @Test
    void parsesValidSpec() {
        var parsed = new OpenApiSpecParser().parse(SPEC);
        assertThat(parsed.title()).isEqualTo("Demo");
        assertThat(parsed.endpointCount()).isEqualTo(1);
    }

    @Test
    void registrySelectsProviderAndGeneratorParsesFileBlocks() {
        // A fake CLAUDE client returning two fenced file blocks.
        LlmClient stub = new LlmClient() {
            @Override public LlmProvider provider() { return LlmProvider.CLAUDE; }
            @Override public String complete(String system, String user) {
                return """
                        ```path=src/test/java/gen/unit/PingTest.java
                        class PingTest {}
                        ```
                        ```path=src/test/java/gen/unit/PongTest.java
                        class PongTest {}
                        ```
                        """;
            }
        };
        var registry = new LlmClientRegistry(List.of(stub));
        var generator = new LlmTestGenerator(registry, new PromptLibrary(), new ModuleCatalog());

        List<GeneratedArtifact> artifacts =
                generator.generate(LlmProvider.CLAUDE, TestType.UNIT, "demo.yaml", SPEC);

        assertThat(artifacts).hasSize(2);
        assertThat(artifacts).extracting(GeneratedArtifact::relativePath)
                .containsExactly("src/test/java/gen/unit/PingTest.java",
                                 "src/test/java/gen/unit/PongTest.java");
    }
}
