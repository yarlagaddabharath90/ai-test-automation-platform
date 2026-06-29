package com.example.testgen.generator;

import com.example.testgen.domain.GeneratedArtifact;
import com.example.testgen.domain.LlmProvider;
import com.example.testgen.domain.TestType;
import com.example.testgen.generator.ModuleCatalog.ModuleSpec;
import com.example.testgen.llm.LlmClientRegistry;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Drives the selected LLM provider for a single {@link TestType} and parses the
 * response into concrete module-relative files.
 */
@Component
public class LlmTestGenerator {

    private static final Pattern FILE_BLOCK = Pattern.compile(
            "```\\s*path=([^\\n`]+)\\n(.*?)```", Pattern.DOTALL);

    private final LlmClientRegistry registry;
    private final PromptLibrary prompts;
    private final ModuleCatalog catalog;

    public LlmTestGenerator(LlmClientRegistry registry, PromptLibrary prompts, ModuleCatalog catalog) {
        this.registry = registry;
        this.prompts = prompts;
        this.catalog = catalog;
    }

    public List<GeneratedArtifact> generate(LlmProvider provider, TestType type,
                                            String specName, String specContent) {
        ModuleSpec module = catalog.specFor(type);
        String output = registry.resolve(provider)
                .complete(prompts.system(), prompts.userPrompt(type, module, specName, specContent));
        return parseArtifacts(type, output);
    }

    private List<GeneratedArtifact> parseArtifacts(TestType type, String output) {
        List<GeneratedArtifact> artifacts = new ArrayList<>();
        Matcher m = FILE_BLOCK.matcher(output);
        while (m.find()) {
            artifacts.add(new GeneratedArtifact(type, m.group(1).trim(), m.group(2)));
        }
        if (artifacts.isEmpty() && !output.isBlank()) {
            String name = type == TestType.COVERAGE ? "COVERAGE.md" : type.name().toLowerCase() + ".txt";
            artifacts.add(new GeneratedArtifact(type, name, output));
        }
        return artifacts;
    }
}
