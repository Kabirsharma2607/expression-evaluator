import { NextRequest, NextResponse } from 'next/server';
import axios from 'axios';

const BACKEND_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080';
const CACHE_EVICT_ENDPOINT = process.env.NEXT_PUBLIC_CACHE_EVICT_ENDPOINT || '/v1/cache/evict';
const CACHE_REFRESH_ENDPOINT = process.env.NEXT_PUBLIC_CACHE_REFRESH_ENDPOINT || '/v1/cache/refresh';

export async function POST(request: NextRequest) {
  try {
    const { action } = await request.json();

    if (action === 'evict') {
      // Call backend cache evict endpoint
      await axios.post(`${BACKEND_URL}${CACHE_EVICT_ENDPOINT}`);
      return NextResponse.json({
        success: true,
        message: 'Cache evicted successfully'
      });
    } else if (action === 'refresh') {
      // Call backend cache refresh endpoint
      await axios.post(`${BACKEND_URL}${CACHE_REFRESH_ENDPOINT}`);
      return NextResponse.json({
        success: true,
        message: 'Cache refreshed successfully'
      });
    } else {
      return NextResponse.json(
        { error: 'Invalid action. Use "evict" or "refresh"' },
        { status: 400 }
      );
    }
  } catch (error) {
    console.error('Cache operation failed:', error);

    // Extract error message from axios error if available
    const errorMessage = axios.isAxiosError(error) && error.response?.data?.message
      ? error.response.data.message
      : 'Cache operation failed';

    return NextResponse.json(
      { error: errorMessage },
      { status: 500 }
    );
  }
}