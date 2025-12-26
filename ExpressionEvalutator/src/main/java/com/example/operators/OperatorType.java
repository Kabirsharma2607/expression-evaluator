package com.example.operators;

/**
 * Categorizes operators by their function type
 */
public enum OperatorType {
    /**
     * Comparison operators: <, <=, >, >=, ==, !=
     */
    COMPARISON,

    /**
     * Logical operators: &&, ||
     */
    LOGICAL,

    /**
     * Arithmetic operators: +, -, *, /, %
     */
    ARITHMETIC,

    /**
     * Unary operators: !, - (unary)
     */
    UNARY
}