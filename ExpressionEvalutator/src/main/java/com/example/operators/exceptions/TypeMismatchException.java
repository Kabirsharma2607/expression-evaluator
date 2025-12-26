package com.example.operators.exceptions;

/**
 * Thrown when operator operands are not of compatible types
 */
public class TypeMismatchException extends RuntimeException {

    public TypeMismatchException(String message) {
        super(message);
    }

    public TypeMismatchException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Creates a TypeMismatchException with a formatted message for operator type mismatches
     */
    public static TypeMismatchException forOperator(String operator, Object leftOperand, Object rightOperand) {
        String leftType = leftOperand != null ? leftOperand.getClass().getSimpleName() : "null";
        String rightType = rightOperand != null ? rightOperand.getClass().getSimpleName() : "null";

        return new TypeMismatchException(
            String.format("Operator '%s' cannot be applied to operands of type '%s' and '%s'",
                operator, leftType, rightType)
        );
    }

    /**
     * Creates a TypeMismatchException for operators requiring specific types
     */
    public static TypeMismatchException requiresType(String operator, String requiredType, Object actualOperand) {
        String actualType = actualOperand != null ? actualOperand.getClass().getSimpleName() : "null";

        return new TypeMismatchException(
            String.format("Operator '%s' requires %s operands, but received: %s",
                operator, requiredType, actualType)
        );
    }
}