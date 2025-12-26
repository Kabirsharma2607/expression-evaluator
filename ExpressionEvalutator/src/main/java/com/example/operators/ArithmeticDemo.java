package com.example.operators;

import com.example.operators.arithmetic.*;
import com.example.operators.unary.*;

/**
 * Demonstration class for Task 4 arithmetic operators and Task 3 unary operators
 */
public class ArithmeticDemo {

    public static void main(String[] args) {
        System.out.println("=== Task 4: Arithmetic Operators Demo ===\n");

        demonstrateArithmeticOperators();
        demonstrateUnaryOperators();
        demonstrateOperatorFactory();
        demonstrateComplexExpressions();
    }

    private static void demonstrateArithmeticOperators() {
        System.out.println("--- Arithmetic Operators ---");

        // Addition
        AddOperator add = new AddOperator();
        System.out.println("5 + 3 = " + add.eval(5, 3));
        System.out.println("5.5 + 2.3 = " + add.eval(5.5, 2.3));
        System.out.println("\"Hello\" + 5 = " + add.eval("Hello", 5));

        // Subtraction
        SubtractOperator subtract = new SubtractOperator();
        System.out.println("10 - 3 = " + subtract.eval(10, 3));
        System.out.println("5.5 - 2.3 = " + subtract.eval(5.5, 2.3));

        // Multiplication
        MultiplyOperator multiply = new MultiplyOperator();
        System.out.println("4 * 6 = " + multiply.eval(4, 6));
        System.out.println("3.5 * 2 = " + multiply.eval(3.5, 2));

        // Division
        DivideOperator divide = new DivideOperator();
        System.out.println("10 / 3 = " + divide.eval(10, 3));
        System.out.println("15.6 / 4 = " + divide.eval(15.6, 4));

        // Modulus
        ModulusOperator modulus = new ModulusOperator();
        System.out.println("10 % 3 = " + modulus.eval(10, 3));
        System.out.println("10.5 % 3.2 = " + modulus.eval(10.5, 3.2));

        System.out.println();
    }

    private static void demonstrateUnaryOperators() {
        System.out.println("--- Unary Operators ---");

        // Negation
        NegateOperator negate = new NegateOperator();
        System.out.println("-(5) = " + negate.eval(5));
        System.out.println("-(-3.5) = " + negate.eval(-3.5));

        // Logical NOT
        NotOperator not = new NotOperator();
        System.out.println("!(true) = " + not.eval(true));
        System.out.println("!(false) = " + not.eval(false));

        System.out.println();
    }

    private static void demonstrateOperatorFactory() {
        System.out.println("--- Operator Factory ---");

        // Show all registered operators
        System.out.println("Registered operators: " + OperatorFactory.getSupportedOperators());
        System.out.println("Total operators: " + OperatorFactory.getOperatorCount());

        // Test arithmetic operators from factory
        OperatorEvaluator addOp = OperatorFactory.getBinaryOperator("+");
        System.out.println("Factory + operator: " + addOp.eval(7, 3));

        OperatorEvaluator multiplyOp = OperatorFactory.getBinaryOperator("*");
        System.out.println("Factory * operator: " + multiplyOp.eval(6, 4));

        // Test unary operators from factory
        UnaryOperatorEvaluator negateOp = OperatorFactory.getUnaryOperator("-");
        System.out.println("Factory - (unary) operator: " + negateOp.eval(10));

        UnaryOperatorEvaluator notOp = OperatorFactory.getUnaryOperator("!");
        System.out.println("Factory ! operator: " + notOp.eval(true));

        System.out.println();
    }

    private static void demonstrateComplexExpressions() {
        System.out.println("--- Complex Expression Simulation ---");

        // Simulate: (5 + 3) * 2 = 16
        AddOperator add = new AddOperator();
        MultiplyOperator multiply = new MultiplyOperator();
        Object step1 = add.eval(5, 3);  // 8
        Object result1 = multiply.eval(step1, 2);  // 16
        System.out.println("(5 + 3) * 2 = " + result1);

        // Simulate: 10 - 3 * 2 = 4 (multiplication first due to precedence)
        SubtractOperator subtract = new SubtractOperator();
        Object step2 = multiply.eval(3, 2);  // 6
        Object result2 = subtract.eval(10, step2);  // 4
        System.out.println("10 - 3 * 2 = " + result2);

        // Simulate: -(5 + 3) = -8
        NegateOperator negate = new NegateOperator();
        Object step3 = add.eval(5, 3);  // 8
        Object result3 = negate.eval(step3);  // -8
        System.out.println("-(5 + 3) = " + result3);

        // Test string concatenation with arithmetic
        Object step4 = add.eval(2, 3);  // 5
        Object result4 = add.eval("Result: ", step4);  // "Result: 5"
        System.out.println("\"Result: \" + (2 + 3) = " + result4);

        System.out.println();
    }
}