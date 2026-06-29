package com.example.testgen.service;

import com.example.testgen.domain.GeneratedArtifact;
import com.example.testgen.domain.GenerationResult;
import com.example.testgen.domain.TestType;
import com.example.testgen.generator.ModuleCatalog;
import com.example.testgen.generator.ModuleCatalog.ModuleSpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Turns a {@link GenerationResult} into a real, importable multi-module Gradle
 * project. Each test type lands in its own submodule with its own build.gradle
 * and run task, so you can open the folder in IntelliJ and run e.g.
 * {@code :perf-tests:gatlingRun} or {@code :cucumber-tests:cucumber} directly.
 */
@Service
public class ProjectScaffolder {

    private static final Logger log = LoggerFactory.getLogger(ProjectScaffolder.class);

    private final ModuleCatalog catalog;

    public ProjectScaffolder(ModuleCatalog catalog) {
        this.catalog = catalog;
    }

    public int scaffold(GenerationResult result, Path root) throws IOException {
        Files.createDirectories(root);

        // Which modules actually have generated files?
        Set<TestType> producedModuleTypes = new LinkedHashSet<>();
        for (GeneratedArtifact a : result.artifacts()) {
            if (catalog.hasModule(a.type()) && a.type() != TestType.COVERAGE) {
                producedModuleTypes.add(a.type());
            }
        }

        int written = 0;

        // 1) Write generated sources into their module's source root.
        for (GeneratedArtifact a : result.artifacts()) {
            if (a.type() == TestType.COVERAGE) {
                written += writeFile(root.resolve("COVERAGE.md"), a.content());
                continue;
            }
            ModuleSpec spec = catalog.specFor(a.type());
            Path target = root.resolve(spec.moduleName()).resolve(a.relativePath()).normalize();
            if (!target.startsWith(root)) {
                throw new IOException("Refusing to write outside project root: " + a.relativePath());
            }
            written += writeFile(target, a.content());
        }

        // 2) Each module's build.gradle.
        for (TestType type : producedModuleTypes) {
            ModuleSpec spec = catalog.specFor(type);
            writeFile(root.resolve(spec.moduleName()).resolve("build.gradle"), spec.buildGradle());
        }

        // 3) Root settings.gradle + build.gradle + README.
        writeFile(root.resolve("settings.gradle"), settingsGradle(producedModuleTypes));
        writeFile(root.resolve("build.gradle"), rootBuildGradle());
        writeFile(root.resolve("README.md"), readme(producedModuleTypes));

        log.info("Scaffolded {} module(s), {} file(s) at {}",
                producedModuleTypes.size(), written, root.toAbsolutePath());
        return written;
    }

    private int writeFile(Path target, String content) throws IOException {
        Files.createDirectories(target.getParent());
        Files.writeString(target, content);
        return 1;
    }

    private String settingsGradle(Set<TestType> types) {
        StringBuilder sb = new StringBuilder("rootProject.name = 'generated-test-suite'\n\n");
        for (TestType t : types) {
            sb.append("include '").append(catalog.specFor(t).moduleName()).append("'\n");
        }
        return sb.toString();
    }

    private String rootBuildGradle() {
        return """
                // Root build for the generated test suite.
                // Per-module config lives in each submodule's build.gradle.
                plugins { id 'base' }

                allprojects {
                    repositories { mavenCentral() }
                }

                // Auto-apply JaCoCo to every Java module and produce a report after tests.
                subprojects {
                    plugins.withId('java') {
                        apply plugin: 'jacoco'
                        tasks.withType(Test).configureEach { finalizedBy(tasks.named('jacocoTestReport')) }
                        tasks.withType(JacocoReport).configureEach {
                            reports { xml.required = true; html.required = true }
                        }
                    }
                }
                """;
    }

    private String readme(Set<TestType> types) {
        StringBuilder sb = new StringBuilder("""
                # Generated Test Suite

                Open this folder in IntelliJ (it imports as a Gradle multi-module project).
                Each test type is its own module with a run task:

                | Module | Test type | Run task |
                | --- | --- | --- |
                """);
        for (TestType t : types) {
            ModuleSpec s = catalog.specFor(t);
            sb.append("| `").append(s.moduleName()).append("` | ")
              .append(t.label()).append(" | `:")
              .append(s.moduleName()).append(":").append(s.runTask()).append("` |\n");
        }
        sb.append("""

                ## Run

                ```bash
                gradle wrapper --gradle-version 9.6.1   # once, to create the wrapper
                ./gradlew test                          # all JVM test modules
                ./gradlew :perf-tests:gatlingRun        # a specific module
                ```

                Coverage reports are written per module under `build/reports/jacoco/`.
                Some modules need external tools on PATH (k6 for load-tests).
                """);
        return sb.toString();
    }
}
