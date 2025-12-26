package com.example.api.service;

import com.example.api.config.S3Properties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.core.exception.SdkServiceException;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service for interacting with S3 to manage policy files
 */
@Service
@Slf4j
public class S3PolicyService {

    private final S3Properties s3Properties;
    private S3Client s3Client;

    public S3PolicyService(S3Properties s3Properties) {
        this.s3Properties = s3Properties;
    }

    @PostConstruct
    public void initializeS3Client() {
        if (!s3Properties.isEnabled()) {
            log.info("S3 integration is disabled");
            return;
        }

        try {
            var clientBuilder = S3Client.builder()
                    .region(Region.of(s3Properties.getRegion()))
                    .overrideConfiguration(config -> config
                            .apiCallTimeout(Duration.ofMillis(s3Properties.getConnectionTimeoutMs()))
                            .apiCallAttemptTimeout(Duration.ofMillis(s3Properties.getReadTimeoutMs()))
                            .retryPolicy(retryPolicy -> retryPolicy.numRetries(s3Properties.getMaxRetries())));

            if (s3Properties.isUseDefaultCredentials()) {
                clientBuilder.credentialsProvider(DefaultCredentialsProvider.create());
                log.info("Using default AWS credentials provider");
            } else if (s3Properties.getAccessKey() != null && s3Properties.getSecretKey() != null) {
                AwsCredentials credentials = AwsBasicCredentials.create(
                        s3Properties.getAccessKey(),
                        s3Properties.getSecretKey()
                );
                clientBuilder.credentialsProvider(StaticCredentialsProvider.create(credentials));
                log.info("Using static AWS credentials");
            } else {
                throw new IllegalArgumentException(
                    "Either enable default credentials or provide access key and secret key"
                );
            }

            s3Client = clientBuilder.build();
            log.info("S3 client initialized for bucket: {} in region: {}",
                    s3Properties.getBucketName(), s3Properties.getRegion());

            // Test connection
            testS3Connection();

        } catch (Exception e) {
            log.error("Failed to initialize S3 client", e);
            if (!s3Properties.isEnableFallback()) {
                throw new RuntimeException("S3 initialization failed and fallback is disabled", e);
            }
            log.warn("S3 initialization failed, fallback to local files is enabled");
        }
    }

    @PreDestroy
    public void closeS3Client() {
        if (s3Client != null) {
            try {
                s3Client.close();
                log.info("S3 client closed");
            } catch (Exception e) {
                log.warn("Error closing S3 client", e);
            }
        }
    }

    /**
     * Download a policy file from S3
     */
    public Optional<String> downloadPolicy(String policyName) {
        if (!isS3Available()) {
            log.warn("S3 is not available, cannot download policy: {}", policyName);
            return Optional.empty();
        }

        // Try both .yml and .yaml extensions
        String[] extensions = {".yml", ".yaml"};

        for (String extension : extensions) {
            String key = s3Properties.getPrefix() + policyName + extension;

            try {
                log.debug("Downloading policy from S3: s3://{}/{}", s3Properties.getBucketName(), key);

                GetObjectRequest request = GetObjectRequest.builder()
                        .bucket(s3Properties.getBucketName())
                        .key(key)
                        .build();

                ResponseInputStream<GetObjectResponse> response = s3Client.getObject(request);
                String content = new String(response.readAllBytes(), StandardCharsets.UTF_8);

                log.info("Successfully downloaded policy: {} ({} bytes)", policyName, content.length());
                return Optional.of(content);

            } catch (NoSuchKeyException e) {
                log.debug("Policy file not found with extension {}: {}", extension, key);
                // Continue to try next extension
            } catch (SdkServiceException e) {
                log.error("AWS service error downloading policy {}: {}", policyName, e.getMessage());
                return Optional.empty();
            } catch (SdkClientException e) {
                log.error("AWS client error downloading policy {}: {}", policyName, e.getMessage());
                return Optional.empty();
            } catch (IOException e) {
                log.error("I/O error reading policy {}: {}", policyName, e.getMessage());
                return Optional.empty();
            } catch (Exception e) {
                log.error("Unexpected error downloading policy {}", policyName, e);
                return Optional.empty();
            }
        }

        log.warn("Policy file not found in S3 with any supported extension: {}", policyName);
        return Optional.empty();
    }

    /**
     * List all policy files in the S3 bucket
     */
    public List<String> listPolicyFiles() {
        if (!isS3Available()) {
            log.warn("S3 is not available, cannot list policy files");
            return List.of();
        }

        try {
            log.debug("Listing policy files from S3 bucket: {}", s3Properties.getBucketName());

            ListObjectsV2Request request = ListObjectsV2Request.builder()
                    .bucket(s3Properties.getBucketName())
                    .prefix(s3Properties.getPrefix())
                    .build();

            ListObjectsV2Response response = s3Client.listObjectsV2(request);

            List<String> policyNames = response.contents().stream()
                    .map(S3Object::key)
                    .filter(key -> key.endsWith(".yml") || key.endsWith(".yaml"))
                    .map(key -> {
                        // Remove prefix and extension
                        String fileName = key.substring(s3Properties.getPrefix().length());
                        return fileName.replaceAll("\\.(yml|yaml)$", "");
                    })
                    .collect(Collectors.toList());

            log.info("Found {} policy files in S3", policyNames.size());
            return policyNames;

        } catch (SdkServiceException e) {
            log.error("AWS service error listing policies: {}", e.getMessage());
            return List.of();
        } catch (SdkClientException e) {
            log.error("AWS client error listing policies: {}", e.getMessage());
            return List.of();
        } catch (Exception e) {
            log.error("Unexpected error listing policies", e);
            return List.of();
        }
    }

    /**
     * Check if a specific policy exists in S3
     */
    public boolean isPolicyExists(String policyName) {
        if (!isS3Available()) {
            return false;
        }

        String key = s3Properties.getPrefix() + policyName + ".yml";

        try {
            HeadObjectRequest request = HeadObjectRequest.builder()
                    .bucket(s3Properties.getBucketName())
                    .key(key)
                    .build();

            s3Client.headObject(request);
            return true;

        } catch (NoSuchKeyException e) {
            return false;
        } catch (Exception e) {
            log.error("Error checking if policy exists: {}", policyName, e);
            return false;
        }
    }

    /**
     * Check if S3 is available and configured
     */
    public boolean isS3Available() {
        return s3Properties.isEnabled() && s3Client != null;
    }

    /**
     * Test S3 connection by listing bucket contents
     */
    private void testS3Connection() {
        try {
            log.debug("Testing S3 connection to bucket: {}", s3Properties.getBucketName());

            ListObjectsV2Request request = ListObjectsV2Request.builder()
                    .bucket(s3Properties.getBucketName())
                    .maxKeys(1)
                    .build();

            s3Client.listObjectsV2(request);
            log.info("S3 connection test successful");

        } catch (NoSuchBucketException e) {
            throw new RuntimeException("S3 bucket does not exist: " + s3Properties.getBucketName(), e);
        } catch (Exception e) {
            throw new RuntimeException("S3 connection test failed", e);
        }
    }

    /**
     * Get S3 client for advanced operations (used internally)
     */
    protected S3Client getS3Client() {
        return s3Client;
    }
}