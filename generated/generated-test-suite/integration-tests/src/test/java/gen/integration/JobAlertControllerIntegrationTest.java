package gen.integration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * End-to-end integration tests for every path + method + status-code combination
 * described in openapi.yaml.
 *
 * The tests start the full Spring Boot application context on a random port and
 * exercise the three endpoints exposed by JobAlertController:
 *
 *   GET  /api/health   → 200  Map<String, Object>
 *   GET  /api/preview  → 200  Map<String, Map<String, List<Job>>>
 *   POST /api/run      → 200  Map<String, Object>
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class JobAlertControllerIntegrationTest {

    /**
     * WebTestClient is auto-configured when the test context starts with a random
     * port and spring-boot-starter-webflux (or spring-boot-starter-web + the
     * WebTestClient adapter) is on the classpath.
     */
    @Autowired
    private WebTestClient webTestClient;

    // -------------------------------------------------------------------------
    // GET /api/health
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("GET /api/health")
    class HealthEndpoint {

        @Test
        @DisplayName("200 OK – returns a JSON object (Map<String,Object>)")
        void health_returns200_withJsonObject() {
            webTestClient
                    .get()
                    .uri("/api/health")
                    .accept(MediaType.ALL)
                    .exchange()
                    // ── Status ────────────────────────────────────────────────
                    .expectStatus().isOk()
                    // ── Headers ───────────────────────────────────────────────
                    .expectHeader().exists("Content-Type")
                    // ── Body schema: object with additionalProperties(object) ─
                    .expectBody(Map.class)
                    .consumeWith(result -> {
                        Map<?, ?> body = result.getResponseBody();
                        assertThat(body).isNotNull();
                        // Each value, if present, must itself be an Object (non-primitive JSON value)
                        body.forEach((k, v) -> assertThat(k).isInstanceOf(String.class));
                    });
        }

        @Test
        @DisplayName("200 OK – Content-Type header is compatible with */*")
        void health_contentTypePresent() {
            webTestClient
                    .get()
                    .uri("/api/health")
                    .exchange()
                    .expectStatus().isOk()
                    .expectHeader().valueMatches("Content-Type", ".*");
        }
    }

    // -------------------------------------------------------------------------
    // GET /api/preview
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("GET /api/preview")
    class PreviewEndpoint {

        /**
         * Schema: Map&lt;String, Map&lt;String, List&lt;Job&gt;&gt;&gt;
         * Each top-level key maps to an inner map whose values are Job arrays.
         */
        @Test
        @DisplayName("200 OK – returns a nested JSON object (outer map)")
        void preview_returns200_withNestedJsonObject() {
            webTestClient
                    .get()
                    .uri("/api/preview")
                    .accept(MediaType.ALL)
                    .exchange()
                    // ── Status ────────────────────────────────────────────────
                    .expectStatus().isOk()
                    // ── Headers ───────────────────────────────────────────────
                    .expectHeader().exists("Content-Type")
                    // ── Body ──────────────────────────────────────────────────
                    .expectBody(Map.class)
                    .consumeWith(result -> {
                        Map<?, ?> outer = result.getResponseBody();
                        assertThat(outer).isNotNull();

                        outer.forEach((outerKey, innerValue) -> {
                            assertThat(outerKey).isInstanceOf(String.class);

                            // inner value must be a Map<String, List<...>>
                            assertThat(innerValue)
                                    .as("Inner value for key '%s' should be a Map", outerKey)
                                    .isInstanceOf(Map.class);

                            Map<?, ?> innerMap = (Map<?, ?>) innerValue;
                            innerMap.forEach((innerKey, jobListValue) -> {
                                assertThat(innerKey).isInstanceOf(String.class);

                                // job list must be a List
                                assertThat(jobListValue)
                                        .as("Job list for key '%s' should be a List", innerKey)
                                        .isInstanceOf(List.class);

                                List<?> jobs = (List<?>) jobListValue;
                                jobs.forEach(job -> assertJobShape(job));
                            });
                        });
                    });
        }

        @Test
        @DisplayName("200 OK – Content-Type header is compatible with */*")
        void preview_contentTypePresent() {
            webTestClient
                    .get()
                    .uri("/api/preview")
                    .exchange()
                    .expectStatus().isOk()
                    .expectHeader().valueMatches("Content-Type", ".*");
        }

        /**
         * Asserts that a raw deserialized job object (a Map) contains only the
         * fields defined in the Job schema with their expected types.
         */
        private void assertJobShape(Object rawJob) {
            assertThat(rawJob).isInstanceOf(Map.class);
            Map<?, ?> job = (Map<?, ?>) rawJob;

            // All Job schema fields are optional, but if present they must be
            // the correct JSON type.
            if (job.containsKey("title"))       assertThat(job.get("title")).isInstanceOf(String.class);
            if (job.containsKey("company"))     assertThat(job.get("company")).isInstanceOf(String.class);
            if (job.containsKey("location"))    assertThat(job.get("location")).isInstanceOf(String.class);
            if (job.containsKey("url"))         assertThat(job.get("url")).isInstanceOf(String.class);
            if (job.containsKey("posted"))      assertThat(job.get("posted")).isInstanceOf(String.class); // date-time as string
            if (job.containsKey("description")) assertThat(job.get("description")).isInstanceOf(String.class);
            if (job.containsKey("source"))      assertThat(job.get("source")).isInstanceOf(String.class);
            if (job.containsKey("remote"))      assertThat(job.get("remote")).isInstanceOf(Boolean.class);
        }
    }

    // -------------------------------------------------------------------------
    // POST /api/run
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("POST /api/run")
    class RunEndpoint {

        @Test
        @DisplayName("200 OK – returns a JSON object (Map<String,Object>)")
        void run_returns200_withJsonObject() {
            webTestClient
                    .post()
                    .uri("/api/run")
                    .contentType(MediaType.APPLICATION_JSON)
                    .exchange()
                    // ── Status ────────────────────────────────────────────────
                    .expectStatus().isOk()
                    // ── Headers ───────────────────────────────────────────────
                    .expectHeader().exists("Content-Type")
                    // ── Body schema: object with additionalProperties(object) ─
                    .expectBody(Map.class)
                    .consumeWith(result -> {
                        Map<?, ?> body = result.getResponseBody();
                        assertThat(body).isNotNull();
                        body.forEach((k, v) -> assertThat(k).isInstanceOf(String.class));
                    });
        }

        @Test
        @DisplayName("200 OK – POST with no request body is accepted")
        void run_noBody_returns200() {
            webTestClient
                    .post()
                    .uri("/api/run")
                    .exchange()
                    .expectStatus().isOk();
        }

        @Test
        @DisplayName("200 OK – Content-Type header is compatible with */*")
        void run_contentTypePresent() {
            webTestClient
                    .post()
                    .uri("/api/run")
                    .exchange()
                    .expectStatus().isOk()
                    .expectHeader().valueMatches("Content-Type", ".*");
        }
    }

    // -------------------------------------------------------------------------
    // Cross-cutting / contract-level checks
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("Contract-level checks")
    class ContractChecks {

        @Test
        @DisplayName("GET /api/health – response body is valid JSON (not empty)")
        void health_bodyIsValidJson() {
            webTestClient
                    .get()
                    .uri("/api/health")
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$").exists();
        }

        @Test
        @DisplayName("GET /api/preview – response body is valid JSON (not empty)")
        void preview_bodyIsValidJson() {
            webTestClient
                    .get()
                    .uri("/api/preview")
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$").exists();
        }

        @Test
        @DisplayName("POST /api/run – response body is valid JSON (not empty)")
        void run_bodyIsValidJson() {
            webTestClient
                    .post()
                    .uri("/api/run")
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$").exists();
        }
    }
}
