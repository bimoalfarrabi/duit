# 🏗️ Arsitektur

## Diagram Sistem

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

## Struktur Repo (Monorepo)

```
personal-finance/
├── backend/        # Laravel REST API
├── web/            # Astro SSR dashboard
├── android/        # Kotlin + Jetpack Compose
├── docs/           # Dokumentasi
└── AGENTS.md       # Panduan AI agent
```

## Prinsip Arsitektur

- **Single source of truth**: Laravel adalah satu-satunya yang akses MySQL
- **No client DB access**: Astro tidak pernah langsung ke MySQL
- **All logic in backend**: kalkulasi statistik, saldo, semua di Laravel
- **No duplication**: tidak ada business logic di klien

## Auth Flow

### Android
```
Login → POST /api/auth/login → token → EncryptedSharedPreferences
Setiap request → OkHttp Interceptor inject Authorization: Bearer {token}
```

### Astro Dashboard
```
Login → POST /api/auth/login → token → HTTP-only cookie (server-side)
Middleware → baca cookie → forward sebagai Bearer ke Laravel
Tidak ada token → redirect /login
```

## Stack per Komponen

| Layer | Tech | Versi |
|-------|------|-------|
| Backend language | PHP | 8.2 |
| Backend framework | Laravel | 12 |
| Auth | Laravel Sanctum | bundled |
| Database | MySQL | 8.0+ |
| Web framework | Astro | 4+ (SSR) |
| Web styling | Tailwind CSS | 3 |
| Web charts | Chart.js | via `<script>` |
| Mobile language | Kotlin | 1.9+ |
| Mobile UI | Jetpack Compose | latest stable |
| Mobile HTTP | Retrofit + OkHttp | 2.11 / 4.12 |
| Mobile DI | Hilt | 2.51 |
| Mobile storage | EncryptedSharedPreferences | Security Crypto |

## Environment Lokal

- **Backend**: XAMPP (Apache port 80, MySQL port 3306)
- **Web**: Node.js 20+, `npm run dev`
- **Android**: Android Studio, min SDK 26 (Android 8.0)

## Hosting (Production)

- Shared hosting PHP — tidak ada Docker, tidak ada queue worker
- `.htaccess` wajib forward `Authorization` header
- `SANCTUM_STATEFUL_DOMAINS` dikonfigurasi untuk domain Astro

## Urutan Development

```
1. Laravel API  →  fondasi, semua klien bergantung
2. Astro Web    →  setelah auth + statistics endpoint siap
3. Android App  →  setelah auth + transaction endpoint siap
```
