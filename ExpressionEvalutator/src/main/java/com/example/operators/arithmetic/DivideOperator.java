package com.example.operators.arithmetic;

import com.example.operators.OperatorEvaluator;
import com.example.operators.OperatorType;
import com.example.operators.OperatorPrecedence;
import com.example.operators.exceptions.TypeMismatchException;

/**
 * Division operator (/)
 * Performs numeric division, always returns double for precision
 */
public class DivideOperator implements OperatorEvaluator {

    @Override
    public Object eval(Object leftOperand, Object rightOperand) {
        if (!(leftOperand instanceof Number) || !(rightOperand instanceof Number)) {
            throw TypeMismatchException.requiresType("/", "numeric operands",
                String.format("%s and %s",
                    leftOperand != null ? leftOperand.getClass().getSimpleName() : "null",
                    rightOperand != null ? rightOperand.getClass().getSimpleName() : "null"));
        }

        Number left = (Number) leftOperand;
        Number right = (Number) rightOperand;

        // Check for division by zero
        double rightValue = right.doubleValue();
        if (rightValue == 0.0) {
            throw new ArithmeticException("Division by zero");
        }

        // Division always returns double to handle fractions properly
        double result = left.doubleValue() / rightValue;

        // Check for special floating-point values
        if (Double.isNaN(result)) {
            throw new ArithmeticException("Division resulted in NaN");
        }
        if (Double.isInfinite(result)) {
            throw new ArithmeticException("Division resulted in infinity");
        }

        return result;
    }

    @Override
    public int getPrecedence() {
        return OperatorPrecedence.MULTIPLICATION; // Same as multiplication
    }

    @Override
    public OperatorType getType() {
        return OperatorType.ARITHMETIC;
    }

    @Override
    public String getSymbol() {
        return "/";
    }

    @Override
    public String toString() {
        return "DivideOperator(/)";
    }
}