package com.example.operators;

import com.example.operators.unary.*;
import com.example.operators.exceptions.TypeMismatchException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive tests for all unary operators
 */
public class UnaryOperatorsTest {

    @Test
    public void testNotOperator() {
        NotOperator operator = new NotOperator();

        // Basic boolean operations
        assertFalse((Boolean) operator.eval(true));
        assertTrue((Boolean) operator.eval(false));

        // Test properties
        assertEquals("!", operator.getSymbol());
        assertEquals(OperatorType.UNARY, operator.getType());
        assertEquals(OperatorPrecedence.UNARY, operator.getPrecedence());
        assertTrue(operator.isUnary());
    }

    @Test
    public void testNotOperator_TypeValidation() {
        NotOperator operator = new NotOperator();

        // Should only accept boolean types (strict type checking)
        assertThrows(TypeMismatchException.class, () -> operator.eval(5));
        assertThrows(TypeMismatchException.class, () -> operator.eval("hello"));
        assertThrows(TypeMismatchException.class, () -> operator.eval(1));
        assertThrows(TypeMismatchException.class, () -> operator.eval(0));
        assertThrows(TypeMismatchException.class, () -> operator.eval(null));
    }

    @Test
    public void testNotOperator_ErrorMessages() {
        NotOperator operator = new NotOperator();

        // Test that error messages are informative
        try {
            operator.eval(5);
            fail("Expected TypeMismatchException");
        } catch (TypeMismatchException e) {
            assertTrue(e.getMessage().contains("!"));
            assertTrue(e.getMessage().contains("boolean"));
        }

        try {
            operator.eval("hello");
            fail("Expected TypeMismatchException");
        } catch (TypeMismatchException e) {
            assertTrue(e.getMessage().contains("!"));
            assertTrue(e.getMessage().contains("boolean"));
        }
    }

    @Test
    public void testNegateOperator() {
        NegateOperator operator = new NegateOperator();

        // Basic numeric negation
        assertEquals(-5, operator.eval(5));
        assertEquals(5, operator.eval(-5));
        assertEquals(0, operator.eval(0));

        // Test properties
        assertEquals("-", operator.getSymbol());
        assertEquals(OperatorType.UNARY, operator.getType());
        assertEquals(OperatorPrecedence.UNARY, operator.getPrecedence());
        assertTrue(operator.isUnary());
    }

    @Test
    public void testNegateOperator_TypePreservation() {
        NegateOperator operator = new NegateOperator();

        // Test that original numeric types are preserved
        Object intResult = operator.eval(5);
        assertTrue(intResult instanceof Integer);
        assertEquals(-5, intResult);

        Object longResult = operator.eval(5L);
        assertTrue(longResult instanceof Long);
        assertEquals(-5L, longResult);

        Object floatResult = operator.eval(5.5f);
        assertTrue(floatResult instanceof Float);
        assertEquals(-5.5f, floatResult);

        Object doubleResult = operator.eval(5.5);
        assertTrue(doubleResult instanceof Double);
        assertEquals(-5.5, doubleResult);

        Object shortResult = operator.eval((short) 10);
        assertTrue(shortResult instanceof Short);
        assertEquals((short) -10, shortResult);

        Object byteResult = operator.eval((byte) 100);
        assertTrue(byteResult instanceof Byte);
        assertEquals((byte) -100, byteResult);
    }

    @Test
    public void testNegateOperator_EdgeCases() {
        NegateOperator operator = new NegateOperator();

        // Test with zero
        assertEquals(0, operator.eval(0));
        assertEquals(0L, operator.eval(0L));
        assertEquals(-0.0f, operator.eval(0.0f));
        assertEquals(-0.0, operator.eval(0.0));

        // Test double negation
        assertEquals(5, operator.eval(-5));
        assertEquals(-10.5, operator.eval(10.5));

        // Test Integer.MIN_VALUE overflow handling
        Object result = operator.eval(Integer.MIN_VALUE);
        // Should promote to Long to handle overflow
        assertTrue(result instanceof Long);
        assertEquals((long) Integer.MAX_VALUE + 1L, result);

        // Test Long.MIN_VALUE (will overflow but that's expected behavior)
        Object longMinResult = operator.eval(Long.MIN_VALUE);
        assertTrue(longMinResult instanceof Long);
        assertEquals(Long.MIN_VALUE, longMinResult); // Overflow behavior
    }

    @Test
    public void testNegateOperator_TypeValidation() {
        NegateOperator operator = new NegateOperator();

        // Should only accept numeric types
        assertThrows(TypeMismatchException.class, () -> operator.eval("5"));
        assertThrows(TypeMismatchException.class, () -> operator.eval(true));
        assertThrows(TypeMismatchException.class, () -> operator.eval(false));
        assertThrows(TypeMismatchException.class, () -> operator.eval(null));
    }

    @Test
    public void testNegateOperator_ErrorMessages() {
        NegateOperator operator = new NegateOperator();

        // Test that error messages are informative
        try {
            operator.eval("5");
            fail("Expected TypeMismatchException");
        } catch (TypeMismatchException e) {
            assertTrue(e.getMessage().contains("-"));
            assertTrue(e.getMessage().contains("numeric"));
        }

        try {
            operator.eval(true);
            fail("Expected TypeMismatchException");
        } catch (TypeMismatchException e) {
            assertTrue(e.getMessage().contains("-"));
            assertTrue(e.getMessage().contains("numeric"));
        }
    }

    @Test
    public void testUnaryOperators_PrecedenceValidation() {
        NotOperator not = new NotOperator();
        NegateOperator negate = new NegateOperator();

        // Both should have the same high precedence
        assertEquals(OperatorPrecedence.UNARY, not.getPrecedence());
        assertEquals(OperatorPrecedence.UNARY, negate.getPrecedence());

        // Should have higher precedence than binary operators
        assertTrue(not.getPrecedence() > OperatorPrecedence.LOGICAL_AND);
        assertTrue(negate.getPrecedence() > OperatorPrecedence.MULTIPLICATION);
    }

    @Test
    public void testUnaryOperators_ToString() {
        NotOperator not = new NotOperator();
        NegateOperator negate = new NegateOperator();

        // Verify toString implementations
        assertNotNull(not.toString());
        assertNotNull(negate.toString());
        assertTrue(not.toString().contains("!"));
        assertTrue(negate.toString().contains("-"));
    }
}

// Note: If you want to enable truthiness conversion in the future,
// uncomment the truthiness logic in NotOperator, then add these additional test methods:

/*
@Test
public void testNotOperator_TruthinessConversion() {
    NotOperator operator = new NotOperator();

    // Test truthiness conversion if supported
    assertFalse((Boolean) operator.eval(1));      // !1 = false
    assertTrue((Boolean) operator.eval(0));       // !0 = true
    assertFalse((Boolean) operator.eval("hello")); // !"hello" = false
    assertTrue((Boolean) operator.eval(""));       // !"" = true
    assertTrue((Boolean) operator.eval(null));     // !null = true
}
*/