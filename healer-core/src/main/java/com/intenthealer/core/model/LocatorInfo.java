package com.intenthealer.core.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

/**
 * Represents a Selenium locator with its strategy and value.
 */
public final class LocatorInfo {
    private final LocatorStrategy strategy;
    private final String value;

    @JsonCreator
    public LocatorInfo(
            @JsonProperty("strategy") LocatorStrategy strategy,
            @JsonProperty("value") String value) {
        this.strategy = Objects.requireNonNull(strategy, "strategy cannot be null");
        this.value = Objects.requireNonNull(value, "value cannot be null");
    }

    public LocatorStrategy getStrategy() {
        return strategy;
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LocatorInfo that = (LocatorInfo) o;
        return strategy == that.strategy && Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(strategy, value);
    }

    @Override
    public String toString() {
        return strategy.name().toLowerCase() + "=" + value;
    }

    /**
     * Locator strategy types.
     */
    public enum LocatorStrategy {
        ID,
        NAME,
        CLASS_NAME,
        CSS,
        XPATH,
        LINK_TEXT,
        PARTIAL_LINK_TEXT,
        TAG_NAME
    }
}
