package io.github.glaciousm.llm;

import io.github.glaciousm.core.model.ElementRect;
import io.github.glaciousm.core.model.ElementSnapshot;
import io.github.glaciousm.core.model.FailureContext;
import io.github.glaciousm.core.model.IntentContract;
import io.github.glaciousm.core.model.UiSnapshot;

import java.util.List;

/**
 * Builds prompts for LLM healing requests.
 */
public class PromptBuilder {

    private static final int MAX_ELEMENTS_IN_PROMPT = 50;
    private static final int MAX_TEXT_LENGTH = 100;

    /**
     * Build the system prompt for healing operations.
     */
    public String buildSystemPrompt() {
        return "You are an expert test automation engineer helping to fix broken UI test selectors.";
    }

    /**
     * Build the healing prompt from failure context and UI snapshot.
     * Alias for buildEvaluationPrompt.
     */
    public String buildEvaluationPrompt(FailureContext failure, UiSnapshot snapshot, IntentContract intent) {
        return buildHealingPrompt(failure, snapshot, intent);
    }

    /**
     * Build the healing prompt from failure context and UI snapshot.
     */
    public String buildHealingPrompt(FailureContext failure, UiSnapshot snapshot, IntentContract intent) {
        return """
            You are an expert test automation engineer analyzing a UI test failure.

            ## Test Context

            **Feature:** %s
            **Scenario:** %s
            **Step:** %s %s
            **Intent:** %s
            **Intent Description:** %s

            ## Failure Information

            **Exception:** %s
            **Original Locator:** %s (strategy: %s)
            **Action:** %s

            ## Current Page State

            **URL:** %s
            **Title:** %s
            **Detected Language:** %s

            ## Available Interactive Elements

            %s

            ## Your Task

            Analyze the test step's intent and the current page state. Determine if there is an element on the current page that serves the same purpose as the original target.

            **Important Guidelines:**
            - Focus on SEMANTIC PURPOSE, not exact text matching
            - Consider that the UI may be in any language
            - The element's purpose matters more than its appearance
            - If multiple candidates could work, choose the most likely based on context
            - If no element clearly matches the intent, respond that healing is not possible
            - NEVER suggest elements that could cause destructive actions (delete, remove, cancel) unless the original intent was destructive

            ## Response Format

            Respond with ONLY a JSON object in this exact format:

            ```json
            {
              "can_heal": true|false,
              "confidence": 0.0-1.0,
              "selected_element_index": <index>|null,
              "reasoning": "<2-3 sentences explaining your decision>",
              "alternative_indices": [<other possible indices>],
              "warnings": ["<any concerns about this heal>"],
              "refusal_reason": "<if can_heal is false, explain why>"|null
            }
            ```

            Confidence guide:
            - 0.95+: Nearly certain match (same text, clear purpose)
            - 0.85-0.94: High confidence (semantic match, clear context)
            - 0.75-0.84: Moderate confidence (likely match, some ambiguity)
            - Below 0.75: Do not heal, set can_heal to false
            """.formatted(
                nullSafe(failure.getFeatureName()),
                nullSafe(failure.getScenarioName()),
                nullSafe(failure.getStepKeyword()),
                nullSafe(failure.getStepText()),
                nullSafe(intent.getAction()),
                nullSafe(intent.getDescription()),
                nullSafe(failure.getExceptionType()),
                failure.getOriginalLocator() != null ? failure.getOriginalLocator().getValue() : "unknown",
                failure.getOriginalLocator() != null ? failure.getOriginalLocator().getStrategy() : "unknown",
                failure.getActionType(),
                nullSafe(snapshot.getUrl()),
                nullSafe(snapshot.getTitle()),
                nullSafe(snapshot.getDetectedLanguage()),
                formatElementsForPrompt(snapshot.getInteractiveElements())
        );
    }

    /**
     * Build a vision-enhanced healing prompt for multimodal LLMs.
     * This prompt works alongside a screenshot for visual analysis.
     */
    public String buildVisionHealingPrompt(FailureContext failure, UiSnapshot snapshot, IntentContract intent) {
        return """
            You are an expert test automation engineer analyzing a UI test failure.
            You have been provided with a screenshot of the current page state.

            ## Test Context

            **Feature:** %s
            **Scenario:** %s
            **Step:** %s %s
            **Intent:** %s
            **Intent Description:** %s

            ## Failure Information

            **Exception:** %s
            **Original Locator:** %s (strategy: %s)
            **Action:** %s

            ## Current Page State

            **URL:** %s
            **Title:** %s

            ## Available Interactive Elements (with visual positions)

            %s

            ## Your Task

            **IMPORTANT: Use both the screenshot AND the element data to make your decision.**

            1. Look at the screenshot to understand the visual layout and context
            2. Match the original intent to visible elements in the screenshot
            3. Cross-reference with the element data below to find the correct index
            4. Consider visual cues like:
               - Button appearance and position
               - Form field locations relative to labels
               - Navigation menu structure
               - Color and visual hierarchy

            **Important Guidelines:**
            - Focus on SEMANTIC PURPOSE, not exact text matching
            - Use the screenshot to understand visual context and element relationships
            - The element's visual appearance and position matter
            - If multiple candidates could work, use visual context to choose the most likely
            - If no element clearly matches the intent visually and semantically, respond that healing is not possible
            - NEVER suggest elements that could cause destructive actions unless the original intent was destructive

            ## Response Format

            Respond with ONLY a JSON object in this exact format:

            ```json
            {
              "can_heal": true|false,
              "confidence": 0.0-1.0,
              "selected_element_index": <index>|null,
              "reasoning": "<2-3 sentences explaining your decision, referencing visual cues>",
              "alternative_indices": [<other possible indices>],
              "warnings": ["<any concerns about this heal>"],
              "refusal_reason": "<if can_heal is false, explain why>"|null
            }
            ```

            Confidence guide:
            - 0.95+: Nearly certain (visually obvious match, clear purpose)
            - 0.85-0.94: High confidence (visual context confirms semantic match)
            - 0.75-0.84: Moderate confidence (likely match, some visual ambiguity)
            - Below 0.75: Do not heal, set can_heal to false
            """.formatted(
                nullSafe(failure.getFeatureName()),
                nullSafe(failure.getScenarioName()),
                nullSafe(failure.getStepKeyword()),
                nullSafe(failure.getStepText()),
                nullSafe(intent.getAction()),
                nullSafe(intent.getDescription()),
                nullSafe(failure.getExceptionType()),
                failure.getOriginalLocator() != null ? failure.getOriginalLocator().getValue() : "unknown",
                failure.getOriginalLocator() != null ? failure.getOriginalLocator().getStrategy() : "unknown",
                failure.getActionType(),
                nullSafe(snapshot.getUrl()),
                nullSafe(snapshot.getTitle()),
                formatElementsForVisionPrompt(snapshot.getInteractiveElements())
        );
    }

    /**
     * Format elements with visual position info for vision prompts.
     */
    private String formatElementsForVisionPrompt(List<ElementSnapshot> elements) {
        if (elements == null || elements.isEmpty()) {
            return "No interactive elements found on the page.";
        }

        StringBuilder sb = new StringBuilder();
        int count = Math.min(elements.size(), MAX_ELEMENTS_IN_PROMPT);

        for (int i = 0; i < count; i++) {
            ElementSnapshot el = elements.get(i);
            sb.append(formatElementWithPosition(el)).append("\n\n");
        }

        if (elements.size() > MAX_ELEMENTS_IN_PROMPT) {
            sb.append("... and %d more elements".formatted(elements.size() - MAX_ELEMENTS_IN_PROMPT));
        }

        return sb.toString();
    }

    /**
     * Format element with visual position info for vision prompts.
     */
    private String formatElementWithPosition(ElementSnapshot el) {
        StringBuilder sb = new StringBuilder();
        sb.append("**[%d]** `<%s>`".formatted(el.getIndex(), el.getTagName()));

        // Add visual position hint if available
        if (el.getRect() != null) {
            String position = describePosition(el.getRect());
            sb.append(" - %s\n".formatted(position));
        } else {
            sb.append("\n");
        }

        if (el.getId() != null && !el.getId().isEmpty()) {
            sb.append("- id: %s\n".formatted(el.getId()));
        }
        if (el.getText() != null && !el.getText().isEmpty()) {
            sb.append("- text: \"%s\"\n".formatted(truncate(el.getText(), MAX_TEXT_LENGTH)));
        }
        if (el.getAriaLabel() != null && !el.getAriaLabel().isEmpty()) {
            sb.append("- aria-label: \"%s\"\n".formatted(el.getAriaLabel()));
        }
        if (el.getPlaceholder() != null && !el.getPlaceholder().isEmpty()) {
            sb.append("- placeholder: \"%s\"\n".formatted(el.getPlaceholder()));
        }
        sb.append("- visible: %s, enabled: %s\n".formatted(el.isVisible(), el.isEnabled()));

        return sb.toString();
    }

    /**
     * Describe element position in human-readable terms for visual context.
     */
    private String describePosition(ElementRect rect) {
        if (rect == null) return "position unknown";

        // Assume typical viewport dimensions
        int viewportWidth = 1920;
        int viewportHeight = 1080;

        String horizontal;
        if (rect.getX() < viewportWidth / 3) {
            horizontal = "left";
        } else if (rect.getX() > viewportWidth * 2 / 3) {
            horizontal = "right";
        } else {
            horizontal = "center";
        }

        String vertical;
        if (rect.getY() < 200) {
            vertical = "top";
        } else if (rect.getY() > viewportHeight - 200) {
            vertical = "bottom";
        } else {
            vertical = "middle";
        }

        return "located at %s-%s (%d,%d)".formatted(vertical, horizontal, rect.getX(), rect.getY());
    }

    /**
     * Build the outcome validation prompt.
     */
    public String buildOutcomeValidationPrompt(
            String expectedOutcome,
            UiSnapshot beforeSnapshot,
            UiSnapshot afterSnapshot) {
        return """
            You are validating whether a test action achieved its expected outcome.

            ## Expected Outcome
            %s

            ## Before Action
            - URL: %s
            - Title: %s
            - Key elements: %s

            ## After Action
            - URL: %s
            - Title: %s
            - Key elements: %s

            ## Question
            Based on the before/after state, did the action achieve the expected outcome?

            Respond with ONLY:
            ```json
            {
              "outcome_achieved": true|false,
              "confidence": 0.0-1.0,
              "reasoning": "<brief explanation>"
            }
            ```
            """.formatted(
                expectedOutcome,
                nullSafe(beforeSnapshot.getUrl()),
                nullSafe(beforeSnapshot.getTitle()),
                summarizeElements(beforeSnapshot),
                nullSafe(afterSnapshot.getUrl()),
                nullSafe(afterSnapshot.getTitle()),
                summarizeElements(afterSnapshot)
        );
    }

    private String formatElementsForPrompt(List<ElementSnapshot> elements) {
        if (elements == null || elements.isEmpty()) {
            return "No interactive elements found on the page.";
        }

        StringBuilder sb = new StringBuilder();
        int count = Math.min(elements.size(), MAX_ELEMENTS_IN_PROMPT);

        for (int i = 0; i < count; i++) {
            ElementSnapshot el = elements.get(i);
            sb.append(formatElement(el)).append("\n\n");
        }

        if (elements.size() > MAX_ELEMENTS_IN_PROMPT) {
            sb.append("... and %d more elements".formatted(elements.size() - MAX_ELEMENTS_IN_PROMPT));
        }

        return sb.toString();
    }

    private String formatElement(ElementSnapshot el) {
        StringBuilder sb = new StringBuilder();
        sb.append("**[%d]** `<%s>`\n".formatted(el.getIndex(), el.getTagName()));

        if (el.getId() != null && !el.getId().isEmpty()) {
            sb.append("- id: %s\n".formatted(el.getId()));
        }
        if (el.getName() != null && !el.getName().isEmpty()) {
            sb.append("- name: %s\n".formatted(el.getName()));
        }
        if (el.getType() != null && !el.getType().isEmpty()) {
            sb.append("- type: %s\n".formatted(el.getType()));
        }
        if (el.getClasses() != null && !el.getClasses().isEmpty()) {
            sb.append("- classes: %s\n".formatted(String.join(", ", el.getClasses())));
        }
        if (el.getText() != null && !el.getText().isEmpty()) {
            sb.append("- text: \"%s\"\n".formatted(truncate(el.getText(), MAX_TEXT_LENGTH)));
        }
        if (el.getAriaLabel() != null && !el.getAriaLabel().isEmpty()) {
            sb.append("- aria-label: \"%s\"\n".formatted(el.getAriaLabel()));
        }
        if (el.getAriaRole() != null && !el.getAriaRole().isEmpty()) {
            sb.append("- role: %s\n".formatted(el.getAriaRole()));
        }
        if (el.getPlaceholder() != null && !el.getPlaceholder().isEmpty()) {
            sb.append("- placeholder: \"%s\"\n".formatted(el.getPlaceholder()));
        }
        if (el.getTitle() != null && !el.getTitle().isEmpty()) {
            sb.append("- title: \"%s\"\n".formatted(el.getTitle()));
        }
        if (el.getContainer() != null && !el.getContainer().isEmpty()) {
            sb.append("- container: %s\n".formatted(el.getContainer()));
        }
        if (el.getNearbyLabels() != null && !el.getNearbyLabels().isEmpty()) {
            sb.append("- nearby labels: %s\n".formatted(String.join(", ", el.getNearbyLabels())));
        }
        sb.append("- visible: %s, enabled: %s\n".formatted(el.isVisible(), el.isEnabled()));

        return sb.toString();
    }

    private String summarizeElements(UiSnapshot snapshot) {
        if (snapshot == null || snapshot.getInteractiveElements().isEmpty()) {
            return "none";
        }

        List<String> summaries = snapshot.getInteractiveElements().stream()
                .limit(5)
                .map(el -> {
                    String label = el.getBestLabel();
                    return el.getTagName() + (label.isEmpty() ? "" : ": " + truncate(label, 30));
                })
                .toList();

        return String.join(", ", summaries);
    }

    private String truncate(String text, int maxLength) {
        if (text == null) return "";
        if (text.length() <= maxLength) return text;
        return text.substring(0, maxLength - 3) + "...";
    }

    private String nullSafe(String value) {
        return value != null ? value : "unknown";
    }
}
