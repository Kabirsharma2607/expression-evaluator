# Task 2: OperatorEvaluator Interface & Logical Operators

## Objective
Define the core operator interface and implement all logical/comparison operators using the Strategy Pattern.

## Interface Definition
```java
public interface OperatorEvaluator {
    Object eval(Object leftOperand, Object rightOperand);
    int getPrecedence();
    OperatorType getType();
    String getSymbol();
}
```

## Subtask Breakdown

### Subtask 2.1: Core Interface Definition
**Estimated Time**: 1 hour

#### Deliverables:
1. **OperatorEvaluator Interface**
   ```java
   public interface OperatorEvaluator {
       /**
        * Evaluates the operator with given operands
        * @param leftOperand Left operand value
        * @param rightOperand Right operand value
        * @return Evaluation result
        */
       Object eval(Object leftOperand, Object rightOperand);

       /**
        * Returns operator precedence (higher number = higher precedence)
        */
       int getPrecedence();

       /**
        * Returns the type of operator
        */
       OperatorType getType();

       /**
        * Returns the string symbol for this operator
        */
       String getSymbol();
   }
   ```

2. **Supporting Enums and Classes**
   ```java
   public enum OperatorType {
       COMPARISON,    // <, <=, >, >=, ==, !=
       LOGICAL,       // &&, ||
       ARITHMETIC,    // +, -, *, /, %
       UNARY         // !, - (unary)
   }

   public class OperatorPrecedence {
       public static final int LOGICAL_OR = 1;
       public static final int LOGICAL_AND = 2;
       public static final int EQUALITY = 3;
       public static final int COMPARISON = 4;
       public static final int ADDITION = 5;
       public static final int MULTIPLICATION = 6;
       public static final int UNARY = 7;
   }
   ```

#### Acceptance Criteria:
- ✅ Clean, well-documented interface
- ✅ Precedence constants defined
- ✅ Operator types categorized correctly
- ✅ Interface supports future extensions

### Subtask 2.2: Comparison Operators Implementation
**Estimated Time**: 1.5 hours

#### 2.2.1: Less Than Operator
```java
public class LessThanOperator implements OperatorEvaluator {
    @Override
    public Object eval(Object leftOperand, Object rightOperand) {
        if (!(leftOperand instanceof Number) || !(rightOperand instanceof Number)) {
            throw new TypeMismatchException("< operator requires numeric operands");
        }
        Number left = (Number) leftOperand;
        Number right = (Number) rightOperand;
        return left.doubleValue() < right.doubleValue();
    }

    @Override
    public int getPrecedence() { return OperatorPrecedence.COMPARISON; }

    @Override
    public OperatorType getType() { return OperatorType.COMPARISON; }

    @Override
    public String getSymbol() { return "<"; }
}
```

#### 2.2.2: Other Comparison Operators
- **LessThanEqualOperator** (`<=`)
- **GreaterThanOperator** (`>`)
- **GreaterThanEqualOperator** (`>=`)
- **EqualOperator** (`==`)
- **NotEqualOperator** (`!=`)

#### Implementation Requirements:
- Type checking for numeric operations
- Support for different Number types (Integer, Double, Float, Long)
- Proper null handling
- Clear error messages for type mismatches

#### Acceptance Criteria:
- ✅ All 6 comparison operators implemented
- ✅ Proper type validation and coercion
- ✅ Null safety and error handling
- ✅ Consistent implementation patterns

### Subtask 2.3: Logical Operators Implementation
**Estimated Time**: 1 hour

#### 2.3.1: AND Operator
```java
public class AndOperator implements OperatorEvaluator {
    @Override
    public Object eval(Object leftOperand, Object rightOperand) {
        Boolean left = convertToBoolean(leftOperand);
        Boolean right = convertToBoolean(rightOperand);

        if (left == null || right == null) {
            throw new TypeMismatchException("&& operator requires boolean operands");
        }

        return left && right;
    }

    private Boolean convertToBoolean(Object operand) {
        if (operand instanceof Boolean) return (Boolean) operand;
        // Add truthiness conversion logic if needed
        return null;
    }

    @Override
    public int getPrecedence() { return OperatorPrecedence.LOGICAL_AND; }

    @Override
    public OperatorType getType() { return OperatorType.LOGICAL; }

    @Override
    public String getSymbol() { return "&&"; }
}
```

#### 2.3.2: OR Operator
```java
public class OrOperator implements OperatorEvaluator {
    // Similar implementation to AndOperator but with || logic
    @Override
    public Object eval(Object leftOperand, Object rightOperand) {
        Boolean left = convertToBoolean(leftOperand);
        Boolean right = convertToBoolean(rightOperand);

        if (left == null || right == null) {
            throw new TypeMismatchException("|| operator requires boolean operands");
        }

        return left || right;
    }

    @Override
    public int getPrecedence() { return OperatorPrecedence.LOGICAL_OR; }

    @Override
    public OperatorType getType() { return OperatorType.LOGICAL; }

    @Override
    public String getSymbol() { return "||"; }
}
```

#### Acceptance Criteria:
- ✅ AND and OR operators implemented correctly
- ✅ Boolean type validation
- ✅ Proper short-circuit evaluation consideration
- ✅ Error handling for non-boolean inputs

### Subtask 2.4: Operator Factory Implementation
**Estimated Time**: 1.5 hours

#### Deliverables:
1. **OperatorFactory Class**
   ```java
   public class OperatorFactory {
       private static final Map<String, OperatorEvaluator> operators = new HashMap<>();

       static {
           registerDefaultOperators();
       }

       public static OperatorEvaluator getOperator(String symbol) {
           OperatorEvaluator operator = operators.get(symbol);
           if (operator == null) {
               throw new UnsupportedOperationException("Operator not supported: " + symbol);
           }
           return operator;
       }

       public static void registerOperator(String symbol, OperatorEvaluator operator) {
           operators.put(symbol, operator);
       }

       public static Set<String> getSupportedOperators() {
           return operators.keySet();
       }

       private static void registerDefaultOperators() {
           // Register all comparison operators
           registerOperator("<", new LessThanOperator());
           registerOperator("<=", new LessThanEqualOperator());
           registerOperator(">", new GreaterThanOperator());
           registerOperator(">=", new GreaterThanEqualOperator());
           registerOperator("==", new EqualOperator());
           registerOperator("!=", new NotEqualOperator());

           // Register logical operators
           registerOperator("&&", new AndOperator());
           registerOperator("||", new OrOperator());
       }
   }
   ```

2. **Custom Exception Classes**
   ```java
   public class TypeMismatchException extends RuntimeException {
       public TypeMismatchException(String message) {
           super(message);
       }
   }

   public class UnsupportedOperatorException extends RuntimeException {
       public UnsupportedOperatorException(String message) {
           super(message);
       }
   }
   ```

#### Acceptance Criteria:
- ✅ Factory provides easy operator lookup
- ✅ Support for custom operator registration
- ✅ Clear error messages for unknown operators
- ✅ Thread-safe implementation

## Testing Requirements

### Unit Tests (Per Operator):
```java
@Test
public void testLessThanOperator_ValidNumbers_ReturnsCorrectResult() {
    LessThanOperator operator = new LessThanOperator();
    assertTrue((Boolean) operator.eval(5, 10));
    assertFalse((Boolean) operator.eval(10, 5));
    assertFalse((Boolean) operator.eval(5, 5));
}

@Test
public void testLessThanOperator_InvalidTypes_ThrowsException() {
    LessThanOperator operator = new LessThanOperator();
    assertThrows(TypeMismatchException.class, () -> operator.eval("5", 10));
}
```

### Integration Tests:
- Test operator factory registration
- Test operator precedence ordering
- Test complex expressions with multiple operators

## File Structure
```
src/main/java/
├── operators/
│   ├── OperatorEvaluator.java
│   ├── OperatorType.java
│   ├── OperatorPrecedence.java
│   ├── OperatorFactory.java
│   ├── comparison/
│   │   ├── LessThanOperator.java
│   │   ├── LessThanEqualOperator.java
│   │   ├── GreaterThanOperator.java
│   │   ├── GreaterThanEqualOperator.java
│   │   ├── EqualOperator.java
│   │   └── NotEqualOperator.java
│   ├── logical/
│   │   ├── AndOperator.java
│   │   └── OrOperator.java
│   └── exceptions/
│       ├── TypeMismatchException.java
│       └── UnsupportedOperatorException.java
```

## Integration Points

### With Task 1 (Policy Execution):
- Operators will be used by expression evaluator
- Factory pattern allows easy operator lookup

### With Task 3 (Unary Operators):
- Interface may need extension for single operand support
- Factory needs to handle unary operator registration

### With Task 4 (Numerical Operators):
- Consistent type handling patterns
- Shared precedence and factory systems

## Success Metrics
- ✅ All 8 operators implemented correctly
- ✅ 100% test coverage for each operator
- ✅ Factory system working with registration
- ✅ Type safety maintained across all operations
- ✅ Clear error messages for debugging
- ✅ Performance: 10,000+ operations per second per operator