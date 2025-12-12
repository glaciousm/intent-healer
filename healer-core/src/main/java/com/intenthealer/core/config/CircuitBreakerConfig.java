package com.intenthealer.core.config;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Configuration for circuit breaker behavior.
 */
public class CircuitBreakerConfig {

    @JsonProperty("enabled")
    private boolean enabled = true;

    @JsonProperty("false_heal_rate_threshold")
    private double falseHealRateThreshold = 0.01;  // 1%

    @JsonProperty("false_heal_rate_window_days")
    private int falseHealRateWindowDays = 7;

    @JsonProperty("false_heal_rate_min_samples")
    private int falseHealRateMinSamples = 20;

    @JsonProperty("consecutive_failures_threshold")
    private int consecutiveFailuresThreshold = 3;

    @JsonProperty("p95_latency_threshold_ms")
    private int p95LatencyThresholdMs = 10000;

    @JsonProperty("latency_window_hours")
    private int latencyWindowHours = 1;

    @JsonProperty("daily_cost_limit_usd")
    private double dailyCostLimitUsd = 10.00;

    @JsonProperty("cooldown_minutes")
    private int cooldownMinutes = 30;

    @JsonProperty("test_heals_required")
    private int testHealsRequired = 3;

    public CircuitBreakerConfig() {
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public double getFalseHealRateThreshold() {
        return falseHealRateThreshold;
    }

    public void setFalseHealRateThreshold(double falseHealRateThreshold) {
        this.falseHealRateThreshold = falseHealRateThreshold;
    }

    public int getFalseHealRateWindowDays() {
        return falseHealRateWindowDays;
    }

    public void setFalseHealRateWindowDays(int falseHealRateWindowDays) {
        this.falseHealRateWindowDays = falseHealRateWindowDays;
    }

    public int getFalseHealRateMinSamples() {
        return falseHealRateMinSamples;
    }

    public void setFalseHealRateMinSamples(int falseHealRateMinSamples) {
        this.falseHealRateMinSamples = falseHealRateMinSamples;
    }

    public int getConsecutiveFailuresThreshold() {
        return consecutiveFailuresThreshold;
    }

    public void setConsecutiveFailuresThreshold(int consecutiveFailuresThreshold) {
        this.consecutiveFailuresThreshold = consecutiveFailuresThreshold;
    }

    public int getP95LatencyThresholdMs() {
        return p95LatencyThresholdMs;
    }

    public void setP95LatencyThresholdMs(int p95LatencyThresholdMs) {
        this.p95LatencyThresholdMs = p95LatencyThresholdMs;
    }

    public int getLatencyWindowHours() {
        return latencyWindowHours;
    }

    public void setLatencyWindowHours(int latencyWindowHours) {
        this.latencyWindowHours = latencyWindowHours;
    }

    public double getDailyCostLimitUsd() {
        return dailyCostLimitUsd;
    }

    public void setDailyCostLimitUsd(double dailyCostLimitUsd) {
        this.dailyCostLimitUsd = dailyCostLimitUsd;
    }

    public int getCooldownMinutes() {
        return cooldownMinutes;
    }

    public void setCooldownMinutes(int cooldownMinutes) {
        this.cooldownMinutes = cooldownMinutes;
    }

    public int getTestHealsRequired() {
        return testHealsRequired;
    }

    public void setTestHealsRequired(int testHealsRequired) {
        this.testHealsRequired = testHealsRequired;
    }

    @Override
    public String toString() {
        return "CircuitBreakerConfig{enabled=" + enabled +
               ", falseHealRateThreshold=" + falseHealRateThreshold + "}";
    }
}
