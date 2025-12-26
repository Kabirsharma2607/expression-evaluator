# Task 5: Testing & Validation

## Objective
Create comprehensive testing suite covering all operators, policy execution, integration scenarios, and edge cases to ensure system reliability and correctness.

## Testing Strategy
- **Unit Testing**: Individual operator testing
- **Integration Testing**: Full policy execution testing
- **Performance Testing**: Benchmarking and stress testing
- **Edge Case Testing**: Error handling and boundary conditions

## Subtask Breakdown

### Subtask 5.1: Unit Testing - Individual Operators
**Estimated Time**: 3 hours

#### 5.1.1: Comparison Operators Tests
```java
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
    }

    @Test
    public void testLessThanOperator_InvalidTypes() {
        LessThanOperator operator = new LessThanOperator();

        assertThrows(TypeMismatchException.class, () -> operator.eval("5", 10));
        assertThrows(TypeMismatchException.class, () -> operator.eval(5, "10"));
        assertThrows(TypeMismatchException.class, () -> operator.eval(null, 10));
        assertThrows(TypeMismatchException.class, () -> operator.eval(5, null));
    }

    @Test
    public void testEqualOperator_DifferentTypes() {
        EqualOperator operator = new EqualOperator();

        // Numeric equality with type coercion
        assertTrue((Boolean) operator.eval(5, 5.0));
        assertTrue((Boolean) operator.eval(5L, 5));

        // String equality
        assertTrue((Boolean) operator.eval("hello", "hello"));
        assertFalse((Boolean) operator.eval("hello", "world"));

        // Boolean equality
        assertTrue((Boolean) operator.eval(true, true));
        assertFalse((Boolean) operator.eval(true, false));

        // Cross-type inequality
        assertFalse((Boolean) operator.eval(5, "5"));
        assertFalse((Boolean) operator.eval(true, 1));
    }

    // Tests for <=, >, >=, != operators...
}
```

#### 5.1.2: Logical Operators Tests
```java
public class LogicalOperatorsTest {

    @Test
    public void testAndOperator() {
        AndOperator operator = new AndOperator();

        assertTrue((Boolean) operator.eval(true, true));
        assertFalse((Boolean) operator.eval(true, false));
        assertFalse((Boolean) operator.eval(false, true));
        assertFalse((Boolean) operator.eval(false, false));
    }

    @Test
    public void testAndOperator_TruthinessConversion() {
        AndOperator operator = new AndOperator();

        // If truthiness conversion is supported
        assertTrue((Boolean) operator.eval(1, "hello"));
        assertFalse((Boolean) operator.eval(1, 0));
        assertFalse((Boolean) operator.eval(0, "hello"));
    }

    @Test
    public void testOrOperator_ShortCircuit() {
        OrOperator operator = new OrOperator();

        assertTrue((Boolean) operator.eval(true, true));
        assertTrue((Boolean) operator.eval(true, false));
        assertTrue((Boolean) operator.eval(false, true));
        assertFalse((Boolean) operator.eval(false, false));
    }

    // Test for proper error handling with non-boolean types
    @Test
    public void testLogicalOperator_TypeValidation() {
        AndOperator operator = new AndOperator();

        assertThrows(TypeMismatchException.class, () -> operator.eval(5, true));
        assertThrows(TypeMismatchException.class, () -> operator.eval("hello", false));
    }
}
```

#### 5.1.3: Arithmetic Operators Tests
```java
public class ArithmeticOperatorsTest {

    @Test
    public void testAddOperator_NumericTypes() {
        AddOperator operator = new AddOperator();

        assertEquals(8, operator.eval(5, 3));
        assertEquals(8L, operator.eval(5L, 3L));
        assertEquals(8.5, operator.eval(5, 3.5));
        assertEquals(8.5, operator.eval(5.5, 3));
    }

    @Test
    public void testAddOperator_StringConcatenation() {
        AddOperator operator = new AddOperator();

        assertEquals("hello3", operator.eval("hello", 3));
        assertEquals("5world", operator.eval(5, "world"));
        assertEquals("helloworld", operator.eval("hello", "world"));
    }

    @Test
    public void testDivideOperator_ZeroDivision() {
        DivideOperator operator = new DivideOperator();

        ArithmeticException exception = assertThrows(ArithmeticException.class,
            () -> operator.eval(5, 0));
        assertEquals("Division by zero", exception.getMessage());

        assertThrows(ArithmeticException.class, () -> operator.eval(5, 0.0));
    }

    @Test
    public void testMultiplyOperator_OverflowHandling() {
        MultiplyOperator operator = new MultiplyOperator();

        // Test normal multiplication
        assertEquals(15, operator.eval(5, 3));

        // Test overflow scenarios
        Object result = operator.eval(Integer.MAX_VALUE, 2);
        assertTrue(result instanceof Long, "Should promote to Long on overflow");
    }

    @Test
    public void testModulusOperator_IntegerAndFloat() {
        ModulusOperator operator = new ModulusOperator();

        assertEquals(2L, operator.eval(8, 3));      // Integer modulus
        assertEquals(1.5, operator.eval(8.5, 3.5)); // Floating-point modulus

        assertThrows(ArithmeticException.class, () -> operator.eval(5, 0));
    }
}
```

#### 5.1.4: Unary Operators Tests
```java
public class UnaryOperatorsTest {

    @Test
    public void testNotOperator() {
        NotOperator operator = new NotOperator();

        assertFalse((Boolean) operator.eval(true));
        assertTrue((Boolean) operator.eval(false));
    }

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

    @Test
    public void testNegateOperator_TypePreservation() {
        NegateOperator operator = new NegateOperator();

        assertEquals(-5, operator.eval(5));
        assertTrue(operator.eval(5) instanceof Integer);

        assertEquals(-5L, operator.eval(5L));
        assertTrue(operator.eval(5L) instanceof Long);

        assertEquals(-5.5f, operator.eval(5.5f));
        assertTrue(operator.eval(5.5f) instanceof Float);

        assertEquals(-5.5, operator.eval(5.5));
        assertTrue(operator.eval(5.5) instanceof Double);
    }

    @Test
    public void testNegateOperator_EdgeCases() {
        NegateOperator operator = new NegateOperator();

        assertEquals(0, operator.eval(0));
        assertEquals(5, operator.eval(-5));

        // Test with MIN_VALUE (potential overflow)
        assertEquals((long) Integer.MAX_VALUE + 1, operator.eval(Integer.MIN_VALUE));
    }
}
```

#### Acceptance Criteria:
- ✅ 100% line coverage for all operator implementations
- ✅ All edge cases tested (null, overflow, type mismatches)
- ✅ Proper exception testing with specific error messages
- ✅ Type preservation and promotion testing

### Subtask 5.2: Integration Testing - Policy Execution
**Estimated Time**: 2 hours

#### 5.2.1: Policy Loading Tests
```java
public class PolicyLoadingTest {

    @Test
    public void testLoadValidPolicy() {
        PolicyLoader loader = new PolicyLoader();
        Policy policy = loader.loadPolicy("fc1Rules");

        assertNotNull(policy);
        assertEquals("fc1Rules", policy.getName());
        assertFalse(policy.getRules().isEmpty());
    }

    @Test
    public void testLoadInvalidPolicy_FileNotFound() {
        PolicyLoader loader = new PolicyLoader();

        assertThrows(PolicyNotFoundException.class,
            () -> loader.loadPolicy("nonExistentPolicy"));
    }

    @Test
    public void testValidatePolicy_CircularDependency() {
        Policy policy = createPolicyWithCircularDependency();
        PolicyLoader loader = new PolicyLoader();

        assertThrows(CircularDependencyException.class,
            () -> loader.validatePolicy(policy));
    }

    private Policy createPolicyWithCircularDependency() {
        // Create policy where rule1 depends on rule2, and rule2 depends on rule1
        Policy policy = new Policy();
        policy.setName("circularTest");

        Rule rule1 = new Rule("rule1", "rule2 && something > 10", Arrays.asList("rule2"));
        Rule rule2 = new Rule("rule2", "rule1 || something < 20", Arrays.asList("rule1"));

        policy.getRules().put("rule1", rule1);
        policy.getRules().put("rule2", rule2);

        return policy;
    }
}
```

#### 5.2.2: End-to-End Policy Execution Tests
```java
public class PolicyExecutionTest {

    @Test
    public void testSimplePolicyExecution() {
        Map<String, Object> featureMap = Map.of(
            "userAge", 25,
            "accountBalance", 5000,
            "transactionCount", 150
        );

        PolicyExecutor executor = new PolicyExecutor();
        PolicyResult result = executor.executePolicy(featureMap, "fc1Rules");

        assertTrue(result.isSuccess());
        assertTrue((Boolean) result.getRuleResults().get("rule1").getResult()); // userAge > 18
        assertTrue((Boolean) result.getRuleResults().get("rule2").getResult()); // accountBalance >= 1000
        assertTrue((Boolean) result.getRuleResults().get("rule3").getResult()); // rule1 && rule2
    }

    @Test
    public void testPolicyExecution_RuleDependencies() {
        Map<String, Object> featureMap = Map.of(
            "score1", 85,
            "score2", 92
        );

        // Policy with dependencies: rule3 = rule1 && rule2
        PolicyExecutor executor = new PolicyExecutor();
        PolicyResult result = executor.executePolicy(featureMap, "scorePolicyRules");

        // Verify execution order and results
        assertTrue(result.isSuccess());

        RuleResult rule1 = result.getRuleResults().get("rule1");
        RuleResult rule2 = result.getRuleResults().get("rule2");
        RuleResult rule3 = result.getRuleResults().get("rule3");

        assertTrue((Boolean) rule1.getResult()); // score1 > 80
        assertTrue((Boolean) rule2.getResult()); // score2 > 90
        assertTrue((Boolean) rule3.getResult()); // rule1 && rule2
    }

    @Test
    public void testPolicyExecution_PartialFailure() {
        Map<String, Object> featureMap = Map.of(
            "userAge", 16, // Fails age check
            "accountBalance", 5000
        );

        PolicyExecutor executor = new PolicyExecutor();
        PolicyResult result = executor.executePolicy(featureMap, "fc1Rules");

        assertTrue(result.isSuccess()); // Policy executes successfully

        assertFalse((Boolean) result.getRuleResults().get("rule1").getResult()); // userAge > 18 fails
        assertTrue((Boolean) result.getRuleResults().get("rule2").getResult());  // accountBalance >= 1000 passes
        assertFalse((Boolean) result.getRuleResults().get("rule3").getResult()); // rule1 && rule2 fails
    }

    @Test
    public void testPolicyExecution_MissingFeatureMapValue() {
        Map<String, Object> featureMap = Map.of(
            "userAge", 25
            // Missing accountBalance
        );

        PolicyExecutor executor = new PolicyExecutor();
        PolicyResult result = executor.executePolicy(featureMap, "fc1Rules");

        assertFalse(result.isSuccess());
        assertTrue(result.getErrors().stream()
            .anyMatch(error -> error.contains("accountBalance")));
    }
}
```

#### 5.2.3: Complex Expression Tests
```java
public class ComplexExpressionTest {

    @Test
    public void testNestedExpressions() {
        Map<String, Object> featureMap = Map.of(
            "age", 25,
            "income", 75000,
            "score", 720
        );

        String expression = "(age >= 21 && income > 50000) || score >= 750";
        ExpressionEvaluator evaluator = new ExpressionEvaluator();

        Boolean result = (Boolean) evaluator.evaluate(expression, featureMap);
        assertTrue(result); // First part is true: (25 >= 21 && 75000 > 50000)
    }

    @Test
    public void testOperatorPrecedence() {
        Map<String, Object> featureMap = Map.of(
            "a", 2,
            "b", 3,
            "c", 4
        );

        // Test: 2 + 3 * 4 should be 14, not 20
        String expression = "a + b * c";
        ExpressionEvaluator evaluator = new ExpressionEvaluator();

        Number result = (Number) evaluator.evaluate(expression, featureMap);
        assertEquals(14, result.intValue());
    }

    @Test
    public void testUnaryOperatorPrecedence() {
        Map<String, Object> featureMap = Map.of(
            "flag", false,
            "value", 5
        );

        // Test: !flag && value > 0 should be true
        String expression = "!flag && value > 0";
        ExpressionEvaluator evaluator = new ExpressionEvaluator();

        Boolean result = (Boolean) evaluator.evaluate(expression, featureMap);
        assertTrue(result);
    }
}
```

#### Acceptance Criteria:
- ✅ End-to-end policy execution working correctly
- ✅ Rule dependency resolution functioning
- ✅ Error handling for missing feature map values
- ✅ Complex expression parsing and evaluation
- ✅ Proper operator precedence in complex expressions

### Subtask 5.3: Performance Testing
**Estimated Time**: 2 hours

#### 5.3.1: Operator Performance Tests
```java
public class PerformanceTest {

    private static final int ITERATIONS = 100_000;

    @Test
    public void testArithmeticOperatorPerformance() {
        AddOperator add = new AddOperator();
        MultiplyOperator multiply = new MultiplyOperator();

        // Warm up JVM
        for (int i = 0; i < 1000; i++) {
            add.eval(i, i + 1);
            multiply.eval(i, 2);
        }

        long startTime = System.nanoTime();
        for (int i = 0; i < ITERATIONS; i++) {
            add.eval(i, i + 1);
        }
        long addDuration = System.nanoTime() - startTime;

        startTime = System.nanoTime();
        for (int i = 0; i < ITERATIONS; i++) {
            multiply.eval(i, 2);
        }
        long multiplyDuration = System.nanoTime() - startTime;

        // Should complete 100k operations in under 50ms
        assertTrue(addDuration < 50_000_000, "Addition performance: " + addDuration + "ns");
        assertTrue(multiplyDuration < 50_000_000, "Multiplication performance: " + multiplyDuration + "ns");

        System.out.printf("Addition: %.2f ops/second%n", ITERATIONS * 1e9 / addDuration);
        System.out.printf("Multiplication: %.2f ops/second%n", ITERATIONS * 1e9 / multiplyDuration);
    }

    @Test
    public void testPolicyExecutionPerformance() {
        Map<String, Object> featureMap = Map.of(
            "userAge", 25,
            "accountBalance", 5000,
            "transactionCount", 150
        );

        PolicyExecutor executor = new PolicyExecutor();

        // Warm up
        for (int i = 0; i < 100; i++) {
            executor.executePolicy(featureMap, "fc1Rules");
        }

        long startTime = System.nanoTime();
        for (int i = 0; i < 1000; i++) {
            executor.executePolicy(featureMap, "fc1Rules");
        }
        long duration = System.nanoTime() - startTime;

        // Should complete 1000 policy executions in under 100ms
        assertTrue(duration < 100_000_000, "Policy execution performance: " + duration + "ns");

        System.out.printf("Policy execution: %.2f executions/second%n", 1000 * 1e9 / duration);
    }

    @Test
    public void testMemoryUsage() {
        Runtime runtime = Runtime.getRuntime();
        long initialMemory = runtime.totalMemory() - runtime.freeMemory();

        Map<String, Object> featureMap = Map.of(
            "userAge", 25,
            "accountBalance", 5000
        );

        PolicyExecutor executor = new PolicyExecutor();

        // Execute many policies
        for (int i = 0; i < 10000; i++) {
            executor.executePolicy(featureMap, "fc1Rules");
        }

        runtime.gc(); // Suggest garbage collection
        Thread.sleep(100); // Wait for GC

        long finalMemory = runtime.totalMemory() - runtime.freeMemory();
        long memoryIncrease = finalMemory - initialMemory;

        // Memory increase should be reasonable (< 50MB for 10k executions)
        assertTrue(memoryIncrease < 50 * 1024 * 1024,
            "Memory increase: " + memoryIncrease + " bytes");
    }
}
```

#### 5.3.2: Stress Testing
```java
public class StressTest {

    @Test
    public void testConcurrentPolicyExecution() throws InterruptedException {
        int numThreads = 10;
        int executionsPerThread = 1000;
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        CountDownLatch latch = new CountDownLatch(numThreads);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger errorCount = new AtomicInteger(0);

        Map<String, Object> featureMap = Map.of(
            "userAge", 25,
            "accountBalance", 5000
        );

        PolicyExecutor policyExecutor = new PolicyExecutor();

        for (int t = 0; t < numThreads; t++) {
            executor.submit(() -> {
                try {
                    for (int i = 0; i < executionsPerThread; i++) {
                        PolicyResult result = policyExecutor.executePolicy(featureMap, "fc1Rules");
                        if (result.isSuccess()) {
                            successCount.incrementAndGet();
                        } else {
                            errorCount.incrementAndGet();
                        }
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(30, TimeUnit.SECONDS);

        assertEquals(numThreads * executionsPerThread, successCount.get());
        assertEquals(0, errorCount.get());

        executor.shutdown();
    }

    @Test
    public void testLargeFeatureMapHandling() {
        // Create a large feature map with 1000 entries
        Map<String, Object> largeFeatureMap = new HashMap<>();
        for (int i = 0; i < 1000; i++) {
            largeFeatureMap.put("feature" + i, i);
        }
        largeFeatureMap.put("targetValue", 500);

        String expression = "targetValue > 400 && feature999 < 1000";
        ExpressionEvaluator evaluator = new ExpressionEvaluator();

        long startTime = System.nanoTime();
        Boolean result = (Boolean) evaluator.evaluate(expression, largeFeatureMap);
        long duration = System.nanoTime() - startTime;

        assertTrue(result);
        // Should handle large feature maps quickly (< 10ms)
        assertTrue(duration < 10_000_000, "Large feature map handling: " + duration + "ns");
    }
}
```

#### Acceptance Criteria:
- ✅ Individual operators: >20,000 operations/second
- ✅ Policy execution: >1,000 executions/second
- ✅ Memory usage stable under repeated execution
- ✅ Thread-safe concurrent execution
- ✅ Efficient handling of large feature maps

### Subtask 5.4: Edge Case and Error Handling Tests
**Estimated Time**: 2 hours

#### 5.4.1: Error Scenario Tests
```java
public class ErrorHandlingTest {

    @Test
    public void testMalformedExpressions() {
        ExpressionEvaluator evaluator = new ExpressionEvaluator();
        Map<String, Object> featureMap = Map.of("x", 5);

        // Missing operand
        assertThrows(SyntaxErrorException.class, () -> evaluator.evaluate("x >", featureMap));

        // Unmatched parentheses
        assertThrows(SyntaxErrorException.class, () -> evaluator.evaluate("(x > 5", featureMap));

        // Invalid operator sequence
        assertThrows(SyntaxErrorException.class, () -> evaluator.evaluate("x > > 5", featureMap));

        // Empty expression
        assertThrows(SyntaxErrorException.class, () -> evaluator.evaluate("", featureMap));
    }

    @Test
    public void testTypeMismatchErrors() {
        ExpressionEvaluator evaluator = new ExpressionEvaluator();
        Map<String, Object> featureMap = Map.of(
            "stringValue", "hello",
            "numberValue", 42
        );

        // Arithmetic on strings (should fail)
        TypeMismatchException exception = assertThrows(TypeMismatchException.class,
            () -> evaluator.evaluate("stringValue * 2", featureMap));

        assertTrue(exception.getMessage().contains("* operator requires numeric operands"));

        // Logical operations on numbers (should fail if strict type checking)
        assertThrows(TypeMismatchException.class,
            () -> evaluator.evaluate("numberValue && true", featureMap));
    }

    @Test
    public void testNullValueHandling() {
        ExpressionEvaluator evaluator = new ExpressionEvaluator();
        Map<String, Object> featureMap = new HashMap<>();
        featureMap.put("nullValue", null);
        featureMap.put("numberValue", 42);

        // Null in comparison
        assertThrows(NullPointerException.class,
            () -> evaluator.evaluate("nullValue > 5", featureMap));

        // Null in arithmetic
        assertThrows(NullPointerException.class,
            () -> evaluator.evaluate("nullValue + 5", featureMap));
    }

    @Test
    public void testDivisionByZeroScenarios() {
        ExpressionEvaluator evaluator = new ExpressionEvaluator();
        Map<String, Object> featureMap = Map.of(
            "zero", 0,
            "number", 10
        );

        ArithmeticException exception = assertThrows(ArithmeticException.class,
            () -> evaluator.evaluate("number / zero", featureMap));
        assertEquals("Division by zero", exception.getMessage());

        // Modulus by zero
        assertThrows(ArithmeticException.class,
            () -> evaluator.evaluate("number % zero", featureMap));
    }
}
```

#### 5.4.2: Boundary Value Tests
```java
public class BoundaryValueTest {

    @Test
    public void testNumericBoundaries() {
        ExpressionEvaluator evaluator = new ExpressionEvaluator();

        // Integer boundaries
        Map<String, Object> intBoundaries = Map.of(
            "maxInt", Integer.MAX_VALUE,
            "minInt", Integer.MIN_VALUE
        );

        // Should handle max/min values correctly
        assertTrue((Boolean) evaluator.evaluate("maxInt > 0", intBoundaries));
        assertTrue((Boolean) evaluator.evaluate("minInt < 0", intBoundaries));

        // Overflow scenarios
        Object result = evaluator.evaluate("maxInt + 1", intBoundaries);
        assertTrue(result instanceof Long, "Should promote to Long on overflow");

        // Double boundaries
        Map<String, Object> doubleBoundaries = Map.of(
            "maxDouble", Double.MAX_VALUE,
            "minDouble", Double.MIN_VALUE,
            "positiveInf", Double.POSITIVE_INFINITY,
            "negativeInf", Double.NEGATIVE_INFINITY,
            "nan", Double.NaN
        );

        assertTrue((Boolean) evaluator.evaluate("maxDouble > 0", doubleBoundaries));
        assertTrue((Boolean) evaluator.evaluate("minDouble > 0", doubleBoundaries));

        // Special float values
        assertFalse((Boolean) evaluator.evaluate("nan == nan", doubleBoundaries));
        assertTrue((Boolean) evaluator.evaluate("positiveInf > maxDouble", doubleBoundaries));
    }

    @Test
    public void testStringBoundaries() {
        ExpressionEvaluator evaluator = new ExpressionEvaluator();

        Map<String, Object> stringTests = Map.of(
            "emptyString", "",
            "longString", "a".repeat(10000),
            "unicodeString", "Hello 世界 🌍"
        );

        // String concatenation with boundaries
        Object result = evaluator.evaluate("emptyString + longString", stringTests);
        assertEquals("a".repeat(10000), result);

        // Unicode handling
        result = evaluator.evaluate("unicodeString + emptyString", stringTests);
        assertEquals("Hello 世界 🌍", result);
    }
}
```

#### Acceptance Criteria:
- ✅ All error scenarios properly handled with clear messages
- ✅ Boundary values processed correctly
- ✅ Graceful degradation for edge cases
- ✅ No crashes or undefined behavior
- ✅ Consistent error reporting format

## Test Organization and Reporting

### Test Suite Structure:
```
src/test/java/
├── operators/
│   ├── comparison/
│   ├── logical/
│   ├── arithmetic/
│   └── unary/
├── policy/
├── integration/
├── performance/
└── edgecases/
```

### Test Reporting Requirements:
- Code coverage reports (target: >95%)
- Performance benchmarks
- Test execution time tracking
- Error rate monitoring

### Continuous Integration:
- Automated test execution on every commit
- Performance regression detection
- Code quality gates (coverage, complexity)

## Success Metrics
- ✅ 100% operator unit test coverage
- ✅ All integration scenarios tested
- ✅ Performance targets met across all test categories
- ✅ Zero critical bugs in error handling
- ✅ Comprehensive edge case coverage
- ✅ Automated test suite execution < 2 minutes
- ✅ Clear test documentation and examples