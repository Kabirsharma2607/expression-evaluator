# Task 6: Spring Boot REST API Implementation

## Objective
Transform the expression evaluator into a Spring Boot REST API application that accepts feature maps via HTTP requests and returns policy evaluation results.

## Architecture Overview
```
Client → REST API → Controller → Service Layer → Policy Executor → Expression Evaluator
                         ↓              ↓
                        DTOs      Policy Loader
                                       ↓
                                  YAML Policies
```

## Task Breakdown

### Phase 1: Spring Boot Setup (30 mins)

#### 1.1 Update Build Configuration
- Add Spring Boot dependencies to `build.gradle.kts`
- Configure Spring Boot plugin
- Add Spring Web, Spring Boot Starter, Validation dependencies
- Add Swagger/OpenAPI for API documentation

```kotlin
// build.gradle.kts additions
plugins {
    id("org.springframework.boot") version "3.2.0"
    id("io.spring.dependency-management") version "1.1.4"
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.3.0")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
}
```

#### 1.2 Create Main Application Class
```java
@SpringBootApplication
@EnableConfigurationProperties
public class ExpressionEvaluatorApplication {
    public static void main(String[] args) {
        SpringApplication.run(ExpressionEvaluatorApplication.class, args);
    }
}
```

### Phase 2: API Layer Implementation (45 mins)

#### 2.1 Request/Response DTOs

**PolicyEvaluationRequest.java**
```java
@Data
@NoArgsConstructor
@AllArgsConstructor
@Validated
public class PolicyEvaluationRequest {

    @NotNull(message = "Policy name is required")
    @NotBlank(message = "Policy name cannot be blank")
    private String policyName;

    @NotNull(message = "Feature map is required")
    @NotEmpty(message = "Feature map cannot be empty")
    private Map<String, Object> featureMap;

    // Optional: for testing specific expressions
    private String expression;
}
```

**PolicyEvaluationResponse.java**
```java
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PolicyEvaluationResponse {
    private String policyName;
    private boolean success;
    private Map<String, RuleResultDto> ruleResults;
    private List<String> errors;
    private Long executionTimeMs;
    private String timestamp;
}

@Data
@Builder
public class RuleResultDto {
    private Object result;
    private boolean success;
    private String error;
    private Long executionTimeMs;
}
```

**ExpressionEvaluationRequest.java** (for direct expression evaluation)
```java
@Data
@Validated
public class ExpressionEvaluationRequest {

    @NotNull(message = "Expression is required")
    @NotBlank(message = "Expression cannot be blank")
    private String expression;

    private Map<String, Object> featureMap = new HashMap<>();
    private Map<String, Object> ruleContext = new HashMap<>();
}
```

#### 2.2 REST Controller
```java
@RestController
@RequestMapping("/api/v1/evaluation")
@Validated
@Slf4j
public class EvaluationController {

    private final PolicyEvaluationService policyService;
    private final ExpressionEvaluationService expressionService;

    @PostMapping("/policy")
    public ResponseEntity<PolicyEvaluationResponse> evaluatePolicy(
            @Valid @RequestBody PolicyEvaluationRequest request) {

        log.info("Evaluating policy: {} with feature map size: {}",
                request.getPolicyName(), request.getFeatureMap().size());

        PolicyEvaluationResponse response = policyService.evaluatePolicy(
            request.getPolicyName(),
            request.getFeatureMap()
        );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/expression")
    public ResponseEntity<Map<String, Object>> evaluateExpression(
            @Valid @RequestBody ExpressionEvaluationRequest request) {

        log.info("Evaluating expression: {}", request.getExpression());

        Object result = expressionService.evaluateExpression(
            request.getExpression(),
            request.getFeatureMap(),
            request.getRuleContext()
        );

        return ResponseEntity.ok(Map.of(
            "expression", request.getExpression(),
            "result", result,
            "timestamp", Instant.now()
        ));
    }

    @GetMapping("/policies")
    public ResponseEntity<List<String>> getAvailablePolicies() {
        List<String> policies = policyService.getAvailablePolicies();
        return ResponseEntity.ok(policies);
    }

    @GetMapping("/policy/{policyName}")
    public ResponseEntity<PolicyDetailsResponse> getPolicyDetails(
            @PathVariable String policyName) {
        PolicyDetailsResponse details = policyService.getPolicyDetails(policyName);
        return ResponseEntity.ok(details);
    }
}
```

### Phase 3: Service Layer (45 mins)

#### 3.1 Policy Evaluation Service
```java
@Service
@Slf4j
public class PolicyEvaluationService {

    private final PolicyExecutor policyExecutor;
    private final PolicyLoader policyLoader;

    @Autowired
    public PolicyEvaluationService() {
        this.policyExecutor = new PolicyExecutor();
        this.policyLoader = new PolicyLoader();
    }

    public PolicyEvaluationResponse evaluatePolicy(String policyName,
                                                   Map<String, Object> featureMap) {
        try {
            PolicyResult result = policyExecutor.executePolicy(featureMap, policyName);

            return PolicyEvaluationResponse.builder()
                .policyName(policyName)
                .success(result.isSuccess())
                .ruleResults(convertRuleResults(result.getRuleResults()))
                .errors(result.getErrors())
                .executionTimeMs(result.getExecutionTimeMs())
                .timestamp(Instant.now().toString())
                .build();

        } catch (PolicyNotFoundException e) {
            log.error("Policy not found: {}", policyName, e);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                "Policy not found: " + policyName);
        } catch (Exception e) {
            log.error("Error evaluating policy: {}", policyName, e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                "Error evaluating policy: " + e.getMessage());
        }
    }

    public List<String> getAvailablePolicies() {
        // Implementation to list all available policy names
        return policyLoader.listAvailablePolicies();
    }

    private Map<String, RuleResultDto> convertRuleResults(Map<String, RuleResult> results) {
        return results.entrySet().stream()
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> RuleResultDto.builder()
                    .result(entry.getValue().getResult())
                    .success(entry.getValue().isSuccess())
                    .error(entry.getValue().getError())
                    .executionTimeMs(entry.getValue().getExecutionTimeMs())
                    .build()
            ));
    }
}
```

#### 3.2 Expression Evaluation Service
```java
@Service
@Slf4j
public class ExpressionEvaluationService {

    private final ExpressionEvaluator expressionEvaluator;

    @Autowired
    public ExpressionEvaluationService() {
        this.expressionEvaluator = new ExpressionEvaluator();
    }

    public Object evaluateExpression(String expression,
                                    Map<String, Object> featureMap,
                                    Map<String, Object> ruleContext) {
        try {
            return expressionEvaluator.evaluate(expression, featureMap, ruleContext);
        } catch (Exception e) {
            log.error("Error evaluating expression: {}", expression, e);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "Invalid expression: " + e.getMessage());
        }
    }
}
```

### Phase 4: Error Handling & Validation (30 mins)

#### 4.1 Global Exception Handler
```java
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(ValidationException e) {
        log.error("Validation error: ", e);
        ErrorResponse error = ErrorResponse.builder()
            .status(HttpStatus.BAD_REQUEST.value())
            .message("Validation failed")
            .details(e.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.toList()))
            .timestamp(Instant.now())
            .build();
        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(PolicyNotFoundException.class)
    public ResponseEntity<ErrorResponse> handlePolicyNotFound(PolicyNotFoundException e) {
        ErrorResponse error = ErrorResponse.builder()
            .status(HttpStatus.NOT_FOUND.value())
            .message(e.getMessage())
            .timestamp(Instant.now())
            .build();
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(ExpressionEvaluationException.class)
    public ResponseEntity<ErrorResponse> handleExpressionError(ExpressionEvaluationException e) {
        ErrorResponse error = ErrorResponse.builder()
            .status(HttpStatus.BAD_REQUEST.value())
            .message("Expression evaluation failed: " + e.getMessage())
            .timestamp(Instant.now())
            .build();
        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception e) {
        log.error("Unexpected error: ", e);
        ErrorResponse error = ErrorResponse.builder()
            .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
            .message("An unexpected error occurred")
            .timestamp(Instant.now())
            .build();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}

@Data
@Builder
public class ErrorResponse {
    private int status;
    private String message;
    private List<String> details;
    private Instant timestamp;
}
```

### Phase 5: Configuration & Properties (20 mins)

#### 5.1 Application Configuration
```yaml
# application.yml
server:
  port: 8080
  servlet:
    context-path: /api

spring:
  application:
    name: expression-evaluator-api
  jackson:
    property-naming-strategy: SNAKE_CASE
    default-property-inclusion: non_null

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics
  endpoint:
    health:
      show-details: always

springdoc:
  api-docs:
    path: /api-docs
  swagger-ui:
    path: /swagger-ui.html
    operationsSorter: alpha
    tagsSorter: alpha

logging:
  level:
    com.example: DEBUG
    org.springframework.web: INFO

# Custom properties
policy:
  location: classpath:policies/
  cache:
    enabled: true
    ttl: 3600
```

#### 5.2 Configuration Class
```java
@Configuration
@ConfigurationProperties(prefix = "policy")
@Data
public class PolicyConfiguration {
    private String location = "classpath:policies/";
    private CacheConfig cache = new CacheConfig();

    @Data
    public static class CacheConfig {
        private boolean enabled = true;
        private long ttl = 3600;
    }
}
```

### Phase 6: API Documentation (20 mins)

#### 6.1 OpenAPI Configuration
```java
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("Expression Evaluator API")
                .version("1.0.0")
                .description("REST API for evaluating policies and expressions")
                .contact(new Contact()
                    .name("API Support")
                    .email("support@example.com")))
            .externalDocs(new ExternalDocumentation()
                .description("API Documentation")
                .url("https://github.com/yourusername/expression-evaluator"));
    }
}
```

#### 6.2 API Documentation Annotations
```java
@Operation(summary = "Evaluate a policy",
           description = "Evaluates a policy against the provided feature map")
@ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Successful evaluation"),
    @ApiResponse(responseCode = "404", description = "Policy not found"),
    @ApiResponse(responseCode = "400", description = "Invalid request"),
    @ApiResponse(responseCode = "500", description = "Internal server error")
})
@PostMapping("/policy")
public ResponseEntity<PolicyEvaluationResponse> evaluatePolicy(...) {
    // Implementation
}
```

### Phase 7: Testing (30 mins)

#### 7.1 Integration Tests
```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class EvaluationControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testEvaluatePolicy_Success() throws Exception {
        Map<String, Object> featureMap = Map.of(
            "userAge", 25,
            "accountBalance", 5000
        );

        PolicyEvaluationRequest request = new PolicyEvaluationRequest();
        request.setPolicyName("fc1Rules");
        request.setFeatureMap(featureMap);

        mockMvc.perform(post("/api/v1/evaluation/policy")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.policy_name").value("fc1Rules"))
                .andExpect(jsonPath("$.rule_results").exists());
    }

    @Test
    void testEvaluateExpression_Success() throws Exception {
        ExpressionEvaluationRequest request = new ExpressionEvaluationRequest();
        request.setExpression("5 + 3 > 7");
        request.setFeatureMap(Map.of());

        mockMvc.perform(post("/api/v1/evaluation/expression")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value(true));
    }
}
```

#### 7.2 Sample API Calls
```bash
# Evaluate a policy
curl -X POST http://localhost:8080/api/v1/evaluation/policy \
  -H "Content-Type: application/json" \
  -d '{
    "policy_name": "fc1Rules",
    "feature_map": {
      "userAge": 25,
      "accountBalance": 5000
    }
  }'

# Evaluate an expression
curl -X POST http://localhost:8080/api/v1/evaluation/expression \
  -H "Content-Type: application/json" \
  -d '{
    "expression": "featureMap.userAge > 18 && featureMap.accountBalance >= 1000",
    "feature_map": {
      "userAge": 25,
      "accountBalance": 5000
    }
  }'

# Get available policies
curl http://localhost:8080/api/v1/evaluation/policies

# Get policy details
curl http://localhost:8080/api/v1/evaluation/policy/fc1Rules
```

## Expected API Response Examples

### Policy Evaluation Response
```json
{
  "policy_name": "fc1Rules",
  "success": true,
  "rule_results": {
    "rule1": {
      "result": true,
      "success": true,
      "execution_time_ms": 5
    },
    "rule2": {
      "result": true,
      "success": true,
      "execution_time_ms": 3
    },
    "rule3": {
      "result": true,
      "success": true,
      "execution_time_ms": 2
    }
  },
  "execution_time_ms": 45,
  "timestamp": "2024-12-26T10:30:00Z"
}
```

### Expression Evaluation Response
```json
{
  "expression": "5 + 3 > 7",
  "result": true,
  "timestamp": "2024-12-26T10:30:00Z"
}
```

### Error Response
```json
{
  "status": 400,
  "message": "Validation failed",
  "details": [
    "Policy name is required",
    "Feature map cannot be empty"
  ],
  "timestamp": "2024-12-26T10:30:00Z"
}
```

## Performance Considerations

1. **Caching**: Cache compiled expressions and policy definitions
2. **Thread Safety**: Use concurrent data structures for shared resources
3. **Connection Pooling**: Configure appropriate thread pools for request handling
4. **Monitoring**: Add metrics for expression evaluation times
5. **Rate Limiting**: Consider adding rate limiting for API endpoints

## Security Considerations

1. **Input Validation**: Validate all inputs to prevent injection attacks
2. **Expression Limits**: Limit expression complexity to prevent DoS
3. **Authentication**: Add JWT/OAuth2 authentication if needed
4. **CORS**: Configure CORS appropriately for browser-based clients
5. **Audit Logging**: Log all policy evaluations for audit purposes

## Deployment Considerations

1. **Docker Support**: Create Dockerfile for containerization
2. **Health Checks**: Implement health check endpoints
3. **Configuration Management**: Externalize configuration for different environments
4. **Graceful Shutdown**: Implement proper shutdown hooks
5. **Metrics & Monitoring**: Integrate with Prometheus/Grafana

## Success Criteria

- ✅ Spring Boot application starts successfully
- ✅ REST endpoints accept JSON requests and return JSON responses
- ✅ Policy evaluation works via API calls
- ✅ Expression evaluation works via API calls
- ✅ Proper error handling with meaningful error messages
- ✅ API documentation available via Swagger UI
- ✅ Integration tests pass
- ✅ Application is ready for containerization