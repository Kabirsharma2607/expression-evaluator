package com.example.policy.model;

/**
 * Represents the result of executing a single rule
 */
public class RuleResult {
    private String ruleName;
    private Object result; // Boolean, Number, String, etc.
    private boolean success;
    private String error;
    private long executionTimeMs;

    public RuleResult() {}

    public RuleResult(String ruleName) {
        this.ruleName = ruleName;
    }

    public RuleResult(String ruleName, Object result, boolean success) {
        this.ruleName = ruleName;
        this.result = result;
        this.success = success;
    }

    public RuleResult(String ruleName, String error) {
        this.ruleName = ruleName;
        this.error = error;
        this.success = false;
    }

    // Getters and Setters
    public String getRuleName() {
        return ruleName;
    }

    public void setRuleName(String ruleName) {
        this.ruleName = ruleName;
    }

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public long getExecutionTimeMs() {
        return executionTimeMs;
    }

    public void setExecutionTimeMs(long executionTimeMs) {
        this.executionTimeMs = executionTimeMs;
    }

    // Utility methods
    public boolean hasError() {
        return error != null && !error.isEmpty();
    }

    public Boolean getBooleanResult() {
        if (result instanceof Boolean) {
            return (Boolean) result;
        }
        return null;
    }

    public Number getNumericResult() {
        if (result instanceof Number) {
            return (Number) result;
        }
        return null;
    }

    public String getStringResult() {
        return result != null ? result.toString() : null;
    }

    @Override
    public String toString() {
        return "RuleResult{" +
                "ruleName='" + ruleName + '\'' +
                ", result=" + result +
                ", success=" + success +
                (hasError() ? ", error='" + error + '\'' : "") +
                ", executionTimeMs=" + executionTimeMs +
                '}';
    }
}