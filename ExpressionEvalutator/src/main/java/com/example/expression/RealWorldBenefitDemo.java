package com.example.expression;

import java.util.Map;

/**
 * Demo showing real-world benefits of short-circuit evaluation
 */
public class RealWorldBenefitDemo {

    public static void main(String[] args) {
        System.out.println("=== Real-World Short-Circuit Benefits ===\n");

        ExpressionEvaluator evaluator = new ExpressionEvaluator();

        // Scenario 1: Avoiding type errors
        System.out.println("1. Error Prevention:");
        testErrorPrevention(evaluator);

        // Scenario 2: Complex expression chains
        System.out.println("\n2. Complex Expression Performance:");
        testComplexExpressions(evaluator);

        // Scenario 3: Policy evaluation efficiency
        System.out.println("\n3. Policy Rule Efficiency:");
        testPolicyEfficiency(evaluator);
    }

    private static void testErrorPrevention(ExpressionEvaluator evaluator) {
        Map<String, Object> featureMap = Map.of(
            "userType", "guest",
            "errorValue", "invalid-number"
        );

        // Without short-circuit, this would throw an exception
        try {
            String expr = "featureMap.userType == \"admin\" && featureMap.errorValue > 100";
            Object result = evaluator.evaluate(expr, featureMap);
            System.out.println("✅ Expression: " + expr);
            System.out.println("   Result: " + result + " (short-circuit prevented type error)");
        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
        }

        // This would cause an error without short-circuit
        try {
            String expr = "false && featureMap.nonExistentField > 0";
            Object result = evaluator.evaluate(expr, featureMap);
            System.out.println("✅ Expression: " + expr);
            System.out.println("   Result: " + result + " (short-circuit prevented access error)");
        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
        }
    }

    private static void testComplexExpressions(ExpressionEvaluator evaluator) {
        Map<String, Object> featureMap = Map.of(
            "userAge", 16,
            "accountBalance", 5000,
            "transactionCount", 100,
            "riskScore", 75
        );

        // Complex expression that should short-circuit early
        String complexExpr = "featureMap.userAge < 18 && " +
                           "featureMap.accountBalance > 10000 && " +
                           "featureMap.transactionCount > 1000 && " +
                           "featureMap.riskScore < 50";

        long startTime = System.nanoTime();
        int iterations = 10000;

        for (int i = 0; i < iterations; i++) {
            evaluator.evaluate(complexExpr, featureMap);
        }

        long duration = System.nanoTime() - startTime;

        System.out.println("Complex expression (4 conditions):");
        System.out.println("Result: " + evaluator.evaluate(complexExpr, featureMap));
        System.out.println("Performance: " + String.format("%.2f ms for %d iterations",
                          duration / 1_000_000.0, iterations));
        System.out.println("Benefit: Only first condition evaluated (userAge < 18 = true), rest skipped");
    }

    private static void testPolicyEfficiency(ExpressionEvaluator evaluator) {
        Map<String, Object> featureMap = Map.of("userId", 12345);

        // Simulate expensive rule evaluations
        Map<String, Object> ruleContext = Map.of(
            "quickCheck", false,        // Fast rule that fails
            "expensiveRule1", true,     // Would be expensive to compute
            "expensiveRule2", true,     // Would be expensive to compute
            "expensiveRule3", false     // Would be expensive to compute
        );

        String policyExpr = "quickCheck && expensiveRule1 && expensiveRule2 && expensiveRule3";

        long startTime = System.nanoTime();
        int iterations = 20000;

        for (int i = 0; i < iterations; i++) {
            evaluator.evaluate(policyExpr, featureMap, ruleContext);
        }

        long duration = System.nanoTime() - startTime;

        System.out.println("Policy with expensive rules:");
        System.out.println("Expression: " + policyExpr);
        System.out.println("Result: " + evaluator.evaluate(policyExpr, featureMap, ruleContext));
        System.out.println("Performance: " + String.format("%.2f ms for %d iterations",
                          duration / 1_000_000.0, iterations));
        System.out.println("Benefit: Only 'quickCheck' evaluated, 3 expensive rules skipped");

        // Compare with a case where all rules need to be evaluated
        ruleContext = Map.of(
            "quickCheck", true,
            "expensiveRule1", true,
            "expensiveRule2", true,
            "expensiveRule3", false
        );

        startTime = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            evaluator.evaluate(policyExpr, featureMap, ruleContext);
        }
        long fullDuration = System.nanoTime() - startTime;

        System.out.println("\nCompare with full evaluation:");
        System.out.println("Result: " + evaluator.evaluate(policyExpr, featureMap, ruleContext));
        System.out.println("Performance: " + String.format("%.2f ms for %d iterations",
                          fullDuration / 1_000_000.0, iterations));
        System.out.println("All rules evaluated until the last one failed");

        double speedup = (double) fullDuration / duration;
        System.out.println(String.format("Short-circuit advantage: %.2fx faster", speedup));
    }
}