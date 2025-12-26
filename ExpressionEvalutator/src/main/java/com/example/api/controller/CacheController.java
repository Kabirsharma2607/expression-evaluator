package com.example.api.controller;

import com.example.api.service.CacheRefreshScheduler;
import com.example.api.service.PolicyCacheManager;
import com.example.api.service.S3PolicyLoader;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * REST controller for cache management operations
 */
@RestController
@RequestMapping("/v1/cache")
@Slf4j
@Tag(name = "Cache Management API", description = "Endpoints for managing policy cache")
public class CacheController {

    private final PolicyCacheManager cacheManager;
    private final S3PolicyLoader s3PolicyLoader;

    @Autowired(required = false)
    private CacheRefreshScheduler refreshScheduler;

    public CacheController(PolicyCacheManager cacheManager, S3PolicyLoader s3PolicyLoader) {
        this.cacheManager = cacheManager;
        this.s3PolicyLoader = s3PolicyLoader;
    }

    @Operation(summary = "Get cache statistics",
               description = "Returns current cache performance statistics")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Cache statistics retrieved")
    })
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getCacheStats() {
        log.info("Getting cache statistics");

        var stats = cacheManager.getCacheStatistics();

        Map<String, Object> response = new HashMap<>();
        response.put("hitCount", stats.getHitCount());
        response.put("missCount", stats.getMissCount());
        response.put("hitRate", String.format("%.2f%%", stats.getHitRate() * 100));
        response.put("evictionCount", stats.getEvictionCount());
        response.put("estimatedSize", stats.getEstimatedSize());
        response.put("averageLoadTimeNs", stats.getAverageLoadTime());
        response.put("cachedPolicyNames", cacheManager.getCachedPolicyNames());
        response.put("isHealthy", cacheManager.isHealthy());
        response.put("timestamp", Instant.now());

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Refresh all policies",
               description = "Refreshes all policies from S3 and updates the cache")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Cache refresh initiated"),
        @ApiResponse(responseCode = "500", description = "Cache refresh failed")
    })
    @PostMapping("/refresh")
    public ResponseEntity<Map<String, Object>> refreshCache() {
        log.info("Manual cache refresh requested");

        try {
            long startTime = System.currentTimeMillis();

            // Get stats before refresh
            var beforeStats = cacheManager.getCacheStatistics();
            long sizeBefore = beforeStats.getEstimatedSize();

            // Refresh cache
            s3PolicyLoader.refreshAllPolicies();

            // Get stats after refresh
            var afterStats = cacheManager.getCacheStatistics();
            long sizeAfter = afterStats.getEstimatedSize();
            long duration = System.currentTimeMillis() - startTime;

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Cache refreshed successfully");
            response.put("durationMs", duration);
            response.put("sizeBefore", sizeBefore);
            response.put("sizeAfter", sizeAfter);
            response.put("timestamp", Instant.now());

            log.info("Manual cache refresh completed in {}ms. Size: {} -> {}", duration, sizeBefore, sizeAfter);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Manual cache refresh failed", e);

            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Cache refresh failed: " + e.getMessage());
            response.put("timestamp", Instant.now());

            return ResponseEntity.status(500).body(response);
        }
    }

    @Operation(summary = "Refresh cache asynchronously",
               description = "Initiates asynchronous cache refresh and returns immediately")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "202", description = "Async refresh initiated"),
        @ApiResponse(responseCode = "500", description = "Failed to initiate refresh")
    })
    @PostMapping("/refresh/async")
    public ResponseEntity<Map<String, Object>> refreshCacheAsync() {
        log.info("Async cache refresh requested");

        if (refreshScheduler == null) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Async refresh not available - S3 integration disabled");
            response.put("timestamp", Instant.now());
            return ResponseEntity.status(503).body(response);
        }

        try {
            CompletableFuture<Void> future = refreshScheduler.refreshCacheAsync();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Async cache refresh initiated");
            response.put("timestamp", Instant.now());

            return ResponseEntity.accepted().body(response);

        } catch (Exception e) {
            log.error("Failed to initiate async cache refresh", e);

            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to initiate async refresh: " + e.getMessage());
            response.put("timestamp", Instant.now());

            return ResponseEntity.status(500).body(response);
        }
    }

    @Operation(summary = "Refresh specific policy",
               description = "Refreshes a specific policy from S3")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Policy refreshed successfully"),
        @ApiResponse(responseCode = "404", description = "Policy not found"),
        @ApiResponse(responseCode = "500", description = "Policy refresh failed")
    })
    @PostMapping("/refresh/{policyName}")
    public ResponseEntity<Map<String, Object>> refreshPolicy(@PathVariable String policyName) {
        log.info("Manual policy refresh requested: {}", policyName);

        try {
            long startTime = System.currentTimeMillis();

            // Check if policy exists before refresh
            boolean existedBefore = cacheManager.contains(policyName);

            // Refresh the specific policy
            s3PolicyLoader.refreshPolicy(policyName);

            long duration = System.currentTimeMillis() - startTime;

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Policy refreshed successfully");
            response.put("policyName", policyName);
            response.put("existedBefore", existedBefore);
            response.put("durationMs", duration);
            response.put("timestamp", Instant.now());

            log.info("Policy refresh completed for {} in {}ms", policyName, duration);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Policy refresh failed for: {}", policyName, e);

            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Policy refresh failed: " + e.getMessage());
            response.put("policyName", policyName);
            response.put("timestamp", Instant.now());

            return ResponseEntity.status(500).body(response);
        }
    }

    @Operation(summary = "Evict specific policy",
               description = "Removes a specific policy from the cache")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Policy evicted successfully"),
        @ApiResponse(responseCode = "404", description = "Policy not found in cache")
    })
    @DeleteMapping("/evict/{policyName}")
    public ResponseEntity<Map<String, Object>> evictPolicy(@PathVariable String policyName) {
        log.info("Policy eviction requested: {}", policyName);

        boolean existed = cacheManager.contains(policyName);
        cacheManager.evict(policyName);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", existed ? "Policy evicted successfully" : "Policy was not in cache");
        response.put("policyName", policyName);
        response.put("existedInCache", existed);
        response.put("timestamp", Instant.now());

        return existed ? ResponseEntity.ok(response) : ResponseEntity.status(404).body(response);
    }

    @Operation(summary = "Evict all policies",
               description = "Clears the entire policy cache")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Cache cleared successfully")
    })
    @DeleteMapping("/evict")
    public ResponseEntity<Map<String, Object>> evictAll() {
        log.info("Cache clear requested");

        long sizeBefore = cacheManager.size();
        cacheManager.evictAll();

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Cache cleared successfully");
        response.put("entriesRemoved", sizeBefore);
        response.put("timestamp", Instant.now());

        log.info("Cache cleared: {} entries removed", sizeBefore);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Warm up cache",
               description = "Pre-loads all available policies into the cache")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Cache warm-up completed"),
        @ApiResponse(responseCode = "500", description = "Cache warm-up failed")
    })
    @PostMapping("/warmup")
    public ResponseEntity<Map<String, Object>> warmUpCache() {
        log.info("Cache warm-up requested");

        try {
            long startTime = System.currentTimeMillis();
            long sizeBefore = cacheManager.size();

            s3PolicyLoader.warmUpCache();

            long sizeAfter = cacheManager.size();
            long duration = System.currentTimeMillis() - startTime;

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Cache warm-up completed");
            response.put("durationMs", duration);
            response.put("sizeBefore", sizeBefore);
            response.put("sizeAfter", sizeAfter);
            response.put("policiesLoaded", sizeAfter - sizeBefore);
            response.put("timestamp", Instant.now());

            log.info("Cache warm-up completed in {}ms. Size: {} -> {}", duration, sizeBefore, sizeAfter);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Cache warm-up failed", e);

            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Cache warm-up failed: " + e.getMessage());
            response.put("timestamp", Instant.now());

            return ResponseEntity.status(500).body(response);
        }
    }

    @Operation(summary = "Cache health check",
               description = "Checks the health status of the cache and S3 connectivity")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Health status retrieved")
    })
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> getCacheHealth() {
        log.debug("Cache health check requested");

        boolean cacheHealthy = cacheManager.isHealthy();
        boolean s3LoaderHealthy = s3PolicyLoader.isHealthy();
        long cacheSize = cacheManager.size();
        var stats = cacheManager.getCacheStatistics();

        Map<String, Object> response = new HashMap<>();
        response.put("cacheHealthy", cacheHealthy);
        response.put("s3LoaderHealthy", s3LoaderHealthy);
        response.put("overallHealthy", cacheHealthy && s3LoaderHealthy);
        response.put("cacheSize", cacheSize);
        response.put("hitRate", String.format("%.2f%%", stats.getHitRate() * 100));
        response.put("timestamp", Instant.now());

        int statusCode = (cacheHealthy && s3LoaderHealthy) ? 200 : 503;
        return ResponseEntity.status(statusCode).body(response);
    }

    @Operation(summary = "Force cache cleanup",
               description = "Manually triggers cache cleanup to remove expired entries")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Cache cleanup completed")
    })
    @PostMapping("/cleanup")
    public ResponseEntity<Map<String, Object>> cleanupCache() {
        log.info("Manual cache cleanup requested");

        long sizeBefore = cacheManager.size();
        cacheManager.cleanup();
        long sizeAfter = cacheManager.size();

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Cache cleanup completed");
        response.put("sizeBefore", sizeBefore);
        response.put("sizeAfter", sizeAfter);
        response.put("entriesRemoved", sizeBefore - sizeAfter);
        response.put("timestamp", Instant.now());

        log.info("Cache cleanup completed. Size: {} -> {}", sizeBefore, sizeAfter);
        return ResponseEntity.ok(response);
    }
}