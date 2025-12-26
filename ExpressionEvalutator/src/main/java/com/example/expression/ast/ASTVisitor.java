package com.example.expression.ast;

/**
 * Visitor interface for AST nodes using the Visitor pattern
 */
public interface ASTVisitor<T> {
    T visitBinaryOperation(BinaryOperationNode node);
    T visitUnaryOperation(UnaryOperationNode node);
    T visitLiteral(LiteralNode node);
    T visitIdentifier(IdentifierNode node);
    T visitFeatureAccess(FeatureAccessNode node);
}