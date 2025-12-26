package com.example.operators;

/**
 * Interface for binary operators that take two operands.
 * Uses Strategy pattern to allow different implementations for each operator type.
 */
public interface OperatorEvaluator {

    /**
     * Evaluates the operator with given operands
     * @param leftOperand Left operand value
     * @param rightOperand Right operand value
     * @return Evaluation result
     * @throws TypeMismatchException if operands are not compatible with this operator
     * @throws ArithmeticException for mathematical errors (division by zero, etc.)
     */
    Object eval(Object leftOperand, Object rightOperand);

    /**
     * Returns operator precedence (higher number = higher precedence)
     * Used for expression parsing to determine evaluation order
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
     * @return operator symbol (e.g., "+", "&&", ">=")
     */
    String getSymbol();

    /**
     * Indicates this is a binary operator
     * @return false (always for binary operators)
     */
    default boolean isUnary() {
        return false;
    }
}