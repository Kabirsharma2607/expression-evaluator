package com.example.operators;

import com.example.operators.comparison.*;
import com.example.operators.logical.*;
import com.example.operators.exceptions.UnsupportedOperatorException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive tests for the OperatorFactory
 */
public class OperatorFactoryTest {

    @BeforeEach
    public void setUp() {
        // Ensure factory is in a known state before each test
        OperatorFactory.resetToDefaults();
    }

    @Test
    public void testDefaultOperatorsRegistration() {
        // Verify all default operators are registered
        Set<String> supportedOperators = OperatorFactory.getSupportedOperators();

        // Comparison operators
        assertTrue(supportedOperators.contains("<"));
        assertTrue(supportedOperators.contains("<="));
        assertTrue(supportedOperators.contains(">"));
        assertTrue(supportedOperators.contains(">="));
        assertTrue(supportedOperators.contains("=="));
        assertTrue(supportedOperators.contains("!="));

        // Logical operators
        assertTrue(supportedOperators.contains("&&"));
        assertTrue(supportedOperators.contains("||"));

        // Unary operators
        assertTrue(supportedOperators.contains("!"));
        assertTrue(supportedOperators.contains("-"));

        // Arithmetic operators (note: "-" appears in both binary and unary)
        assertTrue(supportedOperators.contains("+"));
        assertTrue(supportedOperators.contains("*"));
        assertTrue(supportedOperators.contains("/"));
        assertTrue(supportedOperators.contains("%"));

        // Should have exactly 14 unique symbols (13 binary + 2 unary, but "-" is shared)
        assertEquals(14, supportedOperators.size());
        // But total count should be 15 (13 binary + 2 unary)
        assertEquals(15, OperatorFactory.getOperatorCount());
    }

    @Test
    public void testGetOperator() {
        // Test successful retrieval
        OperatorEvaluator ltOperator = OperatorFactory.getBinaryOperator("<");
        assertNotNull(ltOperator);
        assertTrue(ltOperator instanceof LessThanOperator);
        assertEquals("<", ltOperator.getSymbol());

        OperatorEvaluator andOperator = OperatorFactory.getBinaryOperator("&&");
        assertNotNull(andOperator);
        assertTrue(andOperator instanceof AndOperator);
        assertEquals("&&", andOperator.getSymbol());
    }

    @Test
    public void testGetOperator_Unknown() {
        // Test unknown operator throws exception
        assertThrows(UnsupportedOperatorException.class, () -> {
            OperatorFactory.getOperator("@#$");
        });

        assertThrows(UnsupportedOperatorException.class, () -> {
            OperatorFactory.getOperator("unknown");
        });
    }

    @Test
    public void testRegisterOperator() {
        // Create a custom operator for testing
        OperatorEvaluator customOperator = new OperatorEvaluator() {
            @Override
            public Object eval(Object leftOperand, Object rightOperand) {
                return "custom";
            }

            @Override
            public int getPrecedence() {
                return 10;
            }

            @Override
            public OperatorType getType() {
                return OperatorType.ARITHMETIC;
            }

            @Override
            public String getSymbol() {
                return "**";
            }
        };

        // Register the custom operator
        OperatorFactory.registerOperator("**", customOperator);

        // Verify it was registered
        assertTrue(OperatorFactory.isRegistered("**"));
        assertEquals(16, OperatorFactory.getOperatorCount()); // 15 default + 1 custom

        // Verify it can be retrieved
        OperatorEvaluator retrieved = OperatorFactory.getBinaryOperator("**");
        assertSame(customOperator, retrieved);
        assertEquals("custom", retrieved.eval(null, null));
    }

    @Test
    public void testRegisterOperator_ReplaceExisting() {
        // Register a replacement for an existing operator
        OperatorEvaluator customLT = new LessThanOperator() {
            @Override
            public Object eval(Object leftOperand, Object rightOperand) {
                return "custom_lt";
            }
        };

        int originalCount = OperatorFactory.getOperatorCount();

        OperatorFactory.registerOperator("<", customLT);

        // Count should remain the same (replacement, not addition)
        assertEquals(originalCount, OperatorFactory.getOperatorCount());

        // Should retrieve the custom implementation
        OperatorEvaluator retrieved = OperatorFactory.getBinaryOperator("<");
        assertEquals("custom_lt", retrieved.eval(5, 10));
    }

    @Test
    public void testRegisterOperator_InvalidInputs() {
        OperatorEvaluator validOperator = new LessThanOperator();

        // Test null symbol
        assertThrows(IllegalArgumentException.class, () -> {
            OperatorFactory.registerOperator(null, validOperator);
        });

        // Test empty symbol
        assertThrows(IllegalArgumentException.class, () -> {
            OperatorFactory.registerOperator("", validOperator);
        });

        // Test whitespace-only symbol
        assertThrows(IllegalArgumentException.class, () -> {
            OperatorFactory.registerOperator("   ", validOperator);
        });

        // Test null operator
        assertThrows(IllegalArgumentException.class, () -> {
            OperatorFactory.registerOperator("<", null);
        });
    }

    @Test
    public void testIsRegistered() {
        assertTrue(OperatorFactory.isRegistered("<"));
        assertTrue(OperatorFactory.isRegistered(">="));
        assertTrue(OperatorFactory.isRegistered("&&"));

        assertFalse(OperatorFactory.isRegistered("unknown"));
        assertFalse(OperatorFactory.isRegistered("++"));
        assertFalse(OperatorFactory.isRegistered(null));
    }

    @Test
    public void testGetOperatorsByType() {
        Map<String, OperatorEvaluator> comparisonOps =
            OperatorFactory.getOperatorsByType(OperatorType.COMPARISON);

        assertEquals(6, comparisonOps.size());
        assertTrue(comparisonOps.containsKey("<"));
        assertTrue(comparisonOps.containsKey("<="));
        assertTrue(comparisonOps.containsKey(">"));
        assertTrue(comparisonOps.containsKey(">="));
        assertTrue(comparisonOps.containsKey("=="));
        assertTrue(comparisonOps.containsKey("!="));

        Map<String, OperatorEvaluator> logicalOps =
            OperatorFactory.getOperatorsByType(OperatorType.LOGICAL);

        assertEquals(2, logicalOps.size());
        assertTrue(logicalOps.containsKey("&&"));
        assertTrue(logicalOps.containsKey("||"));

        // Test arithmetic operators
        Map<String, OperatorEvaluator> arithmeticOps =
            OperatorFactory.getOperatorsByType(OperatorType.ARITHMETIC);
        assertEquals(5, arithmeticOps.size());
        assertTrue(arithmeticOps.containsKey("+"));
        assertTrue(arithmeticOps.containsKey("-"));
        assertTrue(arithmeticOps.containsKey("*"));
        assertTrue(arithmeticOps.containsKey("/"));
        assertTrue(arithmeticOps.containsKey("%"));
    }

    @Test
    public void testClearAllOperators() {
        // Verify operators exist initially
        assertTrue(OperatorFactory.getOperatorCount() > 0);
        assertTrue(OperatorFactory.isRegistered("<"));

        // Clear all operators
        OperatorFactory.clearAllOperators();

        // Verify all operators are gone
        assertEquals(0, OperatorFactory.getOperatorCount());
        assertFalse(OperatorFactory.isRegistered("<"));
        assertTrue(OperatorFactory.getSupportedOperators().isEmpty());

        // Verify attempting to get operators throws exception
        assertThrows(UnsupportedOperatorException.class, () -> {
            OperatorFactory.getOperator("<");
        });
    }

    @Test
    public void testResetToDefaults() {
        // Add a custom operator first
        OperatorFactory.registerOperator("custom", new LessThanOperator());
        assertEquals(16, OperatorFactory.getOperatorCount());

        // Reset to defaults
        OperatorFactory.resetToDefaults();

        // Verify only defaults remain
        assertEquals(15, OperatorFactory.getOperatorCount());
        assertTrue(OperatorFactory.isRegistered("<"));
        assertTrue(OperatorFactory.isRegistered("&&"));
        assertFalse(OperatorFactory.isRegistered("custom"));
    }

    @Test
    public void testSupportedOperators_Immutable() {
        Set<String> supportedOps = OperatorFactory.getSupportedOperators();

        // Should not be able to modify the returned set
        assertThrows(UnsupportedOperationException.class, () -> {
            supportedOps.add("new_op");
        });

        assertThrows(UnsupportedOperationException.class, () -> {
            supportedOps.remove("<");
        });
    }

    @Test
    public void testThreadSafety() {
        // This is a basic test for thread safety
        // Register operators from multiple threads simultaneously

        Thread[] threads = new Thread[10];
        for (int i = 0; i < threads.length; i++) {
            final int threadNum = i;
            threads[i] = new Thread(() -> {
                OperatorEvaluator customOp = new LessThanOperator() {
                    @Override
                    public Object eval(Object left, Object right) {
                        return "thread_" + threadNum;
                    }
                };

                OperatorFactory.registerOperator("custom_" + threadNum, customOp);
            });
        }

        // Start all threads
        for (Thread thread : threads) {
            thread.start();
        }

        // Wait for all threads to complete
        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                fail("Thread interrupted");
            }
        }

        // Verify all operators were registered
        for (int i = 0; i < threads.length; i++) {
            assertTrue(OperatorFactory.isRegistered("custom_" + i));
            OperatorEvaluator op = OperatorFactory.getBinaryOperator("custom_" + i);
            assertEquals("thread_" + i, op.eval(null, null));
        }
    }
}