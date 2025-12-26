# Expression Evaluator - Main Task Plan

## Project Objective
Build a rule-based expression evaluator that executes policies (collections of rules) against feature maps using YAML-configured rules and operator strategy pattern.

## Main Tasks Overview

### Task 1: Policy Execution Function
**Objective**: Create main entry point function that processes feature maps against YAML-defined policies
- **Input**: `(featureMap, policyName)`
- **Process**: Load policy from YAML, execute rules, return results
- **Output**: Policy evaluation result

### Task 2: OperatorEvaluator Interface & Logical Operators
**Objective**: Define core operator interface and implement all logical operators
- **Interface**: `OperatorEvaluator` with `eval(leftOperand, rightOperand)` method
- **Scope**: Comparison and logical binary operators

### Task 3: Unary Operators Implementation
**Objective**: Implement single-operand operators
- **Scope**: NOT operator (`!`) and unary minus (`-`)
- **Interface**: Extend for single operand support

### Task 4: Numerical Operators Implementation
**Objective**: Implement mathematical binary operators
- **Scope**: Addition, subtraction, multiplication, division, modulus

### Task 5: Testing & Validation
**Objective**: Comprehensive testing of all operators and policy execution
- **Scope**: Unit tests, integration tests, edge cases

---

## Detailed Task Breakdown

### Task 1: Policy Execution Function
**Priority**: High | **Estimated Effort**: 3-4 hours

#### Subtasks:
1. **YAML Policy Configuration Structure**
   - Define YAML schema for policies and rules
   - Example: `fc1Rules.yaml` with rule definitions

2. **Policy Loader Service**
   - YAML file reading and parsing
   - Policy validation and error handling

3. **Main Execution Function**
   - `executePolicy(featureMap, policyName)` implementation
   - Rule dependency resolution (e.g., rule3 depends on rule1 && rule2)

4. **Feature Map Integration**
   - Variable resolution from feature maps
   - Nested object support (e.g., `featureMap.user.age`)

#### Expected Deliverables:
- YAML policy configuration examples
- Policy loader utility
- Main execution function
- Integration with operator evaluators

### Task 2: OperatorEvaluator Interface & Logical Operators
**Priority**: High | **Estimated Effort**: 2-3 hours

#### 2.1 Core Interface Definition
- Create `OperatorEvaluator` interface
- Define `eval(leftOperand, rightOperand)` method
- Add operator metadata methods (precedence, type)

#### 2.2 Logical Operators Implementation
##### Comparison Operators:
- `LessThanOperator` (`<`)
- `LessThanEqualOperator` (`<=`)
- `GreaterThanOperator` (`>`)
- `GreaterThanEqualOperator` (`>=`)
- `EqualOperator` (`==`)
- `NotEqualOperator` (`!=`)

##### Logical Operators:
- `AndOperator` (`&&`)
- `OrOperator` (`||`)

#### 2.3 Operator Factory
- Create operator registration system
- Implement operator lookup by symbol
- Support for operator precedence handling

### Task 3: Unary Operators Implementation
**Priority**: Medium | **Estimated Effort**: 1-2 hours

#### 3.1 Interface Extension
- Extend/modify interface for single operand support
- Create `evalUnary(operand)` method or overload existing

#### 3.2 Unary Operator Implementations:
- `NotOperator` (`!`) - Logical negation
- `NegateOperator` (`-`) - Numeric negation

#### 3.3 Integration
- Update operator factory for unary operators
- Modify parser to handle unary expressions

### Task 4: Numerical Operators Implementation
**Priority**: Medium | **Estimated Effort**: 2-3 hours

#### 4.1 Arithmetic Operators:
- `AddOperator` (`+`)
- `SubtractOperator` (`-`)
- `MultiplyOperator` (`*`)
- `DivideOperator` (`/`)
- `ModulusOperator` (`%`)

#### 4.2 Type Handling:
- Number type coercion (int, double, float)
- Division by zero handling
- Overflow/underflow considerations

#### 4.3 Integration:
- Register arithmetic operators in factory
- Update precedence rules

### Task 5: Testing & Validation
**Priority**: High | **Estimated Effort**: 3-4 hours

#### 5.1 Unit Testing:
- Test each operator implementation individually
- Test operator factory registration
- Test policy loader functionality

#### 5.2 Integration Testing:
- Test complete policy execution flow
- Test complex expressions with multiple operators
- Test rule dependencies and execution order

#### 5.3 Edge Case Testing:
- Invalid YAML configurations
- Missing feature map values
- Type mismatches
- Division by zero scenarios
- Malformed expressions

#### 5.4 Performance Testing:
- Benchmark policy execution speed
- Test with large feature maps
- Test complex rule dependencies

---

## Success Criteria

### Task 1 Success:
- ✅ Successfully load and parse YAML policy files
- ✅ Execute policies against feature maps
- ✅ Handle rule dependencies correctly
- ✅ Return meaningful evaluation results

### Task 2 Success:
- ✅ Clean operator interface implementation
- ✅ All logical operators working correctly
- ✅ Operator factory system functional
- ✅ Proper precedence handling

### Task 3 Success:
- ✅ Unary operators implemented and integrated
- ✅ Parser handles unary expressions correctly
- ✅ Type safety maintained

### Task 4 Success:
- ✅ All arithmetic operators implemented
- ✅ Proper type handling and coercion
- ✅ Error handling for edge cases

### Task 5 Success:
- ✅ Comprehensive test coverage (>90%)
- ✅ All edge cases handled gracefully
- ✅ Performance meets requirements
- ✅ Documentation complete

---

## Implementation Order
1. **Task 2** → Core operator interface and logical operators (foundation)
2. **Task 3** → Unary operators (extends foundation)
3. **Task 4** → Numerical operators (completes operator set)
4. **Task 1** → Policy execution function (integrates everything)
5. **Task 5** → Testing and validation (validates everything)

---

## Risk Mitigation
- **Dependency Management**: Implement operator registry before complex operators
- **Type Safety**: Establish type handling patterns early
- **YAML Parsing**: Use proven YAML libraries (Jackson, SnakeYAML)
- **Testing**: Write tests alongside implementation, not after