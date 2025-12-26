package com.example.operators;

import com.example.operators.comparison.*;
import com.example.operators.logical.*;

/**
 * Demonstration class for Task 2 operators
 */
public class OperatorDemo {

    public static void main(String[] args) {
        System.out.println("=== Task 2: Operator Evaluator Demo ===\n");

        demonstrateComparisonOperators();
        demonstrateLogicalOperators();
        demonstrateOperatorFactory();
    }

    private static void demonstrateComparisonOperators() {
        System.out.println("--- Comparison Operators ---");

        // Test less than
        LessThanOperator lt = new LessThanOperator();
        System.out.println("5 < 10: " + lt.eval(5, 10));
        System.out.println("10 < 5: " + lt.eval(10, 5));

        // Test greater than or equal
        GreaterThanEqualOperator gte = new GreaterThanEqualOperator();
        System.out.println("10 >= 5: " + gte.eval(10, 5));
        System.out.println("5 >= 5: " + gte.eval(5, 5));

        // Test equality with type coercion
        EqualOperator eq = new EqualOperator();
        System.out.println("5 == 5.0: " + eq.eval(5, 5.0));
        System.out.println("'hello' == 'hello': " + eq.eval("hello", "hello"));

        System.out.println();
    }

    private static void demonstrateLogicalOperators() {
        System.out.println("--- Logical Operators ---");

        AndOperator and = new AndOperator();
        OrOperator or = new OrOperator();

        System.out.println("true && true: " + and.eval(true, true));
        System.out.println("true && false: " + and.eval(true, false));
        System.out.println("false || true: " + or.eval(false, true));
        System.out.println("false || false: " + or.eval(false, false));

        System.out.println();
    }

    private static void demonstrateOperatorFactory() {
        System.out.println("--- Operator Factory ---");

        // Show registered operators
        System.out.println("Registered operators: " + OperatorFactory.getSupportedOperators());
        System.out.println("Total operators: " + OperatorFactory.getOperatorCount());

        // Demonstrate factory usage
        OperatorEvaluator gtOperator = OperatorFactory.getBinaryOperator(">");
        System.out.println("Using factory '>': " + gtOperator.eval(15, 10));

        OperatorEvaluator andOperator = OperatorFactory.getBinaryOperator("&&");
        System.out.println("Using factory '&&': " + andOperator.eval(true, true));

        // Show operators by type
        System.out.println("Comparison operators: " +
            OperatorFactory.getOperatorsByType(OperatorType.COMPARISON).keySet());
        System.out.println("Logical operators: " +
            OperatorFactory.getOperatorsByType(OperatorType.LOGICAL).keySet());

        System.out.println();
    }
}