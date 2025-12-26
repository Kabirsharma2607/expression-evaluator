import {
  S3Client,
  GetObjectCommand,
  PutObjectCommand,
  ListObjectsV2Command,
  CopyObjectCommand,
  DeleteObjectCommand
} from '@aws-sdk/client-s3';

export class S3Service {
  private client: S3Client;
  private bucketName: string;

  constructor() {
    this.client = new S3Client({
      region: process.env.AWS_REGION || 'us-east-1',
      credentials: {
        accessKeyId: process.env.AWS_ACCESS_KEY_ID!,
        secretAccessKey: process.env.AWS_SECRET_ACCESS_KEY!,
      },
    });
    this.bucketName = process.env.S3_BUCKET_NAME || 'expression-evaluator-policies';
  }

  async listPolicies(prefix: string = 'policies/'): Promise<string[]> {
    try {
      const command = new ListObjectsV2Command({
        Bucket: this.bucketName,
        Prefix: prefix,
      });

      const response = await this.client.send(command);
      const files = response.Contents?.map(item => item.Key!) || [];

      // Filter to only .yml files and extract names
      return files
        .filter(key => key.endsWith('.yml'))
        .map(key => key.replace(prefix, '').replace('.yml', ''));
    } catch (error) {
      console.error('Error listing policies:', error);
      throw error;
    }
  }

  async getPolicy(policyName: string): Promise<string> {
    try {
      const key = `policies/${policyName}.yml`;
      const command = new GetObjectCommand({
        Bucket: this.bucketName,
        Key: key,
      });

      const response = await this.client.send(command);
      const body = await response.Body?.transformToString();

      if (!body) {
        throw new Error(`Policy ${policyName} not found`);
      }

      return body;
    } catch (error) {
      console.error('Error getting policy:', error);
      throw error;
    }
  }

  async savePolicy(policyName: string, content: string, isDraft: boolean = false): Promise<void> {
    try {
      const prefix = isDraft ? 'drafts/' : 'policies/';
      const key = `${prefix}${policyName}.yml`;

      // Create backup of existing policy
      if (!isDraft) {
        await this.createBackup(policyName);
      }

      const command = new PutObjectCommand({
        Bucket: this.bucketName,
        Key: key,
        Body: content,
        ContentType: 'text/plain',
        Metadata: {
          'updated-by': 'expression-evaluator-frontend',
          'updated-at': new Date().toISOString(),
        },
      });

      await this.client.send(command);
    } catch (error) {
      console.error('Error saving policy:', error);
      throw error;
    }
  }

  async createBackup(policyName: string): Promise<void> {
    try {
      const sourceKey = `policies/${policyName}.yml`;
      const timestamp = new Date().toISOString().replace(/[:.]/g, '-');
      const backupKey = `backups/${policyName}/${timestamp}.yml`;

      const command = new CopyObjectCommand({
        Bucket: this.bucketName,
        CopySource: `${this.bucketName}/${sourceKey}`,
        Key: backupKey,
      });

      await this.client.send(command);
    } catch (error) {
      console.error('Error creating backup:', error);
      // Don't throw error for backup failure, just log it
    }
  }

  async getVersionHistory(policyName: string): Promise<Array<{
    version: string;
    timestamp: string;
  }>> {
    try {
      const command = new ListObjectsV2Command({
        Bucket: this.bucketName,
        Prefix: `backups/${policyName}/`,
      });

      const response = await this.client.send(command);
      const versions = response.Contents?.map(item => {
        const timestamp = item.Key!.split('/').pop()!.replace('.yml', '');
        return {
          version: item.Key!,
          timestamp: timestamp,
          lastModified: item.LastModified,
        };
      }) || [];

      return versions.sort((a, b) =>
        (b.lastModified?.getTime() || 0) - (a.lastModified?.getTime() || 0)
      );
    } catch (error) {
      console.error('Error getting version history:', error);
      return [];
    }
  }

  async getBackupContent(backupKey: string): Promise<string> {
    try {
      const command = new GetObjectCommand({
        Bucket: this.bucketName,
        Key: backupKey,
      });

      const response = await this.client.send(command);
      const body = await response.Body?.transformToString();

      if (!body) {
        throw new Error(`Backup ${backupKey} not found`);
      }

      return body;
    } catch (error) {
      console.error('Error getting backup:', error);
      throw error;
    }
  }

  async createNewPolicy(policyName: string, template?: string): Promise<void> {
    const defaultTemplate = `policyName: "${policyName}"
description: "New policy created from web interface"
version: "1.0"
rules:
  exampleRule:
    expression: "featureMap.value > 0"
    description: "Example rule - modify as needed"
    dependencies: []
`;

    await this.savePolicy(policyName, template || defaultTemplate);
  }
}

export const s3Service = new S3Service();