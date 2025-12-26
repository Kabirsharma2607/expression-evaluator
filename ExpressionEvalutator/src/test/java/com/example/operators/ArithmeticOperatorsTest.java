package com.example.operators;

import com.example.operators.arithmetic.*;
import com.example.operators.exceptions.TypeMismatchException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive tests for all arithmetic operators
 */
public class ArithmeticOperatorsTest {

    // ==================== Addition Operator Tests ====================

    @Test
    public void testAddOperator_IntegerTypes() {
        AddOperator add = new AddOperator();
        assertEquals(8, add.eval(5, 3));
        assertEquals(0, add.eval(-5, 5));
        assertEquals(-8, add.eval(-5, -3));
    }

    @Test
    public void testAddOperator_TypePromotion() {
        AddOperator add = new AddOperator();

        // int + double = double
        Object result = add.eval(5, 3.5);
        assertEquals(8.5, result);
        assertTrue(result instanceof Double);

        // long + int = long
        Object longResult = add.eval(5L, 3);
        assertEquals(8L, longResult);
        assertTrue(longResult instanceof Long);
    }

    @Test
    public void testAddOperator_StringConcatenation() {
        AddOperator add = new AddOperator();
        assertEquals("hello5", add.eval("hello", 5));
        assertEquals("5world", add.eval(5, "world"));
        assertEquals("helloworld", add.eval("hello", "world"));
        assertEquals("5.5test", add.eval(5.5, "test"));
    }

    @Test
    public void testAddOperator_OverflowHandling() {
        AddOperator add = new AddOperator();

        // Integer overflow should promote to long
        Object result = add.eval(Integer.MAX_VALUE, 1);
        assertTrue(result instanceof Long);
        assertEquals((long) Integer.MAX_VALUE + 1L, result);

        // Long overflow should throw exception
        assertThrows(ArithmeticException.class, () ->
            add.eval(Long.MAX_VALUE, 1L));
    }

    @Test
    public void testAddOperator_NullHandling() {
        AddOperator add = new AddOperator();

        assertThrows(TypeMismatchException.class, () -> add.eval(null, 5));
        assertThrows(TypeMismatchException.class, () -> add.eval(5, null));
    }

    @Test
    public void testAddOperator_InvalidTypes() {
        AddOperator add = new AddOperator();

        assertThrows(TypeMismatchException.class, () -> add.eval(true, 5));
        assertThrows(TypeMismatchException.class, () -> add.eval(5, true));
    }

    @Test
    public void testAddOperator_Properties() {
        AddOperator add = new AddOperator();
        assertEquals("+", add.getSymbol());
        assertEquals(OperatorType.ARITHMETIC, add.getType());
        assertEquals(OperatorPrecedence.ADDITION, add.getPrecedence());
        assertFalse(add.isUnary());
    }

    // ==================== Subtraction Operator Tests ====================

    @Test
    public void testSubtractOperator_BasicOperations() {
        SubtractOperator subtract = new SubtractOperator();
        assertEquals(2, subtract.eval(5, 3));
        assertEquals(-10, subtract.eval(-5, 5));
        assertEquals(-2, subtract.eval(-5, -3));
        assertEquals(0, subtract.eval(5, 5));
    }

    @Test
    public void testSubtractOperator_TypePromotion() {
        SubtractOperator subtract = new SubtractOperator();

        // int - double = double
        Object result = subtract.eval(5, 2.5);
        assertEquals(2.5, result);
        assertTrue(result instanceof Double);

        // long - int = long
        Object longResult = subtract.eval(10L, 3);
        assertEquals(7L, longResult);
        assertTrue(longResult instanceof Long);
    }

    @Test
    public void testSubtractOperator_OverflowHandling() {
        SubtractOperator subtract = new SubtractOperator();

        // Integer overflow should promote to long
        Object result = subtract.eval(Integer.MIN_VALUE, 1);
        assertTrue(result instanceof Long);
        assertEquals((long) Integer.MIN_VALUE - 1L, result);
    }

    @Test
    public void testSubtractOperator_TypeValidation() {
        SubtractOperator subtract = new SubtractOperator();

        assertThrows(TypeMismatchException.class, () -> subtract.eval("hello", 5));
        assertThrows(TypeMismatchException.class, () -> subtract.eval(5, "world"));
        assertThrows(TypeMismatchException.class, () -> subtract.eval(null, 5));
    }

    // ==================== Multiplication Operator Tests ====================

    @Test
    public void testMultiplyOperator_BasicOperations() {
        MultiplyOperator multiply = new MultiplyOperator();
        assertEquals(15, multiply.eval(5, 3));
        assertEquals(-25, multiply.eval(-5, 5));
        assertEquals(15, multiply.eval(-5, -3));
        assertEquals(0, multiply.eval(5, 0));
    }

    @Test
    public void testMultiplyOperator_TypePromotion() {
        MultiplyOperator multiply = new MultiplyOperator();

        // int * double = double
        Object result = multiply.eval(5, 2.5);
        assertEquals(12.5, result);
        assertTrue(result instanceof Double);

        // long * int = long
        Object longResult = multiply.eval(10L, 3);
        assertEquals(30L, longResult);
        assertTrue(longResult instanceof Long);
    }

    @Test
    public void testMultiplyOperator_OverflowHandling() {
        MultiplyOperator multiply = new MultiplyOperator();

        // Integer overflow should promote to long
        Object result = multiply.eval(Integer.MAX_VALUE, 2);
        assertTrue(result instanceof Long);
        assertEquals((long) Integer.MAX_VALUE * 2L, result);

        // Large long multiplication should throw exception
        assertThrows(ArithmeticException.class, () ->
            multiply.eval(Long.MAX_VALUE / 2, 3L));
    }

    @Test
    public void testMultiplyOperator_EdgeCases() {
        MultiplyOperator multiply = new MultiplyOperator();

        assertEquals(0, multiply.eval(0, 100));
        assertEquals(0, multiply.eval(100, 0));
        assertEquals(1, multiply.eval(1, 1));
        assertEquals(-5, multiply.eval(-1, 5));
    }

    // ==================== Division Operator Tests ====================

    @Test
    public void testDivideOperator_BasicOperations() {
        DivideOperator divide = new DivideOperator();
        assertEquals(2.5, divide.eval(5, 2));
        assertEquals(0.5, divide.eval(1, 2));
        assertEquals(-2.5, divide.eval(-5, 2));
        assertEquals(2.5, divide.eval(-5, -2));
    }

    @Test
    public void testDivideOperator_AlwaysReturnsDouble() {
        DivideOperator divide = new DivideOperator();

        Object result = divide.eval(6, 2);
        assertEquals(3.0, result);
        assertTrue(result instanceof Double);

        Object longResult = divide.eval(10L, 5L);
        assertEquals(2.0, longResult);
        assertTrue(longResult instanceof Double);
    }

    @Test
    public void testDivideOperator_DivisionByZero() {
        DivideOperator divide = new DivideOperator();

        assertThrows(ArithmeticException.class, () -> divide.eval(5, 0));
        assertThrows(ArithmeticException.class, () -> divide.eval(5, 0.0));
        assertThrows(ArithmeticException.class, () -> divide.eval(5.0, 0));
    }

    @Test
    public void testDivideOperator_SpecialValues() {
        DivideOperator divide = new DivideOperator();

        assertEquals(0.0, divide.eval(0, 5));
        assertEquals(1.0, divide.eval(5, 5));
        assertEquals(-1.0, divide.eval(-5, 5));
    }

    @Test
    public void testDivideOperator_TypeValidation() {
        DivideOperator divide = new DivideOperator();

        assertThrows(TypeMismatchException.class, () -> divide.eval("hello", 5));
        assertThrows(TypeMismatchException.class, () -> divide.eval(5, "world"));
    }

    // ==================== Modulus Operator Tests ====================

    @Test
    public void testModulusOperator_BasicOperations() {
        ModulusOperator modulus = new ModulusOperator();
        assertEquals(1, modulus.eval(5, 2));
        assertEquals(0, modulus.eval(6, 2));
        assertEquals(2, modulus.eval(8, 3));
        assertEquals(0, modulus.eval(9, 3));
    }

    @Test
    public void testModulusOperator_NegativeNumbers() {
        ModulusOperator modulus = new ModulusOperator();
        assertEquals(-1, modulus.eval(-5, 2));
        assertEquals(1, modulus.eval(5, -2));
        assertEquals(-1, modulus.eval(-5, -2));
    }

    @Test
    public void testModulusOperator_TypePreservation() {
        ModulusOperator modulus = new ModulusOperator();

        // Integer types should return integers
        Object intResult = modulus.eval(5, 2);
        assertTrue(intResult instanceof Integer);
        assertEquals(1, intResult);

        // Long types should return longs
        Object longResult = modulus.eval(5L, 2L);
        assertTrue(longResult instanceof Long);
        assertEquals(1L, longResult);

        // Floating-point should return double
        Object doubleResult = modulus.eval(5.5, 2.0);
        assertTrue(doubleResult instanceof Double);
        assertEquals(1.5, doubleResult);
    }

    @Test
    public void testModulusOperator_FloatingPoint() {
        ModulusOperator modulus = new ModulusOperator();
        assertEquals(1.5, modulus.eval(5.5, 2.0));
        assertEquals(0.5, (Double) modulus.eval(2.5, 1.0), 0.001);
    }

    @Test
    public void testModulusOperator_ModulusByZero() {
        ModulusOperator modulus = new ModulusOperator();

        assertThrows(ArithmeticException.class, () -> modulus.eval(5, 0));
        assertThrows(ArithmeticException.class, () -> modulus.eval(5, 0.0));
        assertThrows(ArithmeticException.class, () -> modulus.eval(5.0, 0));
    }

    @Test
    public void testModulusOperator_TypeValidation() {
        ModulusOperator modulus = new ModulusOperator();

        assertThrows(TypeMismatchException.class, () -> modulus.eval("hello", 5));
        assertThrows(TypeMismatchException.class, () -> modulus.eval(5, "world"));
    }

    // ==================== Operator Properties Tests ====================

    @Test
    public void testArithmeticOperators_Properties() {
        // Test precedence
        MultiplyOperator multiply = new MultiplyOperator();
        DivideOperator divide = new DivideOperator();
        ModulusOperator modulus = new ModulusOperator();
        AddOperator add = new AddOperator();
        SubtractOperator subtract = new SubtractOperator();

        // Multiplication, division, and modulus should have same precedence
        assertEquals(OperatorPrecedence.MULTIPLICATION, multiply.getPrecedence());
        assertEquals(OperatorPrecedence.MULTIPLICATION, divide.getPrecedence());
        assertEquals(OperatorPrecedence.MULTIPLICATION, modulus.getPrecedence());

        // Addition and subtraction should have same precedence
        assertEquals(OperatorPrecedence.ADDITION, add.getPrecedence());
        assertEquals(OperatorPrecedence.ADDITION, subtract.getPrecedence());

        // Multiplication should have higher precedence than addition
        assertTrue(multiply.getPrecedence() > add.getPrecedence());

        // All should be arithmetic type
        assertEquals(OperatorType.ARITHMETIC, multiply.getType());
        assertEquals(OperatorType.ARITHMETIC, divide.getType());
        assertEquals(OperatorType.ARITHMETIC, modulus.getType());
        assertEquals(OperatorType.ARITHMETIC, add.getType());
        assertEquals(OperatorType.ARITHMETIC, subtract.getType());

        // All should be binary (not unary)
        assertFalse(multiply.isUnary());
        assertFalse(divide.isUnary());
        assertFalse(modulus.isUnary());
        assertFalse(add.isUnary());
        assertFalse(subtract.isUnary());
    }

    @Test
    public void testArithmeticOperators_ToString() {
        AddOperator add = new AddOperator();
        SubtractOperator subtract = new SubtractOperator();
        MultiplyOperator multiply = new MultiplyOperator();
        DivideOperator divide = new DivideOperator();
        ModulusOperator modulus = new ModulusOperator();

        assertNotNull(add.toString());
        assertNotNull(subtract.toString());
        assertNotNull(multiply.toString());
        assertNotNull(divide.toString());
        assertNotNull(modulus.toString());

        assertTrue(add.toString().contains("+"));
        assertTrue(subtract.toString().contains("-"));
        assertTrue(multiply.toString().contains("*"));
        assertTrue(divide.toString().contains("/"));
        assertTrue(modulus.toString().contains("%"));
    }

    // ==================== Integration Tests ====================

    @Test
    public void testArithmeticOperators_ComplexExpressions() {
        // Test that operators work correctly together
        AddOperator add = new AddOperator();
        MultiplyOperator multiply = new MultiplyOperator();

        // (2 + 3) * 4 = 20
        Object addResult = add.eval(2, 3);
        Object finalResult = multiply.eval(addResult, 4);
        assertEquals(20, finalResult);

        // 2 + (3 * 4) = 14
        Object multiplyResult = multiply.eval(3, 4);
        Object finalResult2 = add.eval(2, multiplyResult);
        assertEquals(14, finalResult2);
    }

    @Test
    public void testArithmeticOperators_TypeConsistency() {
        // Ensure consistent type handling across operators
        AddOperator add = new AddOperator();
        SubtractOperator subtract = new SubtractOperator();
        MultiplyOperator multiply = new MultiplyOperator();

        // All operations with same types should return same type
        Object addResult = add.eval(5.0, 3.0);
        Object subtractResult = subtract.eval(5.0, 3.0);
        Object multiplyResult = multiply.eval(5.0, 3.0);

        assertTrue(addResult instanceof Double);
        assertTrue(subtractResult instanceof Double);
        assertTrue(multiplyResult instanceof Double);
    }
}