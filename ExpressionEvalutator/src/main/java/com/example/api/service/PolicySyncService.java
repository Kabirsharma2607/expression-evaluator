package com.example.api.service;

import com.example.api.config.S3Properties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.ApplicationArguments;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

/**
 * Downloads all policies from S3 and writes them to local resources/policies folder on startup
 */
@Component
@RequiredArgsConstructor
@Slf4j
@Order(1) // Run early in startup process
public class PolicySyncService implements ApplicationRunner {

    private final S3PolicyService s3PolicyService;
    private final S3Properties s3Properties;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        if (!s3Properties.isEnabled()) {
            log.info("S3 is disabled, skipping policy sync from S3 to local files");
            return;
        }

        log.info("Starting policy sync from S3 to local files...");
        long startTime = System.currentTimeMillis();

        try {
            // Get all policy files from S3
            List<String> policyNames = s3PolicyService.listPolicyFiles();

            if (policyNames.isEmpty()) {
                log.warn("No policy files found in S3");
                return;
            }

            log.info("Found {} policy files in S3 to sync to local files", policyNames.size());

            // Ensure the policies directory exists
            Path policiesDir = Paths.get("policies");
            if (!Files.exists(policiesDir)) {
                Files.createDirectories(policiesDir);
                log.info("Created policies directory: {}", policiesDir.toAbsolutePath());
            }

            int syncedCount = 0;
            int errorCount = 0;

            // Download each policy and write to local file
            for (String policyName : policyNames) {
                try {
                    Optional<String> content = s3PolicyService.downloadPolicy(policyName);
                    if (content.isPresent()) {
                        writeLocalPolicyFile(policyName, content.get());
                        syncedCount++;
                        log.debug("Synced policy to local file: {}.yaml", policyName);
                    } else {
                        log.warn("Policy content not found in S3: {}", policyName);
                        errorCount++;
                    }
                } catch (Exception e) {
                    log.error("Failed to sync policy from S3: {}", policyName, e);
                    errorCount++;
                }
            }

            long duration = System.currentTimeMillis() - startTime;

            log.info("Policy sync completed in {}ms: {}/{} policies synced to local files",
                    duration, syncedCount, policyNames.size());

            if (errorCount > 0) {
                log.warn("Encountered {} errors during policy sync", errorCount);
            }

        } catch (Exception e) {
            log.error("Failed to complete policy sync", e);
            throw new RuntimeException("Policy sync failed", e);
        }
    }

    /**
     * Write policy content to local file in policies directory
     */
    private void writeLocalPolicyFile(String policyName, String content) throws IOException {
        Path filePath = Paths.get("policies", policyName + ".yaml");

        try (FileWriter writer = new FileWriter(filePath.toFile())) {
            writer.write(content);
        }

        log.debug("Written policy to local file: {} ({} bytes)", filePath, content.length());
    }
}