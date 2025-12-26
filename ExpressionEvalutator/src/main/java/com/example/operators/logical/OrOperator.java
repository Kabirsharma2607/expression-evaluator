package com.example.operators.logical;

import com.example.operators.OperatorEvaluator;
import com.example.operators.OperatorType;
import com.example.operators.OperatorPrecedence;
import com.example.operators.exceptions.TypeMismatchException;

/**
 * Logical OR operator (||)
 * Returns true if either operand is true, false if both are false
 */
public class OrOperator implements OperatorEvaluator {

    @Override
    public Object eval(Object leftOperand, Object rightOperand) {
        Boolean left = convertToBoolean(leftOperand);
        Boolean right = convertToBoolean(rightOperand);

        if (left == null) {
            throw TypeMismatchException.requiresType("||", "boolean", leftOperand);
        }
        if (right == null) {
            throw TypeMismatchException.requiresType("||", "boolean", rightOperand);
        }

        return left || right;
    }

    /**
     * Converts operand to boolean with truthiness rules
     * @param operand the operand to convert
     * @return Boolean value or null if conversion not possible
     */
    private Boolean convertToBoolean(Object operand) {
        if (operand instanceof Boolean) {
            return (Boolean) operand;
        }

        // Optional: Support truthiness conversion for other types
        // Uncomment the following if you want JavaScript-like truthiness

        /*
        if (operand instanceof Number) {
            return ((Number) operand).doubleValue() != 0.0;
        }

        if (operand instanceof String) {
            String str = (String) operand;
            return !str.isEmpty() && !str.equalsIgnoreCase("false");
        }

        // null is false, everything else is true
        return operand != null;
        */

        // For strict type checking, only allow Boolean types
        return null;
    }

    @Override
    public int getPrecedence() {
        return OperatorPrecedence.LOGICAL_OR;
    }

    @Override
    public OperatorType getType() {
        return OperatorType.LOGICAL;
    }

    @Override
    public String getSymbol() {
        return "||";
    }

    @Override
    public String toString() {
        return "OrOperator(||)";
    }
}