package com.example.operators.arithmetic;

import com.example.operators.OperatorEvaluator;
import com.example.operators.OperatorType;
import com.example.operators.OperatorPrecedence;
import com.example.operators.exceptions.TypeMismatchException;

/**
 * Binary subtraction operator (-)
 * Performs numeric subtraction between two operands
 */
public class SubtractOperator implements OperatorEvaluator {

    @Override
    public Object eval(Object leftOperand, Object rightOperand) {
        if (!(leftOperand instanceof Number) || !(rightOperand instanceof Number)) {
            throw TypeMismatchException.requiresType("- (binary)", "numeric operands",
                String.format("%s and %s",
                    leftOperand != null ? leftOperand.getClass().getSimpleName() : "null",
                    rightOperand != null ? rightOperand.getClass().getSimpleName() : "null"));
        }

        Number left = (Number) leftOperand;
        Number right = (Number) rightOperand;

        if (isFloatingPoint(left) || isFloatingPoint(right)) {
            return left.doubleValue() - right.doubleValue();
        }

        if (left instanceof Long || right instanceof Long) {
            long result = left.longValue() - right.longValue();
            // Check for overflow
            if (((left.longValue() > 0) && (right.longValue() < 0) && (result <= 0)) ||
                ((left.longValue() < 0) && (right.longValue() > 0) && (result >= 0))) {
                throw new ArithmeticException("Subtraction overflow");
            }
            return result;
        }

        int result = left.intValue() - right.intValue();
        // Check for overflow and promote to long if needed
        if (((left.intValue() > 0) && (right.intValue() < 0) && (result <= 0)) ||
            ((left.intValue() < 0) && (right.intValue() > 0) && (result >= 0))) {
            return left.longValue() - right.longValue();
        }
        return result;
    }

    private boolean isFloatingPoint(Number number) {
        return number instanceof Float || number instanceof Double;
    }

    @Override
    public int getPrecedence() {
        return OperatorPrecedence.ADDITION; // Same as addition
    }

    @Override
    public OperatorType getType() {
        return OperatorType.ARITHMETIC;
    }

    @Override
    public String getSymbol() {
        return "-";
    }

    @Override
    public String toString() {
        return "SubtractOperator(- binary)";
    }
}