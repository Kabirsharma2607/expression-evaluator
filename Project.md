# Expression Evaluator System - Complete Project Documentation

## 📌 Project Overview

The **Expression Evaluator System** is an enterprise-grade, rule-based policy engine that enables organizations to define, manage, and execute complex business logic without code deployment. It provides a complete solution for dynamic rule evaluation with version control, allowing non-technical users to safely manage critical business rules through an intuitive web interface.

### Core Value Proposition
- **No-Code Rule Management**: Business analysts can create and modify rules without developer intervention
- **Version Control**: Complete history of all changes with instant rollback capabilities
- **Real-Time Evaluation**: Millisecond-level rule execution for high-performance applications
- **Enterprise Safety**: Automatic backups, change tracking, and validation before deployment

## 🎯 What It Does

The system evaluates complex conditional expressions against dynamic data (feature maps) to make business decisions. It supports:

1. **Dynamic Rule Evaluation**: Execute business rules defined in YAML against runtime data
2. **Complex Logic Support**: Handle nested conditions, dependencies, and multi-rule policies
3. **Version Management**: Track, compare, and restore any previous version of policies
4. **Visual Editing**: Web-based editor with syntax highlighting and real-time validation
5. **Change Safety**: Automatic backups, diff viewing, and rollback capabilities

### Example Use Case
```yaml
# Fraud Detection Policy
policyName: "fraudDetection"
version: "2.3"
rules:
  highRiskTransaction:
    expression: "amount > 10000 && user.accountAge < 30"
    description: "Flag high-value transactions from new accounts"

  velocityCheck:
    expression: "transactionCount > 5 && timeWindow < 3600"
    description: "Detect rapid transaction velocity"

  fraudAlert:
    expression: "highRiskTransaction || velocityCheck"
    dependencies: ["highRiskTransaction", "velocityCheck"]
```

## 🏗️ System Architecture

### High-Level Architecture
```
┌──────────────────────────────────────────────────────────────────┐
│                         CLIENT LAYER                              │
│  ┌────────────────────────────────────────────────────────────┐  │
│  │             Next.js Web Application (Port 3000)            │  │
│  │  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐ │  │
│  │  │  Editor  │  │   Diff   │  │ Version  │  │  Policy  │ │  │
│  │  │  (Monaco)│  │  Viewer  │  │ History  │  │   List   │ │  │
│  │  └──────────┘  └──────────┘  └──────────┘  └──────────┘ │  │
│  └────────────────────────────────────────────────────────────┘  │
└───────────────────────────────┬──────────────────────────────────┘
                                │ REST API
┌───────────────────────────────▼──────────────────────────────────┐
│                         SERVICE LAYER                             │
│  ┌────────────────────────────────────────────────────────────┐  │
│  │          Spring Boot Application (Port 8080)               │  │
│  │  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐ │  │
│  │  │  Policy  │  │Expression│  │  Cache   │  │   REST   │ │  │
│  │  │ Executor │  │Evaluator │  │ Manager  │  │Controller│ │  │
│  │  └──────────┘  └──────────┘  └──────────┘  └──────────┘ │  │
│  └────────────────────────────────────────────────────────────┘  │
└───────────────────────────────┬──────────────────────────────────┘
                                │
┌───────────────────────────────▼──────────────────────────────────┐
│                         STORAGE LAYER                             │
│  ┌────────────────────────────────────────────────────────────┐  │
│  │                    AWS S3 Bucket                           │  │
│  │  ┌──────────────────────┐  ┌────────────────────────────┐ │  │
│  │  │   policies/          │  │   backups/                 │ │  │
│  │  │   ├── policy1.yml    │  │   ├── policy1/            │ │  │
│  │  │   ├── policy2.yml    │  │   │   ├── 2024-12-26...   │ │  │
│  │  │   └── policy3.yml    │  │   │   ├── 2024-12-25...   │ │  │
│  │  │                      │  │   │   └── 2024-12-24...   │ │  │
│  │  └──────────────────────┘  └────────────────────────────┘ │  │
│  └────────────────────────────────────────────────────────────┘  │
└───────────────────────────────────────────────────────────────────┘
```

### Component Details

#### Backend (Spring Boot)
- **PolicyExecutor**: Orchestrates policy execution with dependency resolution
- **ExpressionEvaluator**: Parses and evaluates expressions using operator framework
- **PolicyLoader**: Loads policies from S3 with caching and validation
- **OperatorFactory**: Creates appropriate operators (arithmetic, logical, comparison, string)
- **S3PolicyService**: Manages S3 operations with versioning support

#### Frontend (Next.js)
- **PolicyEditor**: Monaco editor with YAML syntax highlighting
- **DiffViewer**: Side-by-side comparison of versions
- **VersionHistory**: Browse and restore previous versions
- **CacheManager**: Trigger cache refresh/eviction

## 🚀 Features

### 1. Version Management System
```
Feature: Complete Version Control
├── Automatic Versioning
│   └── Every save creates timestamped backup
├── Version History
│   ├── Browse all previous versions
│   ├── View metadata (timestamp, size, author)
│   └── Sort by date/name
├── Comparison Tools
│   ├── Side-by-side diff viewer
│   ├── Inline change highlighting
│   └── Before/after preview
└── Recovery Options
    ├── One-click restore
    ├── Download specific version
    └── Rollback to any point in time
```

### 2. Expression Language Support
| Feature | Operators/Functions | Example |
|---------|-------------------|---------|
| **Logical** | AND (&&), OR (\|\|), NOT (!) | `isActive && !isBlocked` |
| **Comparison** | <, >, <=, >=, ==, != | `age >= 18 && score > 75` |
| **Arithmetic** | +, -, *, /, % | `price * quantity * (1 - discount)` |
| **String** | contains, startsWith, endsWith | `email.contains('@company.com')` |
| **Null Safety** | ?., ?? | `user?.address?.city ?? 'Unknown'` |
| **Collections** | in, any, all | `status in ['ACTIVE', 'PENDING']` |

### 3. Policy Management
- **Dependency Resolution**: Automatic topological sorting of rule dependencies
- **Circular Dependency Detection**: Prevents infinite loops
- **Dry Run Mode**: Test policies without affecting production
- **Batch Evaluation**: Process multiple feature maps efficiently

### 4. Safety Features
- **Syntax Validation**: Real-time YAML and expression validation
- **Type Checking**: Ensure type compatibility in expressions
- **Backup Creation**: Automatic backup before every change
- **Change Approval**: Optional workflow for production changes
- **Audit Trail**: Complete history of who changed what and when

## 📦 Installation & Setup

### Prerequisites
```bash
# Backend Requirements
- Java 17+
- Maven 3.6+
- AWS CLI configured

# Frontend Requirements
- Node.js 18+
- npm 8+

# Infrastructure
- AWS Account
- S3 Bucket with versioning enabled
```

### Quick Start

#### 1. Backend Setup
```bash
# Clone and navigate to backend
cd /Users/kabirsharma/IdeaProjects/ExpressionEvaluator

# Set environment variables
export AWS_ACCESS_KEY_ID=your-key-id
export AWS_SECRET_ACCESS_KEY=your-secret-key
export AWS_REGION=us-east-1
export S3_BUCKET_NAME=expression-evaluator-policies

# Build and run
mvn clean install
mvn spring-boot:run

# Or use the convenience script
./run-with-s3.sh
```

#### 2. Frontend Setup
```bash
# Navigate to frontend
cd /Users/kabirsharma/IdeaProjects/expression-evaluator-frontend

# Install dependencies
npm install

# Configure environment
cp .env.example .env
# Edit .env with your settings:
# AWS_REGION=us-east-1
# AWS_ACCESS_KEY_ID=your-key
# AWS_SECRET_ACCESS_KEY=your-secret
# S3_BUCKET_NAME=your-bucket
# NEXT_PUBLIC_API_URL=http://localhost:8080

# Start development server
npm run dev
```

#### 3. Access the Application
- **Web Interface**: http://localhost:3000
- **API Endpoint**: http://localhost:8080
- **Health Check**: http://localhost:8080/actuator/health

## 📖 How to Use

### Via Web Interface

#### Creating a New Policy
1. Open http://localhost:3000
2. Click the **+** button in the sidebar
3. Enter policy name (without .yml extension)
4. Edit the template in the Monaco editor
5. Use the preview tab to verify structure
6. Click **Save** to deploy to S3

#### Editing Existing Policies
1. Select policy from the sidebar
2. Make changes in the editor
3. View changes in **Diff** mode
4. Orange dot indicates unsaved changes
5. Click **Save** when ready

#### Managing Versions
1. **View History**: Click version history icon
2. **Compare Versions**: Select two versions to diff
3. **Restore Version**: Click restore on any previous version
4. **Download Version**: Export specific version locally

### Via API

#### Evaluate a Policy
```bash
curl -X POST http://localhost:8080/api/evaluate \
  -H "Content-Type: application/json" \
  -d '{
    "policyName": "fraudDetection",
    "featureMap": {
      "amount": 15000,
      "user": {
        "accountAge": 25
      },
      "transactionCount": 7,
      "timeWindow": 3000
    }
  }'
```

#### Response Format
```json
{
  "policyName": "fraudDetection",
  "success": true,
  "executionTimeMs": 15,
  "ruleResults": {
    "highRiskTransaction": {
      "result": true,
      "success": true,
      "executionTimeMs": 3
    },
    "velocityCheck": {
      "result": true,
      "success": true,
      "executionTimeMs": 2
    },
    "fraudAlert": {
      "result": true,
      "success": true,
      "executionTimeMs": 1
    }
  }
}
```

## 🔄 Version Management Deep Dive

### How Versions Are Stored
```
s3://your-bucket/
├── policies/                    # Current active policies
│   ├── fraud-detection.yml      # Latest version
│   ├── access-control.yml
│   └── pricing-rules.yml
│
└── backups/                     # All historical versions
    ├── fraud-detection/
    │   ├── 2024-12-26T10-30-45-123Z.yml  # Timestamp format
    │   ├── 2024-12-25T14-22-10-456Z.yml  # ISO-8601 compatible
    │   └── 2024-12-24T09-15-30-789Z.yml
    │
    └── access-control/
        ├── 2024-12-26T11-45-20-321Z.yml
        └── 2024-12-25T16-30-15-654Z.yml
```

### Version Operations

#### Automatic Backup Process
```javascript
// Every save triggers this flow:
1. Read current policy from S3
2. Copy to backups/policyName/timestamp.yml
3. Save new version to policies/policyName.yml
4. Update metadata (author, timestamp, checksum)
5. Invalidate cache
```

#### Restore Process
```javascript
// Restoring a version:
1. Select version from history
2. System creates backup of current
3. Copy selected version to active
4. Refresh cache
5. Log restoration event
```

### Version Metadata
Each version maintains:
- **Timestamp**: When the version was created
- **Author**: Who made the changes (when auth implemented)
- **Size**: File size in bytes
- **Checksum**: MD5 hash for integrity
- **Parent Version**: Previous version reference
- **Change Summary**: Auto-generated or manual description

### Application Security
1. **Input Validation**: All expressions are validated before execution
5. **Encryption**: Use S3 encryption at rest and TLS in transit

## 🛠️ Configuration

### Backend Configuration (`application.yml`)
```yaml
spring:
  application:
    name: expression-evaluator

server:
  port: 8080

aws:
  s3:
    bucket: ${S3_BUCKET_NAME}
    region: ${AWS_REGION:us-east-1}

policy:
  cache:
    enabled: true
    ttl: 300000  # 5 minutes
    size: 100    # Max policies in cache

  evaluation:
    timeout: 5000  # 5 seconds max per evaluation
    maxDepth: 10   # Max nested rule depth

  versioning:
    enabled: true
    maxVersions: 50  # Keep last 50 versions
    compression: true
```

### Frontend Configuration (`.env`)
```env
# AWS Configuration
AWS_REGION=us-east-1
AWS_ACCESS_KEY_ID=AKIAXXXXXXXXXXXXXX
AWS_SECRET_ACCESS_KEY=xxxxxxxxxxxxxxxxxxxxxxxx
S3_BUCKET_NAME=expression-evaluator-policies

# API Configuration
NEXT_PUBLIC_API_URL=http://localhost:8080
NEXT_PUBLIC_CACHE_EVICT_ENDPOINT=/v1/cache/evict
NEXT_PUBLIC_CACHE_REFRESH_ENDPOINT=/v1/cache/refresh

# Feature Flags
NEXT_PUBLIC_ENABLE_VERSIONING=true
NEXT_PUBLIC_ENABLE_DIFF_VIEW=true
NEXT_PUBLIC_AUTO_SAVE=false
NEXT_PUBLIC_MAX_FILE_SIZE=1048576  # 1MB
```

## 🐳 Docker Deployment

### Docker Compose Setup
```yaml
version: '3.8'

services:
  backend:
    build:
      context: ./ExpressionEvaluator
      dockerfile: Dockerfile
    ports:
      - "8080:8080"
    environment:
      - AWS_ACCESS_KEY_ID=${AWS_ACCESS_KEY_ID}
      - AWS_SECRET_ACCESS_KEY=${AWS_SECRET_ACCESS_KEY}
      - AWS_REGION=${AWS_REGION}
      - S3_BUCKET_NAME=${S3_BUCKET_NAME}
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8080/actuator/health"]
      interval: 30s
      timeout: 10s
      retries: 3

  frontend:
    build:
      context: ./expression-evaluator-frontend
      dockerfile: Dockerfile
    ports:
      - "3000:3000"
    environment:
      - NEXT_PUBLIC_API_URL=http://backend:8080
    depends_on:
      backend:
        condition: service_healthy
    volumes:
      - ./expression-evaluator-frontend:/app
      - /app/node_modules
      - /app/.next

  # Optional: Local S3 for development
  localstack:
    image: localstack/localstack:latest
    ports:
      - "4566:4566"
    environment:
      - SERVICES=s3
      - DEBUG=1
      - DATA_DIR=/tmp/localstack/data
    volumes:
      - "./localstack-data:/tmp/localstack"
```

### Production Deployment
```bash
# Build images
docker build -t expression-evaluator-backend ./ExpressionEvaluator
docker build -t expression-evaluator-frontend ./expression-evaluator-frontend

# Run with production config
docker run -d \
  --name evaluator-backend \
  -p 8080:8080 \
  --env-file .env.prod \
  expression-evaluator-backend

docker run -d \
  --name evaluator-frontend \
  -p 3000:3000 \
  --env-file .env.prod \
  expression-evaluator-frontend
```

## 📊 Monitoring & Observability

### Health Endpoints
- `/actuator/health` - Overall system health
- `/actuator/metrics` - Performance metrics
- `/api/cache/stats` - Cache statistics
- `/api/policies/stats` - Policy usage statistics

## 🔄 Maintenance & Operations

### Backup Strategy
```bash
# Daily backup script
#!/bin/bash
aws s3 sync s3://your-bucket/policies/ ./backups/daily/$(date +%Y%m%d)/
aws s3 sync s3://your-bucket/backups/ ./backups/versions/
```

### Cache Management
```bash
# Clear cache
curl -X POST http://localhost:8080/api/cache/evict

# Refresh cache
curl -X POST http://localhost:8080/api/cache/refresh

# View cache stats
curl http://localhost:8080/api/cache/stats
```

### Version Cleanup
```bash
# Remove versions older than 90 days
aws s3 rm s3://your-bucket/backups/ \
  --recursive \
  --exclude "*" \
  --include "*.yml" \
  --storage-class GLACIER \
  --older-than 90
```

### Debug Mode
```bash
# Enable debug logging
export LOGGING_LEVEL_COM_EXAMPLE=DEBUG
export LOGGING_LEVEL_ROOT=INFO

# View real-time logs
tail -f logs/application.log
```

## 🎯 Use Cases

### 1. Financial Services
- **Fraud Detection**: Real-time transaction monitoring
- **Credit Scoring**: Dynamic risk assessment
- **Compliance Rules**: Regulatory requirement enforcement

### 2. E-Commerce
- **Pricing Rules**: Dynamic pricing strategies
- **Discount Eligibility**: Promotional rule management
- **Inventory Management**: Stock allocation rules

### 3. Healthcare
- **Clinical Decisions**: Treatment protocol selection
- **Insurance Claims**: Automated claim validation
- **Patient Routing**: Care pathway determination

### 4. Access Control
- **Authorization**: Dynamic permission evaluation
- **Feature Flags**: Gradual feature rollout
- **Rate Limiting**: Usage quota enforcement

## 📚 Additional Resources

- **API Documentation**: http://localhost:8080/swagger-ui.html (when implemented)
---

**Built with Spring Boot, Next.js, AWS S3, and Monaco Editor**

*Version 1.0.0 | Last Updated: December 2024*