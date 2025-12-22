package io.github.glaciousm.intellij.ui;

import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiIdentifier;
import com.intellij.psi.PsiMethod;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import io.github.glaciousm.intellij.settings.HealerSettings;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for IntentLineMarkerProvider.
 * Uses mocking to simulate IntelliJ PSI elements.
 *
 * Note: Tests for positive cases (where line markers are created) require
 * IntelliJ platform runtime and are tested via integration tests.
 * These unit tests focus on the logic that returns null.
 */
@ExtendWith(MockitoExtension.class)
class IntentLineMarkerProviderTest {

    private IntentLineMarkerProvider provider;

    @Mock
    private PsiIdentifier mockIdentifier;

    @Mock
    private PsiMethod mockMethod;

    @Mock
    private PsiAnnotation mockIntentAnnotation;

    @Mock
    private PsiAnnotation mockOutcomeAnnotation;

    @Mock
    private HealerSettings mockSettings;

    @BeforeEach
    void setUp() {
        provider = new IntentLineMarkerProvider();
    }

    @Nested
    @DisplayName("When line markers are disabled")
    class LineMarkersDisabledTests {

        @Test
        @DisplayName("should return null when showLineMarkers is false")
        void returnsNullWhenDisabled() {
            try (MockedStatic<HealerSettings> settingsMock = mockStatic(HealerSettings.class)) {
                mockSettings.showLineMarkers = false;
                settingsMock.when(HealerSettings::getInstance).thenReturn(mockSettings);

                var result = provider.getLineMarkerInfo(mockIdentifier);

                assertNull(result);
            }
        }
    }

    @Nested
    @DisplayName("When element is not a PsiIdentifier")
    class NonIdentifierElementTests {

        @Test
        @DisplayName("should return null for non-PsiIdentifier elements")
        void returnsNullForNonIdentifier() {
            try (MockedStatic<HealerSettings> settingsMock = mockStatic(HealerSettings.class)) {
                mockSettings.showLineMarkers = true;
                settingsMock.when(HealerSettings::getInstance).thenReturn(mockSettings);

                PsiElement mockElement = mock(PsiElement.class);
                var result = provider.getLineMarkerInfo(mockElement);

                assertNull(result);
            }
        }
    }

    @Nested
    @DisplayName("When parent is not a PsiMethod")
    class NonMethodParentTests {

        @Test
        @DisplayName("should return null when parent is not a method")
        void returnsNullForNonMethodParent() {
            try (MockedStatic<HealerSettings> settingsMock = mockStatic(HealerSettings.class)) {
                mockSettings.showLineMarkers = true;
                settingsMock.when(HealerSettings::getInstance).thenReturn(mockSettings);

                PsiElement mockParent = mock(PsiElement.class);
                when(mockIdentifier.getParent()).thenReturn(mockParent);

                var result = provider.getLineMarkerInfo(mockIdentifier);

                assertNull(result);
            }
        }
    }

    @Nested
    @DisplayName("When method has @Intent annotation")
    class IntentAnnotationTests {

        @Test
        @DisplayName("should check for fully qualified @Intent annotation first")
        void checksFullyQualifiedIntentFirst() {
            try (MockedStatic<HealerSettings> settingsMock = mockStatic(HealerSettings.class)) {
                mockSettings.showLineMarkers = true;
                settingsMock.when(HealerSettings::getInstance).thenReturn(mockSettings);

                when(mockIdentifier.getParent()).thenReturn(mockMethod);
                when(mockMethod.getAnnotation("io.github.glaciousm.cucumber.annotations.Intent"))
                        .thenReturn(mockIntentAnnotation);

                // Verify annotation lookup was performed
                try {
                    provider.getLineMarkerInfo(mockIdentifier);
                } catch (Exception e) {
                    // NPE expected due to missing IntelliJ platform
                }

                verify(mockMethod).getAnnotation("io.github.glaciousm.cucumber.annotations.Intent");
            }
        }

        @Test
        @DisplayName("should fallback to simple @Intent annotation")
        void fallsBackToSimpleIntent() {
            try (MockedStatic<HealerSettings> settingsMock = mockStatic(HealerSettings.class)) {
                mockSettings.showLineMarkers = true;
                settingsMock.when(HealerSettings::getInstance).thenReturn(mockSettings);

                when(mockIdentifier.getParent()).thenReturn(mockMethod);
                when(mockMethod.getAnnotation("io.github.glaciousm.cucumber.annotations.Intent"))
                        .thenReturn(null);
                when(mockMethod.getAnnotation("Intent")).thenReturn(mockIntentAnnotation);

                // Verify annotation lookup was performed
                try {
                    provider.getLineMarkerInfo(mockIdentifier);
                } catch (Exception e) {
                    // NPE expected due to missing IntelliJ platform
                }

                verify(mockMethod).getAnnotation("Intent");
            }
        }
    }

    @Nested
    @DisplayName("When method has @Outcome annotation")
    class OutcomeAnnotationTests {

        @Test
        @DisplayName("should check for @Outcome annotation after Intent")
        void checksOutcomeAfterIntent() {
            try (MockedStatic<HealerSettings> settingsMock = mockStatic(HealerSettings.class)) {
                mockSettings.showLineMarkers = true;
                settingsMock.when(HealerSettings::getInstance).thenReturn(mockSettings);

                when(mockIdentifier.getParent()).thenReturn(mockMethod);
                when(mockMethod.getAnnotation("io.github.glaciousm.cucumber.annotations.Intent"))
                        .thenReturn(null);
                when(mockMethod.getAnnotation("Intent")).thenReturn(null);
                when(mockMethod.getAnnotation("io.github.glaciousm.cucumber.annotations.Outcome"))
                        .thenReturn(mockOutcomeAnnotation);

                // Verify annotation lookup was performed
                try {
                    provider.getLineMarkerInfo(mockIdentifier);
                } catch (Exception e) {
                    // NPE expected due to missing IntelliJ platform
                }

                verify(mockMethod).getAnnotation("io.github.glaciousm.cucumber.annotations.Outcome");
            }
        }
    }

    @Nested
    @DisplayName("When method has no relevant annotations")
    class NoAnnotationTests {

        @Test
        @DisplayName("should return null when no Intent or Outcome annotation")
        void returnsNullForNoAnnotation() {
            try (MockedStatic<HealerSettings> settingsMock = mockStatic(HealerSettings.class)) {
                mockSettings.showLineMarkers = true;
                settingsMock.when(HealerSettings::getInstance).thenReturn(mockSettings);

                when(mockIdentifier.getParent()).thenReturn(mockMethod);
                when(mockMethod.getAnnotation(anyString())).thenReturn(null);

                var result = provider.getLineMarkerInfo(mockIdentifier);

                assertNull(result);
            }
        }
    }
}
