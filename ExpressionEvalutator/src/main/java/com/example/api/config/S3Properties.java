package com.example.api.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuration properties for S3 policy storage
 */
@Data
@Component
@ConfigurationProperties(prefix = "s3.policy")
public class S3Properties {

    /**
     * Whether S3 integration is enabled
     */
    private boolean enabled = true;

    /**
     * S3 bucket name containing policy files
     */
    private String bucketName = "expression-evaluator-policies";

    /**
     * AWS region for S3 bucket
     */
    private String region = "ap-south-1";

    /**
     * Prefix/folder path for policy files in S3
     */
    private String prefix = "policies/";

    /**
     * Cache TTL in minutes
     */
    private Integer cacheTtlMinutes = 60;

    /**
     * Refresh interval for periodic cache updates (in milliseconds)
     */
    private Long refreshIntervalMs = 3600000L; // 1 hour

    /**
     * Maximum retry attempts for S3 operations
     */
    private Integer maxRetries = 3;

    /**
     * Connection timeout in milliseconds
     */
    private Integer connectionTimeoutMs = 5000;

    /**
     * Read timeout in milliseconds
     */
    private Integer readTimeoutMs = 10000;

    /**
     * Whether to use default AWS credential provider chain
     */
    private boolean useDefaultCredentials = true;

    /**
     * AWS access key (optional - prefer IAM roles)
     */
    private String accessKey;

    /**
     * AWS secret key (optional - prefer IAM roles)
     */
    private String secretKey;

    /**
     * Whether to enable fallback to local files on S3 failure
     */
    private boolean enableFallback = true;

    /**
     * Local fallback directory path
     */
    private String fallbackPath = "classpath:policies/";
}