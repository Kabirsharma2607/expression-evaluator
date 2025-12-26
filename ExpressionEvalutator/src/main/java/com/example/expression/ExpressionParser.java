package com.example.expression;

import com.example.expression.ast.*;
import com.example.operators.OperatorPrecedence;

import java.util.List;

/**
 * Recursive descent parser for expressions with operator precedence
 */
public class ExpressionParser {

    private final List<Token> tokens;
    private int currentTokenIndex;

    public ExpressionParser(List<Token> tokens) {
        this.tokens = tokens;
        this.currentTokenIndex = 0;
    }

    /**
     * Parses the token stream into an AST
     */
    public ASTNode parse() {
        if (tokens.isEmpty() || (tokens.size() == 1 && tokens.get(0).getType() == Token.TokenType.EOF)) {
            throw new ParseException("Empty expression");
        }

        ASTNode result = parseExpression();

        if (!isAtEnd()) {
            throw new ParseException("Unexpected token after expression: " + peek().getValue());
        }

        return result;
    }

    /**
     * Parses expressions with all operators (lowest precedence)
     */
    private ASTNode parseExpression() {
        return parseLogicalOr();
    }

    /**
     * Parses logical OR (||) - lowest precedence
     */
    private ASTNode parseLogicalOr() {
        ASTNode expr = parseLogicalAnd();

        while (match(Token.TokenType.OR)) {
            String operator = previous().getValue();
            ASTNode right = parseLogicalAnd();
            expr = new BinaryOperationNode(expr, operator, right);
        }

        return expr;
    }

    /**
     * Parses logical AND (&&)
     */
    private ASTNode parseLogicalAnd() {
        ASTNode expr = parseEquality();

        while (match(Token.TokenType.AND)) {
            String operator = previous().getValue();
            ASTNode right = parseEquality();
            expr = new BinaryOperationNode(expr, operator, right);
        }

        return expr;
    }

    /**
     * Parses equality operators (==, !=)
     */
    private ASTNode parseEquality() {
        ASTNode expr = parseComparison();

        while (match(Token.TokenType.EQUAL, Token.TokenType.NOT_EQUAL)) {
            String operator = previous().getValue();
            ASTNode right = parseComparison();
            expr = new BinaryOperationNode(expr, operator, right);
        }

        return expr;
    }

    /**
     * Parses comparison operators (<, <=, >, >=)
     */
    private ASTNode parseComparison() {
        ASTNode expr = parseAddition();

        while (match(Token.TokenType.LESS_THAN, Token.TokenType.LESS_EQUAL,
                    Token.TokenType.GREATER_THAN, Token.TokenType.GREATER_EQUAL)) {
            String operator = previous().getValue();
            ASTNode right = parseAddition();
            expr = new BinaryOperationNode(expr, operator, right);
        }

        return expr;
    }

    /**
     * Parses addition and subtraction (+, -)
     */
    private ASTNode parseAddition() {
        ASTNode expr = parseMultiplication();

        while (match(Token.TokenType.PLUS, Token.TokenType.MINUS)) {
            String operator = previous().getValue();
            ASTNode right = parseMultiplication();
            expr = new BinaryOperationNode(expr, operator, right);
        }

        return expr;
    }

    /**
     * Parses multiplication, division, and modulus (*, /, %)
     */
    private ASTNode parseMultiplication() {
        ASTNode expr = parseUnary();

        while (match(Token.TokenType.MULTIPLY, Token.TokenType.DIVIDE, Token.TokenType.MODULUS)) {
            String operator = previous().getValue();
            ASTNode right = parseUnary();
            expr = new BinaryOperationNode(expr, operator, right);
        }

        return expr;
    }

    /**
     * Parses unary operators (!, -)
     */
    private ASTNode parseUnary() {
        if (match(Token.TokenType.NOT, Token.TokenType.MINUS)) {
            String operator = previous().getValue();
            ASTNode operand = parseUnary(); // Right associative
            return new UnaryOperationNode(operator, operand);
        }

        return parsePrimary();
    }

    /**
     * Parses primary expressions (literals, identifiers, parenthesized expressions)
     */
    private ASTNode parsePrimary() {
        // Literals
        if (match(Token.TokenType.NUMBER)) {
            String value = previous().getValue();
            try {
                // Try parsing as integer first
                if (!value.contains(".")) {
                    return new LiteralNode(Integer.parseInt(value), LiteralNode.LiteralType.NUMBER);
                } else {
                    return new LiteralNode(Double.parseDouble(value), LiteralNode.LiteralType.NUMBER);
                }
            } catch (NumberFormatException e) {
                throw new ParseException("Invalid number: " + value);
            }
        }

        if (match(Token.TokenType.STRING)) {
            return new LiteralNode(previous().getValue(), LiteralNode.LiteralType.STRING);
        }

        if (match(Token.TokenType.BOOLEAN)) {
            return new LiteralNode(Boolean.parseBoolean(previous().getValue()), LiteralNode.LiteralType.BOOLEAN);
        }

        // Parenthesized expression
        if (match(Token.TokenType.LEFT_PAREN)) {
            ASTNode expr = parseExpression();
            consume(Token.TokenType.RIGHT_PAREN, "Expected ')' after expression");
            return expr;
        }

        // Identifiers and feature access
        if (match(Token.TokenType.IDENTIFIER)) {
            String name = previous().getValue();

            // Check for feature map access (featureMap.something)
            if ("featureMap".equals(name) && check(Token.TokenType.DOT)) {
                return parseFeatureAccess(name);
            }

            // Regular identifier (rule name, variable)
            return new IdentifierNode(name);
        }

        throw new ParseException("Unexpected token: " + peek().getValue());
    }

    /**
     * Parses feature map access (featureMap.property.subProperty)
     */
    private ASTNode parseFeatureAccess(String baseName) {
        StringBuilder path = new StringBuilder(baseName);

        while (match(Token.TokenType.DOT)) {
            if (!match(Token.TokenType.IDENTIFIER)) {
                throw new ParseException("Expected identifier after '.'");
            }
            path.append(".").append(previous().getValue());
        }

        return new FeatureAccessNode(path.toString());
    }

    // Utility methods for parsing

    private boolean match(Token.TokenType... types) {
        for (Token.TokenType type : types) {
            if (check(type)) {
                advance();
                return true;
            }
        }
        return false;
    }

    private boolean check(Token.TokenType type) {
        if (isAtEnd()) return false;
        return peek().getType() == type;
    }

    private Token advance() {
        if (!isAtEnd()) currentTokenIndex++;
        return previous();
    }

    private boolean isAtEnd() {
        return currentTokenIndex >= tokens.size() || peek().getType() == Token.TokenType.EOF;
    }

    private Token peek() {
        if (currentTokenIndex >= tokens.size()) {
            return new Token(Token.TokenType.EOF, "", -1);
        }
        return tokens.get(currentTokenIndex);
    }

    private Token previous() {
        return tokens.get(currentTokenIndex - 1);
    }

    private Token consume(Token.TokenType type, String message) {
        if (check(type)) return advance();
        throw new ParseException(message + ". Got: " + peek().getValue());
    }

    /**
     * Custom exception for parse errors
     */
    public static class ParseException extends RuntimeException {
        public ParseException(String message) {
            super(message);
        }
    }
}