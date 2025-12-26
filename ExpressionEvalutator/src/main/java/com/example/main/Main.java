package com.example.main;

import com.example.policy.PolicyExecutor;
import com.example.policy.model.PolicyResult;
import com.example.policy.model.RuleResult;

import java.util.HashMap;
import java.util.Map;

/**
 * Demonstrates the Expression Evaluator with Policy Execution
 */
public class Main {
    public static void main(String[] args) {
        System.out.println("=== Expression Evaluator Demo ===");

        // Create a PolicyExecutor
        PolicyExecutor executor = new PolicyExecutor();

        // Demo 1: Basic feature validation (fc1Rules)
        demonstrateFC1Rules(executor);

        // Demo 2: Access control rules
        demonstrateAccessControl(executor);

        // Demo 3: Alert monitoring rules
        demonstrateAlertRules(executor);
    }

    private static void demonstrateFC1Rules(PolicyExecutor executor) {
        System.out.println("\n--- Demo 1: FC1 Rules (Basic Validation) ---");

        // Create feature map for a valid user
        Map<String, Object> featureMap = new HashMap<>();
        featureMap.put("userAge", 25);
        featureMap.put("accountBalance", 5000);

        System.out.println("Feature Map: " + featureMap);

        try {
            PolicyResult result = executor.executePolicy(featureMap, "fc1Rules");
            printPolicyResult(result);
        } catch (Exception e) {
            System.err.println("Error executing fc1Rules: " + e.getMessage());
        }

        // Test with invalid user (under 18)
        System.out.println("\n--- Testing with underage user ---");
        featureMap.put("userAge", 16);

        try {
            PolicyResult result = executor.executePolicy(featureMap, "fc1Rules");
            printPolicyResult(result);
        } catch (Exception e) {
            System.err.println("Error executing fc1Rules: " + e.getMessage());
        }
    }

    private static void demonstrateAccessControl(PolicyExecutor executor) {
        System.out.println("\n--- Demo 2: Access Control Rules ---");

        // Test admin user
        Map<String, Object> adminFeatureMap = new HashMap<>();
        adminFeatureMap.put("userRole", "admin");
        adminFeatureMap.put("department", "engineering");
        adminFeatureMap.put("securityClearance", 1);

        System.out.println("Admin Feature Map: " + adminFeatureMap);

        try {
            PolicyResult result = executor.executePolicy(adminFeatureMap, "accessControlRules");
            printPolicyResult(result);
        } catch (Exception e) {
            System.err.println("Error executing accessControlRules: " + e.getMessage());
        }
    }

    private static void demonstrateAlertRules(PolicyExecutor executor) {
        System.out.println("\n--- Demo 3: Alert Rules ---");

        // Test system with high CPU usage
        Map<String, Object> systemFeatureMap = new HashMap<>();
        systemFeatureMap.put("cpuUsage", 85);
        systemFeatureMap.put("memoryUsage", 65);
        systemFeatureMap.put("diskSpace", 30);

        System.out.println("System Feature Map: " + systemFeatureMap);

        try {
            PolicyResult result = executor.executePolicy(systemFeatureMap, "alertRules");
            printPolicyResult(result);
        } catch (Exception e) {
            System.err.println("Error executing alertRules: " + e.getMessage());
        }
    }

    private static void printPolicyResult(PolicyResult result) {
        System.out.println("Policy Result: " + result.getPolicyName());
        System.out.println("  Success: " + result.isSuccess());
        System.out.println("  Execution Time: " + result.getExecutionTimeMs() + "ms");

        if (result.hasErrors()) {
            System.out.println("  Errors:");
            for (String error : result.getErrors()) {
                System.out.println("    - " + error);
            }
        }

        System.out.println("  Rule Results:");
        for (Map.Entry<String, RuleResult> entry : result.getRuleResults().entrySet()) {
            RuleResult ruleResult = entry.getValue();
            System.out.println("    " + ruleResult.getRuleName() + ": " +
                             ruleResult.getResult() + " (success: " + ruleResult.isSuccess() + ")");
            if (ruleResult.hasError()) {
                System.out.println("      Error: " + ruleResult.getError());
            }
        }
    }
}