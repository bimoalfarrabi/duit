export async function apiFetch<T>(path: string, token: string, init?: RequestInit): Promise<T> {
  const base = process.env.API_BASE_URL;
  const res = await fetch(`${base}${path}`, {
    ...init,
    headers: {
      'Authorization': `Bearer ${token}`,
      'Accept': 'application/json',
      'Content-Type': 'application/json',
      ...init?.headers,
    },
  });
  if (!res.ok) throw new Error(`API error ${res.status}`);
  return res.json();
}
