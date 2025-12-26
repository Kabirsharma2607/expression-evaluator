# S3 Integration Task Plan

## Overview
Implement S3 integration to fetch policy YAML files from an S3 bucket and cache them locally on application startup, with periodic refresh capabilities.

## Architecture Design

### Components
1. **S3 Client Service** - Handles S3 operations (download, list)
2. **Policy Cache Manager** - In-memory cache with TTL support
3. **S3 Policy Loader** - Enhanced PolicyLoader that fetches from S3
4. **Cache Warmer** - Startup component to pre-load policies
5. **Cache Refresh Scheduler** - Periodic refresh mechanism
6. **Fallback Handler** - Handle S3 failures gracefully

## Detailed Tasks

### Task 1: Add AWS S3 Dependencies
**File**: `build.gradle.kts`
- Add AWS SDK v2 for S3
- Add Spring Boot Starter Cache
- Add Caffeine cache implementation

```kotlin
implementation("software.amazon.awssdk:s3:2.20.0")
implementation("software.amazon.awssdk:sts:2.20.0")
implementation("org.springframework.boot:spring-boot-starter-cache")
implementation("com.github.ben-manes.caffeine:caffeine:3.1.8")
```

### Task 2: Create S3 Configuration Properties
**File**: `S3Properties.java`
- Bucket name
- Region
- Prefix/folder for policies
- Access credentials (support IAM roles)
- Cache TTL settings
- Retry configuration

```java
@ConfigurationProperties(prefix = "s3.policy")
public class S3Properties {
    private String bucketName;
    private String region;
    private String prefix = "policies/";
    private Integer cacheTtlMinutes = 60;
    private Integer maxRetries = 3;
    // getters/setters
}
```

### Task 3: Implement S3 Client Service
**File**: `S3PolicyService.java`
- Initialize S3 client
- Download policy file method
- List all policy files method
- Handle S3 exceptions
- Implement retry logic

```java
@Service
public class S3PolicyService {
    public byte[] downloadPolicy(String key);
    public List<String> listPolicyFiles();
    public boolean isPolicyExists(String key);
}
```

### Task 4: Create Policy Cache Manager
**File**: `PolicyCacheManager.java`
- In-memory cache using Caffeine
- TTL-based eviction
- Cache statistics
- Thread-safe operations
- Support for different cache strategies

```java
@Component
public class PolicyCacheManager {
    private final Cache<String, Policy> policyCache;
    public void put(String key, Policy policy);
    public Optional<Policy> get(String key);
    public void evict(String key);
    public void evictAll();
    public CacheStats getStats();
}
```

### Task 5: Implement S3PolicyLoader
**File**: `S3PolicyLoader.java`
- Extend current PolicyLoader
- Fetch from S3 first
- Cache downloaded policies
- Fall back to local if S3 fails
- Parse YAML to Policy objects

```java
@Component
@Primary
public class S3PolicyLoader extends PolicyLoader {
    @Override
    public Policy loadPolicy(String policyName);
    public void refreshPolicy(String policyName);
    public void refreshAllPolicies();
}
```

### Task 6: Add Cache Warming on Startup
**File**: `CacheWarmupRunner.java`
- Implement ApplicationRunner
- Load all policies from S3 on startup
- Populate cache
- Log loading status
- Handle startup failures gracefully

```java
@Component
public class CacheWarmupRunner implements ApplicationRunner {
    @Override
    public void run(ApplicationArguments args) {
        // Warm up cache
    }
}
```

### Task 7: Create Cache Refresh Scheduler
**File**: `CacheRefreshScheduler.java`
- Scheduled task for periodic refresh
- Configurable refresh interval
- Async refresh to avoid blocking
- Error handling and alerting

```java
@Component
@EnableScheduling
public class CacheRefreshScheduler {
    @Scheduled(fixedDelayString = "${s3.policy.refresh-interval-ms:3600000}")
    public void refreshCache() {
        // Refresh logic
    }
}
```

### Task 8: Add Cache Management Endpoints
**File**: Update `EvaluationController.java`
- POST /api/v1/cache/refresh - Manual refresh
- DELETE /api/v1/cache/evict/{policyName} - Evict specific
- GET /api/v1/cache/stats - Cache statistics
- POST /api/v1/cache/warm - Re-warm cache

### Task 9: Implement Fallback Mechanism
**File**: `PolicyFallbackHandler.java`
- Local file fallback when S3 unavailable
- Circuit breaker pattern
- Health check for S3 connectivity
- Alert on fallback activation

```java
@Component
public class PolicyFallbackHandler {
    public Policy loadWithFallback(String policyName);
    public boolean isS3Available();
}
```

### Task 10: Update Application Configuration
**File**: `application.yml`
```yaml
s3:
  policy:
    enabled: true
    bucket-name: ${S3_POLICY_BUCKET:expression-evaluator-policies}
    region: ${AWS_REGION:us-east-1}
    prefix: policies/
    cache-ttl-minutes: 60
    refresh-interval-ms: 3600000
    max-retries: 3

cache:
  caffeine:
    spec: maximumSize=100,expireAfterWrite=60m

aws:
  credentials:
    use-default-provider: true  # Use IAM roles/instance profile
```

### Task 11: Create Integration Tests
**File**: `S3PolicyIntegrationTest.java`
- Use LocalStack for S3 simulation
- Test cache warming
- Test refresh mechanism
- Test fallback scenarios
- Performance testing

### Task 12: Add Monitoring & Metrics
**File**: `CacheMetricsService.java`
- Cache hit/miss rates
- S3 download times
- Cache size metrics
- Error rates
- Expose via Actuator endpoints

## Implementation Order
1. Dependencies & Configuration (Tasks 1, 2, 10)
2. Core S3 Service (Task 3)
3. Cache Implementation (Tasks 4, 5)
4. Startup & Scheduling (Tasks 6, 7)
5. Management Endpoints (Task 8)
6. Fallback & Resilience (Task 9)
7. Testing & Monitoring (Tasks 11, 12)

## Environment Variables Required
```bash
S3_POLICY_BUCKET=your-bucket-name
AWS_REGION=us-east-1
AWS_ACCESS_KEY_ID=your-access-key  # Optional if using IAM roles
AWS_SECRET_ACCESS_KEY=your-secret  # Optional if using IAM roles
```

## Benefits
1. **Centralized Policy Management** - All policies in one S3 bucket
2. **Dynamic Updates** - Change policies without redeployment
3. **Scalability** - Multiple instances share same policies
4. **Version Control** - S3 versioning for policy history
5. **Cost Effective** - Reduced memory usage with caching
6. **High Availability** - Local cache prevents S3 dependency

## Considerations
1. **Security** - Use IAM roles, encrypt S3 bucket
2. **Performance** - Initial startup time increase
3. **Network** - S3 access from deployment environment
4. **Consistency** - Cache synchronization across instances
5. **Monitoring** - Alert on S3 failures or cache issues