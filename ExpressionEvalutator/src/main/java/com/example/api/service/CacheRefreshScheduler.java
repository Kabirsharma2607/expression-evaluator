package com.example.api.service;

import com.example.api.config.S3Properties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

/**
 * Scheduler for periodic cache refresh operations
 */
@Component
@EnableScheduling
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(value = "s3.policy.enabled", havingValue = "true", matchIfMissing = false)
public class CacheRefreshScheduler {

    private final S3PolicyLoader s3PolicyLoader;
    private final PolicyCacheManager cacheManager;
    private final S3Properties s3Properties;

    /**
     * Scheduled task to refresh all policies periodically
     * Default: every hour (3600000ms)
     */
    @Scheduled(fixedDelayString = "${s3.policy.refresh-interval-ms:3600000}")
    @Async
    public void scheduledRefresh() {
        if (!s3Properties.isEnabled()) {
            log.debug("S3 integration is disabled, skipping scheduled refresh");
            return;
        }

        log.info("Starting scheduled policy cache refresh");

        try {
            long startTime = System.currentTimeMillis();

            // Get current cache stats before refresh
            var beforeStats = cacheManager.getCacheStatistics();
            long sizeBefore = beforeStats.getEstimatedSize();

            // Refresh all policies
            s3PolicyLoader.refreshAllPolicies();

            // Get stats after refresh
            var afterStats = cacheManager.getCacheStatistics();
            long sizeAfter = afterStats.getEstimatedSize();
            long duration = System.currentTimeMillis() - startTime;

            log.info("Scheduled cache refresh completed in {}ms. Cache size: {} -> {} policies",
                    duration, sizeBefore, sizeAfter);

            // Log cache performance
            if (afterStats.getHitRate() > 0) {
                log.info("Cache hit rate: {:.2f}%, total hits: {}, total misses: {}",
                        afterStats.getHitRate() * 100, afterStats.getHitCount(), afterStats.getMissCount());
            }

        } catch (Exception e) {
            log.error("Scheduled cache refresh failed", e);

            // Check if we should attempt fallback refresh
            if (s3Properties.isEnableFallback()) {
                log.info("Attempting fallback refresh due to S3 failure");
                attemptFallbackRefresh();
            }
        }
    }

    /**
     * Cleanup expired cache entries
     * Runs more frequently than full refresh
     */
    @Scheduled(fixedDelayString = "${s3.policy.cleanup-interval-ms:300000}") // 5 minutes
    public void scheduledCleanup() {
        try {
            log.debug("Running scheduled cache cleanup");
            cacheManager.cleanup();

            // Log cache stats after cleanup
            var stats = cacheManager.getCacheStatistics();
            if (stats.getEvictionCount() > 0) {
                log.debug("Cache cleanup completed. Evictions: {}, Current size: {}",
                        stats.getEvictionCount(), stats.getEstimatedSize());
            }

        } catch (Exception e) {
            log.error("Scheduled cache cleanup failed", e);
        }
    }

    /**
     * Health check for cache and S3 connectivity
     */
    @Scheduled(fixedDelayString = "${s3.policy.health-check-interval-ms:600000}") // 10 minutes
    public void scheduledHealthCheck() {
        try {
            log.debug("Running scheduled health check");

            boolean isHealthy = s3PolicyLoader.isHealthy();
            long cacheSize = cacheManager.size();

            if (!isHealthy) {
                log.warn("Health check failed - S3PolicyLoader is not healthy");
            }

            if (cacheSize == 0) {
                log.warn("Health check warning - Cache is empty, attempting to warm up");
                warmUpCacheAsync();
            }

            log.debug("Health check completed. Healthy: {}, Cache size: {}", isHealthy, cacheSize);

        } catch (Exception e) {
            log.error("Scheduled health check failed", e);
        }
    }

    /**
     * Refresh cache in the background (async)
     */
    @Async
    public CompletableFuture<Void> refreshCacheAsync() {
        return CompletableFuture.runAsync(() -> {
            try {
                log.info("Starting async cache refresh");
                s3PolicyLoader.refreshAllPolicies();
                log.info("Async cache refresh completed");
            } catch (Exception e) {
                log.error("Async cache refresh failed", e);
                throw new RuntimeException("Async cache refresh failed", e);
            }
        });
    }

    /**
     * Warm up cache in the background (async)
     */
    @Async
    public CompletableFuture<Void> warmUpCacheAsync() {
        return CompletableFuture.runAsync(() -> {
            try {
                log.info("Starting async cache warm-up");
                s3PolicyLoader.warmUpCache();
                log.info("Async cache warm-up completed");
            } catch (Exception e) {
                log.error("Async cache warm-up failed", e);
                throw new RuntimeException("Async cache warm-up failed", e);
            }
        });
    }

    /**
     * Refresh a specific policy asynchronously
     */
    @Async
    public CompletableFuture<Void> refreshPolicyAsync(String policyName) {
        return CompletableFuture.runAsync(() -> {
            try {
                log.info("Starting async refresh for policy: {}", policyName);
                s3PolicyLoader.refreshPolicy(policyName);
                log.info("Async refresh completed for policy: {}", policyName);
            } catch (Exception e) {
                log.error("Async refresh failed for policy: {}", policyName, e);
                throw new RuntimeException("Async refresh failed for policy: " + policyName, e);
            }
        });
    }

    /**
     * Attempt fallback refresh when S3 fails
     */
    private void attemptFallbackRefresh() {
        try {
            log.info("Attempting fallback refresh");

            // Clear current cache
            cacheManager.evictAll();

            // Try to reload from local files
            String[] fallbackPolicies = {"fc1Rules", "accessControlRules", "alertRules"};
            int loadedCount = 0;

            for (String policyName : fallbackPolicies) {
                try {
                    if (s3PolicyLoader.policyExists(policyName)) {
                        s3PolicyLoader.loadPolicy(policyName);
                        loadedCount++;
                        log.debug("Loaded fallback policy: {}", policyName);
                    }
                } catch (Exception e) {
                    log.debug("Could not load fallback policy: {} - {}", policyName, e.getMessage());
                }
            }

            if (loadedCount > 0) {
                log.info("Fallback refresh completed: {} policies loaded", loadedCount);
            } else {
                log.warn("Fallback refresh failed: no policies could be loaded");
            }

        } catch (Exception e) {
            log.error("Fallback refresh failed", e);
        }
    }

    /**
     * Get next scheduled refresh time (for monitoring)
     */
    public long getNextRefreshTime() {
        return System.currentTimeMillis() + s3Properties.getRefreshIntervalMs();
    }

    /**
     * Force immediate refresh (for manual triggers)
     */
    public void forceRefresh() {
        log.info("Force refresh triggered");
        scheduledRefresh();
    }
}