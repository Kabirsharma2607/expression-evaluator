package com.example.policy.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a single rule within a policy
 */
public class Rule {
    private String name;
    private String expression;
    private String description;

    @JsonProperty("dependencies")
    private List<String> dependencies = new ArrayList<>();

    public Rule() {}

    public Rule(String name, String expression, String description) {
        this.name = name;
        this.expression = expression;
        this.description = description;
    }

    public Rule(String name, String expression, String description, List<String> dependencies) {
        this.name = name;
        this.expression = expression;
        this.description = description;
        this.dependencies = dependencies != null ? new ArrayList<>(dependencies) : new ArrayList<>();
    }

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getExpression() {
        return expression;
    }

    public void setExpression(String expression) {
        this.expression = expression;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getDependencies() {
        return dependencies;
    }

    public void setDependencies(List<String> dependencies) {
        this.dependencies = dependencies != null ? new ArrayList<>(dependencies) : new ArrayList<>();
    }

    public boolean hasDependencies() {
        return !dependencies.isEmpty();
    }

    public void addDependency(String dependency) {
        if (!this.dependencies.contains(dependency)) {
            this.dependencies.add(dependency);
        }
    }

    @Override
    public String toString() {
        return "Rule{" +
                "name='" + name + '\'' +
                ", expression='" + expression + '\'' +
                ", description='" + description + '\'' +
                ", dependencies=" + dependencies +
                '}';
    }
}