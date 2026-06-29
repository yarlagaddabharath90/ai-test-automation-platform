package com.example.testgen.generator;

import com.example.testgen.domain.TestType;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.Map;

/**
 * Defines how each {@link TestType} becomes a Gradle submodule: its directory
 * name, where generated sources live, the module's build.gradle, and the Gradle
 * task you run it with (e.g. :perf-tests:gatlingRun). COVERAGE is not a module —
 * it is wired at the root build instead.
 */
@Component
public class ModuleCatalog {

    public record ModuleSpec(
            String moduleName,   // directory + gradle include name
            String sourceRoot,   // where generated files are placed (module-relative)
            String basePackage,  // base package for generated code
            String runTask,      // gradle task to execute the module
            String buildGradle   // the module's build.gradle contents
    ) {}

    private static final int JAVA = 25;

    private final Map<TestType, ModuleSpec> specs = new EnumMap<>(TestType.class);

    public ModuleCatalog() {
        specs.put(TestType.UNIT, new ModuleSpec(
                "unit-tests", "src/test/java", "gen.unit", "test",
                """
                plugins { id 'java' }
                repositories { mavenCentral() }
                java { toolchain { languageVersion = JavaLanguageVersion.of(%d) } }
                dependencies {
                    testImplementation platform('org.junit:junit-bom:5.11.3')
                    testImplementation 'org.junit.jupiter:junit-jupiter'
                    testImplementation 'org.assertj:assertj-core:3.26.3'
                    testImplementation 'org.mockito:mockito-core:5.14.2'
                    testRuntimeOnly 'org.junit.platform:junit-platform-launcher'
                }
                tasks.named('test') { useJUnitPlatform() }
                """.formatted(JAVA)));

        specs.put(TestType.INTEGRATION, new ModuleSpec(
                "integration-tests", "src/test/java", "gen.integration", "test",
                """
                plugins {
                    id 'java'
                    id 'org.springframework.boot' version '4.0.6'
                    id 'io.spring.dependency-management' version '1.1.7'
                }
                repositories { mavenCentral() }
                java { toolchain { languageVersion = JavaLanguageVersion.of(%d) } }
                dependencies {
                    testImplementation 'org.springframework.boot:spring-boot-starter-webflux'
                    testImplementation 'org.springframework.boot:spring-boot-starter-test'
                    testRuntimeOnly 'org.junit.platform:junit-platform-launcher'
                }
                tasks.named('test') { useJUnitPlatform() }
                """.formatted(JAVA)));

        specs.put(TestType.CUCUMBER, new ModuleSpec(
                "cucumber-tests", "src/test", "gen.cucumber", "cucumber",
                """
                plugins { id 'java' }
                repositories { mavenCentral() }
                java { toolchain { languageVersion = JavaLanguageVersion.of(%d) } }
                dependencies {
                    testImplementation platform('org.junit:junit-bom:5.11.3')
                    testImplementation 'io.cucumber:cucumber-java:7.20.1'
                    testImplementation 'io.cucumber:cucumber-junit-platform-engine:7.20.1'
                    testImplementation 'org.junit.platform:junit-platform-suite:1.11.3'
                    testImplementation 'io.rest-assured:rest-assured:5.5.0'
                    testRuntimeOnly 'org.junit.platform:junit-platform-launcher'
                }
                tasks.named('test') {
                    useJUnitPlatform()
                    systemProperty 'cucumber.junit-platform.naming-strategy', 'long'
                }
                // Run the BDD suite with: ./gradlew :cucumber-tests:cucumber
                tasks.register('cucumber') { dependsOn 'test' }
                """.formatted(JAVA)));

        specs.put(TestType.CONTRACT, new ModuleSpec(
                "contract-tests", "src/test/java", "gen.contract", "test",
                """
                plugins { id 'java' }
                repositories { mavenCentral() }
                java { toolchain { languageVersion = JavaLanguageVersion.of(%d) } }
                dependencies {
                    testImplementation platform('org.junit:junit-bom:5.11.3')
                    testImplementation 'org.junit.jupiter:junit-jupiter'
                    testImplementation 'au.com.dius.pact.consumer:junit5:4.6.15'
                    testImplementation 'au.com.dius.pact.provider:junit5:4.6.15'
                    testRuntimeOnly 'org.junit.platform:junit-platform-launcher'
                }
                tasks.named('test') { useJUnitPlatform() }
                """.formatted(JAVA)));

        specs.put(TestType.LOAD, new ModuleSpec(
                "load-tests", "src/k6", "", "k6",
                """
                plugins { id 'base' }
                // Requires the k6 binary on PATH (https://k6.io). Run: ./gradlew :load-tests:k6
                tasks.register('k6', Exec) {
                    group = 'verification'
                    description = 'Run k6 load tests'
                    workingDir = projectDir
                    commandLine 'k6', 'run', 'src/k6/main.js'
                }
                """));

        specs.put(TestType.GATLING, new ModuleSpec(
                "perf-tests", "src/gatling/java", "gen.perf", "gatlingRun",
                """
                plugins {
                    id 'java'
                    // Version may need bumping to the latest Gatling plugin.
                    id 'io.gatling.gradle' version '3.13.5'
                }
                repositories { mavenCentral() }
                java { toolchain { languageVersion = JavaLanguageVersion.of(%d) } }
                // Run the simulations with: ./gradlew :perf-tests:gatlingRun
                """.formatted(JAVA)));

        specs.put(TestType.SECURITY, new ModuleSpec(
                "security-tests", "src/test/java", "gen.security", "test",
                """
                plugins { id 'java' }
                repositories { mavenCentral() }
                java { toolchain { languageVersion = JavaLanguageVersion.of(%d) } }
                dependencies {
                    testImplementation platform('org.junit:junit-bom:5.11.3')
                    testImplementation 'org.junit.jupiter:junit-jupiter'
                    testImplementation 'io.rest-assured:rest-assured:5.5.0'
                    testImplementation 'org.assertj:assertj-core:3.26.3'
                    testRuntimeOnly 'org.junit.platform:junit-platform-launcher'
                }
                tasks.named('test') { useJUnitPlatform() }
                """.formatted(JAVA)));

        specs.put(TestType.MOCK, new ModuleSpec(
                "mock-service", "src/main", "gen.mock", "run",
                """
                plugins {
                    id 'java'
                    id 'application'
                }
                repositories { mavenCentral() }
                java { toolchain { languageVersion = JavaLanguageVersion.of(%d) } }
                dependencies { implementation 'org.wiremock:wiremock:3.9.2' }
                // Boots WireMock against the generated mappings. Run: ./gradlew :mock-service:run
                application { mainClass = 'gen.mock.MockServer' }
                """.formatted(JAVA)));
    }

    public ModuleSpec specFor(TestType type) {
        return specs.get(type);
    }

    public boolean hasModule(TestType type) {
        return specs.containsKey(type);
    }
}
