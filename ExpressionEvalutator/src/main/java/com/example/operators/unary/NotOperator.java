package com.example.operators.unary;

import com.example.operators.UnaryOperatorEvaluator;
import com.example.operators.OperatorType;
import com.example.operators.OperatorPrecedence;
import com.example.operators.exceptions.TypeMismatchException;

/**
 * Logical NOT operator (!)
 * Returns the logical negation of a boolean operand
 */
public class NotOperator implements UnaryOperatorEvaluator {

    @Override
    public Object eval(Object operand) {
        Boolean value = convertToBoolean(operand);

        if (value == null) {
            throw TypeMismatchException.requiresType("!", "boolean", operand);
        }

        return !value;
    }

    /**
     * Converts operand to boolean with optional truthiness rules
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
        return OperatorPrecedence.UNARY;
    }

    @Override
    public OperatorType getType() {
        return OperatorType.UNARY;
    }

    @Override
    public String getSymbol() {
        return "!";
    }

    @Override
    public String toString() {
        return "NotOperator(!)";
    }
}