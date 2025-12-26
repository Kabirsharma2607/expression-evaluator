# Google Cloud Run Deployment Guide

This guide walks you through deploying your Expression Evaluator application to Google Cloud Run.

## Prerequisites

1. **Google Cloud Account**: Ensure you have a Google Cloud account with billing enabled
2. **Google Cloud CLI**: Install the `gcloud` CLI tool
3. **Docker**: Install Docker Desktop on your local machine
4. **Project Setup**: Create or select a Google Cloud project

## Step 1: Setup Google Cloud CLI

```bash
# Install gcloud CLI (if not already installed)
# For macOS:
brew install google-cloud-sdk

# Authenticate with Google Cloud
gcloud auth login

# Set your project ID
export PROJECT_ID="your-project-id"
gcloud config set project $PROJECT_ID

# Enable required APIs
gcloud services enable cloudbuild.googleapis.com
gcloud services enable run.googleapis.com
gcloud services enable containerregistry.googleapis.com
```

## Step 2: Configure Environment Variables

Create a `.env.production` file for your production environment:

```bash
# S3 Configuration
S3_ENABLED=true
S3_BUCKET_NAME=your-production-s3-bucket
S3_REGION=us-east-1
S3_PREFIX=policies/
S3_USE_DEFAULT_CREDENTIALS=false
S3_ACCESS_KEY=your-access-key
S3_SECRET_KEY=your-secret-key
S3_CONNECTION_TIMEOUT_MS=30000
S3_READ_TIMEOUT_MS=30000
S3_MAX_RETRIES=3
S3_ENABLE_FALLBACK=true

# Spring Configuration
SPRING_PROFILES_ACTIVE=production
```

## Step 3: Build and Test Docker Image Locally

```bash
# Build the Docker image
docker build -t expression-evaluator .

# Test locally with docker-compose
docker-compose up -d

# Test the health endpoint
curl http://localhost:8080/v1/evaluation/health

# Stop local testing
docker-compose down
```

## Step 4: Deploy to Google Cloud Run

### Option A: Using Cloud Build (Recommended)

1. **Create cloudbuild.yaml**:

```yaml
steps:
  # Build the container image
  - name: 'gcr.io/cloud-builders/docker'
    args: ['build', '-t', 'gcr.io/$PROJECT_ID/expression-evaluator:$COMMIT_SHA', '.']

  # Push the container image to Container Registry
  - name: 'gcr.io/cloud-builders/docker'
    args: ['push', 'gcr.io/$PROJECT_ID/expression-evaluator:$COMMIT_SHA']

  # Deploy container image to Cloud Run
  - name: 'gcr.io/cloud-builders/gcloud'
    args:
    - 'run'
    - 'deploy'
    - 'expression-evaluator'
    - '--image'
    - 'gcr.io/$PROJECT_ID/expression-evaluator:$COMMIT_SHA'
    - '--region'
    - 'us-central1'
    - '--platform'
    - 'managed'
    - '--allow-unauthenticated'
    - '--port'
    - '8080'
    - '--memory'
    - '1Gi'
    - '--cpu'
    - '1'
    - '--max-instances'
    - '10'
    - '--set-env-vars'
    - 'S3_ENABLED=true,S3_BUCKET_NAME=your-bucket,S3_REGION=us-east-1'

images:
  - 'gcr.io/$PROJECT_ID/expression-evaluator:$COMMIT_SHA'
```

2. **Submit the build**:

```bash
# Submit build to Cloud Build
gcloud builds submit --tag gcr.io/$PROJECT_ID/expression-evaluator

# Or with custom cloudbuild.yaml
gcloud builds submit --config cloudbuild.yaml
```

### Option B: Direct Deployment

```bash
# Build and push to Container Registry
docker build -t gcr.io/$PROJECT_ID/expression-evaluator .
docker push gcr.io/$PROJECT_ID/expression-evaluator

# Deploy to Cloud Run
gcloud run deploy expression-evaluator \
  --image gcr.io/$PROJECT_ID/expression-evaluator \
  --region us-central1 \
  --platform managed \
  --allow-unauthenticated \
  --port 8080 \
  --memory 1Gi \
  --cpu 1 \
  --max-instances 10 \
  --set-env-vars "S3_ENABLED=true,S3_BUCKET_NAME=your-bucket,S3_REGION=us-east-1,S3_ACCESS_KEY=your-key,S3_SECRET_KEY=your-secret"
```

## Step 5: Configure Environment Variables (Secure Method)

For production, use Secret Manager for sensitive data:

```bash
# Create secrets in Secret Manager
gcloud secrets create s3-access-key --data-file=- <<< "your-access-key"
gcloud secrets create s3-secret-key --data-file=- <<< "your-secret-key"

# Update Cloud Run service to use secrets
gcloud run services update expression-evaluator \
  --region us-central1 \
  --update-env-vars "S3_ENABLED=true,S3_BUCKET_NAME=your-bucket,S3_REGION=us-east-1" \
  --update-secrets "S3_ACCESS_KEY=s3-access-key:latest,S3_SECRET_KEY=s3-secret-key:latest"
```

## Step 6: Set Up Custom Domain (Optional)

```bash
# Map a custom domain
gcloud run domain-mappings create \
  --service expression-evaluator \
  --domain api.yourdomain.com \
  --region us-central1
```

## Step 7: Configure IAM and Security

```bash
# Create a service account for the application
gcloud iam service-accounts create expression-evaluator-sa \
  --display-name "Expression Evaluator Service Account"

# Grant necessary permissions (if using GCP services)
gcloud projects add-iam-policy-binding $PROJECT_ID \
  --member "serviceAccount:expression-evaluator-sa@$PROJECT_ID.iam.gserviceaccount.com" \
  --role "roles/secretmanager.secretAccessor"

# Update Cloud Run to use the service account
gcloud run services update expression-evaluator \
  --region us-central1 \
  --service-account expression-evaluator-sa@$PROJECT_ID.iam.gserviceaccount.com
```

## Step 8: Monitor and Scale

```bash
# View logs
gcloud run logs read expression-evaluator --region us-central1

# Update scaling settings
gcloud run services update expression-evaluator \
  --region us-central1 \
  --min-instances 1 \
  --max-instances 20 \
  --concurrency 100
```

## Testing Your Deployment

```bash
# Get the service URL
SERVICE_URL=$(gcloud run services describe expression-evaluator --region us-central1 --format 'value(status.url)')

# Test health endpoint
curl $SERVICE_URL/v1/evaluation/health

# Test policy evaluation
curl -X POST $SERVICE_URL/v1/evaluation/policy \
  -H "Content-Type: application/json" \
  -d '{
    "policyName": "creditScoreEvaluation",
    "featureMap": {
      "credit_score": 750,
      "annual_income": 80000,
      "employment_length": 5
    }
  }'
```

## Cost Optimization Tips

1. **Use minimum instances wisely**: Set min-instances to 0 for development, 1+ for production
2. **Right-size resources**: Start with 1 CPU and 1Gi memory, adjust based on load
3. **Enable CPU allocation**: Use `--cpu-boost` for better cold start performance
4. **Monitor usage**: Use Cloud Monitoring to track resource utilization

## Troubleshooting

### Common Issues:

1. **Container fails to start**:
   ```bash
   gcloud run logs read expression-evaluator --region us-central1 --limit 50
   ```

2. **S3 connection issues**:
   - Verify AWS credentials in environment variables
   - Check S3 bucket permissions and region settings

3. **Memory/CPU issues**:
   - Increase memory allocation: `--memory 2Gi`
   - Increase CPU: `--cpu 2`

4. **Timeout issues**:
   - Increase request timeout: `--timeout 300`

## Continuous Deployment

Set up a CI/CD pipeline using GitHub Actions or Cloud Build triggers:

```yaml
# .github/workflows/deploy.yml
name: Deploy to Cloud Run
on:
  push:
    branches: [main]

jobs:
  deploy:
    runs-on: ubuntu-latest
    steps:
    - uses: actions/checkout@v2
    - uses: google-github-actions/setup-gcloud@v0
      with:
        service_account_key: ${{ secrets.GCP_SA_KEY }}
        project_id: ${{ secrets.GCP_PROJECT_ID }}
    - run: gcloud builds submit --tag gcr.io/${{ secrets.GCP_PROJECT_ID }}/expression-evaluator
```

Your Expression Evaluator is now ready for production on Google Cloud Run! 🚀