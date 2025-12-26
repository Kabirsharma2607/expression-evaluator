package com.example.expression;

import com.example.expression.ast.*;
import com.example.operators.OperatorEvaluator;
import com.example.operators.OperatorFactory;
import com.example.operators.UnaryOperatorEvaluator;
import com.example.policy.FeatureMapResolver;

import java.util.List;
import java.util.Map;

/**
 * Evaluates expressions by traversing the AST and using the operator system
 */
public class ExpressionEvaluator implements ASTVisitor<Object> {

    private final FeatureMapResolver featureResolver;
    private Map<String, Object> featureMap;
    private Map<String, Object> ruleContext;

    public ExpressionEvaluator() {
        this.featureResolver = new FeatureMapResolver();
    }

    /**
     * Main entry point for evaluating an expression string
     */
    public Object evaluate(String expression, Map<String, Object> featureMap) {
        return evaluate(expression, featureMap, Map.of());
    }

    /**
     * Evaluates an expression string with feature map and rule context
     */
    public Object evaluate(String expression, Map<String, Object> featureMap, Map<String, Object> ruleContext) {
        if (expression == null || expression.trim().isEmpty()) {
            throw new IllegalArgumentException("Expression cannot be null or empty");
        }

        this.featureMap = featureMap;
        this.ruleContext = ruleContext;

        try {
            // Tokenize the expression
            Tokenizer tokenizer = new Tokenizer(expression);
            List<Token> tokens = tokenizer.tokenize();

            // Parse into AST
            ExpressionParser parser = new ExpressionParser(tokens);
            ASTNode ast = parser.parse();

            // Evaluate the AST
            return ast.accept(this);

        } catch (Exception e) {
            throw new ExpressionEvaluationException("Failed to evaluate expression: " + expression, e);
        }
    }

    @Override
    public Object visitBinaryOperation(BinaryOperationNode node) {
        String operatorSymbol = node.getOperator();

        // Short-circuit evaluation for logical operators
        if ("&&".equals(operatorSymbol)) {
            Object leftValue = node.getLeft().accept(this);

            // If left is false, short-circuit and return false without evaluating right
            if (leftValue instanceof Boolean && !((Boolean) leftValue)) {
                return false;
            }

            // Left is true, evaluate right operand
            Object rightValue = node.getRight().accept(this);

            try {
                OperatorEvaluator operator = OperatorFactory.getBinaryOperator(operatorSymbol);
                return operator.eval(leftValue, rightValue);
            } catch (Exception e) {
                throw new ExpressionEvaluationException(
                    String.format("Error evaluating && operation '%s && %s': %s",
                        leftValue, rightValue, e.getMessage()), e);
            }
        }

        if ("||".equals(operatorSymbol)) {
            Object leftValue = node.getLeft().accept(this);

            // If left is true, short-circuit and return true without evaluating right
            if (leftValue instanceof Boolean && ((Boolean) leftValue)) {
                return true;
            }

            // Left is false, evaluate right operand
            Object rightValue = node.getRight().accept(this);

            try {
                OperatorEvaluator operator = OperatorFactory.getBinaryOperator(operatorSymbol);
                return operator.eval(leftValue, rightValue);
            } catch (Exception e) {
                throw new ExpressionEvaluationException(
                    String.format("Error evaluating || operation '%s || %s': %s",
                        leftValue, rightValue, e.getMessage()), e);
            }
        }

        // For all other operators, evaluate both operands normally
        Object leftValue = node.getLeft().accept(this);
        Object rightValue = node.getRight().accept(this);

        try {
            OperatorEvaluator operator = OperatorFactory.getBinaryOperator(operatorSymbol);
            return operator.eval(leftValue, rightValue);
        } catch (Exception e) {
            throw new ExpressionEvaluationException(
                String.format("Error evaluating binary operation '%s %s %s': %s",
                    leftValue, operatorSymbol, rightValue, e.getMessage()), e);
        }
    }

    @Override
    public Object visitUnaryOperation(UnaryOperationNode node) {
        Object operandValue = node.getOperand().accept(this);
        String operatorSymbol = node.getOperator();

        try {
            UnaryOperatorEvaluator operator = OperatorFactory.getUnaryOperator(operatorSymbol);
            return operator.eval(operandValue);
        } catch (Exception e) {
            throw new ExpressionEvaluationException(
                String.format("Error evaluating unary operation '%s%s': %s",
                    operatorSymbol, operandValue, e.getMessage()), e);
        }
    }

    @Override
    public Object visitLiteral(LiteralNode node) {
        return node.getValue();
    }

    @Override
    public Object visitIdentifier(IdentifierNode node) {
        String name = node.getName();

        // Check rule context first (for rule references like rule1, rule2)
        if (ruleContext.containsKey(name)) {
            return ruleContext.get(name);
        }

        // Check if it's a direct feature map key
        if (featureMap.containsKey(name)) {
            return featureMap.get(name);
        }

        throw new ExpressionEvaluationException("Unknown identifier: " + name);
    }

    @Override
    public Object visitFeatureAccess(FeatureAccessNode node) {
        String path = node.getPath();

        try {
            return featureResolver.resolveValue(path, featureMap);
        } catch (Exception e) {
            throw new ExpressionEvaluationException(
                "Failed to access feature path: " + path + " - " + e.getMessage(), e);
        }
    }

    /**
     * Custom exception for expression evaluation errors
     */
    public static class ExpressionEvaluationException extends RuntimeException {
        public ExpressionEvaluationException(String message) {
            super(message);
        }

        public ExpressionEvaluationException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}