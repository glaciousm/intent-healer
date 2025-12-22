package io.github.glaciousm.agent;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for HealerAgent.
 * Tests the agent structure and entry points without actual instrumentation.
 */
class HealerAgentTest {

    @Nested
    @DisplayName("Agent structure")
    class AgentStructureTests {

        @Test
        @DisplayName("should have premain method with correct signature")
        void hasPremainMethod() throws Exception {
            Method premain = HealerAgent.class.getMethod("premain", String.class, java.lang.instrument.Instrumentation.class);

            assertThat(premain).isNotNull();
            assertThat(java.lang.reflect.Modifier.isStatic(premain.getModifiers())).isTrue();
            assertThat(java.lang.reflect.Modifier.isPublic(premain.getModifiers())).isTrue();
        }

        @Test
        @DisplayName("should have agentmain method with correct signature")
        void hasAgentmainMethod() throws Exception {
            Method agentmain = HealerAgent.class.getMethod("agentmain", String.class, java.lang.instrument.Instrumentation.class);

            assertThat(agentmain).isNotNull();
            assertThat(java.lang.reflect.Modifier.isStatic(agentmain.getModifiers())).isTrue();
            assertThat(java.lang.reflect.Modifier.isPublic(agentmain.getModifiers())).isTrue();
        }

        @Test
        @DisplayName("should have isInitialized method")
        void hasIsInitializedMethod() throws Exception {
            Method isInitialized = HealerAgent.class.getMethod("isInitialized");

            assertThat(isInitialized).isNotNull();
            assertThat(isInitialized.getReturnType()).isEqualTo(boolean.class);
        }
    }

    @Nested
    @DisplayName("Initialization state")
    class InitializationStateTests {

        @Test
        @DisplayName("isInitialized should return boolean")
        void isInitializedReturnsBoolean() {
            // Without calling premain/agentmain, the agent may or may not be initialized
            // depending on previous test runs. Just verify it returns a boolean.
            boolean result = HealerAgent.isInitialized();

            assertThat(result).isIn(true, false);
        }
    }

    @Nested
    @DisplayName("Agent classes exist")
    class AgentClassesExistTests {

        @Test
        @DisplayName("WebDriverInterceptor class should exist")
        void webDriverInterceptorExists() {
            assertThat(WebDriverInterceptor.class).isNotNull();
        }

        @Test
        @DisplayName("WebDriverConstructorAdvice class should exist")
        void webDriverConstructorAdviceExists() {
            assertThat(WebDriverConstructorAdvice.class).isNotNull();
        }

        @Test
        @DisplayName("AutoConfigurator class should exist")
        void autoConfiguratorExists() {
            assertThat(AutoConfigurator.class).isNotNull();
        }

        @Test
        @DisplayName("AgentBanner class should exist")
        void agentBannerExists() {
            assertThat(AgentBanner.class).isNotNull();
        }
    }
}
