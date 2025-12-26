package com.example.policy.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents the result of executing a policy
 */
public class PolicyResult {
    private String policyName;
    private boolean success;
    private Map<String, RuleResult> ruleResults;
    private List<String> errors;
    private long executionTimeMs;

    public PolicyResult() {
        this.ruleResults = new HashMap<>();
        this.errors = new ArrayList<>();
    }

    public PolicyResult(String policyName) {
        this();
        this.policyName = policyName;
    }

    // Getters and Setters
    public String getPolicyName() {
        return policyName;
    }

    public void setPolicyName(String policyName) {
        this.policyName = policyName;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public Map<String, RuleResult> getRuleResults() {
        return ruleResults;
    }

    public void setRuleResults(Map<String, RuleResult> ruleResults) {
        this.ruleResults = ruleResults;
    }

    public List<String> getErrors() {
        return errors;
    }

    public void setErrors(List<String> errors) {
        this.errors = errors;
    }

    public long getExecutionTimeMs() {
        return executionTimeMs;
    }

    public void setExecutionTimeMs(long executionTimeMs) {
        this.executionTimeMs = executionTimeMs;
    }

    // Utility methods
    public void addRuleResult(String ruleName, RuleResult result) {
        this.ruleResults.put(ruleName, result);
    }

    public void addError(String error) {
        this.errors.add(error);
    }

    public boolean hasErrors() {
        return !this.errors.isEmpty();
    }

    public RuleResult getRuleResult(String ruleName) {
        return this.ruleResults.get(ruleName);
    }

    @Override
    public String toString() {
        return "PolicyResult{" +
                "policyName='" + policyName + '\'' +
                ", success=" + success +
                ", ruleResults=" + ruleResults.size() + " rules" +
                ", errors=" + errors.size() + " errors" +
                ", executionTimeMs=" + executionTimeMs +
                '}';
    }
}