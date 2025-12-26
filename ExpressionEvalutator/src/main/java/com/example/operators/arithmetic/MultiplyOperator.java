package com.example.operators.arithmetic;

import com.example.operators.OperatorEvaluator;
import com.example.operators.OperatorType;
import com.example.operators.OperatorPrecedence;
import com.example.operators.exceptions.TypeMismatchException;

/**
 * Multiplication operator (*)
 * Performs numeric multiplication with overflow detection
 */
public class MultiplyOperator implements OperatorEvaluator {

    @Override
    public Object eval(Object leftOperand, Object rightOperand) {
        if (!(leftOperand instanceof Number) || !(rightOperand instanceof Number)) {
            throw TypeMismatchException.requiresType("*", "numeric operands",
                String.format("%s and %s",
                    leftOperand != null ? leftOperand.getClass().getSimpleName() : "null",
                    rightOperand != null ? rightOperand.getClass().getSimpleName() : "null"));
        }

        Number left = (Number) leftOperand;
        Number right = (Number) rightOperand;

        if (isFloatingPoint(left) || isFloatingPoint(right)) {
            return left.doubleValue() * right.doubleValue();
        }

        if (left instanceof Long || right instanceof Long) {
            long leftLong = left.longValue();
            long rightLong = right.longValue();

            // Check for overflow using division check
            if (leftLong != 0 && rightLong != 0) {
                if ((leftLong > 0 && rightLong > 0 && leftLong > Long.MAX_VALUE / rightLong) ||
                    (leftLong < 0 && rightLong < 0 && leftLong < Long.MAX_VALUE / rightLong) ||
                    (leftLong > 0 && rightLong < 0 && rightLong < Long.MIN_VALUE / leftLong) ||
                    (leftLong < 0 && rightLong > 0 && leftLong < Long.MIN_VALUE / rightLong)) {
                    throw new ArithmeticException("Multiplication overflow");
                }
            }
            return leftLong * rightLong;
        }

        int leftInt = left.intValue();
        int rightInt = right.intValue();

        // Check for overflow and promote to long if needed
        if (leftInt != 0 && rightInt != 0) {
            if ((leftInt > 0 && rightInt > 0 && leftInt > Integer.MAX_VALUE / rightInt) ||
                (leftInt < 0 && rightInt < 0 && leftInt < Integer.MAX_VALUE / rightInt) ||
                (leftInt > 0 && rightInt < 0 && rightInt < Integer.MIN_VALUE / leftInt) ||
                (leftInt < 0 && rightInt > 0 && leftInt < Integer.MIN_VALUE / rightInt)) {
                return (long) leftInt * rightInt; // Promote to long
            }
        }
        return leftInt * rightInt;
    }

    private boolean isFloatingPoint(Number number) {
        return number instanceof Float || number instanceof Double;
    }

    @Override
    public int getPrecedence() {
        return OperatorPrecedence.MULTIPLICATION;
    }

    @Override
    public OperatorType getType() {
        return OperatorType.ARITHMETIC;
    }

    @Override
    public String getSymbol() {
        return "*";
    }

    @Override
    public String toString() {
        return "MultiplyOperator(*)";
    }
}