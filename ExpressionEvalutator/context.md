# Expression Evaluator Project Context

## Project Summary
Building a rule-based expression evaluator system in Java that evaluates mathematical and logical expressions against feature maps (key-value stores) using the Strategy Pattern for operator implementations.

## Key Architecture Components

### Core System
1. **Feature Map**: Key-value data structure for storing evaluation variables
2. **Expression Parser**: Tokenizes and builds AST from rule strings
3. **Operator Evaluators**: Strategy pattern implementations for each operator
4. **Evaluation Engine**: Orchestrates the evaluation process

### Supported Operators

#### Binary Operators
- Arithmetic: `+`, `-`, `*`, `/`, `%`
- Comparison: `<`, `<=`, `>`, `>=`, `==`, `!=`
- Logical: `&&`, `||`

#### Unary Operators
- Logical NOT: `!`
- Numeric negation: `-` (unary)

### Key Interfaces
```java
interface OperatorEvaluator {
    Object eval(Object... operands);
    int getPrecedence();
    OperatorType getType();
}
```

### Example Usage
```java
featureMap = {"userAge": 25, "accountBalance": 5000}
rule = "userAge > 18 && accountBalance >= 1000"
evaluate(featureMap, rule) → true
```

## Technology Stack
- **Language**: Java
- **Pattern**: Strategy Pattern for operators
- **Data Structure**: HashMap for feature maps
- **Parsing**: Custom AST-based expression parser

## Current Status
- ✅ Project overview and requirements defined
- ⏳ Implementation phase not started

## Next Steps
- Define project structure and packages
- Implement core interfaces and base classes
- Create operator implementations
- Build expression parser
- Implement evaluation engine
- Add comprehensive testing

## Key Design Decisions
1. **Strategy Pattern**: Each operator has its own evaluator class for modularity
2. **AST-based parsing**: For proper operator precedence handling
3. **Type-agnostic feature maps**: Support for numbers, booleans, strings
4. **Extensible operator registration**: Allow custom operators via factory pattern

## Success Metrics
- Support for all defined operators with correct precedence
- Type-safe evaluation with clear error handling
- Modular, testable, and extensible codebase
- Performance-optimized for production use