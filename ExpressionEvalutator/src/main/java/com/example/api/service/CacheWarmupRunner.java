package com.example.api.service;

import com.example.api.config.S3Properties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Warms up the policy cache on application startup
 */
@Component
@RequiredArgsConstructor
@Slf4j
@Order(1) // Run early in startup process
public class CacheWarmupRunner implements ApplicationRunner {

    private final S3PolicyLoader s3PolicyLoader;
    private final S3Properties s3Properties;
    private final PolicyCacheManager cacheManager;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        if (!s3Properties.isEnabled()) {
            log.info("S3 integration is disabled, skipping cache warm-up");
            return;
        }

        log.info("Starting policy cache warm-up...");

        try {
            long startTime = System.currentTimeMillis();

            // Warm up the cache
            s3PolicyLoader.warmUpCache();

            long duration = System.currentTimeMillis() - startTime;
            long cacheSize = cacheManager.size();

            log.info("Cache warm-up completed successfully in {}ms. Cache size: {} policies", duration, cacheSize);

            // Log cache statistics
            var stats = cacheManager.getCacheStatistics();
            log.info("Initial cache stats - Hit rate: {:.2f}%, Size: {}",
                    stats.getHitRate() * 100, stats.getEstimatedSize());

        } catch (Exception e) {
            log.error("Cache warm-up failed", e);

            // Check if fallback is enabled
            if (s3Properties.isEnableFallback()) {
                log.warn("Cache warm-up failed but fallback is enabled, application will continue");
                // Try to load at least one policy to test fallback
                tryFallbackWarmup();
            } else {
                log.error("Cache warm-up failed and fallback is disabled, this may cause issues");
                // Don't fail the application startup, but log the error
            }
        }
    }

    /**
     * Try to warm up cache with local fallback policies
     */
    private void tryFallbackWarmup() {
        try {
            log.info("Attempting fallback cache warm-up with local policies");

            // Try to load some common policy names that might exist locally
            String[] commonPolicyNames = {"fc1Rules", "accessControlRules", "alertRules"};

            int loadedCount = 0;
            for (String policyName : commonPolicyNames) {
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
                log.info("Fallback warm-up completed: {} policies loaded", loadedCount);
            } else {
                log.warn("No fallback policies could be loaded");
            }

        } catch (Exception e) {
            log.error("Fallback warm-up also failed", e);
        }
    }
}