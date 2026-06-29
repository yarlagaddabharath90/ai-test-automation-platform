# AI Test Automation Platform

Generates a full **multi-module test suite** from an **OpenAPI specification**, using your
choice of LLM provider (Claude or Gemini). The output is a real Gradle project you open in
IntelliJ — each test type is its own submodule with its own run task.

**Stack:** Java 25 (LTS) · Spring Boot 4.0.6 (Spring Framework 7) · Gradle 9.6.1

## Providers

Pick the provider per request. The platform uses that provider's API + key:

| Provider | Request value | Env var | Default model |
| --- | --- | --- | --- |
| Anthropic Claude | `providers=claude` | `ANTHROPIC_API_KEY` | `claude-sonnet-4-6` |
| Google Gemini | `providers=gemini` | `GEMINI_API_KEY` | `gemini-2.5-flash` |

If no provider is given, `testgen.default-provider` (claude) is used. Only the key for the
chosen provider needs to be set.

## What it produces

A `generated-test-suite/` Gradle project. Each requested type becomes a submodule:

| Module | Test type | Run task |
| --- | --- | --- |
| `unit-tests` | JUnit 5 unit | `:unit-tests:test` |
| `integration-tests` | Spring WebTestClient | `:integration-tests:test` |
| `cucumber-tests` | Cucumber BDD | `:cucumber-tests:cucumber` |
| `contract-tests` | Pact contract | `:contract-tests:test` |
| `load-tests` | k6 load | `:load-tests:k6` |
| `perf-tests` | Gatling perf | `:perf-tests:gatlingRun` |
| `security-tests` | OWASP API security | `:security-tests:test` |
| `mock-service` | WireMock mock | `:mock-service:run` |

Coverage is wired at the root: JaCoCo runs after each module's tests (`build/reports/jacoco/`).

## How it works

```
OpenAPI spec ─▶ OpenApiSpecParser (validate)
            ─▶ GenerationOrchestrator
                 ├─ LlmClientRegistry.resolve(provider)   claude | gemini
                 ├─ LlmTestGenerator (one prompt per type, module-aware paths)
                 └─ ProjectScaffolder → writes settings.gradle, root + per-module
                    build.gradle, and sources into ./generated/generated-test-suite
```

## One-time setup

```bash
gradle wrapper --gradle-version 9.6.1   # generate the wrapper jar, then commit it
export ANTHROPIC_API_KEY=sk-ant-...      # and/or
export GEMINI_API_KEY=AIza...
```

## Run locally

**Service (REST):**

```bash
./gradlew bootRun

curl -X POST http://localhost:8080/api/generate \
  -F "spec=@specs/openapi.yaml" \
  -F "types=unit,integration" \
  -F "providers=claude"
```

The response is a JSON summary (provider, types, files written, output dir). The actual
project lands in `generated/generated-test-suite/`. `GET /api/providers` and
`GET /api/test-types` list the options.

**Headless (what CI uses):**

```bash
./gradlew generate \
  -Pspec=specs/openapi.yaml \
  -Pprovider=gemini \
  -Ptypes=unit,cucumber,gatling \
  -Pout=build/generated
```

## Use the generated suite

```bash
cd generated/generated-test-suite
gradle wrapper --gradle-version 9.6.1
./gradlew :cucumber-tests:cucumber
./gradlew :perf-tests:gatlingRun
```

Or just **open the folder in IntelliJ** — it imports as a Gradle project and every task
shows up in the Gradle tool window.

## GitHub Actions — on demand

`.github/workflows/generate-tests.yml` uses `workflow_dispatch` with inputs for spec path,
test types, and **provider** (claude/gemini dropdown). Add `ANTHROPIC_API_KEY` and/or
`GEMINI_API_KEY` under **Settings → Secrets → Actions**, then **Actions → Run workflow**, or:

```bash
gh workflow run "Generate Tests (on demand)" \
  -f spec_path=specs/openapi.yaml \
  -f types=unit,cucumber,gatling \
  -f provider=gemini
```

The generated suite uploads as a build artifact on the run summary.

## Project layout (the generator app)

```
src/main/java/com/example/testgen/
├── api/         REST controller (provider + types params)
├── cli/         headless runner used by CI
├── config/      typed properties (providers map)
├── domain/      records, TestType + LlmProvider enums
├── generator/   prompt library, module catalog, LLM generator
├── llm/         LlmClient + Anthropic + Gemini + registry
└── service/     spec parser, orchestrator, project scaffolder
```

## Notes

- Generated tests are a strong first draft — review them (security/contract especially).
- Module dependency versions in the catalog are sensible defaults; bump as needed
  (e.g. the Gatling Gradle plugin version).
