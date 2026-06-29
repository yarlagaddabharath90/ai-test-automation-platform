package com.example.testgen.cli;

import com.example.testgen.domain.GenerationRequest;
import com.example.testgen.domain.GenerationResult;
import com.example.testgen.domain.LlmProvider;
import com.example.testgen.domain.TestType;
import com.example.testgen.service.GenerationOrchestrator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Headless entry point used by CI (`./gradlew generate`). Active only when
 * started with `--spec=...`; scaffolds the project and exits.
 *
 * Usage:
 *   --spec=specs/openapi.yaml --provider=gemini --types=unit,cucumber,gatling --out=build/generated
 */
@Component
public class GenerateRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(GenerateRunner.class);

    private final GenerationOrchestrator orchestrator;

    public GenerateRunner(GenerationOrchestrator orchestrator) {
        this.orchestrator = orchestrator;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        if (!args.containsOption("spec")) {
            return; // normal web boot
        }
        String specPath = first(args, "spec");
        String out = args.containsOption("out") ? first(args, "out") : orchestrator.defaultOutputDir();
        LlmProvider provider = LlmProvider.from(
                args.containsOption("provider") ? first(args, "provider") : null);
        List<TestType> types = parseTypes(args.containsOption("types") ? first(args, "types") : null);

        String content = Files.readString(Path.of(specPath));
        var request = new GenerationRequest(
                Path.of(specPath).getFileName().toString(), content, provider, types, Map.of());

        GenerationResult result = orchestrator.generate(request);
        Path root = Path.of(out, "generated-test-suite");
        int written = orchestrator.scaffold(result, root);

        log.info("Done. {} file(s) scaffolded into {}", written, root);
        result.warnings().forEach(w -> log.warn("WARNING: {}", w));
        if (written == 0) System.exit(2);
    }

    private static String first(ApplicationArguments args, String name) {
        return args.getOptionValues(name).get(0);
    }

    private List<TestType> parseTypes(String csv) {
        if (csv == null || csv.isBlank() || csv.equalsIgnoreCase("all")) {
            return Arrays.asList(TestType.values());
        }
        return Arrays.stream(csv.split(","))
                .map(String::trim).filter(s -> !s.isEmpty())
                .map(s -> TestType.valueOf(s.toUpperCase()))
                .toList();
    }
}
