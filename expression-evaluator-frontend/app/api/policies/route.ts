import { NextRequest, NextResponse } from 'next/server';
import { s3Service } from '@/lib/s3-service';

export async function GET(request: NextRequest) {
  try {
      console.log("Reaching here")
    const policies = await s3Service.listPolicies();
      console.log(policies)
    return NextResponse.json({ policies });
  } catch (error) {
      console.log("Reaching here 3")
    console.error('Error fetching policies:', error);
    return NextResponse.json(
      { error: 'Failed to fetch policies' },
      { status: 500 }
    );
  }
}

export async function POST(request: NextRequest) {
  try {
    const { policyName, template } = await request.json();

    if (!policyName) {
      return NextResponse.json(
        { error: 'Policy name is required' },
        { status: 400 }
      );
    }

    await s3Service.createNewPolicy(policyName, template);

    return NextResponse.json({
      success: true,
      message: `Policy ${policyName} created successfully`
    });
  } catch (error) {
    console.error('Error creating policy:', error);
    return NextResponse.json(
      { error: 'Failed to create policy' },
      { status: 500 }
    );
  }
}