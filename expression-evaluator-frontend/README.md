# Expression Evaluator Frontend

A Next.js web application for managing and editing policy rules stored in AWS S3 for the Expression Evaluator system.

## Features

- 📝 **Visual Policy Editor** - Edit YAML policies with syntax highlighting using Monaco Editor
- 🔄 **Diff Viewer** - Compare changes between original and modified versions side-by-side
- 💾 **S3 Integration** - Direct read/write to AWS S3 buckets
- 🗂️ **Version History** - Automatic backup creation with each save
- 🔍 **Policy Preview** - Visual preview of policy structure and rules
- 🚀 **Cache Management** - Evict and refresh backend cache with one click
- ✨ **Real-time Validation** - YAML syntax validation as you type

## Prerequisites

- Node.js 18+ and npm
- AWS account with S3 bucket configured
- Expression Evaluator backend running (for cache management)

## Setup

1. **Install dependencies:**
   ```bash
   npm install
   ```

2. **Configure environment variables:**
   ```bash
   cp .env.example .env
   ```

   Edit `.env` with your AWS credentials and S3 bucket details:
   ```
   AWS_REGION=us-east-1
   AWS_ACCESS_KEY_ID=your_access_key
   AWS_SECRET_ACCESS_KEY=your_secret_key
   S3_BUCKET_NAME=your-bucket-name
   NEXT_PUBLIC_API_URL=http://localhost:8080
   ```

3. **Run the development server:**
   ```bash
   npm run dev
   ```

4. **Open browser:**
   Navigate to [http://localhost:3000](http://localhost:3000)

## Usage

### Creating a New Policy

1. Click the **+** button in the sidebar
2. Enter a policy name (without .yaml extension)
3. The editor will open with a default template
4. Edit the policy and click **Save**

### Editing Existing Policies

1. Select a policy from the sidebar
2. Edit the content in the editor
3. Use view modes:
   - **Edit**: Modify the policy
   - **Diff**: Compare changes with original
   - **Preview**: See formatted policy structure
4. Click **Save** when done

### Managing Cache

After saving policies to S3:
- Click **Evict Cache** to clear the backend cache
- Click **Refresh Cache** to reload policies from S3

## Project Structure

```
expression-evaluator-frontend/
├── app/
│   ├── api/               # Next.js API routes
│   │   ├── policies/       # S3 policy operations
│   │   └── cache/          # Cache management
│   ├── layout.tsx          # Root layout
│   ├── globals.css         # Global styles
│   └── page.tsx            # Main dashboard
├── components/
│   └── PolicyEditor.tsx    # Monaco editor component
├── lib/
│   └── s3-service.ts       # AWS S3 operations
└── public/                 # Static assets
```

## API Endpoints

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/api/policies` | GET | List all policies |
| `/api/policies` | POST | Create new policy |
| `/api/policies/[name]` | GET | Get policy content |
| `/api/policies/[name]` | PUT | Update policy |
| `/api/policies/[name]/versions` | GET | Get version history |
| `/api/cache` | POST | Evict or refresh cache |

## S3 Bucket Structure

```
your-bucket/
├── policies/           # Active policies
│   ├── policy1.yaml
│   └── policy2.yaml
├── drafts/             # Draft versions
│   └── policy1-uuid.yaml
└── backups/            # Version history
    └── policy1/
        └── 2024-01-01T12-00-00.yaml
```

## Development

```bash
# Install dependencies
npm install

# Run development server
npm run dev

# Build for production
npm run build

# Start production server
npm run start

# Run linting
npm run lint
```

## Security Considerations

- Store AWS credentials securely (use IAM roles in production)
- Limit S3 bucket permissions to minimum required
- Implement authentication/authorization for production use
- Consider using AWS STS for temporary credentials
- Enable S3 versioning for additional safety

## Troubleshooting

**Policy not loading:**
- Check AWS credentials in `.env`
- Verify S3 bucket name and region
- Ensure policy exists in S3

**Save failing:**
- Check write permissions on S3 bucket
- Verify YAML syntax is valid
- Check network connectivity

**Cache operations failing:**
- Ensure backend is running
- Verify `NEXT_PUBLIC_API_URL` is correct
- Check backend cache endpoints exist

## License

MIT