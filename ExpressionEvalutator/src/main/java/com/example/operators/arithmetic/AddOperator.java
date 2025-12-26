package com.example.operators.arithmetic;

import com.example.operators.OperatorEvaluator;
import com.example.operators.OperatorType;
import com.example.operators.OperatorPrecedence;
import com.example.operators.exceptions.TypeMismatchException;

/**
 * Addition operator (+)
 * Supports both numeric addition and string concatenation
 */
public class AddOperator implements OperatorEvaluator {

    @Override
    public Object eval(Object leftOperand, Object rightOperand) {
        if (leftOperand == null || rightOperand == null) {
            throw TypeMismatchException.requiresType("+", "non-null operands",
                leftOperand == null ? "null" : rightOperand);
        }

        // String concatenation support
        if (leftOperand instanceof String || rightOperand instanceof String) {
            return leftOperand.toString() + rightOperand.toString();
        }

        // Numeric addition
        if (leftOperand instanceof Number && rightOperand instanceof Number) {
            return performNumericAddition((Number) leftOperand, (Number) rightOperand);
        }

        throw TypeMismatchException.requiresType("+", "numeric or string operands",
            String.format("%s and %s",
                leftOperand.getClass().getSimpleName(),
                rightOperand.getClass().getSimpleName()));
    }

    private Number performNumericAddition(Number left, Number right) {
        // Determine result type based on operand types
        if (isFloatingPoint(left) || isFloatingPoint(right)) {
            return left.doubleValue() + right.doubleValue();
        }

        if (left instanceof Long || right instanceof Long) {
            long result = left.longValue() + right.longValue();
            // Check for overflow
            if (((left.longValue() > 0) && (right.longValue() > 0) && (result <= 0)) ||
                ((left.longValue() < 0) && (right.longValue() < 0) && (result >= 0))) {
                throw new ArithmeticException("Addition overflow");
            }
            return result;
        }

        int result = left.intValue() + right.intValue();
        // Check for overflow and promote to long if needed
        if (((left.intValue() > 0) && (right.intValue() > 0) && (result <= 0)) ||
            ((left.intValue() < 0) && (right.intValue() < 0) && (result >= 0))) {
            return left.longValue() + right.longValue();
        }
        return result;
    }

    private boolean isFloatingPoint(Number number) {
        return number instanceof Float || number instanceof Double;
    }

    @Override
    public int getPrecedence() {
        return OperatorPrecedence.ADDITION;
    }

    @Override
    public OperatorType getType() {
        return OperatorType.ARITHMETIC;
    }

    @Override
    public String getSymbol() {
        return "+";
    }

    @Override
    public String toString() {
        return "AddOperator(+)";
    }
}