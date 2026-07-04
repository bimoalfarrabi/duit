# 🖥️ Dashboard Web

## Stack

- Astro 7 (`output: 'server'`, SSR mode)
- Tailwind CSS 4
- Chart.js 4
- Node.js adapter (`@astrojs/node` standalone mode)

## Setup

```bash
npm create astro@latest web -- --template minimal --typescript strict
npm install @astrojs/node @tailwindcss/vite tailwindcss chart.js
# astro.config.mjs: output: 'server', adapter: node({ mode: 'standalone' })
```

`.env` (lokal):
```
API_BASE_URL=http://localhost:8000/api
```

`.env.production` (gitignored — set via cPanel Node.js App env vars di production):
```
API_BASE_URL=https://api.duit.viasco.my.id/api
```

> **Catatan production**: `import.meta.env.API_BASE_URL` di-bake sebagai string `"undefined"` oleh Vite saat build. Gunakan `process.env.API_BASE_URL` langsung di server-side code — Passenger (cPanel) inject env vars ke `process.env`.

## Auth Flow

```
/login → POST /api/auth/login (Laravel)
       ← token → HTTP-only cookie "auth_token" (server-side set)
Middleware → baca cookie → forward sebagai Bearer ke setiap request Laravel
Tidak ada token → redirect /login
```

## Halaman

| Route | Deskripsi |
|-------|-----------|
| `/` | Redirect ke `/dashboard` atau `/login` |
| `/login` | Form email + password |
| `/dashboard` | Ringkasan saldo, grafik bulanan, grafik kategori, saldo per wallet |
| `/transactions` | Tabel riwayat + filter bulan/tipe/wallet |
| `/reports` | Link download export CSV dari Laravel |

## Middleware (`src/middleware/index.ts`)

```ts
import { defineMiddleware } from 'astro:middleware';
const PUBLIC_ROUTES = ['/login'];

export const onRequest = defineMiddleware(async ({ url, cookies, locals, redirect }, next) => {
  const token = cookies.get('auth_token')?.value;
  if (!PUBLIC_ROUTES.includes(url.pathname) && !token) return redirect('/login');
  locals.token = token ?? '';
  return next();
});
```

## API Helper (`src/lib/api.ts`)

```ts
export async function apiFetch<T>(path: string, token: string, init?: RequestInit): Promise<T> {
  const res = await fetch(`${import.meta.env.API_BASE_URL}${path}`, {
    ...init,
    headers: { 'Authorization': `Bearer ${token}`, 'Accept': 'application/json', ...init?.headers },
  });
  if (!res.ok) throw new Error(`API error ${res.status}`);
  return res.json();
}
```

## Dashboard (`/dashboard`)

Data di-fetch server-side:
- `GET /api/statistics/summary?month=&year=` → 3 StatCard (pemasukan, pengeluaran, saldo)
- `GET /api/statistics/monthly?year=` → grafik batang 12 bulan (Chart.js)
- `GET /api/statistics/by-category?month=&year=` → grafik donut kategori (Chart.js)
- `GET /api/statistics/by-wallet?month=&year=` → card saldo per wallet

Chart.js di-init via `<script define:vars={{ data }}>` — tidak ada client-side fetch.

## Halaman Transactions (`/transactions`)

- Query params: `?month=&year=&type=&category_id=&wallet_id=`
- Filter form: HTML `<form method="GET">` — no JS required
- Tabel kolom: tanggal, judul, kategori, wallet, tipe, nominal

## Halaman Reports (`/reports`)

- Link `<a href="/api/export/transactions?month=...&format=csv" download>` → Laravel handles the file

## Logout

- Endpoint `src/pages/api/logout.ts` — hapus cookie `auth_token` → redirect `/login`

## Batasan v1

- Read-only: tidak ada form tambah/edit transaksi dari web
- Tidak ada real-time update (no WebSocket/polling)
- Tidak ada client-side state management
