package com.example.operators.comparison;

import com.example.operators.OperatorEvaluator;
import com.example.operators.OperatorType;
import com.example.operators.OperatorPrecedence;

import java.util.Objects;

/**
 * Equal operator (==)
 * Compares two values for equality with type-aware comparison
 */
public class EqualOperator implements OperatorEvaluator {

    @Override
    public Object eval(Object leftOperand, Object rightOperand) {
        // Handle null cases
        if (leftOperand == null && rightOperand == null) {
            return true;
        }
        if (leftOperand == null || rightOperand == null) {
            return false;
        }

        // If both are numbers, compare as doubles for type coercion
        if (leftOperand instanceof Number && rightOperand instanceof Number) {
            Number left = (Number) leftOperand;
            Number right = (Number) rightOperand;
            return Double.compare(left.doubleValue(), right.doubleValue()) == 0;
        }

        // For all other types, use standard equals comparison
        return Objects.equals(leftOperand, rightOperand);
    }

    @Override
    public int getPrecedence() {
        return OperatorPrecedence.EQUALITY;
    }

    @Override
    public OperatorType getType() {
        return OperatorType.COMPARISON;
    }

    @Override
    public String getSymbol() {
        return "==";
    }

    @Override
    public String toString() {
        return "EqualOperator(==)";
    }
}