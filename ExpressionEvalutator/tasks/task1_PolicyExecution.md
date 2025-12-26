# Task 1: Policy Execution Function

## Objective
Create the main entry point function that processes feature maps against YAML-defined policies with rule dependency resolution.

## Function Signature
```java
public PolicyResult executePolicy(Map<String, Object> featureMap, String policyName)
```

## Subtask Breakdown

### Subtask 1.1: YAML Policy Configuration Structure
**Estimated Time**: 1 hour

#### Deliverables:
1. **YAML Schema Definition**
   ```yaml
   # Example: fc1Rules.yaml
   policyName: "fc1Rules"
   description: "Feature check policy 1"
   version: "1.0"
   rules:
     rule1:
       expression: "featureMap.something > 700"
       description: "Check something threshold"
     rule2:
       expression: "featureMap.something2 > 800"
       description: "Check something2 threshold"
     rule3:
       expression: "rule1 && rule2"
       description: "Combined rule check"
       dependencies: ["rule1", "rule2"]
   ```

2. **YAML File Examples**
   - `fc1Rules.yaml` - Business rules example
   - `accessControlRules.yaml` - Access control example
   - `alertRules.yaml` - Alert conditions example

#### Acceptance Criteria:
- ✅ YAML structure supports rule definitions
- ✅ Rule dependencies clearly defined
- ✅ Metadata included (descriptions, versions)
- ✅ Multiple policy examples created

### Subtask 1.2: Policy Loader Service
**Estimated Time**: 1.5 hours

#### Deliverables:
1. **PolicyLoader Class**
   ```java
   public class PolicyLoader {
       public Policy loadPolicy(String policyName);
       public Policy loadPolicyFromFile(String filePath);
       public void validatePolicy(Policy policy);
   }
   ```

2. **Policy Data Model**
   ```java
   public class Policy {
       private String name;
       private String description;
       private String version;
       private Map<String, Rule> rules;
       // getters, setters, constructors
   }

   public class Rule {
       private String name;
       private String expression;
       private String description;
       private List<String> dependencies;
       // getters, setters, constructors
   }
   ```

#### Acceptance Criteria:
- ✅ Loads YAML files into Policy objects
- ✅ Validates policy structure and dependencies
- ✅ Handles file not found and parsing errors
- ✅ Supports policy caching for performance

### Subtask 1.3: Main Execution Function
**Estimated Time**: 1.5 hours

#### Deliverables:
1. **PolicyExecutor Class**
   ```java
   public class PolicyExecutor {
       public PolicyResult executePolicy(Map<String, Object> featureMap, String policyName);
       private RuleResult executeRule(Rule rule, Map<String, Object> featureMap, Map<String, Object> ruleContext);
       private List<Rule> resolveDependencyOrder(Policy policy);
   }
   ```

2. **Result Data Models**
   ```java
   public class PolicyResult {
       private boolean success;
       private Map<String, RuleResult> ruleResults;
       private List<String> errors;
       private long executionTimeMs;
   }

   public class RuleResult {
       private String ruleName;
       private Object result; // Boolean, Number, etc.
       private boolean success;
       private String error;
   }
   ```

#### Acceptance Criteria:
- ✅ Executes rules in dependency order
- ✅ Handles rule dependencies (rule3 uses rule1 && rule2 results)
- ✅ Returns comprehensive execution results
- ✅ Graceful error handling for failed rules

### Subtask 1.4: Feature Map Integration
**Estimated Time**: 1 hour

#### Deliverables:
1. **FeatureMapResolver Class**
   ```java
   public class FeatureMapResolver {
       public Object resolveValue(String path, Map<String, Object> featureMap);
       public boolean hasValue(String path, Map<String, Object> featureMap);
       public Class<?> getValueType(String path, Map<String, Object> featureMap);
   }
   ```

2. **Variable Resolution Support**
   - Support dot notation: `featureMap.user.profile.age`
   - Support array indexing: `featureMap.transactions[0].amount`
   - Support default values: `featureMap.optionalField ?? 0`

#### Acceptance Criteria:
- ✅ Resolves nested object paths correctly
- ✅ Handles missing values gracefully
- ✅ Type-safe value extraction
- ✅ Clear error messages for resolution failures

## Integration Points

### With Task 2 (Operator Evaluators):
- Policy execution will use operator evaluators for expression evaluation
- Need expression parser to convert rule strings to evaluable expressions

### With Task 3 & 4 (Operators):
- All operator types must be available for rule expressions
- Operator factory integration for dynamic operator lookup

### With Task 5 (Testing):
- Comprehensive policy execution testing
- Rule dependency testing
- Error scenario validation

## Example Usage

```java
// Feature map setup
Map<String, Object> featureMap = new HashMap<>();
featureMap.put("something", 750);
featureMap.put("something2", 850);

// Execute policy
PolicyExecutor executor = new PolicyExecutor();
PolicyResult result = executor.executePolicy(featureMap, "fc1Rules");

// Check results
if (result.isSuccess()) {
    RuleResult rule1Result = result.getRuleResults().get("rule1"); // true
    RuleResult rule2Result = result.getRuleResults().get("rule2"); // true
    RuleResult rule3Result = result.getRuleResults().get("rule3"); // true
}
```

## File Structure
```
src/main/java/
├── policy/
│   ├── PolicyLoader.java
│   ├── PolicyExecutor.java
│   ├── FeatureMapResolver.java
│   └── model/
│       ├── Policy.java
│       ├── Rule.java
│       ├── PolicyResult.java
│       └── RuleResult.java
└── resources/
    ├── policies/
    │   ├── fc1Rules.yaml
    │   ├── accessControlRules.yaml
    │   └── alertRules.yaml
```

## Dependencies
- YAML parsing library (Jackson YAML or SnakeYAML)
- Expression evaluation framework (integration with Task 2-4)
- Logging framework for execution tracking

## Success Metrics
- ✅ Load complex YAML policies correctly
- ✅ Execute rules in proper dependency order
- ✅ Handle nested feature map access
- ✅ Process 1000+ rule executions per second
- ✅ Zero data loss in error scenarios
- ✅ Clear error messages for debugging