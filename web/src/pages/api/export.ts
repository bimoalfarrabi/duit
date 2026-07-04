import type { APIRoute } from 'astro';

export const GET: APIRoute = async ({ request, locals }) => {
  const url = new URL(request.url);
  const month = url.searchParams.get('month') ?? '';
  const year = url.searchParams.get('year') ?? '';
  const apiUrl = `${import.meta.env.API_BASE_URL}/export/transactions?month=${month}&year=${year}`;
  const res = await fetch(apiUrl, {
    headers: { 'Authorization': `Bearer ${locals.token}`, 'Accept': 'text/csv' },
  });
  const csv = await res.text();
  return new Response(csv, {
    headers: {
      'Content-Type': 'text/csv',
      'Content-Disposition': `attachment; filename="transaksi-${year}-${month}.csv"`,
    },
  });
};
