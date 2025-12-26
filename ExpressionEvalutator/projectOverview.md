# Expression Evaluator with Operator Strategy Pattern - Project Overview

## Project Description
A rule-based expression evaluator system that evaluates mathematical and logical expressions against a feature map (key-value store). The system uses a strategy pattern for operator evaluation, where each operator type has its own implementation class that handles the evaluation logic.

## Core Concept
The system evaluates rules against feature maps using a modular operator-based architecture:
- **Feature Map**: Key-value pairs containing data to evaluate against
- **Rules**: Expression strings that define conditions using feature map values
- **Operator Evaluators**: Individual classes for each operator type that implement evaluation logic

### Example Usage
```
featureMap = {
    "userAge": 25,
    "accountBalance": 5000,
    "transactionCount": 150,
    "isPremium": true
}

rule = "userAge > 18 && accountBalance >= 1000"

// System internally uses:
// - GreaterThanOperator.eval(25, 18) → true
// - GreaterThanEqualOperator.eval(5000, 1000) → true
// - AndOperator.eval(true, true) → true

evaluate(featureMap, rule) → true
```

## Architecture Design

### Core Components

#### 1. Feature Map
- Stores key-value pairs of data
- Supports nested object access (e.g., `user.profile.age`)
- Type-agnostic value storage

#### 2. Expression Parser
- Tokenizes the rule expression
- Builds Abstract Syntax Tree (AST)
- Identifies operators and operands
- Resolves feature map references

#### 3. Operator Evaluator Interface
```
interface OperatorEvaluator {
    Object eval(Object... operands);
    int getPrecedence();
    OperatorType getType();
}
```

#### 4. Operator Implementations

##### Binary Operators (Two Operands)
- **LessThanOperator**: `<` - `LessThanOperator.eval(left, right)`
- **LessThanEqualOperator**: `<=` - `LessThanEqualOperator.eval(left, right)`
- **GreaterThanOperator**: `>` - `GreaterThanOperator.eval(left, right)`
- **GreaterThanEqualOperator**: `>=` - `GreaterThanEqualOperator.eval(left, right)`
- **EqualOperator**: `==` - `EqualOperator.eval(left, right)`
- **NotEqualOperator**: `!=` - `NotEqualOperator.eval(left, right)`
- **AndOperator**: `&&` - `AndOperator.eval(left, right)`
- **OrOperator**: `||` - `OrOperator.eval(left, right)`
- **AddOperator**: `+` - `AddOperator.eval(left, right)`
- **SubtractOperator**: `-` - `SubtractOperator.eval(left, right)`
- **MultiplyOperator**: `*` - `MultiplyOperator.eval(left, right)`
- **DivideOperator**: `/` - `DivideOperator.eval(left, right)`
- **ModulusOperator**: `%` - `ModulusOperator.eval(left, right)`

##### Unary Operators (Single Operand)
- **NotOperator**: `!` - `NotOperator.eval(operand)`
- **NegateOperator**: `-` (unary) - `NegateOperator.eval(operand)`

#### 5. Operator Factory
```
class OperatorFactory {
    OperatorEvaluator getOperator(String symbol);
    void registerOperator(String symbol, OperatorEvaluator operator);
}
```

#### 6. Expression Evaluator Engine
- Traverses the AST
- Resolves feature map values
- Delegates to appropriate operator evaluators
- Returns final evaluation result

## Operator Implementation Examples

### Comparison Operator
```java
class LessThanOperator implements OperatorEvaluator {
    public Object eval(Object... operands) {
        Number left = (Number) operands[0];
        Number right = (Number) operands[1];
        return left.doubleValue() < right.doubleValue();
    }
}
```

### Logical Operator
```java
class AndOperator implements OperatorEvaluator {
    public Object eval(Object... operands) {
        Boolean left = (Boolean) operands[0];
        Boolean right = (Boolean) operands[1];
        return left && right;
    }
}
```

### Unary Operator
```java
class NotOperator implements OperatorEvaluator {
    public Object eval(Object... operands) {
        Boolean operand = (Boolean) operands[0];
        return !operand;
    }
}
```

## Rule Processing Flow

1. **Input**: Feature map and rule expression
2. **Tokenization**: Break expression into tokens
3. **Parsing**: Build AST with operator nodes and operand nodes
4. **Resolution**: Replace variable references with feature map values
5. **Evaluation**:
   - Traverse AST from leaves to root
   - For each operator node, invoke corresponding operator evaluator
   - Pass resolved operands to evaluator
6. **Output**: Return final evaluation result

### Example Flow
```
Rule: "age > 18 && score >= 75"
Feature Map: {"age": 25, "score": 80}

1. Parse to AST:
       &&
      /  \
     >    >=
    / \   / \
  age 18 score 75

2. Resolve variables:
       &&
      /  \
     >    >=
    / \   / \
   25 18  80 75

3. Evaluate bottom-up:
   - GreaterThanOperator.eval(25, 18) → true
   - GreaterThanEqualOperator.eval(80, 75) → true
   - AndOperator.eval(true, true) → true

4. Result: true
```

## Operator Precedence
1. Parentheses `()`
2. Unary operators (`!`, `-`)
3. Multiplication, Division, Modulus (`*`, `/`, `%`)
4. Addition, Subtraction (`+`, `-`)
5. Comparison operators (`<`, `<=`, `>`, `>=`)
6. Equality operators (`==`, `!=`)
7. Logical AND (`&&`)
8. Logical OR (`||`)

## Extensibility Features

### Custom Operator Registration
```java
// Register custom operator
operatorFactory.registerOperator("contains", new ContainsOperator());

// Use in rule
"featureMap.tags contains 'premium'"
```

### Operator Chaining
Support for complex expressions with multiple operators:
```
"(age > 18 && age < 65) || (status == 'vip' && !blacklisted)"
```

## Error Handling

### Type Safety
- Type checking before operator evaluation
- Automatic type coercion where applicable
- Clear error messages for type mismatches

### Error Types
1. **UndefinedVariableError**: Feature map key not found
2. **TypeMismatchError**: Invalid operand types for operator
3. **SyntaxError**: Malformed expression
4. **EvaluationError**: Runtime evaluation failures

## Benefits of Operator Strategy Pattern

1. **Modularity**: Each operator is independently implemented
2. **Extensibility**: Easy to add new operators without modifying existing code
3. **Testability**: Each operator can be unit tested in isolation
4. **Maintainability**: Clear separation of concerns
5. **Reusability**: Operators can be reused across different rule types

## Example Rule Scenarios

### Business Rules
```
featureMap = {"orderValue": 150, "customerType": "gold", "itemCount": 5}
rule = "orderValue > 100 && customerType == 'gold'"
// Uses: GreaterThanOperator, EqualOperator, AndOperator
```

### Access Control
```
featureMap = {"userRole": "admin", "ipWhitelisted": true}
rule = "userRole == 'admin' || ipWhitelisted"
// Uses: EqualOperator, OrOperator
```

### Alert Conditions
```
featureMap = {"cpuUsage": 85, "memoryUsage": 45}
rule = "cpuUsage > 80 || memoryUsage > 90"
// Uses: GreaterThanOperator, OrOperator
```

## Success Criteria
- Clean operator abstraction with consistent interface
- Support for all common mathematical and logical operators
- Proper operator precedence handling
- Type-safe operator evaluation
- Easy addition of new custom operators
- Comprehensive test coverage for each operator
- Performance-optimized evaluation engine