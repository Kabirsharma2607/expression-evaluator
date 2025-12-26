package com.example.operators.arithmetic;

import com.example.operators.OperatorEvaluator;
import com.example.operators.OperatorType;
import com.example.operators.OperatorPrecedence;
import com.example.operators.exceptions.TypeMismatchException;

/**
 * Modulus operator (%)
 * Performs remainder operation for both integer and floating-point numbers
 */
public class ModulusOperator implements OperatorEvaluator {

    @Override
    public Object eval(Object leftOperand, Object rightOperand) {
        if (!(leftOperand instanceof Number) || !(rightOperand instanceof Number)) {
            throw TypeMismatchException.requiresType("%", "numeric operands",
                String.format("%s and %s",
                    leftOperand != null ? leftOperand.getClass().getSimpleName() : "null",
                    rightOperand != null ? rightOperand.getClass().getSimpleName() : "null"));
        }

        Number left = (Number) leftOperand;
        Number right = (Number) rightOperand;

        // Check for modulus by zero
        if (right.doubleValue() == 0.0) {
            throw new ArithmeticException("Modulus by zero");
        }

        // For integer types, use integer modulus to preserve type
        if (isIntegerType(left) && isIntegerType(right)) {
            if (left instanceof Long || right instanceof Long) {
                return left.longValue() % right.longValue();
            } else {
                return left.intValue() % right.intValue();
            }
        }

        // For floating-point, use floating-point modulus
        double result = left.doubleValue() % right.doubleValue();

        // Check for special floating-point values
        if (Double.isNaN(result)) {
            throw new ArithmeticException("Modulus resulted in NaN");
        }

        return result;
    }

    private boolean isIntegerType(Number number) {
        return number instanceof Integer || number instanceof Long ||
               number instanceof Short || number instanceof Byte;
    }

    @Override
    public int getPrecedence() {
        return OperatorPrecedence.MULTIPLICATION; // Same as multiplication and division
    }

    @Override
    public OperatorType getType() {
        return OperatorType.ARITHMETIC;
    }

    @Override
    public String getSymbol() {
        return "%";
    }

    @Override
    public String toString() {
        return "ModulusOperator(%)";
    }
}