package io.github.glaciousm.agent;

import io.github.glaciousm.core.model.LocatorInfo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for AutoConfigurator.
 * Tests locator conversion methods using reflection since they are private.
 */
class AutoConfiguratorTest {

    @Nested
    @DisplayName("byToLocatorInfo conversion")
    class ByToLocatorInfoTests {

        @Test
        @DisplayName("should convert By.id to LocatorInfo")
        void convertsById() throws Exception {
            By by = By.id("test-id");
            LocatorInfo result = invokeByToLocatorInfo(by);

            assertThat(result.getStrategy()).isEqualTo(LocatorInfo.LocatorStrategy.ID);
            assertThat(result.getValue()).isEqualTo("test-id");
        }

        @Test
        @DisplayName("should convert By.name to LocatorInfo")
        void convertsByName() throws Exception {
            By by = By.name("test-name");
            LocatorInfo result = invokeByToLocatorInfo(by);

            assertThat(result.getStrategy()).isEqualTo(LocatorInfo.LocatorStrategy.NAME);
            assertThat(result.getValue()).isEqualTo("test-name");
        }

        @Test
        @DisplayName("should convert By.className to LocatorInfo")
        void convertsByClassName() throws Exception {
            By by = By.className("test-class");
            LocatorInfo result = invokeByToLocatorInfo(by);

            assertThat(result.getStrategy()).isEqualTo(LocatorInfo.LocatorStrategy.CLASS_NAME);
            assertThat(result.getValue()).isEqualTo("test-class");
        }

        @Test
        @DisplayName("should convert By.cssSelector to LocatorInfo")
        void convertsByCssSelector() throws Exception {
            By by = By.cssSelector("#test .class");
            LocatorInfo result = invokeByToLocatorInfo(by);

            assertThat(result.getStrategy()).isEqualTo(LocatorInfo.LocatorStrategy.CSS);
            assertThat(result.getValue()).isEqualTo("#test .class");
        }

        @Test
        @DisplayName("should convert By.xpath to LocatorInfo")
        void convertsByXpath() throws Exception {
            By by = By.xpath("//div[@id='test']");
            LocatorInfo result = invokeByToLocatorInfo(by);

            assertThat(result.getStrategy()).isEqualTo(LocatorInfo.LocatorStrategy.XPATH);
            assertThat(result.getValue()).isEqualTo("//div[@id='test']");
        }

        @Test
        @DisplayName("should convert By.linkText to LocatorInfo")
        void convertsByLinkText() throws Exception {
            By by = By.linkText("Click here");
            LocatorInfo result = invokeByToLocatorInfo(by);

            assertThat(result.getStrategy()).isEqualTo(LocatorInfo.LocatorStrategy.LINK_TEXT);
            assertThat(result.getValue()).isEqualTo("Click here");
        }

        @Test
        @DisplayName("should convert By.partialLinkText to LocatorInfo")
        void convertsByPartialLinkText() throws Exception {
            By by = By.partialLinkText("Click");
            LocatorInfo result = invokeByToLocatorInfo(by);

            assertThat(result.getStrategy()).isEqualTo(LocatorInfo.LocatorStrategy.PARTIAL_LINK_TEXT);
            assertThat(result.getValue()).isEqualTo("Click");
        }

        @Test
        @DisplayName("should convert By.tagName to LocatorInfo")
        void convertsByTagName() throws Exception {
            By by = By.tagName("button");
            LocatorInfo result = invokeByToLocatorInfo(by);

            assertThat(result.getStrategy()).isEqualTo(LocatorInfo.LocatorStrategy.TAG_NAME);
            assertThat(result.getValue()).isEqualTo("button");
        }
    }

    @Nested
    @DisplayName("parseLocatorString conversion")
    class ParseLocatorStringTests {

        @Test
        @DisplayName("should parse CSS selector starting with #")
        void parsesCssSelectorWithHash() throws Exception {
            LocatorInfo result = invokeParseLocatorString("#submit-btn");

            assertThat(result.getStrategy()).isEqualTo(LocatorInfo.LocatorStrategy.CSS);
            assertThat(result.getValue()).isEqualTo("#submit-btn");
        }

        @Test
        @DisplayName("should parse CSS selector starting with .")
        void parsesCssSelectorWithDot() throws Exception {
            LocatorInfo result = invokeParseLocatorString(".btn-primary");

            assertThat(result.getStrategy()).isEqualTo(LocatorInfo.LocatorStrategy.CSS);
            assertThat(result.getValue()).isEqualTo(".btn-primary");
        }

        @Test
        @DisplayName("should parse XPath starting with //")
        void parsesXpathWithDoubleSlash() throws Exception {
            LocatorInfo result = invokeParseLocatorString("//button[@type='submit']");

            assertThat(result.getStrategy()).isEqualTo(LocatorInfo.LocatorStrategy.XPATH);
            assertThat(result.getValue()).isEqualTo("//button[@type='submit']");
        }

        @Test
        @DisplayName("should parse XPath with parentheses")
        void parsesXpathWithParens() throws Exception {
            LocatorInfo result = invokeParseLocatorString("(//button)[1]");

            assertThat(result.getStrategy()).isEqualTo(LocatorInfo.LocatorStrategy.XPATH);
            assertThat(result.getValue()).isEqualTo("(//button)[1]");
        }

        @Test
        @DisplayName("should parse id: prefix")
        void parsesIdPrefix() throws Exception {
            LocatorInfo result = invokeParseLocatorString("id:submit-btn");

            assertThat(result.getStrategy()).isEqualTo(LocatorInfo.LocatorStrategy.ID);
            assertThat(result.getValue()).isEqualTo("submit-btn");
        }

        @Test
        @DisplayName("should parse id= prefix")
        void parsesIdEquals() throws Exception {
            LocatorInfo result = invokeParseLocatorString("id=submit-btn");

            assertThat(result.getStrategy()).isEqualTo(LocatorInfo.LocatorStrategy.ID);
            assertThat(result.getValue()).isEqualTo("submit-btn");
        }

        @Test
        @DisplayName("should parse name: prefix")
        void parsesNamePrefix() throws Exception {
            LocatorInfo result = invokeParseLocatorString("name:username");

            assertThat(result.getStrategy()).isEqualTo(LocatorInfo.LocatorStrategy.NAME);
            assertThat(result.getValue()).isEqualTo("username");
        }

        @Test
        @DisplayName("should parse css: prefix")
        void parsesCssPrefix() throws Exception {
            LocatorInfo result = invokeParseLocatorString("css:#form .input");

            assertThat(result.getStrategy()).isEqualTo(LocatorInfo.LocatorStrategy.CSS);
            assertThat(result.getValue()).isEqualTo("#form .input");
        }

        @Test
        @DisplayName("should parse xpath: prefix")
        void parsesXpathPrefix() throws Exception {
            LocatorInfo result = invokeParseLocatorString("xpath://div[@class='container']");

            assertThat(result.getStrategy()).isEqualTo(LocatorInfo.LocatorStrategy.XPATH);
            assertThat(result.getValue()).isEqualTo("//div[@class='container']");
        }

        @Test
        @DisplayName("should handle null input")
        void handlesNull() throws Exception {
            LocatorInfo result = invokeParseLocatorString(null);

            assertThat(result.getStrategy()).isEqualTo(LocatorInfo.LocatorStrategy.CSS);
            assertThat(result.getValue()).isEmpty();
        }

        @Test
        @DisplayName("should handle empty input")
        void handlesEmpty() throws Exception {
            LocatorInfo result = invokeParseLocatorString("");

            assertThat(result.getStrategy()).isEqualTo(LocatorInfo.LocatorStrategy.CSS);
            assertThat(result.getValue()).isEmpty();
        }

        @Test
        @DisplayName("should default unknown to CSS")
        void defaultsToCss() throws Exception {
            LocatorInfo result = invokeParseLocatorString("button.submit");

            assertThat(result.getStrategy()).isEqualTo(LocatorInfo.LocatorStrategy.CSS);
            assertThat(result.getValue()).isEqualTo("button.submit");
        }
    }

    @Nested
    @DisplayName("locatorInfoToBy conversion")
    class LocatorInfoToByTests {

        @Test
        @DisplayName("should convert ID to By.id")
        void convertsId() throws Exception {
            LocatorInfo locator = new LocatorInfo(LocatorInfo.LocatorStrategy.ID, "my-id");
            By result = invokeLocatorInfoToBy(locator);

            assertThat(result.toString()).contains("By.id: my-id");
        }

        @Test
        @DisplayName("should convert NAME to By.name")
        void convertsName() throws Exception {
            LocatorInfo locator = new LocatorInfo(LocatorInfo.LocatorStrategy.NAME, "my-name");
            By result = invokeLocatorInfoToBy(locator);

            assertThat(result.toString()).contains("By.name: my-name");
        }

        @Test
        @DisplayName("should convert CLASS_NAME to By.className")
        void convertsClassName() throws Exception {
            LocatorInfo locator = new LocatorInfo(LocatorInfo.LocatorStrategy.CLASS_NAME, "my-class");
            By result = invokeLocatorInfoToBy(locator);

            assertThat(result.toString()).contains("By.className: my-class");
        }

        @Test
        @DisplayName("should convert CSS to By.cssSelector")
        void convertsCss() throws Exception {
            LocatorInfo locator = new LocatorInfo(LocatorInfo.LocatorStrategy.CSS, "#id .class");
            By result = invokeLocatorInfoToBy(locator);

            assertThat(result.toString()).contains("By.cssSelector: #id .class");
        }

        @Test
        @DisplayName("should convert XPATH to By.xpath")
        void convertsXpath() throws Exception {
            LocatorInfo locator = new LocatorInfo(LocatorInfo.LocatorStrategy.XPATH, "//div");
            By result = invokeLocatorInfoToBy(locator);

            assertThat(result.toString()).contains("By.xpath: //div");
        }
    }

    @Nested
    @DisplayName("Round-trip conversion")
    class RoundTripTests {

        @Test
        @DisplayName("should preserve locator through By -> LocatorInfo -> String -> LocatorInfo -> By")
        void roundTripConversion() throws Exception {
            By original = By.id("test-element");

            // By -> LocatorInfo
            LocatorInfo locatorInfo = invokeByToLocatorInfo(original);

            // LocatorInfo -> string format
            String locatorString = locatorInfo.getStrategy().name().toLowerCase() + ":" + locatorInfo.getValue();

            // String -> LocatorInfo
            LocatorInfo parsed = invokeParseLocatorString(locatorString);

            // LocatorInfo -> By
            By result = invokeLocatorInfoToBy(parsed);

            assertThat(result.toString()).isEqualTo(original.toString());
        }
    }

    // Helper methods to invoke private static methods via reflection

    private LocatorInfo invokeByToLocatorInfo(By by) throws Exception {
        Method method = AutoConfigurator.class.getDeclaredMethod("byToLocatorInfo", By.class);
        method.setAccessible(true);
        return (LocatorInfo) method.invoke(null, by);
    }

    private LocatorInfo invokeParseLocatorString(String locatorStr) throws Exception {
        Method method = AutoConfigurator.class.getDeclaredMethod("parseLocatorString", String.class);
        method.setAccessible(true);
        return (LocatorInfo) method.invoke(null, locatorStr);
    }

    private By invokeLocatorInfoToBy(LocatorInfo locator) throws Exception {
        Method method = AutoConfigurator.class.getDeclaredMethod("locatorInfoToBy", LocatorInfo.class);
        method.setAccessible(true);
        return (By) method.invoke(null, locator);
    }
}
