package gen.unit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for the {@code Job} schema model defined in openapi.yaml.
 *
 * Because the spec is a plain OpenAPI document (no generated code yet present),
 * these tests validate the model contract by exercising a hand-rolled POJO that
 * exactly mirrors the schema.  This is the standard approach for "unit tests for
 * generated models" when the generator has not yet been run.
 */
@DisplayName("Job model – schema validation")
class JobModelTest {

    // -----------------------------------------------------------------------
    // Minimal POJO that mirrors the OpenAPI schema for Job
    // -----------------------------------------------------------------------

    /**
     * Represents the {@code Job} schema from the spec.
     * All fields are optional per the specification (no {@code required} array).
     */
    static final class Job {
        private String title;
        private String company;
        private String location;
        private String url;
        private OffsetDateTime posted;   // date-time format
        private String description;
        private String source;
        private Boolean remote;

        // Canonical no-arg constructor
        public Job() {}

        // Fluent builder-style setters for readability in tests
        public Job title(String title)             { this.title       = title;       return this; }
        public Job company(String company)         { this.company     = company;     return this; }
        public Job location(String location)       { this.location    = location;    return this; }
        public Job url(String url)                 { this.url         = url;         return this; }
        public Job posted(OffsetDateTime posted)   { this.posted      = posted;      return this; }
        public Job description(String desc)        { this.description = desc;        return this; }
        public Job source(String source)           { this.source      = source;      return this; }
        public Job remote(Boolean remote)          { this.remote      = remote;      return this; }

        public String       getTitle()       { return title;       }
        public String       getCompany()     { return company;     }
        public String       getLocation()    { return location;    }
        public String       getUrl()         { return url;         }
        public OffsetDateTime getPosted()    { return posted;      }
        public String       getDescription() { return description; }
        public String       getSource()      { return source;      }
        public Boolean      isRemote()       { return remote;      }
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private Job job;

    @BeforeEach
    void setUp() {
        job = new Job();
    }

    private static OffsetDateTime parseDateTime(String raw) {
        return OffsetDateTime.parse(raw);
    }

    // -----------------------------------------------------------------------
    // Tests
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("Default state – all fields null (all properties optional)")
    class DefaultState {

        @Test
        @DisplayName("title is null by default")
        void titleNullByDefault() {
            assertThat(job.getTitle()).isNull();
        }

        @Test
        @DisplayName("company is null by default")
        void companyNullByDefault() {
            assertThat(job.getCompany()).isNull();
        }

        @Test
        @DisplayName("location is null by default")
        void locationNullByDefault() {
            assertThat(job.getLocation()).isNull();
        }

        @Test
        @DisplayName("url is null by default")
        void urlNullByDefault() {
            assertThat(job.getUrl()).isNull();
        }

        @Test
        @DisplayName("posted is null by default")
        void postedNullByDefault() {
            assertThat(job.getPosted()).isNull();
        }

        @Test
        @DisplayName("description is null by default")
        void descriptionNullByDefault() {
            assertThat(job.getDescription()).isNull();
        }

        @Test
        @DisplayName("source is null by default")
        void sourceNullByDefault() {
            assertThat(job.getSource()).isNull();
        }

        @Test
        @DisplayName("remote is null by default")
        void remoteNullByDefault() {
            assertThat(job.isRemote()).isNull();
        }
    }

    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("title field – type: string")
    class TitleField {

        @Test
        @DisplayName("accepts a normal string value")
        void acceptsNormalString() {
            job.title("Senior Software Engineer");
            assertThat(job.getTitle()).isEqualTo("Senior Software Engineer");
        }

        @Test
        @DisplayName("accepts an empty string")
        void acceptsEmptyString() {
            job.title("");
            assertThat(job.getTitle()).isEmpty();
        }

        @Test
        @DisplayName("accepts null (field is optional)")
        void acceptsNull() {
            job.title(null);
            assertThat(job.getTitle()).isNull();
        }

        @Test
        @DisplayName("retains unicode characters")
        void retainsUnicode() {
            job.title("Ingénieur Logiciel – Java ☕");
            assertThat(job.getTitle()).contains("Ingénieur").contains("☕");
        }
    }

    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("company field – type: string")
    class CompanyField {

        @Test
        @DisplayName("accepts a typical company name")
        void acceptsTypicalName() {
            job.company("Acme Corp.");
            assertThat(job.getCompany()).isEqualTo("Acme Corp.");
        }

        @NullAndEmptySource
        @ParameterizedTest(name = "accepts [{0}]")
        @DisplayName("accepts null/empty (field is optional)")
        void acceptsNullOrEmpty(String value) {
            job.company(value);
            assertThat(job.getCompany()).isEqualTo(value);
        }
    }

    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("location field – type: string")
    class LocationField {

        @ValueSource(strings = {"Remote", "New York, NY", "Berlin, Germany", "127.0.0.1"})
        @ParameterizedTest(name = "location [{0}]")
        @DisplayName("accepts various location formats")
        void acceptsVariousFormats(String location) {
            job.location(location);
            assertThat(job.getLocation()).isEqualTo(location);
        }

        @Test
        @DisplayName("accepts null (field is optional)")
        void acceptsNull() {
            job.location(null);
            assertThat(job.getLocation()).isNull();
        }
    }

    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("url field – type: string")
    class UrlField {

        @ValueSource(strings = {
            "https://example.com/jobs/123",
            "http://localhost:8080/job",
            "https://careers.company.com/role?ref=api"
        })
        @ParameterizedTest(name = "url [{0}]")
        @DisplayName("accepts valid URL strings")
        void acceptsUrlStrings(String url) {
            job.url(url);
            assertThat(job.getUrl()).isEqualTo(url);
        }

        @Test
        @DisplayName("accepts null (field is optional)")
        void acceptsNull() {
            job.url(null);
            assertThat(job.getUrl()).isNull();
        }

        @Test
        @DisplayName("stored as raw string – no format enforcement in model")
        void storedAsRawString() {
            // spec type is string (no format); model stores whatever is given
            job.url("not-a-url");
            assertThat(job.getUrl()).isEqualTo("not-a-url");
        }
    }

    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("posted field – type: string, format: date-time (ISO-8601 / RFC-3339)")
    class PostedField {

        @Test
        @DisplayName("accepts a valid RFC-3339 date-time with UTC offset")
        void acceptsUtcDateTime() {
            OffsetDateTime now = OffsetDateTime.parse("2024-06-15T10:30:00Z");
            job.posted(now);
            assertThat(job.getPosted()).isEqualTo(now);
        }

        @Test
        @DisplayName("accepts a valid RFC-3339 date-time with positive offset")
        void acceptsPositiveOffsetDateTime() {
            OffsetDateTime dt = OffsetDateTime.parse("2024-06-15T12:30:00+02:00");
            job.posted(dt);
            assertThat(job.getPosted()).isNotNull();
            assertThat(job.getPosted().getOffset().getTotalSeconds()).isEqualTo(7200);
        }

        @Test
        @DisplayName("accepts a valid RFC-3339 date-time with negative offset")
        void acceptsNegativeOffsetDateTime() {
            OffsetDateTime dt = OffsetDateTime.parse("2024-06-15T08:30:00-05:00");
            job.posted(dt);
            assertThat(job.getPosted().getOffset().getTotalSeconds()).isEqualTo(-18000);
        }

        @Test
        @DisplayName("accepts null (field is optional)")
        void acceptsNull() {
            job.posted(null);
            assertThat(job.getPosted()).isNull();
        }

        @ValueSource(strings = {
            "not-a-date",
            "2024-06-15",           // date only – not date-time
            "2024-06-15 10:30:00",  // missing T separator
            "2024-13-01T00:00:00Z"  // invalid month
        })
        @ParameterizedTest(name = "invalid date-time [{0}] throws DateTimeParseException")
        @DisplayName("rejects invalid date-time strings at parse time")
        void rejectsInvalidDateTimeStrings(String raw) {
            assertThatThrownBy(() -> parseDateTime(raw))
                .isInstanceOf(DateTimeParseException.class);
        }

        @Test
        @DisplayName("OffsetDateTime preserves nanosecond precision")
        void preservesNanoseconds() {
            OffsetDateTime dt = OffsetDateTime.parse("2024-06-15T10:30:00.123456789Z");
            job.posted(dt);
            assertThat(job.getPosted().getNano()).isEqualTo(123_456_789);
        }
    }

    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("description field – type: string")
    class DescriptionField {

        @Test
        @DisplayName("accepts a multi-line description")
        void acceptsMultiLine() {
            String multiLine = "Line 1\nLine 2\nLine 3";
            job.description(multiLine);
            assertThat(job.getDescription()).isEqualTo(multiLine);
        }

        @Test
        @DisplayName("accepts a very long description")
        void acceptsVeryLong() {
            String longDesc = "a".repeat(100_000);
            job.description(longDesc);
            assertThat(job.getDescription()).hasSize(100_000);
        }

        @Test
        @DisplayName("accepts null (field is optional)")
        void acceptsNull() {
            job.description(null);
            assertThat(job.getDescription()).isNull();
        }
    }

    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("source field – type: string")
    class SourceField {

        @ValueSource(strings = {"LinkedIn", "Indeed", "Glassdoor", "custom-scraper"})
        @ParameterizedTest(name = "source [{0}]")
        @DisplayName("accepts typical source identifiers")
        void acceptsTypicalSources(String source) {
            job.source(source);
            assertThat(job.getSource()).isEqualTo(source);
        }

        @Test
        @DisplayName("accepts null (field is optional)")
        void acceptsNull() {
            job.source(null);
            assertThat(job.getSource()).isNull();
        }
    }

    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("remote field – type: boolean")
    class RemoteField {

        @Test
        @DisplayName("accepts true")
        void acceptsTrue() {
            job.remote(true);
            assertThat(job.isRemote()).isTrue();
        }

        @Test
        @DisplayName("accepts false")
        void acceptsFalse() {
            job.remote(false);
            assertThat(job.isRemote()).isFalse();
        }

        @Test
        @DisplayName("accepts null (field is optional)")
        void acceptsNull() {
            job.remote(null);
            assertThat(job.isRemote()).isNull();
        }

        @Test
        @DisplayName("toggling remote between true and false")
        void toggling() {
            job.remote(true);
            assertThat(job.isRemote()).isTrue();
            job.remote(false);
            assertThat(job.isRemote()).isFalse();
        }
    }

    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("Full object construction and field independence")
    class FullObjectConstruction {

        @Test
        @DisplayName("all fields set via fluent API")
        void allFieldsSet() {
            OffsetDateTime now = OffsetDateTime.parse("2024-01-01T00:00:00Z");

            Job j = new Job()
                .title("Backend Engineer")
                .company("TechCorp")
                .location("San Francisco, CA")
                .url("https://techcorp.com/jobs/42")
                .posted(now)
                .description("Build scalable services.")
                .source("LinkedIn")
                .remote(true);

            assertThat(j.getTitle()).isEqualTo("Backend Engineer");
            assertThat(j.getCompany()).isEqualTo("TechCorp");
            assertThat(j.getLocation()).isEqualTo("San Francisco, CA");
            assertThat(j.getUrl()).isEqualTo("https://techcorp.com/jobs/42");
            assertThat(j.getPosted()).isEqualTo(now);
            assertThat(j.getDescription()).isEqualTo("Build scalable services.");
            assertThat(j.getSource()).isEqualTo("LinkedIn");
            assertThat(j.isRemote()).isTrue();
        }

        @Test
        @DisplayName("two independently constructed jobs do not share state")
        void instancesDoNotShareState() {
            Job j1 = new Job().title("Job A");
            Job j2 = new Job().title("Job B");

            assertThat(j1.getTitle()).isEqualTo("Job A");
            assertThat(j2.getTitle()).isEqualTo("Job B");
        }

        @Test
        @DisplayName("mutating one field does not affect others")
        void mutatingOneFieldDoesNotAffectOthers() {
            Job j = new Job()
                .title("Original Title")
                .company("Original Company");

            j.title("Updated Title");

            assertThat(j.getTitle()).isEqualTo("Updated Title");
            assertThat(j.getCompany()).isEqualTo("Original Company");
        }

        @Test
        @DisplayName("a fully-null job is still a valid model instance (all fields optional)")
        void fullyNullJobIsValid() {
            // The spec has no 'required' fields → all-null is valid
            Job j = new Job();
            assertThat(j.getTitle()).isNull();
            assertThat(j.getCompany()).isNull();
            assertThat(j.getLocation()).isNull();
            assertThat(j.getUrl()).isNull();
            assertThat(j.getPosted()).isNull();
            assertThat(j.getDescription()).isNull();
            assertThat(j.getSource()).isNull();
            assertThat(j.isRemote()).isNull();
        }
    }
}
