package com.example.operators;

import com.example.operators.comparison.*;
import com.example.operators.logical.*;
import com.example.operators.unary.*;
import com.example.operators.arithmetic.*;
import com.example.operators.exceptions.UnsupportedOperatorException;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Factory class for managing operator registration and lookup.
 * Implements thread-safe singleton pattern for operator management.
 * Supports both binary and unary operators.
 */
public class OperatorFactory {

    // Thread-safe map for storing registered binary operators
    private static final Map<String, OperatorEvaluator> binaryOperators = new ConcurrentHashMap<>();

    // Thread-safe map for storing registered unary operators
    private static final Map<String, UnaryOperatorEvaluator> unaryOperators = new ConcurrentHashMap<>();

    // Static initialization block to register default operators
    static {
        registerDefaultOperators();
    }

    /**
     * Retrieves a binary operator by its symbol
     * @param symbol the operator symbol (e.g., "+", "&&", ">=")
     * @return the binary operator evaluator instance
     * @throws UnsupportedOperatorException if operator is not found
     */
    public static OperatorEvaluator getBinaryOperator(String symbol) {
        OperatorEvaluator operator = binaryOperators.get(symbol);
        if (operator == null) {
            throw UnsupportedOperatorException.unknownOperator(symbol + " (binary)");
        }
        return operator;
    }

    /**
     * Retrieves a unary operator by its symbol
     * @param symbol the operator symbol (e.g., "!", "-")
     * @return the unary operator evaluator instance
     * @throws UnsupportedOperatorException if operator is not found
     */
    public static UnaryOperatorEvaluator getUnaryOperator(String symbol) {
        UnaryOperatorEvaluator operator = unaryOperators.get(symbol);
        if (operator == null) {
            throw UnsupportedOperatorException.unknownOperator(symbol + " (unary)");
        }
        return operator;
    }

    /**
     * Retrieves an operator by its symbol (for backward compatibility)
     * Tries binary operators first, then unary operators
     * @param symbol the operator symbol
     * @return the operator evaluator instance
     * @throws UnsupportedOperatorException if operator is not found
     */
    public static Object getOperator(String symbol) {
        // Try binary first
        if (binaryOperators.containsKey(symbol)) {
            return getBinaryOperator(symbol);
        }

        // Try unary
        if (unaryOperators.containsKey(symbol)) {
            return getUnaryOperator(symbol);
        }

        throw UnsupportedOperatorException.unknownOperator(symbol);
    }

    /**
     * Registers a new binary operator or replaces an existing one
     * @param symbol the operator symbol
     * @param operator the binary operator evaluator implementation
     */
    public static void registerBinaryOperator(String symbol, OperatorEvaluator operator) {
        if (symbol == null || symbol.trim().isEmpty()) {
            throw new IllegalArgumentException("Operator symbol cannot be null or empty");
        }
        if (operator == null) {
            throw new IllegalArgumentException("Operator evaluator cannot be null");
        }

        binaryOperators.put(symbol, operator);
    }

    /**
     * Registers a new unary operator or replaces an existing one
     * @param symbol the operator symbol
     * @param operator the unary operator evaluator implementation
     */
    public static void registerUnaryOperator(String symbol, UnaryOperatorEvaluator operator) {
        if (symbol == null || symbol.trim().isEmpty()) {
            throw new IllegalArgumentException("Operator symbol cannot be null or empty");
        }
        if (operator == null) {
            throw new IllegalArgumentException("Operator evaluator cannot be null");
        }

        unaryOperators.put(symbol, operator);
    }

    /**
     * Legacy method for binary operator registration (for backward compatibility)
     * @param symbol the operator symbol
     * @param operator the binary operator evaluator implementation
     */
    public static void registerOperator(String symbol, OperatorEvaluator operator) {
        registerBinaryOperator(symbol, operator);
    }

    /**
     * Checks if a binary operator is registered
     * @param symbol the operator symbol to check
     * @return true if the binary operator exists, false otherwise
     */
    public static boolean isBinaryOperatorRegistered(String symbol) {
        if (symbol == null) {
            return false;
        }
        return binaryOperators.containsKey(symbol);
    }

    /**
     * Checks if a unary operator is registered
     * @param symbol the operator symbol to check
     * @return true if the unary operator exists, false otherwise
     */
    public static boolean isUnaryOperatorRegistered(String symbol) {
        if (symbol == null) {
            return false;
        }
        return unaryOperators.containsKey(symbol);
    }

    /**
     * Checks if an operator (binary or unary) is registered
     * @param symbol the operator symbol to check
     * @return true if the operator exists, false otherwise
     */
    public static boolean isRegistered(String symbol) {
        return isBinaryOperatorRegistered(symbol) || isUnaryOperatorRegistered(symbol);
    }

    /**
     * Returns a set of all registered operator symbols (binary and unary)
     * @return set of operator symbols
     */
    public static Set<String> getSupportedOperators() {
        Set<String> allOperators = new HashSet<>();
        allOperators.addAll(binaryOperators.keySet());
        allOperators.addAll(unaryOperators.keySet());
        return Set.copyOf(allOperators);
    }

    /**
     * Returns the total number of registered operators (binary and unary)
     * @return count of operators
     */
    public static int getOperatorCount() {
        return binaryOperators.size() + unaryOperators.size();
    }

    /**
     * Returns binary operators of a specific type
     * @param operatorType the type to filter by
     * @return map of symbol -> operator for the specified type
     */
    public static Map<String, OperatorEvaluator> getBinaryOperatorsByType(OperatorType operatorType) {
        Map<String, OperatorEvaluator> result = new HashMap<>();

        for (Map.Entry<String, OperatorEvaluator> entry : binaryOperators.entrySet()) {
            if (entry.getValue().getType() == operatorType) {
                result.put(entry.getKey(), entry.getValue());
            }
        }

        return result;
    }

    /**
     * Returns unary operators of a specific type
     * @param operatorType the type to filter by
     * @return map of symbol -> operator for the specified type
     */
    public static Map<String, UnaryOperatorEvaluator> getUnaryOperatorsByType(OperatorType operatorType) {
        Map<String, UnaryOperatorEvaluator> result = new HashMap<>();

        for (Map.Entry<String, UnaryOperatorEvaluator> entry : unaryOperators.entrySet()) {
            if (entry.getValue().getType() == operatorType) {
                result.put(entry.getKey(), entry.getValue());
            }
        }

        return result;
    }

    /**
     * Legacy method - returns binary operators of a specific type (for backward compatibility)
     * @param operatorType the type to filter by
     * @return map of symbol -> operator for the specified type
     */
    public static Map<String, OperatorEvaluator> getOperatorsByType(OperatorType operatorType) {
        return getBinaryOperatorsByType(operatorType);
    }

    /**
     * Clears all registered operators (useful for testing)
     * WARNING: This will remove all operators including defaults
     */
    public static void clearAllOperators() {
        binaryOperators.clear();
        unaryOperators.clear();
    }

    /**
     * Registers all default operators
     */
    private static void registerDefaultOperators() {
        // Register comparison operators
        registerBinaryOperator("<", new LessThanOperator());
        registerBinaryOperator("<=", new LessThanEqualOperator());
        registerBinaryOperator(">", new GreaterThanOperator());
        registerBinaryOperator(">=", new GreaterThanEqualOperator());
        registerBinaryOperator("==", new EqualOperator());
        registerBinaryOperator("!=", new NotEqualOperator());

        // Register logical operators
        registerBinaryOperator("&&", new AndOperator());
        registerBinaryOperator("||", new OrOperator());

        // Register unary operators
        registerUnaryOperator("!", new NotOperator());
        registerUnaryOperator("-", new NegateOperator());

        // Register arithmetic operators
        registerBinaryOperator("+", new AddOperator());
        registerBinaryOperator("-", new SubtractOperator());
        registerBinaryOperator("*", new MultiplyOperator());
        registerBinaryOperator("/", new DivideOperator());
        registerBinaryOperator("%", new ModulusOperator());
    }

    /**
     * Re-registers all default operators (useful after clearing)
     */
    public static void resetToDefaults() {
        clearAllOperators();
        registerDefaultOperators();
    }

    // Private constructor to prevent instantiation
    private OperatorFactory() {}
}