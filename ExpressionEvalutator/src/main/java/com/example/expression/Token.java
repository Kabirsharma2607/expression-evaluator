package com.example.expression;

/**
 * Represents a token in an expression
 */
public class Token {

    public enum TokenType {
        // Literals
        NUMBER,        // 123, 45.67
        STRING,        // "hello", 'world'
        BOOLEAN,       // true, false
        IDENTIFIER,    // variable names, rule names

        // Operators
        PLUS,          // +
        MINUS,         // -
        MULTIPLY,      // *
        DIVIDE,        // /
        MODULUS,       // %

        // Comparison
        LESS_THAN,     // <
        LESS_EQUAL,    // <=
        GREATER_THAN,  // >
        GREATER_EQUAL, // >=
        EQUAL,         // ==
        NOT_EQUAL,     // !=

        // Logical
        AND,           // &&
        OR,            // ||
        NOT,           // !

        // Punctuation
        LEFT_PAREN,    // (
        RIGHT_PAREN,   // )
        DOT,           // .

        // Special
        EOF,           // End of input
        UNKNOWN        // Invalid token
    }

    private final TokenType type;
    private final String value;
    private final int position;

    public Token(TokenType type, String value, int position) {
        this.type = type;
        this.value = value;
        this.position = position;
    }

    public TokenType getType() {
        return type;
    }

    public String getValue() {
        return value;
    }

    public int getPosition() {
        return position;
    }

    public boolean isOperator() {
        return type == TokenType.PLUS || type == TokenType.MINUS || type == TokenType.MULTIPLY || type == TokenType.DIVIDE || type == TokenType.MODULUS ||
               type == TokenType.LESS_THAN || type == TokenType.LESS_EQUAL || type == TokenType.GREATER_THAN || type == TokenType.GREATER_EQUAL ||
               type == TokenType.EQUAL || type == TokenType.NOT_EQUAL || type == TokenType.AND || type == TokenType.OR;
    }

    public boolean isUnaryOperator() {
        return type == TokenType.NOT || type == TokenType.MINUS;
    }

    public boolean isBinaryOperator() {
        return isOperator() && !isUnaryOperator();
    }

    public boolean isLiteral() {
        return type == TokenType.NUMBER || type == TokenType.STRING || type == TokenType.BOOLEAN;
    }

    @Override
    public String toString() {
        return String.format("Token{%s, '%s', pos=%d}", type, value, position);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Token token = (Token) obj;
        return position == token.position &&
               type == token.type &&
               java.util.Objects.equals(value, token.value);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(type, value, position);
    }
}