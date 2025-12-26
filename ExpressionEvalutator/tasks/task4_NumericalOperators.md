# Task 4: Numerical Operators Implementation

## Objective
Implement all mathematical binary operators (addition, subtraction, multiplication, division, modulus) with proper type handling and error management.

## Operator List
- **Addition**: `+`
- **Subtraction**: `-` (binary)
- **Multiplication**: `*`
- **Division**: `/`
- **Modulus**: `%`

## Subtask Breakdown

### Subtask 4.1: Addition Operator Implementation
**Estimated Time**: 1 hour

#### Implementation:
```java
public class AddOperator implements BinaryOperatorEvaluator {

    @Override
    public Object eval(Object leftOperand, Object rightOperand) {
        // Handle null operands
        if (leftOperand == null || rightOperand == null) {
            throw new TypeMismatchException("+ operator cannot handle null operands");
        }

        // String concatenation support
        if (leftOperand instanceof String || rightOperand instanceof String) {
            return leftOperand.toString() + rightOperand.toString();
        }

        // Numeric addition
        if (leftOperand instanceof Number && rightOperand instanceof Number) {
            return performNumericAddition((Number) leftOperand, (Number) rightOperand);
        }

        throw new TypeMismatchException(
            String.format("+ operator requires numeric or string operands, got: %s and %s",
                leftOperand.getClass().getSimpleName(),
                rightOperand.getClass().getSimpleName())
        );
    }

    private Number performNumericAddition(Number left, Number right) {
        // Determine result type based on operand types
        if (isFloatingPoint(left) || isFloatingPoint(right)) {
            return left.doubleValue() + right.doubleValue();
        }

        if (left instanceof Long || right instanceof Long) {
            return left.longValue() + right.longValue();
        }

        return left.intValue() + right.intValue();
    }

    private boolean isFloatingPoint(Number number) {
        return number instanceof Float || number instanceof Double;
    }

    @Override
    public int getPrecedence() {
        return OperatorPrecedence.ADDITION;
    }

    @Override
    public OperatorType getType() {
        return OperatorType.ARITHMETIC;
    }

    @Override
    public String getSymbol() {
        return "+";
    }
}
```

#### Features:
- **String Concatenation**: Supports mixed string/number operations
- **Type Promotion**: Intelligent numeric type promotion
- **Overflow Protection**: Consider BigInteger for large numbers

#### Acceptance Criteria:
- ✅ Handles all numeric type combinations correctly
- ✅ String concatenation works as expected
- ✅ Proper type promotion (int + double = double)
- ✅ Clear error messages for invalid types

### Subtask 4.2: Subtraction Operator Implementation
**Estimated Time**: 0.5 hours

#### Implementation:
```java
public class SubtractOperator implements BinaryOperatorEvaluator {

    @Override
    public Object eval(Object leftOperand, Object rightOperand) {
        if (!(leftOperand instanceof Number) || !(rightOperand instanceof Number)) {
            throw new TypeMismatchException(
                String.format("- operator requires numeric operands, got: %s and %s",
                    leftOperand != null ? leftOperand.getClass().getSimpleName() : "null",
                    rightOperand != null ? rightOperand.getClass().getSimpleName() : "null")
            );
        }

        Number left = (Number) leftOperand;
        Number right = (Number) rightOperand;

        if (isFloatingPoint(left) || isFloatingPoint(right)) {
            return left.doubleValue() - right.doubleValue();
        }

        if (left instanceof Long || right instanceof Long) {
            return left.longValue() - right.longValue();
        }

        return left.intValue() - right.intValue();
    }

    private boolean isFloatingPoint(Number number) {
        return number instanceof Float || number instanceof Double;
    }

    @Override
    public int getPrecedence() {
        return OperatorPrecedence.ADDITION; // Same as addition
    }

    @Override
    public OperatorType getType() {
        return OperatorType.ARITHMETIC;
    }

    @Override
    public String getSymbol() {
        return "-";
    }
}
```

#### Acceptance Criteria:
- ✅ Handles all numeric type combinations
- ✅ Proper type promotion rules
- ✅ Strict numeric-only validation
- ✅ Consistent with addition operator patterns

### Subtask 4.3: Multiplication Operator Implementation
**Estimated Time**: 0.5 hours

#### Implementation:
```java
public class MultiplyOperator implements BinaryOperatorEvaluator {

    @Override
    public Object eval(Object leftOperand, Object rightOperand) {
        if (!(leftOperand instanceof Number) || !(rightOperand instanceof Number)) {
            throw new TypeMismatchException(
                String.format("* operator requires numeric operands, got: %s and %s",
                    leftOperand != null ? leftOperand.getClass().getSimpleName() : "null",
                    rightOperand != null ? rightOperand.getClass().getSimpleName() : "null")
            );
        }

        Number left = (Number) leftOperand;
        Number right = (Number) rightOperand;

        if (isFloatingPoint(left) || isFloatingPoint(right)) {
            return left.doubleValue() * right.doubleValue();
        }

        if (left instanceof Long || right instanceof Long) {
            long result = left.longValue() * right.longValue();
            // Check for overflow
            if (left.longValue() != 0 && result / left.longValue() != right.longValue()) {
                throw new ArithmeticException("Multiplication overflow");
            }
            return result;
        }

        int result = left.intValue() * right.intValue();
        // Check for overflow
        if (left.intValue() != 0 && result / left.intValue() != right.intValue()) {
            return left.longValue() * right.longValue(); // Promote to long
        }
        return result;
    }

    private boolean isFloatingPoint(Number number) {
        return number instanceof Float || number instanceof Double;
    }

    @Override
    public int getPrecedence() {
        return OperatorPrecedence.MULTIPLICATION;
    }

    @Override
    public OperatorType getType() {
        return OperatorType.ARITHMETIC;
    }

    @Override
    public String getSymbol() {
        return "*";
    }
}
```

#### Features:
- **Overflow Detection**: Automatic promotion to larger types
- **Performance Optimization**: Efficient overflow checking
- **Type Safety**: Strict numeric validation

#### Acceptance Criteria:
- ✅ Handles overflow gracefully
- ✅ Proper type promotion on overflow
- ✅ Efficient overflow detection algorithm
- ✅ Consistent numeric type handling

### Subtask 4.4: Division Operator Implementation
**Estimated Time**: 1 hour

#### Implementation:
```java
public class DivideOperator implements BinaryOperatorEvaluator {

    @Override
    public Object eval(Object leftOperand, Object rightOperand) {
        if (!(leftOperand instanceof Number) || !(rightOperand instanceof Number)) {
            throw new TypeMismatchException(
                String.format("/ operator requires numeric operands, got: %s and %s",
                    leftOperand != null ? leftOperand.getClass().getSimpleName() : "null",
                    rightOperand != null ? rightOperand.getClass().getSimpleName() : "null")
            );
        }

        Number left = (Number) leftOperand;
        Number right = (Number) rightOperand;

        // Check for division by zero
        if (right.doubleValue() == 0.0) {
            throw new ArithmeticException("Division by zero");
        }

        // Division always returns double to handle fractions
        return left.doubleValue() / right.doubleValue();
    }

    @Override
    public int getPrecedence() {
        return OperatorPrecedence.MULTIPLICATION; // Same as multiplication
    }

    @Override
    public OperatorType getType() {
        return OperatorType.ARITHMETIC;
    }

    @Override
    public String getSymbol() {
        return "/";
    }
}
```

#### Features:
- **Zero Division Protection**: Clear error for division by zero
- **Fractional Results**: Always returns double for accuracy
- **Precision Handling**: Maintains decimal precision

#### Alternative: Integer Division Support
```java
public class IntegerDivideOperator implements BinaryOperatorEvaluator {
    // For cases where integer division is needed: 7 // 3 = 2
    @Override
    public Object eval(Object leftOperand, Object rightOperand) {
        // Similar to DivideOperator but returns integer result
        return left.longValue() / right.longValue();
    }
}
```

#### Acceptance Criteria:
- ✅ Proper division by zero handling
- ✅ Returns appropriate precision (double)
- ✅ Handles very small divisors correctly
- ✅ Consider special floating-point values (NaN, Infinity)

### Subtask 4.5: Modulus Operator Implementation
**Estimated Time**: 0.5 hours

#### Implementation:
```java
public class ModulusOperator implements BinaryOperatorEvaluator {

    @Override
    public Object eval(Object leftOperand, Object rightOperand) {
        if (!(leftOperand instanceof Number) || !(rightOperand instanceof Number)) {
            throw new TypeMismatchException(
                String.format("%% operator requires numeric operands, got: %s and %s",
                    leftOperand != null ? leftOperand.getClass().getSimpleName() : "null",
                    rightOperand != null ? rightOperand.getClass().getSimpleName() : "null")
            );
        }

        Number left = (Number) leftOperand;
        Number right = (Number) rightOperand;

        // Check for modulus by zero
        if (right.doubleValue() == 0.0) {
            throw new ArithmeticException("Modulus by zero");
        }

        // For integer types, use integer modulus
        if (isIntegerType(left) && isIntegerType(right)) {
            return left.longValue() % right.longValue();
        }

        // For floating-point, use floating-point modulus
        return left.doubleValue() % right.doubleValue();
    }

    private boolean isIntegerType(Number number) {
        return number instanceof Integer || number instanceof Long ||
               number instanceof Short || number instanceof Byte;
    }

    @Override
    public int getPrecedence() {
        return OperatorPrecedence.MULTIPLICATION; // Same as multiplication and division
    }

    @Override
    public OperatorType getType() {
        return OperatorType.ARITHMETIC;
    }

    @Override
    public String getSymbol() {
        return "%";
    }
}
```

#### Features:
- **Type-Appropriate Modulus**: Integer vs floating-point modulus
- **Zero Modulus Protection**: Clear error handling
- **Sign Handling**: Proper sign rules for negative numbers

#### Acceptance Criteria:
- ✅ Handles both integer and floating-point modulus
- ✅ Proper zero modulus error handling
- ✅ Correct sign handling for negative operands
- ✅ Consistent type promotion rules

### Subtask 4.6: Type Handling Utilities
**Estimated Time**: 1 hour

#### Common Type Handling Class:
```java
public class NumberTypeUtils {

    public static boolean isFloatingPoint(Number number) {
        return number instanceof Float || number instanceof Double;
    }

    public static boolean isIntegerType(Number number) {
        return number instanceof Integer || number instanceof Long ||
               number instanceof Short || number instanceof Byte;
    }

    public static Number promoteToCommonType(Number left, Number right) {
        if (isFloatingPoint(left) || isFloatingPoint(right)) {
            return Double.class;
        }
        if (left instanceof Long || right instanceof Long) {
            return Long.class;
        }
        return Integer.class;
    }

    public static Number performOperation(Number left, Number right, ArithmeticOperation operation) {
        Class<?> resultType = promoteToCommonType(left, right);

        if (resultType == Double.class) {
            return operation.applyAsDouble(left.doubleValue(), right.doubleValue());
        } else if (resultType == Long.class) {
            return operation.applyAsLong(left.longValue(), right.longValue());
        } else {
            return operation.applyAsInt(left.intValue(), right.intValue());
        }
    }

    @FunctionalInterface
    public interface ArithmeticOperation {
        default double applyAsDouble(double left, double right) {
            throw new UnsupportedOperationException();
        }

        default long applyAsLong(long left, long right) {
            throw new UnsupportedOperationException();
        }

        default int applyAsInt(int left, int right) {
            throw new UnsupportedOperationException();
        }
    }
}
```

#### Usage Example:
```java
public class AddOperator implements BinaryOperatorEvaluator {
    @Override
    public Object eval(Object leftOperand, Object rightOperand) {
        // ... validation code ...

        return NumberTypeUtils.performOperation(
            (Number) leftOperand,
            (Number) rightOperand,
            new NumberTypeUtils.ArithmeticOperation() {
                @Override
                public double applyAsDouble(double left, double right) {
                    return left + right;
                }

                @Override
                public long applyAsLong(long left, long right) {
                    return left + right;
                }

                @Override
                public int applyAsInt(int left, int right) {
                    return left + right;
                }
            }
        );
    }
}
```

#### Acceptance Criteria:
- ✅ Consistent type promotion across all operators
- ✅ Reusable utility functions
- ✅ Performance-optimized type checking
- ✅ Clear type conversion rules

## Testing Requirements

### Unit Tests Per Operator:

#### Addition Tests:
```java
@Test
public void testAddition_IntegerTypes() {
    AddOperator add = new AddOperator();
    assertEquals(8, add.eval(5, 3));
    assertEquals(0, add.eval(-5, 5));
}

@Test
public void testAddition_TypePromotion() {
    AddOperator add = new AddOperator();
    assertEquals(8.5, add.eval(5, 3.5));
    assertTrue(add.eval(5, 3.5) instanceof Double);
}

@Test
public void testAddition_StringConcatenation() {
    AddOperator add = new AddOperator();
    assertEquals("hello5", add.eval("hello", 5));
    assertEquals("5world", add.eval(5, "world"));
}
```

#### Division Tests:
```java
@Test
public void testDivision_ValidNumbers() {
    DivideOperator divide = new DivideOperator();
    assertEquals(2.5, divide.eval(5, 2));
    assertEquals(0.5, divide.eval(1, 2));
}

@Test
public void testDivision_ByZero_ThrowsException() {
    DivideOperator divide = new DivideOperator();
    assertThrows(ArithmeticException.class, () -> divide.eval(5, 0));
    assertThrows(ArithmeticException.class, () -> divide.eval(5, 0.0));
}
```

### Integration Tests:
- Test complex arithmetic expressions: `(5 + 3) * 2 / 4`
- Test operator precedence: `2 + 3 * 4` should equal `14`, not `20`
- Test type mixing: `5 + 3.5 - 2` handling

### Performance Tests:
```java
@Test
public void testArithmeticPerformance() {
    AddOperator add = new AddOperator();
    long startTime = System.nanoTime();

    for (int i = 0; i < 100000; i++) {
        add.eval(i, i + 1);
    }

    long duration = System.nanoTime() - startTime;
    assertTrue("Addition should complete 100k operations in <100ms",
        duration < 100_000_000); // 100ms in nanoseconds
}
```

## File Structure
```
src/main/java/
├── operators/
│   ├── arithmetic/
│   │   ├── AddOperator.java
│   │   ├── SubtractOperator.java
│   │   ├── MultiplyOperator.java
│   │   ├── DivideOperator.java
│   │   └── ModulusOperator.java
│   └── utils/
│       └── NumberTypeUtils.java
```

## Updated Operator Factory Registration
```java
private static void registerDefaultOperators() {
    // ... existing operators ...

    // Register arithmetic operators
    registerBinaryOperator("+", new AddOperator());
    registerBinaryOperator("-", new SubtractOperator());
    registerBinaryOperator("*", new MultiplyOperator());
    registerBinaryOperator("/", new DivideOperator());
    registerBinaryOperator("%", new ModulusOperator());
}
```

## Success Metrics
- ✅ All 5 arithmetic operators implemented correctly
- ✅ Proper type promotion and overflow handling
- ✅ String concatenation support for addition
- ✅ Comprehensive error handling (division by zero, etc.)
- ✅ 100% test coverage for all operators
- ✅ Performance: 20,000+ arithmetic operations per second
- ✅ Type safety maintained across all operations