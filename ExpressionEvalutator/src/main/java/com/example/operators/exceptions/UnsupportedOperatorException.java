package com.example.operators.exceptions;

/**
 * Thrown when an unknown or unsupported operator is requested
 */
public class UnsupportedOperatorException extends RuntimeException {

    public UnsupportedOperatorException(String message) {
        super(message);
    }

    public UnsupportedOperatorException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Creates an UnsupportedOperatorException for unknown operators
     */
    public static UnsupportedOperatorException unknownOperator(String operator) {
        return new UnsupportedOperatorException("Unknown operator: " + operator);
    }

    /**
     * Creates an UnsupportedOperatorException for operators not supported in a context
     */
    public static UnsupportedOperatorException notSupportedInContext(String operator, String context) {
        return new UnsupportedOperatorException(
            String.format("Operator '%s' is not supported in context: %s", operator, context)
        );
    }
}