package io.github.glaciousm.core.model;

import org.junit.jupiter.api.*;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for UiSnapshot model class.
 */
@DisplayName("UiSnapshot")
class UiSnapshotTest {

    private ElementSnapshot createElement(int index, String tagName) {
        return ElementSnapshot.builder()
                .index(index)
                .tagName(tagName)
                .visible(true)
                .enabled(true)
                .build();
    }

    @Nested
    @DisplayName("Builder")
    class BuilderTests {

        @Test
        @DisplayName("should create snapshot with all fields")
        void shouldCreateSnapshotWithAllFields() {
            Instant now = Instant.now();
            List<ElementSnapshot> elements = List.of(
                    createElement(0, "button"),
                    createElement(1, "input")
            );

            UiSnapshot snapshot = UiSnapshot.builder()
                    .url("https://example.com")
                    .title("Test Page")
                    .detectedLanguage("en")
                    .interactiveElements(elements)
                    .timestamp(now)
                    .screenshotBase64("base64data")
                    .domSnapshot("<html></html>")
                    .build();

            assertThat(snapshot.getUrl()).isEqualTo("https://example.com");
            assertThat(snapshot.getTitle()).isEqualTo("Test Page");
            assertThat(snapshot.getDetectedLanguage()).isEqualTo("en");
            assertThat(snapshot.getInteractiveElements()).hasSize(2);
            assertThat(snapshot.getTimestamp()).isEqualTo(now);
            assertThat(snapshot.getScreenshotBase64()).contains("base64data");
            assertThat(snapshot.getDomSnapshot()).contains("<html></html>");
        }

        @Test
        @DisplayName("should use default timestamp when not provided")
        void shouldUseDefaultTimestamp() {
            Instant before = Instant.now();

            UiSnapshot snapshot = UiSnapshot.builder()
                    .url("https://example.com")
                    .title("Page")
                    .build();

            Instant after = Instant.now();

            assertThat(snapshot.getTimestamp()).isAfterOrEqualTo(before);
            assertThat(snapshot.getTimestamp()).isBeforeOrEqualTo(after);
        }

        @Test
        @DisplayName("should handle null elements list")
        void shouldHandleNullElementsList() {
            UiSnapshot snapshot = UiSnapshot.builder()
                    .url("https://example.com")
                    .title("Page")
                    .interactiveElements(null)
                    .build();

            assertThat(snapshot.getInteractiveElements()).isEmpty();
        }
    }

    @Nested
    @DisplayName("Element Access")
    class ElementAccessTests {

        @Test
        @DisplayName("getElement should return element by index")
        void getElementShouldReturnElementByIndex() {
            List<ElementSnapshot> elements = List.of(
                    createElement(0, "button"),
                    createElement(5, "input"),
                    createElement(10, "select")
            );

            UiSnapshot snapshot = UiSnapshot.builder()
                    .url("https://example.com")
                    .title("Page")
                    .interactiveElements(elements)
                    .build();

            Optional<ElementSnapshot> element = snapshot.getElement(5);

            assertThat(element).isPresent();
            assertThat(element.get().getTagName()).isEqualTo("input");
        }

        @Test
        @DisplayName("getElement should return empty for non-existent index")
        void getElementShouldReturnEmptyForNonExistentIndex() {
            UiSnapshot snapshot = UiSnapshot.builder()
                    .url("https://example.com")
                    .title("Page")
                    .interactiveElements(List.of(createElement(0, "button")))
                    .build();

            Optional<ElementSnapshot> element = snapshot.getElement(99);

            assertThat(element).isEmpty();
        }

        @Test
        @DisplayName("getElementRequired should throw for non-existent index")
        void getElementRequiredShouldThrowForNonExistentIndex() {
            UiSnapshot snapshot = UiSnapshot.builder()
                    .url("https://example.com")
                    .title("Page")
                    .interactiveElements(List.of(createElement(0, "button")))
                    .build();

            assertThatThrownBy(() -> snapshot.getElementRequired(99))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Element with index 99 not found");
        }

        @Test
        @DisplayName("getElementRequired should return element for existing index")
        void getElementRequiredShouldReturnElementForExistingIndex() {
            UiSnapshot snapshot = UiSnapshot.builder()
                    .url("https://example.com")
                    .title("Page")
                    .interactiveElements(List.of(createElement(0, "button")))
                    .build();

            ElementSnapshot element = snapshot.getElementRequired(0);

            assertThat(element.getTagName()).isEqualTo("button");
        }
    }

    @Nested
    @DisplayName("Element Filtering")
    class ElementFilteringTests {

        @Test
        @DisplayName("getElementsByTag should filter by tag name")
        void getElementsByTagShouldFilterByTagName() {
            List<ElementSnapshot> elements = List.of(
                    createElement(0, "button"),
                    createElement(1, "input"),
                    createElement(2, "button"),
                    createElement(3, "select")
            );

            UiSnapshot snapshot = UiSnapshot.builder()
                    .url("https://example.com")
                    .title("Page")
                    .interactiveElements(elements)
                    .build();

            List<ElementSnapshot> buttons = snapshot.getElementsByTag("button");

            assertThat(buttons).hasSize(2);
            assertThat(buttons).allMatch(e -> e.getTagName().equals("button"));
        }

        @Test
        @DisplayName("getInteractableElements should filter visible and enabled")
        void getInteractableElementsShouldFilterVisibleAndEnabled() {
            ElementSnapshot visible = ElementSnapshot.builder()
                    .index(0)
                    .tagName("button")
                    .visible(true)
                    .enabled(true)
                    .build();
            ElementSnapshot hidden = ElementSnapshot.builder()
                    .index(1)
                    .tagName("input")
                    .visible(false)
                    .enabled(true)
                    .build();
            ElementSnapshot disabled = ElementSnapshot.builder()
                    .index(2)
                    .tagName("button")
                    .visible(true)
                    .enabled(false)
                    .build();

            UiSnapshot snapshot = UiSnapshot.builder()
                    .url("https://example.com")
                    .title("Page")
                    .interactiveElements(List.of(visible, hidden, disabled))
                    .build();

            List<ElementSnapshot> interactable = snapshot.getInteractableElements();

            assertThat(interactable).hasSize(1);
            assertThat(interactable.get(0).getIndex()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("Convenience Methods")
    class ConvenienceMethodTests {

        @Test
        @DisplayName("getElementCount should return correct count")
        void getElementCountShouldReturnCorrectCount() {
            UiSnapshot snapshot = UiSnapshot.builder()
                    .url("https://example.com")
                    .title("Page")
                    .interactiveElements(List.of(
                            createElement(0, "button"),
                            createElement(1, "input"),
                            createElement(2, "select")
                    ))
                    .build();

            assertThat(snapshot.getElementCount()).isEqualTo(3);
        }

        @Test
        @DisplayName("hasElements should return true when elements exist")
        void hasElementsShouldReturnTrueWhenElementsExist() {
            UiSnapshot snapshot = UiSnapshot.builder()
                    .url("https://example.com")
                    .title("Page")
                    .interactiveElements(List.of(createElement(0, "button")))
                    .build();

            assertThat(snapshot.hasElements()).isTrue();
        }

        @Test
        @DisplayName("hasElements should return false when no elements")
        void hasElementsShouldReturnFalseWhenNoElements() {
            UiSnapshot snapshot = UiSnapshot.builder()
                    .url("https://example.com")
                    .title("Page")
                    .interactiveElements(List.of())
                    .build();

            assertThat(snapshot.hasElements()).isFalse();
        }

        @Test
        @DisplayName("getHtml should return DOM snapshot")
        void getHtmlShouldReturnDomSnapshot() {
            UiSnapshot snapshot = UiSnapshot.builder()
                    .url("https://example.com")
                    .title("Page")
                    .domSnapshot("<html><body></body></html>")
                    .build();

            assertThat(snapshot.getHtml()).isEqualTo("<html><body></body></html>");
        }
    }

    @Nested
    @DisplayName("Optional Fields")
    class OptionalFieldTests {

        @Test
        @DisplayName("getScreenshotBase64 should return empty when not set")
        void getScreenshotShouldReturnEmptyWhenNotSet() {
            UiSnapshot snapshot = UiSnapshot.builder()
                    .url("https://example.com")
                    .title("Page")
                    .build();

            assertThat(snapshot.getScreenshotBase64()).isEmpty();
        }

        @Test
        @DisplayName("getDomSnapshot should return empty when not set")
        void getDomSnapshotShouldReturnEmptyWhenNotSet() {
            UiSnapshot snapshot = UiSnapshot.builder()
                    .url("https://example.com")
                    .title("Page")
                    .build();

            assertThat(snapshot.getDomSnapshot()).isEmpty();
        }
    }

    @Nested
    @DisplayName("equals and hashCode")
    class EqualsHashCodeTests {

        @Test
        @DisplayName("equal snapshots should be equal")
        void equalSnapshotsShouldBeEqual() {
            UiSnapshot s1 = UiSnapshot.builder()
                    .url("https://example.com")
                    .title("Page")
                    .interactiveElements(List.of(createElement(0, "button")))
                    .build();
            UiSnapshot s2 = UiSnapshot.builder()
                    .url("https://example.com")
                    .title("Page")
                    .interactiveElements(List.of(createElement(0, "button")))
                    .build();

            assertThat(s1).isEqualTo(s2);
            assertThat(s1.hashCode()).isEqualTo(s2.hashCode());
        }

        @Test
        @DisplayName("should not be equal to null")
        void shouldNotBeEqualToNull() {
            UiSnapshot snapshot = UiSnapshot.builder()
                    .url("https://example.com")
                    .title("Page")
                    .build();

            assertThat(snapshot).isNotEqualTo(null);
        }
    }

    @Nested
    @DisplayName("toString")
    class ToStringTests {

        @Test
        @DisplayName("should include url, title, and element count")
        void shouldIncludeKeyInfo() {
            UiSnapshot snapshot = UiSnapshot.builder()
                    .url("https://example.com/login")
                    .title("Login Page")
                    .interactiveElements(List.of(
                            createElement(0, "button"),
                            createElement(1, "input")
                    ))
                    .build();

            String str = snapshot.toString();

            assertThat(str).contains("https://example.com/login");
            assertThat(str).contains("Login Page");
            assertThat(str).contains("elements=2");
        }
    }
}
