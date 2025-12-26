#!/bin/bash

# S3 Configuration
export S3_POLICY_ENABLED=true
export S3_POLICY_BUCKET=expression-evaluator-policies  # Replace with your actual bucket name
export AWS_REGION=ap-south-1

# AWS Credentials - REPLACE WITH YOUR ACTUAL KEYS
export AWS_ACCESS_KEY_ID=YOUR_ACCESS_KEY_HERE          # Replace with your Access Key ID
export AWS_SECRET_ACCESS_KEY=YOUR_SECRET_ACCESS_KEY    # Replace with your Secret Access Key

# Optional: Override other S3 settings
# export S3_POLICY_PREFIX=policies/
# export S3_CACHE_TTL_MINUTES=60
# export S3_REFRESH_INTERVAL_MS=3600000

echo "Starting application with S3 integration..."
echo "Bucket: $S3_POLICY_BUCKET"
echo "Region: $AWS_REGION"
echo "S3 Enabled: $S3_POLICY_ENABLED"

# Run the application
JAVA_HOME=$(/usr/libexec/java_home -v17) ./gradlew bootRun