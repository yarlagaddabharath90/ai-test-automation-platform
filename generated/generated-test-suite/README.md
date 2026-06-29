# Generated Test Suite

Open this folder in IntelliJ (it imports as a Gradle multi-module project).
Each test type is its own module with a run task:

| Module | Test type | Run task |
| --- | --- | --- |
| `unit-tests` | Unit tests | `:unit-tests:test` |
| `integration-tests` | Integration tests | `:integration-tests:test` |

## Run

```bash
gradle wrapper --gradle-version 9.6.1   # once, to create the wrapper
./gradlew test                          # all JVM test modules
./gradlew :perf-tests:gatlingRun        # a specific module
```

Coverage reports are written per module under `build/reports/jacoco/`.
Some modules need external tools on PATH (k6 for load-tests).
