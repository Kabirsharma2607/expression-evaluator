package com.example.expression;

import java.util.Map;

/**
 * Demo to test expression parsing and evaluation
 */
public class ExpressionDemo {

    public static void main(String[] args) {
        System.out.println("=== Expression Parsing Demo ===\n");

        ExpressionEvaluator evaluator = new ExpressionEvaluator();

        // Test simple literals
        try {
            System.out.println("Testing literals:");
            System.out.println("42 = " + evaluator.evaluate("42", Map.of()));
            System.out.println("true = " + evaluator.evaluate("true", Map.of()));
            System.out.println("\"hello\" = " + evaluator.evaluate("\"hello\"", Map.of()));
        } catch (Exception e) {
            System.out.println("Error with literals: " + e.getMessage());
            e.printStackTrace();
        }

        // Test simple arithmetic
        try {
            System.out.println("\nTesting arithmetic:");
            System.out.println("5 + 3 = " + evaluator.evaluate("5 + 3", Map.of()));
            System.out.println("10 - 4 = " + evaluator.evaluate("10 - 4", Map.of()));
            System.out.println("6 * 7 = " + evaluator.evaluate("6 * 7", Map.of()));
        } catch (Exception e) {
            System.out.println("Error with arithmetic: " + e.getMessage());
            e.printStackTrace();
        }

        // Test feature map access
        Map<String, Object> featureMap = Map.of("userAge", 25, "accountBalance", 5000);
        try {
            System.out.println("\nTesting feature map access:");
            System.out.println("featureMap.userAge = " + evaluator.evaluate("featureMap.userAge", featureMap));
            System.out.println("featureMap.accountBalance = " + evaluator.evaluate("featureMap.accountBalance", featureMap));
        } catch (Exception e) {
            System.out.println("Error with feature map access: " + e.getMessage());
            e.printStackTrace();
        }

        // Test comparisons
        try {
            System.out.println("\nTesting comparisons:");
            System.out.println("featureMap.userAge > 18 = " + evaluator.evaluate("featureMap.userAge > 18", featureMap));
            System.out.println("featureMap.accountBalance >= 1000 = " + evaluator.evaluate("featureMap.accountBalance >= 1000", featureMap));
        } catch (Exception e) {
            System.out.println("Error with comparisons: " + e.getMessage());
            e.printStackTrace();
        }

        // Test logical operations
        try {
            System.out.println("\nTesting logical operations:");
            System.out.println("true && false = " + evaluator.evaluate("true && false", Map.of()));
            System.out.println("true || false = " + evaluator.evaluate("true || false", Map.of()));
        } catch (Exception e) {
            System.out.println("Error with logical operations: " + e.getMessage());
            e.printStackTrace();
        }

        // Test rule references
        Map<String, Object> ruleContext = Map.of("rule1", true, "rule2", false);
        try {
            System.out.println("\nTesting rule references:");
            System.out.println("rule1 = " + evaluator.evaluate("rule1", Map.of(), ruleContext));
            System.out.println("rule1 && rule2 = " + evaluator.evaluate("rule1 && rule2", Map.of(), ruleContext));
        } catch (Exception e) {
            System.out.println("Error with rule references: " + e.getMessage());
            e.printStackTrace();
        }
    }
}