package com.example.operators;

import com.example.operators.comparison.*;
import com.example.operators.exceptions.TypeMismatchException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive tests for all comparison operators
 */
public class ComparisonOperatorsTest {

    @Test
    public void testLessThanOperator() {
        LessThanOperator operator = new LessThanOperator();

        // Basic numeric comparisons
        assertTrue((Boolean) operator.eval(5, 10));
        assertFalse((Boolean) operator.eval(10, 5));
        assertFalse((Boolean) operator.eval(5, 5));

        // Type mixing
        assertTrue((Boolean) operator.eval(5, 5.1));
        assertFalse((Boolean) operator.eval(5.1, 5));

        // Different number types
        assertTrue((Boolean) operator.eval(5, 10L));
        assertTrue((Boolean) operator.eval(5.0f, 10.0));

        // Test properties
        assertEquals("<", operator.getSymbol());
        assertEquals(OperatorType.COMPARISON, operator.getType());
        assertEquals(OperatorPrecedence.COMPARISON, operator.getPrecedence());
    }

    @Test
    public void testLessThanOperator_InvalidTypes() {
        LessThanOperator operator = new LessThanOperator();

        assertThrows(TypeMismatchException.class, () -> operator.eval("5", 10));
        assertThrows(TypeMismatchException.class, () -> operator.eval(5, "10"));
        assertThrows(TypeMismatchException.class, () -> operator.eval(null, 10));
        assertThrows(TypeMismatchException.class, () -> operator.eval(5, null));
        assertThrows(TypeMismatchException.class, () -> operator.eval(true, 5));
    }

    @Test
    public void testLessThanEqualOperator() {
        LessThanEqualOperator operator = new LessThanEqualOperator();

        // Basic numeric comparisons
        assertTrue((Boolean) operator.eval(5, 10));
        assertFalse((Boolean) operator.eval(10, 5));
        assertTrue((Boolean) operator.eval(5, 5)); // Equal case

        // Type mixing
        assertTrue((Boolean) operator.eval(5, 5.0));
        assertTrue((Boolean) operator.eval(5.0, 5));

        // Test properties
        assertEquals("<=", operator.getSymbol());
        assertEquals(OperatorType.COMPARISON, operator.getType());
        assertEquals(OperatorPrecedence.COMPARISON, operator.getPrecedence());
    }

    @Test
    public void testGreaterThanOperator() {
        GreaterThanOperator operator = new GreaterThanOperator();

        // Basic numeric comparisons
        assertFalse((Boolean) operator.eval(5, 10));
        assertTrue((Boolean) operator.eval(10, 5));
        assertFalse((Boolean) operator.eval(5, 5));

        // Type mixing
        assertFalse((Boolean) operator.eval(5, 5.1));
        assertTrue((Boolean) operator.eval(5.1, 5));

        // Test properties
        assertEquals(">", operator.getSymbol());
        assertEquals(OperatorType.COMPARISON, operator.getType());
        assertEquals(OperatorPrecedence.COMPARISON, operator.getPrecedence());
    }

    @Test
    public void testGreaterThanEqualOperator() {
        GreaterThanEqualOperator operator = new GreaterThanEqualOperator();

        // Basic numeric comparisons
        assertFalse((Boolean) operator.eval(5, 10));
        assertTrue((Boolean) operator.eval(10, 5));
        assertTrue((Boolean) operator.eval(5, 5)); // Equal case

        // Type mixing
        assertTrue((Boolean) operator.eval(5.0, 5));
        assertFalse((Boolean) operator.eval(5, 5.1));

        // Test properties
        assertEquals(">=", operator.getSymbol());
        assertEquals(OperatorType.COMPARISON, operator.getType());
        assertEquals(OperatorPrecedence.COMPARISON, operator.getPrecedence());
    }

    @Test
    public void testEqualOperator() {
        EqualOperator operator = new EqualOperator();

        // Numeric equality with type coercion
        assertTrue((Boolean) operator.eval(5, 5.0));
        assertTrue((Boolean) operator.eval(5L, 5));
        assertFalse((Boolean) operator.eval(5, 6));

        // String equality
        assertTrue((Boolean) operator.eval("hello", "hello"));
        assertFalse((Boolean) operator.eval("hello", "world"));

        // Boolean equality
        assertTrue((Boolean) operator.eval(true, true));
        assertFalse((Boolean) operator.eval(true, false));

        // Null handling
        assertTrue((Boolean) operator.eval(null, null));
        assertFalse((Boolean) operator.eval(null, 5));
        assertFalse((Boolean) operator.eval(5, null));

        // Cross-type inequality (no type coercion for non-numbers)
        assertFalse((Boolean) operator.eval(5, "5"));
        assertFalse((Boolean) operator.eval(true, 1));

        // Test properties
        assertEquals("==", operator.getSymbol());
        assertEquals(OperatorType.COMPARISON, operator.getType());
        assertEquals(OperatorPrecedence.EQUALITY, operator.getPrecedence());
    }

    @Test
    public void testNotEqualOperator() {
        NotEqualOperator operator = new NotEqualOperator();

        // Should be opposite of EqualOperator
        assertFalse((Boolean) operator.eval(5, 5.0));
        assertFalse((Boolean) operator.eval(5L, 5));
        assertTrue((Boolean) operator.eval(5, 6));

        assertTrue((Boolean) operator.eval("hello", "world"));
        assertFalse((Boolean) operator.eval("hello", "hello"));

        assertFalse((Boolean) operator.eval(null, null));
        assertTrue((Boolean) operator.eval(null, 5));
        assertTrue((Boolean) operator.eval(5, null));

        // Test properties
        assertEquals("!=", operator.getSymbol());
        assertEquals(OperatorType.COMPARISON, operator.getType());
        assertEquals(OperatorPrecedence.EQUALITY, operator.getPrecedence());
    }

    @Test
    public void testComparisonOperators_EdgeCases() {
        // Test with floating point precision
        GreaterThanOperator gt = new GreaterThanOperator();
        LessThanOperator lt = new LessThanOperator();
        EqualOperator eq = new EqualOperator();

        // Very small differences
        assertTrue((Boolean) gt.eval(1.0000001, 1.0));
        assertTrue((Boolean) lt.eval(1.0, 1.0000001));
        assertFalse((Boolean) eq.eval(1.0000001, 1.0));

        // Integer overflow scenarios
        assertTrue((Boolean) gt.eval(Integer.MAX_VALUE + 1L, Integer.MAX_VALUE));
        assertTrue((Boolean) eq.eval(Integer.MAX_VALUE, Integer.MAX_VALUE));

        // Zero comparisons
        assertTrue((Boolean) eq.eval(0, 0.0));
        assertFalse((Boolean) gt.eval(0, 0.0));

        // Positive vs negative numbers
        assertTrue((Boolean) gt.eval(1, -1));
        assertTrue((Boolean) lt.eval(-1, 1));
    }
}