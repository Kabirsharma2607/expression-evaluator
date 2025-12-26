package com.example.operators.comparison;

import com.example.operators.OperatorEvaluator;
import com.example.operators.OperatorType;
import com.example.operators.OperatorPrecedence;

/**
 * Not equal operator (!=)
 * Compares two values for inequality (opposite of EqualOperator)
 */
public class NotEqualOperator implements OperatorEvaluator {

    private final EqualOperator equalOperator = new EqualOperator();

    @Override
    public Object eval(Object leftOperand, Object rightOperand) {
        // Delegate to EqualOperator and negate the result
        Boolean equalResult = (Boolean) equalOperator.eval(leftOperand, rightOperand);
        return !equalResult;
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
        return "!=";
    }

    @Override
    public String toString() {
        return "NotEqualOperator(!=)";
    }
}