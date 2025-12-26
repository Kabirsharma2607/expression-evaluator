package com.example.operators;

/**
 * Defines operator precedence constants.
 * Higher numbers indicate higher precedence (evaluated first).
 *
 * Precedence Order (High to Low):
 * 1. Parentheses () - handled by parser
 * 2. Unary operators (!, -)
 * 3. Multiplication, Division, Modulus (*, /, %)
 * 4. Addition, Subtraction (+, -)
 * 5. Comparison operators (<, <=, >, >=)
 * 6. Equality operators (==, !=)
 * 7. Logical AND (&&)
 * 8. Logical OR (||)
 */
public class OperatorPrecedence {

    /** Logical OR has lowest precedence */
    public static final int LOGICAL_OR = 1;

    /** Logical AND */
    public static final int LOGICAL_AND = 2;

    /** Equality operators (==, !=) */
    public static final int EQUALITY = 3;

    /** Comparison operators (<, <=, >, >=) */
    public static final int COMPARISON = 4;

    /** Addition and Subtraction (+, -) */
    public static final int ADDITION = 5;

    /** Multiplication, Division, Modulus (*, /, %) */
    public static final int MULTIPLICATION = 6;

    /** Unary operators (!, -) have highest precedence */
    public static final int UNARY = 7;

    // Private constructor to prevent instantiation
    private OperatorPrecedence() {}
}