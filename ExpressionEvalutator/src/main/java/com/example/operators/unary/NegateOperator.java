package com.example.operators.unary;

import com.example.operators.UnaryOperatorEvaluator;
import com.example.operators.OperatorType;
import com.example.operators.OperatorPrecedence;
import com.example.operators.exceptions.TypeMismatchException;

/**
 * Unary minus operator (-)
 * Returns the numeric negation of a numeric operand
 */
public class NegateOperator implements UnaryOperatorEvaluator {

    @Override
    public Object eval(Object operand) {
        if (!(operand instanceof Number)) {
            throw TypeMismatchException.requiresType("- (unary)", "numeric", operand);
        }

        Number number = (Number) operand;

        // Preserve original number type when possible
        if (number instanceof Integer) {
            int value = number.intValue();
            // Handle Integer.MIN_VALUE overflow
            if (value == Integer.MIN_VALUE) {
                return (long) Integer.MAX_VALUE + 1L; // Promote to long
            }
            return -value;
        } else if (number instanceof Long) {
            long value = number.longValue();
            // Handle Long.MIN_VALUE overflow
            if (value == Long.MIN_VALUE) {
                // Could promote to BigInteger, but for simplicity we'll allow the overflow
                return -value; // This will overflow to Long.MIN_VALUE
            }
            return -value;
        } else if (number instanceof Float) {
            return -number.floatValue();
        } else if (number instanceof Double) {
            return -number.doubleValue();
        } else if (number instanceof Short) {
            return (short) -number.shortValue();
        } else if (number instanceof Byte) {
            return (byte) -number.byteValue();
        } else {
            // Fallback to double for other Number types
            return -number.doubleValue();
        }
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
        return "-";
    }

    @Override
    public String toString() {
        return "NegateOperator(- unary)";
    }
}