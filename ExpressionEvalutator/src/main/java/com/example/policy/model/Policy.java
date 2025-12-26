package com.example.policy.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents a policy containing multiple rules with their relationships
 */
public class Policy {
    @JsonProperty("policyName")
    private String name;

    private String description;
    private String version;

    @JsonProperty("rules")
    private Map<String, Rule> rules = new HashMap<>();

    public Policy() {}

    public Policy(String name, String description, String version) {
        this.name = name;
        this.description = description;
        this.version = version;
    }

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Map<String, Rule> getRules() {
        return rules;
    }

    public void setRules(Map<String, Rule> rules) {
        this.rules = rules;
    }

    public void addRule(String name, Rule rule) {
        this.rules.put(name, rule);
    }

    public Rule getRule(String name) {
        return this.rules.get(name);
    }

    public boolean hasRule(String name) {
        return this.rules.containsKey(name);
    }

    @Override
    public String toString() {
        return "Policy{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", version='" + version + '\'' +
                ", rules=" + rules.size() + " rules" +
                '}';
    }
}