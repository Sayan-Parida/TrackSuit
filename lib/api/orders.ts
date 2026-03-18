export interface ApiOrder {
  id: number;
  platform: string;
  productName: string;
  deliveryDate: string;
  status: string;
}

const API_BASE_URL = process.env.NEXT_PUBLIC_API_BASE_URL ?? 'http://localhost:8080';

export async function fetchOrders(): Promise<ApiOrder[]> {
  const response = await fetch(`${API_BASE_URL}/api/v1/orders`, {
    method: 'GET',
    headers: {
      Accept: 'application/json',
    },
    cache: 'no-store',
  });

  if (!response.ok) {
    throw new Error(`Failed to fetch orders. Status: ${response.status}`);
  }

  return response.json();
}