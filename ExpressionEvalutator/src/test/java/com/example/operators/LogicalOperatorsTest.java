package com.example.operators;

import com.example.operators.logical.*;
import com.example.operators.exceptions.TypeMismatchException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive tests for all logical operators
 */
public class LogicalOperatorsTest {

    @Test
    public void testAndOperator() {
        AndOperator operator = new AndOperator();

        // Basic boolean operations - truth table
        assertTrue((Boolean) operator.eval(true, true));
        assertFalse((Boolean) operator.eval(true, false));
        assertFalse((Boolean) operator.eval(false, true));
        assertFalse((Boolean) operator.eval(false, false));

        // Test properties
        assertEquals("&&", operator.getSymbol());
        assertEquals(OperatorType.LOGICAL, operator.getType());
        assertEquals(OperatorPrecedence.LOGICAL_AND, operator.getPrecedence());
    }

    @Test
    public void testAndOperator_TypeValidation() {
        AndOperator operator = new AndOperator();

        // Should only accept boolean types (strict type checking)
        assertThrows(TypeMismatchException.class, () -> operator.eval(5, true));
        assertThrows(TypeMismatchException.class, () -> operator.eval(true, "hello"));
        assertThrows(TypeMismatchException.class, () -> operator.eval(1, 0));
        assertThrows(TypeMismatchException.class, () -> operator.eval(null, true));
        assertThrows(TypeMismatchException.class, () -> operator.eval(true, null));
    }

    @Test
    public void testOrOperator() {
        OrOperator operator = new OrOperator();

        // Basic boolean operations - truth table
        assertTrue((Boolean) operator.eval(true, true));
        assertTrue((Boolean) operator.eval(true, false));
        assertTrue((Boolean) operator.eval(false, true));
        assertFalse((Boolean) operator.eval(false, false));

        // Test properties
        assertEquals("||", operator.getSymbol());
        assertEquals(OperatorType.LOGICAL, operator.getType());
        assertEquals(OperatorPrecedence.LOGICAL_OR, operator.getPrecedence());
    }

    @Test
    public void testOrOperator_TypeValidation() {
        OrOperator operator = new OrOperator();

        // Should only accept boolean types (strict type checking)
        assertThrows(TypeMismatchException.class, () -> operator.eval(5, true));
        assertThrows(TypeMismatchException.class, () -> operator.eval(false, "world"));
        assertThrows(TypeMismatchException.class, () -> operator.eval("true", "false"));
        assertThrows(TypeMismatchException.class, () -> operator.eval(null, false));
        assertThrows(TypeMismatchException.class, () -> operator.eval(false, null));
    }

    @Test
    public void testLogicalOperators_PrecedenceValidation() {
        AndOperator and = new AndOperator();
        OrOperator or = new OrOperator();

        // AND should have higher precedence than OR
        assertTrue(and.getPrecedence() > or.getPrecedence());

        // Verify specific precedence values
        assertEquals(OperatorPrecedence.LOGICAL_AND, and.getPrecedence());
        assertEquals(OperatorPrecedence.LOGICAL_OR, or.getPrecedence());
    }

    @Test
    public void testLogicalOperators_ErrorMessages() {
        AndOperator and = new AndOperator();

        // Test that error messages are informative
        try {
            and.eval(5, true);
            fail("Expected TypeMismatchException");
        } catch (TypeMismatchException e) {
            assertTrue(e.getMessage().contains("&&"));
            assertTrue(e.getMessage().contains("boolean"));
        }

        try {
            and.eval(true, "hello");
            fail("Expected TypeMismatchException");
        } catch (TypeMismatchException e) {
            assertTrue(e.getMessage().contains("&&"));
            assertTrue(e.getMessage().contains("boolean"));
        }
    }

    @Test
    public void testLogicalOperators_ToString() {
        AndOperator and = new AndOperator();
        OrOperator or = new OrOperator();

        // Verify toString implementations
        assertNotNull(and.toString());
        assertNotNull(or.toString());
        assertTrue(and.toString().contains("&&"));
        assertTrue(or.toString().contains("||"));
    }
}

// Note: If you want to enable truthiness conversion in the future,
// uncomment the truthiness logic in AndOperator and OrOperator,
// then add these additional test methods:

/*
@Test
public void testAndOperator_TruthinessConversion() {
    AndOperator operator = new AndOperator();

    // Test truthiness conversion if supported
    assertTrue((Boolean) operator.eval(1, "hello"));    // 1 and "hello" = true
    assertFalse((Boolean) operator.eval(1, 0));         // 1 and 0 = false
    assertFalse((Boolean) operator.eval(0, "hello"));   // 0 and "hello" = false
    assertFalse((Boolean) operator.eval("", "hello"));  // "" and "hello" = false
    assertFalse((Boolean) operator.eval(null, true));   // null and true = false
}

@Test
public void testOrOperator_TruthinessConversion() {
    OrOperator operator = new OrOperator();

    // Test truthiness conversion if supported
    assertTrue((Boolean) operator.eval(1, 0));          // 1 or 0 = true
    assertTrue((Boolean) operator.eval(0, "hello"));    // 0 or "hello" = true
    assertTrue((Boolean) operator.eval("", "hello"));   // "" or "hello" = true
    assertFalse((Boolean) operator.eval(0, ""));        // 0 or "" = false
    assertFalse((Boolean) operator.eval(null, 0));      // null or 0 = false
}
*/