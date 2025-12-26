package com.example.expression.ast;

/**
 * Base interface for all Abstract Syntax Tree nodes
 */
public interface ASTNode {
    /**
     * Accepts a visitor for the visitor pattern
     */
    <T> T accept(ASTVisitor<T> visitor);
}