package com.example.expression;

import java.util.Map;

/**
 * Demo to measure performance impact of short-circuit evaluation
 */
public class PerformanceDemo {

    public static void main(String[] args) {
        System.out.println("=== Short-Circuit Performance Demo ===\n");

        ExpressionEvaluator evaluator = new ExpressionEvaluator();
        Map<String, Object> featureMap = Map.of("userAge", 25, "accountBalance", 5000);

        // Warm up JVM
        warmUp(evaluator, featureMap);

        // Test performance difference
        testPerformance(evaluator, featureMap);

        // Test with the actual policy expressions
        testPolicyPerformance(evaluator, featureMap);
    }

    private static void warmUp(ExpressionEvaluator evaluator, Map<String, Object> featureMap) {
        System.out.println("Warming up JVM...");
        for (int i = 0; i < 10000; i++) {
            evaluator.evaluate("false && (featureMap.userAge > 18)", featureMap);
            evaluator.evaluate("true || (featureMap.userAge > 18)", featureMap);
        }
        System.out.println("Warm-up complete.\n");
    }

    private static void testPerformance(ExpressionEvaluator evaluator, Map<String, Object> featureMap) {
        int iterations = 100000;

        // Test short-circuit && (should be very fast)
        long startTime = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            evaluator.evaluate("false && (featureMap.userAge > 18) && (featureMap.accountBalance >= 1000)", featureMap);
        }
        long shortCircuitTime = System.nanoTime() - startTime;

        // Test non-short-circuit && (should be slower)
        startTime = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            evaluator.evaluate("true && (featureMap.userAge > 18) && (featureMap.accountBalance >= 1000)", featureMap);
        }
        long fullEvalTime = System.nanoTime() - startTime;

        // Test short-circuit || (should be very fast)
        startTime = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            evaluator.evaluate("true || (featureMap.userAge > 18) || (featureMap.accountBalance >= 1000)", featureMap);
        }
        long shortCircuitOrTime = System.nanoTime() - startTime;

        // Test non-short-circuit || (should be slower)
        startTime = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            evaluator.evaluate("false || (featureMap.userAge > 18) || (featureMap.accountBalance >= 1000)", featureMap);
        }
        long fullEvalOrTime = System.nanoTime() - startTime;

        System.out.printf("Performance Results (%d iterations):%n", iterations);
        System.out.printf("Short-circuit &&:  %6.2f ms (%.0f ops/sec)%n",
            shortCircuitTime / 1_000_000.0, iterations * 1e9 / shortCircuitTime);
        System.out.printf("Full evaluation &&: %6.2f ms (%.0f ops/sec)%n",
            fullEvalTime / 1_000_000.0, iterations * 1e9 / fullEvalTime);
        System.out.printf("Speed improvement:  %.2fx faster%n", (double) fullEvalTime / shortCircuitTime);

        System.out.println();
        System.out.printf("Short-circuit ||:  %6.2f ms (%.0f ops/sec)%n",
            shortCircuitOrTime / 1_000_000.0, iterations * 1e9 / shortCircuitOrTime);
        System.out.printf("Full evaluation ||: %6.2f ms (%.0f ops/sec)%n",
            fullEvalOrTime / 1_000_000.0, iterations * 1e9 / fullEvalOrTime);
        System.out.printf("Speed improvement:  %.2fx faster%n", (double) fullEvalOrTime / shortCircuitOrTime);
        System.out.println();
    }

    private static void testPolicyPerformance(ExpressionEvaluator evaluator, Map<String, Object> featureMap) {
        System.out.println("Policy Expression Performance:");

        // Simulate a rule that would fail early
        Map<String, Object> ruleContext = Map.of("rule1", false, "rule2", true, "rule3", true);

        String expression = "rule1 && rule2 && rule3"; // Should short-circuit at rule1
        int iterations = 50000;

        long startTime = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            evaluator.evaluate(expression, featureMap, ruleContext);
        }
        long duration = System.nanoTime() - startTime;

        System.out.printf("Expression: %s%n", expression);
        System.out.printf("Result: %s (short-circuited at rule1)%n",
            evaluator.evaluate(expression, featureMap, ruleContext));
        System.out.printf("Performance: %.2f ms for %d iterations (%.0f ops/sec)%n",
            duration / 1_000_000.0, iterations, iterations * 1e9 / duration);

        // Compare with a rule that evaluates all parts
        ruleContext = Map.of("rule1", true, "rule2", true, "rule3", false);
        expression = "rule1 && rule2 && rule3"; // Should evaluate all three

        startTime = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            evaluator.evaluate(expression, featureMap, ruleContext);
        }
        long fullDuration = System.nanoTime() - startTime;

        System.out.printf("%nExpression: %s%n", expression);
        System.out.printf("Result: %s (evaluated all rules)%n",
            evaluator.evaluate(expression, featureMap, ruleContext));
        System.out.printf("Performance: %.2f ms for %d iterations (%.0f ops/sec)%n",
            fullDuration / 1_000_000.0, iterations, iterations * 1e9 / fullDuration);

        System.out.printf("Short-circuit advantage: %.2fx faster%n", (double) fullDuration / duration);
    }
}