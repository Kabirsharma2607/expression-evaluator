package com.example.api.service;

import com.example.api.config.S3Properties;
import com.example.policy.model.Policy;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.stats.CacheStats;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Cache manager for policy objects using Caffeine cache
 */
@Component
@Slf4j
public class PolicyCacheManager {

    private final S3Properties s3Properties;
    private Cache<String, Policy> policyCache;
    private final Map<String, Long> lastModifiedTimes = new ConcurrentHashMap<>();

    public PolicyCacheManager(S3Properties s3Properties) {
        this.s3Properties = s3Properties;
    }

    @PostConstruct
    public void initializeCache() {
        Duration ttl = Duration.ofMinutes(s3Properties.getCacheTtlMinutes());

        this.policyCache = Caffeine.newBuilder()
                .maximumSize(1000) // Maximum number of policies to cache
                .expireAfterWrite(ttl) // TTL for cache entries
                .recordStats() // Enable statistics
                .removalListener((key, value, cause) -> {
                    log.debug("Policy cache entry removed: {} (cause: {})", key, cause);
                })
                .build();

        log.info("Policy cache initialized with TTL: {} minutes, max size: 1000",
                s3Properties.getCacheTtlMinutes());
    }

    /**
     * Store a policy in the cache
     */
    public void put(String policyName, Policy policy) {
        if (policy == null) {
            log.warn("Attempted to cache null policy for: {}", policyName);
            return;
        }

        policyCache.put(policyName, policy);
        lastModifiedTimes.put(policyName, System.currentTimeMillis());

        log.debug("Policy cached: {} (size: {} rules)", policyName,
                policy.getRules() != null ? policy.getRules().size() : 0);
    }

    /**
     * Retrieve a policy from the cache
     */
    public Optional<Policy> get(String policyName) {
        Policy policy = policyCache.getIfPresent(policyName);

        if (policy != null) {
            log.debug("Cache hit for policy: {}", policyName);
            return Optional.of(policy);
        } else {
            log.debug("Cache miss for policy: {}", policyName);
            return Optional.empty();
        }
    }

    /**
     * Check if a policy exists in the cache
     */
    public boolean contains(String policyName) {
        return policyCache.getIfPresent(policyName) != null;
    }

    /**
     * Remove a specific policy from the cache
     */
    public void evict(String policyName) {
        policyCache.invalidate(policyName);
        lastModifiedTimes.remove(policyName);
        log.info("Evicted policy from cache: {}", policyName);
    }

    /**
     * Clear all policies from the cache
     */
    public void evictAll() {
        long size = policyCache.estimatedSize();
        policyCache.invalidateAll();
        lastModifiedTimes.clear();
        log.info("Evicted all policies from cache (was {} entries)", size);
    }

    /**
     * Get all policy names currently in the cache
     */
    public Set<String> getCachedPolicyNames() {
        return policyCache.asMap().keySet();
    }

    /**
     * Get the number of policies in the cache
     */
    public long size() {
        return policyCache.estimatedSize();
    }

    /**
     * Get cache statistics
     */
    public CacheStats getStats() {
        return policyCache.stats();
    }

    /**
     * Get human-readable cache statistics
     */
    public PolicyCacheStats getCacheStatistics() {
        CacheStats stats = policyCache.stats();

        return PolicyCacheStats.builder()
                .hitCount(stats.hitCount())
                .missCount(stats.missCount())
                .hitRate(stats.hitRate())
                .evictionCount(stats.evictionCount())
                .estimatedSize(policyCache.estimatedSize())
                .averageLoadTime(stats.averageLoadPenalty())
                .build();
    }

    /**
     * Refresh a policy in the cache (mark for reload)
     */
    public void refresh(String policyName) {
        evict(policyName);
        log.info("Marked policy for refresh: {}", policyName);
    }

    /**
     * Get the last modified time of a cached policy
     */
    public Optional<Long> getLastModifiedTime(String policyName) {
        return Optional.ofNullable(lastModifiedTimes.get(policyName));
    }

    /**
     * Check if the cache is healthy (not null and operational)
     */
    public boolean isHealthy() {
        try {
            return policyCache != null && policyCache.estimatedSize() >= 0;
        } catch (Exception e) {
            log.error("Cache health check failed", e);
            return false;
        }
    }

    /**
     * Warm up the cache with a set of policy names
     */
    public void warmUp(Map<String, Policy> policies) {
        if (policies == null || policies.isEmpty()) {
            log.debug("No policies provided for cache warm-up");
            return;
        }

        long startTime = System.currentTimeMillis();
        int successCount = 0;

        for (Map.Entry<String, Policy> entry : policies.entrySet()) {
            try {
                put(entry.getKey(), entry.getValue());
                successCount++;
            } catch (Exception e) {
                log.error("Failed to warm up cache for policy: {}", entry.getKey(), e);
            }
        }

        long duration = System.currentTimeMillis() - startTime;
        log.info("Cache warm-up completed: {}/{} policies loaded in {}ms",
                successCount, policies.size(), duration);
    }

    /**
     * Clean up expired entries manually
     */
    public void cleanup() {
        policyCache.cleanUp();
        log.debug("Cache cleanup completed");
    }

    /**
     * Data class for cache statistics
     */
    public static class PolicyCacheStats {
        private final long hitCount;
        private final long missCount;
        private final double hitRate;
        private final long evictionCount;
        private final long estimatedSize;
        private final double averageLoadTime;

        private PolicyCacheStats(long hitCount, long missCount, double hitRate,
                               long evictionCount, long estimatedSize, double averageLoadTime) {
            this.hitCount = hitCount;
            this.missCount = missCount;
            this.hitRate = hitRate;
            this.evictionCount = evictionCount;
            this.estimatedSize = estimatedSize;
            this.averageLoadTime = averageLoadTime;
        }

        public static PolicyCacheStatsBuilder builder() {
            return new PolicyCacheStatsBuilder();
        }

        // Getters
        public long getHitCount() { return hitCount; }
        public long getMissCount() { return missCount; }
        public double getHitRate() { return hitRate; }
        public long getEvictionCount() { return evictionCount; }
        public long getEstimatedSize() { return estimatedSize; }
        public double getAverageLoadTime() { return averageLoadTime; }

        public static class PolicyCacheStatsBuilder {
            private long hitCount;
            private long missCount;
            private double hitRate;
            private long evictionCount;
            private long estimatedSize;
            private double averageLoadTime;

            public PolicyCacheStatsBuilder hitCount(long hitCount) {
                this.hitCount = hitCount;
                return this;
            }

            public PolicyCacheStatsBuilder missCount(long missCount) {
                this.missCount = missCount;
                return this;
            }

            public PolicyCacheStatsBuilder hitRate(double hitRate) {
                this.hitRate = hitRate;
                return this;
            }

            public PolicyCacheStatsBuilder evictionCount(long evictionCount) {
                this.evictionCount = evictionCount;
                return this;
            }

            public PolicyCacheStatsBuilder estimatedSize(long estimatedSize) {
                this.estimatedSize = estimatedSize;
                return this;
            }

            public PolicyCacheStatsBuilder averageLoadTime(double averageLoadTime) {
                this.averageLoadTime = averageLoadTime;
                return this;
            }

            public PolicyCacheStats build() {
                return new PolicyCacheStats(hitCount, missCount, hitRate,
                                          evictionCount, estimatedSize, averageLoadTime);
            }
        }
    }
}