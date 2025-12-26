package com.example.expression.ast;

/**
 * AST node representing a literal value (number, string, boolean)
 */
public class LiteralNode implements ASTNode {
    private final Object value;
    private final LiteralType type;

    public enum LiteralType {
        NUMBER, STRING, BOOLEAN
    }

    public LiteralNode(Object value, LiteralType type) {
        this.value = value;
        this.type = type;
    }

    public Object getValue() {
        return value;
    }

    public LiteralType getType() {
        return type;
    }

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visitLiteral(this);
    }

    @Override
    public String toString() {
        if (type == LiteralType.STRING) {
            return "\"" + value + "\"";
        }
        return String.valueOf(value);
    }
}