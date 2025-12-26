import { NextRequest, NextResponse } from 'next/server';
import { s3Service } from '@/lib/s3-service';

export async function GET(
  request: NextRequest,
  { params }: { params: { name: string } }
) {
  try {
    const versions = await s3Service.getVersionHistory(params.name);
    return NextResponse.json({ versions });
  } catch (error) {
    console.error('Error fetching versions:', error);
    return NextResponse.json(
      { error: 'Failed to fetch version history' },
      { status: 500 }
    );
  }
}