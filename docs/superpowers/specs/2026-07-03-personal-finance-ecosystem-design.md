# Personal Finance Ecosystem — Design Spec

**Date:** 2026-07-03
**Status:** Approved
**Scope:** v1 MVP — Laravel API + Astro Dashboard + Android App

---

## 1. Overview

Aplikasi pencatatan keuangan personal dengan arsitektur 3 komponen:
- **Laravel** — REST API, single source of truth
- **Astro** — Dashboard web, read-only, SSR
- **Android** — Input utama (Kotlin + Jetpack Compose)

Tujuan: catat transaksi dengan cepat, support multi-user, berjalan di shared hosting.

---

## 2. Repository Structure

Monorepo tunggal:

```
personal-finance/
├── backend/        # Laravel REST API
├── web/            # Astro SSR dashboard
├── android/        # Kotlin + Jetpack Compose
└── README.md
```

Satu repo, tiga proyek independen. Deploy masing-masing terpisah.

---

## 3. Arsitektur & Data Flow

```
Android App
    │ REST API (Bearer token, JSON)
    ▼
Laravel API ──── MySQL
    ▲
    │ REST API (Bearer token via HTTP-only cookie)
Astro SSR Dashboard
    │
    ▼
Browser (read-only)
```

**Prinsip:**
- Laravel adalah satu-satunya yang akses database
- Astro tidak pernah langsung ke MySQL
- Semua kalkulasi (statistik, saldo) dilakukan di Laravel
- Tidak ada duplikasi business logic di klien

---

## 4. Laravel API (backend/)

### Tech Stack
- Laravel 11, PHP 8.2+
- MySQL
- Laravel Sanctum (auth)
- Shared hosting compatible — tidak ada queue worker, tidak ada Redis

### Database Schema

```sql
users
  id, name, email, password, timestamps

categories
  id, user_id, name, type ENUM('income','expense'), color, icon, timestamps

transactions
  id, user_id, category_id, title, amount, type ENUM('income','expense'),
  date, note, timestamps
```

### API Endpoints (v1)

```
Auth
  POST   /api/auth/register
  POST   /api/auth/login
  POST   /api/auth/logout
  GET    /api/auth/me

Categories
  GET    /api/categories
  POST   /api/categories
  PUT    /api/categories/{id}
  DELETE /api/categories/{id}

Transactions
  GET    /api/transactions          ?month=&year=&type=&category_id=
  POST   /api/transactions
  PUT    /api/transactions/{id}
  DELETE /api/transactions/{id}

Statistics
  GET    /api/statistics/summary    ?month=&year=   → { income, expense, balance } bulan itu
  GET    /api/statistics/by-category?month=&year=   → breakdown per kategori
  GET    /api/statistics/monthly    ?year=           → array 12 bulan { month, income, expense }
```

### Auth Strategy
- Android: Bearer token di Authorization header
- Astro: Bearer token di-wrap dalam HTTP-only cookie, di-forward server-side
- Token tidak punya expiry tetap di v1 — logout hapus token dari DB (Sanctum default)

### Hosting Constraints
- Shared hosting: semua request sinkron
- Tidak ada job queue, tidak ada scheduled tasks di v1
- Export laporan: generate langsung saat request (cukup untuk v1)

---

## 5. Astro Dashboard (web/)

### Tech Stack
- Astro 4+, `output: 'server'` (SSR mode)
- Tailwind CSS
- Chart.js via `<script>` tag (no npm chart component)

### Auth Flow
```
/login page → POST ke Laravel /api/auth/login
           ← terima token → simpan di HTTP-only cookie (server-side)
Middleware  → baca cookie → forward ke Laravel API sebagai Bearer header
           → gagal / tidak ada cookie → redirect ke /login
```

### Halaman

| Route          | Deskripsi                                          |
|----------------|----------------------------------------------------|
| `/`            | Redirect ke `/dashboard` atau `/login`             |
| `/login`       | Form email + password                              |
| `/dashboard`   | Ringkasan saldo, grafik bulanan, grafik kategori   |
| `/transactions`| Tabel riwayat + filter bulan / tipe / kategori     |
| `/reports`     | Link export laporan (hit endpoint Laravel)         |

### Data Fetching
- Semua data di-fetch **server-side** di `Astro.props` / frontmatter
- Tidak ada client-side fetch, tidak ada state management library
- Chart.js di-init via inline `<script>` dengan data di-inject dari server

### Batasan v1
- Read-only — tidak ada form tambah/edit transaksi
- Tidak ada real-time update (no WebSocket, no polling)
- Export: redirect ke endpoint Laravel, bukan generate di Astro

---

## 6. Android App (android/)

### Tech Stack
- Kotlin, Jetpack Compose
- Retrofit + OkHttp (HTTP client)
- Hilt (dependency injection)
- EncryptedSharedPreferences (token storage)
- Jetpack Navigation Compose

### Architecture
Clean Architecture ringan: **UI → ViewModel → Repository**

- `ViewModel` tidak tahu soal Retrofit, hanya tahu `Repository`
- `Repository` tidak tahu soal ViewModel, hanya return data atau throw exception
- Tidak ada Room di v1 — semua langsung ke API

### Auth Flow
```
LoginScreen → POST /api/auth/login
           ← token → simpan di EncryptedSharedPreferences
OkHttp Interceptor → baca token → tambahkan Authorization: Bearer header
Token tidak ada / expired → redirect ke LoginScreen
```

### Screen List (MVP)

| Screen                  | Deskripsi                                              |
|-------------------------|--------------------------------------------------------|
| `LoginScreen`           | Email + password                                       |
| `HomeScreen`            | Saldo hari ini, 5 transaksi terbaru                    |
| `AddTransactionScreen`  | Form: judul, nominal, tipe, kategori, tanggal          |
| `TransactionListScreen` | Filter bulan, scroll tanpa pagination (v1)             |
| `CategoryScreen`        | List kategori user + tambah kategori baru              |

### Navigation
Bottom nav bar — 3 tab: **Home / Add / History**

### Offline
Tidak ada offline mode di v1. Tidak ada koneksi → tampil error Snackbar.
Local cache masuk v2.

---

## 7. Roadmap

| Versi | Fitur                                              |
|-------|----------------------------------------------------|
| **v1**| Input manual, Laravel API, Astro dashboard         |
| **v2**| Budget bulanan, target tabungan, notifikasi, cache |
| **v3**| Scan struk (Google ML Kit OCR + parser)            |
| **v4**| Input suara (Android SpeechRecognizer + parser)    |
| **v5**| Multi-user lanjutan: family wallet, shared budget  |

### Catatan OCR & Voice (v3/v4)
Tidak menggunakan LLM. Semua parsing dilakukan **on-device**:
- OCR: Google ML Kit Text Recognition → parser regex sederhana
- Voice: Android SpeechRecognizer → parser kalimat sederhana
- Server hanya menerima JSON hasil parsing, tidak ikut proses

Parser output: `{ title, amount, type, category, date }`

---

## 8. Keputusan Desain

| Keputusan                          | Alasan                                                        |
|------------------------------------|---------------------------------------------------------------|
| Monorepo                           | Proyek personal, coordination benefit > overhead              |
| Laravel Sanctum                    | Ringan, support token + cookie, cocok shared hosting          |
| Astro SSR (bukan SSG)              | Data keuangan real-time, tidak cocok di-build statis          |
| Dashboard read-only                | Input hanya via Android, web untuk analisis                   |
| Tidak ada LLM                      | Shared hosting, cost, dan privasi data                        |
| Tidak ada Room di v1               | Simpel dulu, tambah cache di v2 kalau perlu                   |
| Chart.js via `<script>` tag        | Tidak perlu framework chart, satu file script cukup           |

---

## 9. Out of Scope (v1)

- OCR scan struk
- Voice input
- Offline mode / local cache
- Budget & target tabungan
- Notifikasi
- Multi-currency
- Export PDF/Excel (hanya link download ke Laravel di v1)
- PWA dashboard
- Scan QRIS
- Family wallet / shared budget
