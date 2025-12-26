package com.example.api.service;

import com.example.api.config.S3Properties;
import com.example.policy.PolicyLoader;
import com.example.policy.model.Policy;
import com.example.policy.model.Rule;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Enhanced PolicyLoader that fetches policies from S3 with caching and fallback support
 */
@Component
@Primary
@Slf4j
public class S3PolicyLoader extends PolicyLoader {

    private final S3PolicyService s3PolicyService;
    private final PolicyCacheManager cacheManager;
    private final S3Properties s3Properties;
    private final ObjectMapper yamlMapper;

    public S3PolicyLoader(S3PolicyService s3PolicyService,
                         PolicyCacheManager cacheManager,
                         S3Properties s3Properties) {
        super();
        this.s3PolicyService = s3PolicyService;
        this.cacheManager = cacheManager;
        this.s3Properties = s3Properties;
        this.yamlMapper = new ObjectMapper(new YAMLFactory());
    }

    /**
     * Enhanced loadPolicy that checks cache first, then S3, then falls back to local
     */
    @Override
    public Policy loadPolicy(String policyName) {
        log.debug("Loading policy: {}", policyName);

        // 1. Check cache first
        Optional<Policy> cachedPolicy = cacheManager.get(policyName);
        if (cachedPolicy.isPresent()) {
            log.debug("Policy found in cache: {}", policyName);
            return cachedPolicy.get();
        }

        // 2. Try loading from S3
        if (s3Properties.isEnabled()) {
            try {
                Policy policy = loadFromS3(policyName);
                if (policy != null) {
                    // Cache the policy
                    cacheManager.put(policyName, policy);
                    log.info("Policy loaded from S3 and cached: {}", policyName);
                    return policy;
                }
            } catch (Exception e) {
                log.error("Failed to load policy from S3: {}", policyName, e);
                if (!s3Properties.isEnableFallback()) {
                    throw new PolicyLoadException("Failed to load policy from S3 and fallback is disabled", e);
                }
                log.warn("Falling back to local file for policy: {}", policyName);
            }
        }

        // 3. Fallback to local file
        try {
            Policy policy = loadFromLocal(policyName);
            if (policy != null) {
                // Cache the policy
                cacheManager.put(policyName, policy);
                log.info("Policy loaded from local fallback and cached: {}", policyName);
                return policy;
            }
        } catch (Exception e) {
            log.error("Failed to load policy from local fallback: {}", policyName, e);
        }

        throw new PolicyNotFoundException("Policy not found in S3 or local: " + policyName);
    }

    /**
     * Load policy from S3
     */
    private Policy loadFromS3(String policyName) {
        try {

        Optional<String> content = s3PolicyService.downloadPolicy(policyName);
        if (content.isEmpty()) {
            log.debug("Policy not found in S3: {}", policyName);
            return null;
        }
        writeLocalPolicyFile(policyName, content.get());

        return parseYamlContent(content.get(), policyName, "S3");
        }catch (Exception e){
            log.error(e.getMessage());
            return null;
        }
    }

    /**
     * Load policy from local resources (fallback)
     */
    private Policy loadFromLocal(String policyName) {
        String resourcePath = "/policies/" + policyName + ".yaml";

        try (InputStream inputStream = getClass().getResourceAsStream(resourcePath)) {
            if (inputStream == null) {
                log.debug("Policy not found in local resources: {}", resourcePath);
                return null;
            }

            String content = new String(inputStream.readAllBytes());
            return parseYamlContent(content, policyName, "local");

        } catch (Exception e) {
            throw new PolicyLoadException("Failed to load policy from local: " + resourcePath, e);
        }
    }

    /**
     * Parse YAML content into Policy object
     */
    private Policy parseYamlContent(String content, String policyName, String source) {
        try {
            Policy policy = yamlMapper.readValue(new StringReader(content), Policy.class);

            // Set policy name if not set in YAML
            if (policy.getName() == null || policy.getName().trim().isEmpty()) {
                policy.setName(policyName);
            }

            // Set rule names based on YAML keys
            if (policy.getRules() != null) {
                for (Map.Entry<String, Rule> entry : policy.getRules().entrySet()) {
                    entry.getValue().setName(entry.getKey());
                }
            }

            // Validate policy
            validatePolicy(policy);

            log.debug("Policy parsed successfully from {}: {} ({} rules)",
                    source, policyName, policy.getRules().size());

            return policy;

        } catch (Exception e) {
            throw new PolicyLoadException("Failed to parse policy YAML from " + source + ": " + policyName, e);
        }
    }

    /**
     * Refresh a specific policy (evict from cache and reload)
     */
    public void refreshPolicy(String policyName) {
        log.info("Refreshing policy: {}", policyName);

        // Remove from cache
        cacheManager.evict(policyName);

        // Reload the policy (this will fetch from S3 and cache again)
        try {
            loadPolicy(policyName);
            log.info("Policy refreshed successfully: {}", policyName);
        } catch (Exception e) {
            log.error("Failed to refresh policy: {}", policyName, e);
            throw e;
        }
    }

    /**
     * Refresh all policies
     */
    public void refreshAllPolicies() {
        log.info("Refreshing all policies");

        long startTime = System.currentTimeMillis();
        int successCount = 0;
        int errorCount = 0;

        // Get list of policies from S3
        List<String> policyNames = s3PolicyService.listPolicyFiles();

        if (policyNames.isEmpty()) {
            log.warn("No policy files found in S3");
            return;
        }

        // Clear cache
        cacheManager.evictAll();

        // Reload each policy
        for (String policyName : policyNames) {
            try {
                loadPolicy(policyName);
                loadFromS3(policyName);
                successCount++;
                log.debug("Refreshed policy: {}", policyName);
            } catch (Exception e) {
                errorCount++;
                log.error("Failed to refresh policy: {}", policyName, e);
            }
        }

        long duration = System.currentTimeMillis() - startTime;
        log.info("Policy refresh completed: {}/{} policies refreshed in {}ms (errors: {})",
                successCount, policyNames.size(), duration, errorCount);
    }

    /**
     * Get list of available policies (from S3 or cache)
     */
    public List<String> getAvailablePolicies() {
        if (s3PolicyService.isS3Available()) {
            return s3PolicyService.listPolicyFiles();
        } else {
            log.warn("S3 not available, returning cached policy names");
            return cacheManager.getCachedPolicyNames().stream().toList();
        }
    }

    /**
     * Check if a policy exists (in S3 or cache)
     */
    public boolean policyExists(String policyName) {
        // Check cache first
        if (cacheManager.contains(policyName)) {
            return true;
        }

        // Check S3
        if (s3PolicyService.isS3Available()) {
            return s3PolicyService.isPolicyExists(policyName);
        }

        // Check local as fallback
        String resourcePath = "/policies/" + policyName + ".yaml";
        return getClass().getResourceAsStream(resourcePath) != null;
    }

    /**
     * Warm up the cache with all available policies
     */
    public void warmUpCache() {
        log.info("Warming up policy cache");

        long startTime = System.currentTimeMillis();
        int loadedCount = 0;
        int errorCount = 0;

        List<String> policyNames = getAvailablePolicies();

        if (policyNames.isEmpty()) {
            log.warn("No policies found to warm up cache");
            return;
        }

        for (String policyName : policyNames) {
            try {
                // This will load from S3 and cache
                loadPolicy(policyName);
                loadedCount++;
                log.debug("Cache warmed for policy: {}", policyName);
            } catch (Exception e) {
                errorCount++;
                log.error("Failed to warm cache for policy: {}", policyName, e);
            }
        }

        long duration = System.currentTimeMillis() - startTime;
        log.info("Cache warm-up completed: {}/{} policies loaded in {}ms (errors: {})",
                loadedCount, policyNames.size(), duration, errorCount);
    }

    /**
     * Get cache statistics
     */
    public PolicyCacheManager.PolicyCacheStats getCacheStats() {
        return cacheManager.getCacheStatistics();
    }

    /**
     * Check health of S3 and cache
     */
    public boolean isHealthy() {
        return cacheManager.isHealthy() &&
               (s3PolicyService.isS3Available() || s3Properties.isEnableFallback());
    }

    private void writeLocalPolicyFile(String policyName, String content) throws IOException {
        Path filePath = Paths.get("src/main/resources/policies", policyName + ".yaml");

        try (FileWriter writer = new FileWriter(filePath.toFile())) {
            writer.write(content);
        }

        log.debug("Written policy to local file: {} ({} bytes)", filePath, content.length());
    }
}