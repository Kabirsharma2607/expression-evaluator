package com.example.operators;

/**
 * Interface for unary operators that take a single operand.
 * Extends the base operator evaluator concept for single-operand operations.
 */
public interface UnaryOperatorEvaluator {

    /**
     * Evaluates the unary operator with a single operand
     * @param operand the operand to evaluate
     * @return evaluation result
     * @throws TypeMismatchException if operand is not compatible with this operator
     * @throws ArithmeticException for mathematical errors
     */
    Object eval(Object operand);

    /**
     * Returns operator precedence (higher number = higher precedence)
     * Unary operators typically have high precedence
     * @return precedence value
     */
    int getPrecedence();

    /**
     * Returns the type category of this operator
     * @return operator type
     */
    OperatorType getType();

    /**
     * Returns the string symbol for this operator
     * @return operator symbol (e.g., "!", "-")
     */
    String getSymbol();

    /**
     * Indicates this is a unary operator
     * @return true (always for unary operators)
     */
    default boolean isUnary() {
        return true;
    }
}