package com.example.expression.ast;

/**
 * AST node representing feature map access (e.g., featureMap.userAge)
 */
public class FeatureAccessNode implements ASTNode {
    private final String path;

    public FeatureAccessNode(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visitFeatureAccess(this);
    }

    @Override
    public String toString() {
        return path;
    }
}