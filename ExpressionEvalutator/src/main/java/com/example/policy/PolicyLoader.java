package com.example.policy;

import com.example.policy.model.Policy;
import com.example.policy.model.Rule;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.InputStream;
import java.util.*;

/**
 * Loads and validates policies from YAML files
 */
public class PolicyLoader {
    private final ObjectMapper yamlMapper;
    private final Map<String, Policy> policyCache;

    public PolicyLoader() {
        this.yamlMapper = new ObjectMapper(new YAMLFactory());
        this.policyCache = new HashMap<>();
    }

    /**
     * Loads a policy by name from resources/policies directory
     */
    public Policy loadPolicy(String policyName) {
        // Check cache first
        if (policyCache.containsKey(policyName)) {
            return policyCache.get(policyName);
        }

        String resourcePath = "/policies/" + policyName + ".yaml";
        return loadPolicyFromResource(resourcePath);
    }

    /**
     * Loads policy from a specific file path in resources
     */
    public Policy loadPolicyFromResource(String resourcePath) {
        // First try to load from filesystem (for deployment with S3 sync)
        String policyName = resourcePath.replace("/policies/", "").replace(".yaml", "");
        Policy policy = loadFromFilesystem(policyName);
        if (policy != null) {
            return policy;
        }

        // Fallback to classpath resources
        try (InputStream inputStream = getClass().getResourceAsStream(resourcePath)) {
            if (inputStream == null) {
                throw new PolicyNotFoundException("Policy file not found: " + resourcePath);
            }

            policy = yamlMapper.readValue(inputStream, Policy.class);

            // Set rule names based on YAML keys
            for (Map.Entry<String, Rule> entry : policy.getRules().entrySet()) {
                entry.getValue().setName(entry.getKey());
            }

            validatePolicy(policy);

            // Cache the policy
            policyCache.put(policy.getName(), policy);

            return policy;
        } catch (Exception e) {
            throw new PolicyLoadException("Failed to load policy from " + resourcePath, e);
        }
    }

    /**
     * Try to load policy from filesystem (for S3-synced files)
     */
    private Policy loadFromFilesystem(String policyName) {
        try {
            java.nio.file.Path filePath = java.nio.file.Paths.get("policies", policyName + ".yaml");
            if (java.nio.file.Files.exists(filePath)) {
                String content = java.nio.file.Files.readString(filePath);
                Policy policy = yamlMapper.readValue(content, Policy.class);

                // Set policy name if not set in YAML
                if (policy.getName() == null || policy.getName().trim().isEmpty()) {
                    policy.setName(policyName);
                }

                // Set rule names based on YAML keys
                for (Map.Entry<String, Rule> entry : policy.getRules().entrySet()) {
                    entry.getValue().setName(entry.getKey());
                }

                validatePolicy(policy);

                // Cache the policy
                policyCache.put(policy.getName(), policy);

                return policy;
            }
        } catch (Exception e) {
            // Log but don't throw - will fallback to classpath
            System.err.println("Failed to load from filesystem: " + e.getMessage());
        }
        return null;
    }

    /**
     * Validates policy structure and dependencies
     */
    public void validatePolicy(Policy policy) {
        if (policy.getName() == null || policy.getName().trim().isEmpty()) {
            throw new PolicyValidationException("Policy name is required");
        }

        if (policy.getRules() == null || policy.getRules().isEmpty()) {
            throw new PolicyValidationException("Policy must contain at least one rule");
        }

        Set<String> ruleNames = policy.getRules().keySet();

        for (Map.Entry<String, Rule> entry : policy.getRules().entrySet()) {
            String ruleName = entry.getKey();
            Rule rule = entry.getValue();

            // Validate rule
            if (rule.getExpression() == null || rule.getExpression().trim().isEmpty()) {
                throw new PolicyValidationException("Rule '" + ruleName + "' must have an expression");
            }

            // Validate dependencies exist
            for (String dependency : rule.getDependencies()) {
                if (!ruleNames.contains(dependency)) {
                    throw new PolicyValidationException(
                        "Rule '" + ruleName + "' depends on non-existent rule: " + dependency
                    );
                }
            }
        }

        // Check for circular dependencies
        checkCircularDependencies(policy);
    }

    /**
     * Checks for circular dependencies in rules
     */
    private void checkCircularDependencies(Policy policy) {
        Map<String, Set<String>> dependencyGraph = buildDependencyGraph(policy);
        Set<String> visiting = new HashSet<>();
        Set<String> visited = new HashSet<>();

        for (String ruleName : policy.getRules().keySet()) {
            if (!visited.contains(ruleName)) {
                if (hasCircularDependency(ruleName, dependencyGraph, visiting, visited)) {
                    throw new CircularDependencyException("Circular dependency detected involving rule: " + ruleName);
                }
            }
        }
    }

    private Map<String, Set<String>> buildDependencyGraph(Policy policy) {
        Map<String, Set<String>> graph = new HashMap<>();

        for (Map.Entry<String, Rule> entry : policy.getRules().entrySet()) {
            String ruleName = entry.getKey();
            Set<String> dependencies = new HashSet<>(entry.getValue().getDependencies());
            graph.put(ruleName, dependencies);
        }

        return graph;
    }

    private boolean hasCircularDependency(String ruleName, Map<String, Set<String>> graph,
                                         Set<String> visiting, Set<String> visited) {
        if (visiting.contains(ruleName)) {
            return true; // Circular dependency found
        }

        if (visited.contains(ruleName)) {
            return false; // Already processed
        }

        visiting.add(ruleName);

        Set<String> dependencies = graph.getOrDefault(ruleName, Collections.emptySet());
        for (String dependency : dependencies) {
            if (hasCircularDependency(dependency, graph, visiting, visited)) {
                return true;
            }
        }

        visiting.remove(ruleName);
        visited.add(ruleName);
        return false;
    }

    /**
     * Resolves the execution order of rules based on dependencies
     */
    public List<String> resolveDependencyOrder(Policy policy) {
        Map<String, Set<String>> dependencyGraph = buildDependencyGraph(policy);
        List<String> executionOrder = new ArrayList<>();
        Set<String> processed = new HashSet<>();

        while (processed.size() < policy.getRules().size()) {
            boolean progress = false;

            for (String ruleName : policy.getRules().keySet()) {
                if (!processed.contains(ruleName)) {
                    Set<String> dependencies = dependencyGraph.getOrDefault(ruleName, Collections.emptySet());

                    if (processed.containsAll(dependencies)) {
                        executionOrder.add(ruleName);
                        processed.add(ruleName);
                        progress = true;
                    }
                }
            }

            if (!progress) {
                throw new PolicyValidationException("Unable to resolve rule dependency order");
            }
        }

        return executionOrder;
    }

    // Custom Exceptions
    public static class PolicyNotFoundException extends RuntimeException {
        public PolicyNotFoundException(String message) {
            super(message);
        }
    }

    public static class PolicyLoadException extends RuntimeException {
        public PolicyLoadException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public static class PolicyValidationException extends RuntimeException {
        public PolicyValidationException(String message) {
            super(message);
        }
    }

    public static class CircularDependencyException extends RuntimeException {
        public CircularDependencyException(String message) {
            super(message);
        }
    }
}