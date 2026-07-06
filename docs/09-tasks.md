# 📋 Tasks

Checklist pengembangan v1 MVP. Update status saat implementasi.

Legend: `[ ]` pending · `[x]` done · `[-]` skip/tidak perlu

---

## 1. Laravel API (`backend/`)

### Setup
- [x] `composer create-project laravel/laravel backend`
- [x] Konfigurasi `.env`: `DB_*`, `APP_URL`
- [x] Install Sanctum: `composer require laravel/sanctum`
- [x] Publish Sanctum config
- [x] Tambah `HasApiTokens` ke model `User`

### Database & Models
- [x] Migration: `categories`
- [x] Migration: `wallets`
- [x] Migration: `transactions` (dengan `wallet_id`)
- [x] Model `User` — tambah relasi `HasMany(Wallet)`, `HasMany(Category)`, `HasMany(Transaction)`
- [x] Model `Category`
- [x] Model `Wallet` — `User::created` event auto-create cash wallet
- [x] Model `Transaction`
- [x] Tambah index: `transactions(user_id, date)`, `transactions(wallet_id)`, `transactions(category_id)`

### Auth Endpoints
- [x] `POST /api/auth/register`
- [x] `POST /api/auth/login`
- [x] `POST /api/auth/logout`
- [x] `GET /api/auth/me`

### Wallet Endpoints
- [x] `GET /api/wallets`
- [x] `POST /api/wallets` — validasi cash singleton
- [x] `PUT /api/wallets/{id}`
- [x] `DELETE /api/wallets/{id}`

### Category Endpoints
- [x] `GET /api/categories`
- [x] `POST /api/categories`
- [x] `PUT /api/categories/{id}`
- [x] `DELETE /api/categories/{id}`

### Transaction Endpoints
- [x] `GET /api/transactions` — filter: month, year, type, category_id, wallet_id
- [x] `POST /api/transactions` — validasi wallet_id + category_id wajib
- [x] `PUT /api/transactions/{id}`
- [x] `DELETE /api/transactions/{id}`

### Statistics Endpoints
- [x] `GET /api/statistics/summary`
- [x] `GET /api/statistics/by-category`
- [x] `GET /api/statistics/by-wallet`
- [x] `GET /api/statistics/monthly`

### Export
- [x] `GET /api/export/transactions` → CSV download

### API Resources
- [x] `UserResource`
- [x] `CategoryResource`
- [x] `WalletResource`
- [x] `TransactionResource`

### CORS & Hosting
- [x] `SANCTUM_STATEFUL_DOMAINS` di `.env`
- [x] `config/cors.php` — allow Astro domain
- [-] `.htaccess` — forward `Authorization` header (sudah ada di Laravel default)

### Testing
- [x] Feature test: register + login
- [x] Feature test: CRUD transaksi (ownership check)
- [x] Feature test: statistics/summary
- [x] Feature test: cash wallet singleton validation
- [x] `php artisan test` → hijau (19/19 passed)

---

## 2. Astro Dashboard (`web/`)

### Setup
- [x] `npm create astro@latest web`
- [x] Install `@astrojs/node`, `@tailwindcss/vite`, `tailwindcss` (Tailwind v4 — `@astrojs/tailwind` deprecated)
- [x] Set `output: 'server'`, `adapter: node()`
- [x] `.env`: `API_BASE_URL`

### Auth
- [x] Middleware `src/middleware/index.ts` — cookie check + redirect
- [x] API helper `src/lib/api.ts` — `apiFetch<T>()`

### Halaman
- [x] `/login` — form + set cookie
- [x] `/dashboard` — summary cards + charts + wallet breakdown
- [x] `/transactions` — tabel + filter form
- [x] `/reports` — link download CSV via proxy `/api/export`
- [x] `/api/logout.ts` — hapus cookie + redirect

### Komponen
- [x] `DashboardLayout.astro` — sidebar + top bar
- [x] `StatCard.astro` — card KPI
- [x] `TransactionRow.astro` — baris transaksi

### Charts
- [x] Grafik batang bulanan (Chart.js via CDN + define:vars)
- [x] Grafik donut per kategori (Chart.js)
- [x] Card saldo per wallet

### QA
- [x] `npm run build` sukses
- [x] `/dashboard` redirect ke `/login` tanpa token (via middleware)
- [x] Data fetch server-side di frontmatter — tidak ada client-side fetch
- [-] Data match API (perlu server aktif untuk verify)

---

## 3. Android App (`android/`)

### Setup
- [x] Buat project: Empty Activity, Kotlin, Compose
- [x] Tambah dependencies: Hilt, Retrofit, OkHttp, Navigation, Security
- [x] `@HiltAndroidApp` di Application class
- [-] `BASE_URL` di `BuildConfig` (hardcoded di NetworkModule, cukup untuk v1)

### Network Layer
- [x] `ApiService.kt` — semua endpoint termasuk wallet
- [x] `AuthInterceptor.kt` — inject Bearer token
- [x] `NetworkModule.kt` — provide OkHttp + Retrofit + ApiService

### Data Layer
- [x] `TokenStorage.kt` — EncryptedSharedPreferences wrapper
- [x] `AuthRepository.kt`
- [x] `CategoryRepository.kt`
- [x] `WalletRepository.kt`
- [x] `TransactionRepository.kt`
- [-] `StatisticsRepository.kt` (digabung ke TransactionRepository.getSummary())

### Domain Models
- [x] `User`, `Category`, `Wallet`, `Transaction`, `Summary`

### Navigation
- [x] `NavGraph.kt` — AuthGraph + MainGraph
- [x] Bottom nav: Home / Add / History / Wallet
- [x] Startup: cek token → route ke graph yang tepat

### Screens
- [x] `LoginScreen` + `LoginViewModel`
- [x] `HomeScreen` + `HomeViewModel`
- [x] `AddTransactionScreen` + `AddTransactionViewModel` (include wallet picker)
- [x] `TransactionListScreen` + `TransactionListViewModel` (filter wallet)
- [x] `CategoryScreen` + `CategoryViewModel`
- [x] `WalletScreen` + `WalletViewModel` (cash singleton check)

### QA
- [x] `./gradlew assembleDebug` sukses
- [x] Login + tambah transaksi flow end-to-end
- [x] Cash wallet hanya muncul 1 di WalletScreen
- [x] No-internet → Snackbar, tidak crash

---

## Done Criteria (v1 Complete)

- [x] Semua endpoint Laravel → `php artisan test` hijau (19/19)
- [x] Deploy backend ke shared hosting → berfungsi (`api.duit.viasco.my.id`)
- [x] Astro build + deploy → `npm run build` sukses (`duit.viasco.my.id`)
- [x] Android APK debug build sukses
- [ ] End-to-end: tambah transaksi di Android → muncul di dashboard web
- [ ] Data isolation: user A tidak bisa lihat data user B

## Deploy Notes (Production)

- **Backend API**: `https://api.duit.viasco.my.id/api` — Laravel di shared hosting cPanel, PHP 8.2
- **Web Dashboard**: `https://duit.viasco.my.id` — Astro SSR via CloudLinux Passenger (Node.js 22)
- **Subdomain terpisah**: api.* untuk Laravel, duit.* untuk Astro — sharing doc root tidak memungkinkan satu `.htaccess`
- **`API_BASE_URL`**: inject via cPanel Node.js App env vars (`process.env`), bukan `import.meta.env` — Vite bake string `"undefined"` saat build jika tidak ada di `.env`
- **CSRF**: `checkOrigin: false` di `astro.config.mjs` — Astro 4.9+ block form POST tanpa ini
- **`App\Http\Middleware\Authenticate`**: override `redirectTo()` → `null` + alias di `bootstrap/app.php` — tanpa ini Laravel cari route `[login]` dan throw 500

---

## Rencana v2 — Auth Lanjutan

> Diimplementasi manual tanpa Fortify — pure API, no Blade.

### Password Reset
- [x] Migration: tabel `password_reset_tokens` (sudah ada di Laravel default)
- [x] `POST /api/auth/forgot-password` — kirim email dengan signed token (expire 1 jam via `PasswordResetController`)
- [x] `POST /api/auth/reset-password` — verifikasi token + update password + revoke semua token
- [x] Konfigurasi Laravel mailer di `.env.example` (`MAIL_MAILER=sendmail` untuk cPanel)
- [x] Feature test: request reset + reset sukses + token invalid + password policy + token revoke (`PasswordResetTest`)

### Email Verification
- [x] Kolom `email_verified_at` sudah ada di default Laravel — `MustVerifyEmail` diaktifkan di `User` model
- [x] `POST /api/auth/email/verification-notification` — kirim ulang link verifikasi (`EmailVerificationController`)
- [x] `GET /api/auth/email/verify/{id}/{hash}` — verifikasi via signed URL
- [-] Middleware `verified` untuk endpoint — tidak diimplementasi di v2 (optional, semua user bisa akses)
- [x] Feature test: send notification + verify sukses + hash invalid (`EmailVerificationTest`)

### Two-Factor Authentication (TOTP)
- [x] Migration: kolom `two_factor_secret` dan `two_factor_confirmed_at` di `users` (`add_two_factor_to_users_table`)
- [x] `POST /api/auth/two-factor-authentication` — enable 2FA, return secret + QR URL
- [x] `POST /api/auth/two-factor-authentication/confirm` — konfirmasi TOTP code untuk aktivasi
- [x] `DELETE /api/auth/two-factor-authentication` — disable 2FA (butuh TOTP code)
- [x] `POST /api/auth/two-factor-challenge` — verifikasi TOTP code saat login (pakai temp_token)
- [x] Login flow: jika 2FA aktif, return `temp_token` (ability: `2fa-challenge`, expire 5 menit) — token penuh tidak dikembalikan sampai TOTP diverifikasi
- [ ] Android: screen TOTP input setelah login sukses jika 2FA aktif — dikerjakan di track Android v2
- [x] Feature test: enable → confirm → login challenge → disable flow (`TwoFactorTest`)

### Dependencies
- [x] `pragmarx/google2fa-laravel` ^3.0 terinstall
