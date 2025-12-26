import { NextRequest, NextResponse } from 'next/server';
import { s3Service } from '@/lib/s3-service';

export async function GET(
  request: NextRequest,
  { params }: { params: { name: string } }
) {
  try {
    const content = await s3Service.getPolicy(params.name);
    return NextResponse.json({ content });
  } catch (error) {
    console.error('Error fetching policy:', error);
    return NextResponse.json(
      { error: `Failed to fetch policy ${params.name}` },
      { status: 404 }
    );
  }
}

export async function PUT(
  request: NextRequest,
  { params }: { params: { name: string } }
) {
  try {
    const { content, isDraft } = await request.json();

    if (!content) {
      return NextResponse.json(
        { error: 'Content is required' },
        { status: 400 }
      );
    }

    await s3Service.savePolicy(params.name, content, isDraft);

    return NextResponse.json({
      success: true,
      message: `Policy ${params.name} saved successfully`
    });
  } catch (error) {
    console.error('Error saving policy:', error);
    return NextResponse.json(
      { error: 'Failed to save policy' },
      { status: 500 }
    );
  }
}