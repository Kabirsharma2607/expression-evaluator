package com.example.operators.comparison;

import com.example.operators.OperatorEvaluator;
import com.example.operators.OperatorType;
import com.example.operators.OperatorPrecedence;
import com.example.operators.exceptions.TypeMismatchException;

/**
 * Greater than or equal operator (>=)
 * Compares two numeric values and returns true if left >= right
 */
public class GreaterThanEqualOperator implements OperatorEvaluator {

    @Override
    public Object eval(Object leftOperand, Object rightOperand) {
        // Validate that both operands are numbers
        if (!(leftOperand instanceof Number) || !(rightOperand instanceof Number)) {
            throw TypeMismatchException.requiresType(">=", "numeric",
                !(leftOperand instanceof Number) ? leftOperand : rightOperand);
        }

        Number left = (Number) leftOperand;
        Number right = (Number) rightOperand;

        // Compare as double values to handle all numeric types consistently
        return left.doubleValue() >= right.doubleValue();
    }

    @Override
    public int getPrecedence() {
        return OperatorPrecedence.COMPARISON;
    }

    @Override
    public OperatorType getType() {
        return OperatorType.COMPARISON;
    }

    @Override
    public String getSymbol() {
        return ">=";
    }

    @Override
    public String toString() {
        return "GreaterThanEqualOperator(>=)";
    }
}