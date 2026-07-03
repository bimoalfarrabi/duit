# Implementation Plan — Astro Dashboard (web/)

**Versi:** v1 MVP
**Tanggal:** 2026-07-03
**Referensi:** [PRD v1](../prd/v1-mvp.md) · [Design Spec](../specs/2026-07-03-personal-finance-ecosystem-design.md)
**Depends on:** Laravel API selesai dan running

---

## Scope

Read-only dashboard web, Astro SSR, auth via Laravel Sanctum cookie, Tailwind CSS, Chart.js.

---

## Prerequisites

- Node.js 20+, npm
- Laravel API running dan accessible
- URL Laravel API tersimpan di `.env`

---

## Fase 1 — Setup Project

- [ ] `npm create astro@latest web -- --template minimal --typescript strict`
- [ ] `npm install @astrojs/node tailwindcss @astrojs/tailwind`
- [ ] Set `output: 'server'` + `adapter: node()` di `astro.config.mjs`
- [ ] Setup Tailwind: `npx astro add tailwind`
- [ ] `.env`: `API_BASE_URL=http://your-laravel-api.com`

---

## Fase 2 — Auth Middleware

File: `src/middleware/index.ts`

- [ ] Baca cookie `auth_token` dari setiap request
- [ ] Halaman publik (`/login`) — skip check
- [ ] Halaman protected — tidak ada token → redirect ke `/login`
- [ ] Simpan token di `Astro.locals.token` untuk dipakai di setiap page

```ts
// src/middleware/index.ts
import { defineMiddleware } from 'astro:middleware';

const PUBLIC_ROUTES = ['/login'];

export const onRequest = defineMiddleware(async ({ url, cookies, locals, redirect }, next) => {
  const token = cookies.get('auth_token')?.value;
  if (!PUBLIC_ROUTES.includes(url.pathname) && !token) {
    return redirect('/login');
  }
  locals.token = token ?? '';
  return next();
});
```

---

## Fase 3 — API Helper

File: `src/lib/api.ts`

- [ ] Satu fungsi `apiFetch(path, token, options?)` — wrapper fetch ke Laravel
- [ ] Base URL dari `import.meta.env.API_BASE_URL`
- [ ] Set `Authorization: Bearer {token}` header otomatis
- [ ] Return typed response atau throw error

```ts
// ponytail: simple wrapper, no retry logic needed for v1
export async function apiFetch<T>(path: string, token: string, init?: RequestInit): Promise<T> {
  const res = await fetch(`${import.meta.env.API_BASE_URL}${path}`, {
    ...init,
    headers: { 'Authorization': `Bearer ${token}`, 'Accept': 'application/json', ...init?.headers },
  });
  if (!res.ok) throw new Error(`API error ${res.status}`);
  return res.json();
}
```

---

## Fase 4 — Halaman Login

File: `src/pages/login.astro`

- [ ] Form email + password
- [ ] POST ke `/api/auth/login` via action atau fetch client-side
- [ ] Sukses → simpan token di cookie HTTP-only → redirect ke `/dashboard`
- [ ] Error → tampil pesan error

Gunakan Astro Actions (Astro 4.x) atau simple form POST ke API route.

---

## Fase 5 — Layout & Komponen Dasar

File: `src/layouts/DashboardLayout.astro`

- [ ] Sidebar navigasi: Dashboard / Transactions / Reports
- [ ] Top bar: nama user + tombol logout
- [ ] Slot untuk konten halaman
- [ ] Tailwind CSS, responsive

Komponen minimal:
- [ ] `src/components/StatCard.astro` — card KPI (label + value)
- [ ] `src/components/TransactionRow.astro` — satu baris transaksi

---

## Fase 6 — Halaman Dashboard

File: `src/pages/dashboard.astro`

- [ ] Fetch `GET /api/statistics/summary?month=&year=` → tampil 3 StatCard (pemasukan, pengeluaran, saldo)
- [ ] Fetch `GET /api/statistics/monthly?year=` → data untuk grafik batang bulanan
- [ ] Fetch `GET /api/statistics/by-category?month=&year=` → data untuk grafik donut kategori
- [ ] Fetch `GET /api/statistics/by-wallet?month=&year=` → tampil saldo per wallet (bar atau card kecil)
- [ ] Chart.js di-init via `<script>` tag, data di-inject sebagai JSON inline

```astro
---
// server-side fetch
const summary = await apiFetch('/api/statistics/summary?month=7&year=2026', token);
const monthly = await apiFetch('/api/statistics/monthly?year=2026', token);
---
<script define:vars={{ monthly }}>
  new Chart(document.getElementById('monthly-chart'), { ... monthly ... });
</script>
```

---

## Fase 7 — Halaman Transactions

File: `src/pages/transactions.astro`

- [ ] Terima query params: `?month=&year=&type=&category_id=&wallet_id=`
- [ ] Fetch `GET /api/transactions` dengan params tersebut
- [ ] Render tabel: tanggal, judul, kategori, tipe, nominal
- [ ] Filter form (simple HTML form, GET method) untuk month/year/type

---

## Fase 8 — Halaman Reports

File: `src/pages/reports.astro`

- [ ] Link download yang hit endpoint Laravel untuk export
- [ ] Tidak ada logic generate di Astro — semua delegasi ke Laravel

---

## Fase 9 — Logout

- [ ] Endpoint `src/pages/api/logout.ts` (atau Astro action)
- [ ] Hapus cookie `auth_token`
- [ ] Redirect ke `/login`

---

## Checklist Final

- [ ] `npm run build` sukses
- [ ] `/login` bisa diakses tanpa token
- [ ] `/dashboard` redirect ke `/login` kalau tidak ada token
- [ ] Data di dashboard match data dari Laravel API
- [ ] Tidak ada client-side fetch (semua data dari server-side frontmatter)
