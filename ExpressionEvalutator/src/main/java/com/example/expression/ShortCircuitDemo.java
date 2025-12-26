package com.example.expression;

import java.util.Map;

/**
 * Demo to test short-circuit evaluation
 */
public class ShortCircuitDemo {

    public static void main(String[] args) {
        System.out.println("=== Short-Circuit Evaluation Demo ===\n");

        ExpressionEvaluator evaluator = new ExpressionEvaluator();
        Map<String, Object> featureMap = Map.of("userAge", 25, "errorValue", "not-a-number");

        // Test && short-circuit
        System.out.println("Testing && short-circuit:");
        testAndShortCircuit(evaluator, featureMap);

        // Test || short-circuit
        System.out.println("\nTesting || short-circuit:");
        testOrShortCircuit(evaluator, featureMap);

        // Test complex expressions
        System.out.println("\nTesting complex expressions:");
        testComplexExpressions(evaluator, featureMap);
    }

    private static void testAndShortCircuit(ExpressionEvaluator evaluator, Map<String, Object> featureMap) {
        // This should short-circuit at false without evaluating the invalid operation
        try {
            System.out.println("Expression: false && (featureMap.errorValue > 10)");
            Object result = evaluator.evaluate("false && (featureMap.errorValue > 10)", featureMap);
            System.out.println("Result: " + result + " (short-circuited successfully)");
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage() + " (short-circuit failed)");
        }

        // This should evaluate both sides since left is true
        try {
            System.out.println("Expression: true && (featureMap.userAge > 18)");
            Object result = evaluator.evaluate("true && (featureMap.userAge > 18)", featureMap);
            System.out.println("Result: " + result + " (both sides evaluated)");
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }

        // This should short-circuit at false userAge comparison
        try {
            System.out.println("Expression: (featureMap.userAge < 18) && (featureMap.errorValue > 10)");
            Object result = evaluator.evaluate("(featureMap.userAge < 18) && (featureMap.errorValue > 10)", featureMap);
            System.out.println("Result: " + result + " (short-circuited after first comparison)");
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage() + " (short-circuit failed)");
        }
    }

    private static void testOrShortCircuit(ExpressionEvaluator evaluator, Map<String, Object> featureMap) {
        // This should short-circuit at true without evaluating the invalid operation
        try {
            System.out.println("Expression: true || (featureMap.errorValue > 10)");
            Object result = evaluator.evaluate("true || (featureMap.errorValue > 10)", featureMap);
            System.out.println("Result: " + result + " (short-circuited successfully)");
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage() + " (short-circuit failed)");
        }

        // This should evaluate both sides since left is false
        try {
            System.out.println("Expression: false || (featureMap.userAge > 18)");
            Object result = evaluator.evaluate("false || (featureMap.userAge > 18)", featureMap);
            System.out.println("Result: " + result + " (both sides evaluated)");
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }

        // This should short-circuit at true userAge comparison
        try {
            System.out.println("Expression: (featureMap.userAge > 18) || (featureMap.errorValue > 10)");
            Object result = evaluator.evaluate("(featureMap.userAge > 18) || (featureMap.errorValue > 10)", featureMap);
            System.out.println("Result: " + result + " (short-circuited after first comparison)");
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage() + " (short-circuit failed)");
        }
    }

    private static void testComplexExpressions(ExpressionEvaluator evaluator, Map<String, Object> featureMap) {
        // Complex expression with multiple short-circuit opportunities
        try {
            System.out.println("Expression: false && true && (featureMap.errorValue > 10)");
            Object result = evaluator.evaluate("false && true && (featureMap.errorValue > 10)", featureMap);
            System.out.println("Result: " + result + " (short-circuited at first false)");
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage() + " (short-circuit failed)");
        }

        try {
            System.out.println("Expression: true || false || (featureMap.errorValue > 10)");
            Object result = evaluator.evaluate("true || false || (featureMap.errorValue > 10)", featureMap);
            System.out.println("Result: " + result + " (short-circuited at first true)");
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage() + " (short-circuit failed)");
        }

        // Mixed operators - should only short-circuit logical operators
        try {
            System.out.println("Expression: (5 + 3) > 7 && false");
            Object result = evaluator.evaluate("(5 + 3) > 7 && false", featureMap);
            System.out.println("Result: " + result + " (arithmetic evaluated, then short-circuited)");
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}