# 📋 Tasks

Checklist pengembangan v1 & v2. Update status saat implementasi.

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
- [x] End-to-end: tambah transaksi di Android → muncul di dashboard web
- [x] Data isolation: user A tidak bisa lihat data user B

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
- [x] Android: screen TOTP input setelah login sukses jika 2FA aktif — dikerjakan di track Android v2
- [x] Feature test: enable → confirm → login challenge → disable flow (`TwoFactorTest`)

### Dependencies
- [x] `pragmarx/google2fa-laravel` ^3.0 terinstall

---

## Rencana v2 — Budget & Savings

### Budget Bulanan

- [x] Migration: `budgets` (`user_id`, `category_id`, `month`, `year`, `amount`)
- [x] Model `Budget` — cast `amount` decimal:2
- [x] Relasi `User::budgets()`
- [x] `GET /api/budgets` — filter bulan/tahun, include `spent` live dari transactions
- [x] `POST /api/budgets` — upsert by `(category_id, month, year)`
- [x] `DELETE /api/budgets/{id}` — ownership check
- [x] `BudgetResource` — include `spent`
- [x] Feature test: `BudgetTest` (7 test cases)

### Target Tabungan

- [x] Migration: `savings_goals` (`user_id`, `name`, `target_amount`, `current_amount`, `deadline`, `is_completed`)
- [x] Model `SavingsGoal` — cast boolean + decimal
- [x] Relasi `User::savingsGoals()`
- [x] `GET /api/savings` — list semua savings goal
- [x] `POST /api/savings` — buat goal baru
- [x] `PUT /api/savings/{id}` — topup `current_amount`, auto-complete jika >= target
- [x] `DELETE /api/savings/{id}` — ownership check
- [x] `SavingsGoalResource`
- [x] Feature test: `SavingsGoalTest` (6 test cases)

### QA Backend v2 Budget & Savings

- [x] `phpunit --filter="BudgetTest|SavingsGoalTest"` → 13/13 passed
- [x] Full suite → 49/49 passed (1 skip)
- [x] Deploy ke production + run migration
- [x] Update docs v2 selesai

### Android v2 Budget & Savings

- [x] `BudgetRepository.kt` + `BudgetDto`
- [x] `SavingsRepository.kt` + `SavingsDto`
- [x] `BudgetScreen` + `BudgetViewModel`
- [x] `SavingsScreen` + `SavingsViewModel`
- [x] Navigasi ke BudgetScreen + SavingsScreen dari bottom nav / home

### Android v2 Notifikasi Lokal

- [x] WorkManager (`work-runtime-ktx 2.9.0`) + Hilt Work dependency
- [x] `NotificationHelper.kt` — channel setup + send helper
- [x] `BudgetCheckWorker.kt` — cek budget > 80% terpakai + savings goal tercapai
- [x] Schedule `PeriodicWorkRequest` 1x/hari di `DuitApplication`
- [x] `AndroidManifest`: `POST_NOTIFICATIONS` + `RECEIVE_BOOT_COMPLETED` permissions

---

## Rencana v3 — OCR Scan Struk

### Android v3 OCR

- [x] Tambah deps: `mlkit-text-recognition 16.0.1` + `camerax 1.3.1` ke `libs.versions.toml` + `build.gradle.kts`
- [x] `AndroidManifest.xml`: tambah `CAMERA` permission + `uses-feature` (required=false)
- [x] `OcrParser.kt`: regex ekstrak nominal (dengan/tanpa Rp), tanggal (DD/MM/YYYY, YYYY-MM-DD), judul (skip keyword total/subtotal/dll)
- [x] `OcrViewModel.kt`: ML Kit `TextRecognition`, process `ImageProxy`, parse result, expose `OcrUiState`
- [x] `OcrScreen.kt`: CameraX preview + `ImageAnalysis`, permission via `ActivityResultContracts`, loading overlay, instruksi overlay
- [x] `AddTransactionScreen.kt`: baca OCR prefill dari nav args (`ocr_title`, `ocr_amount`, `ocr_date`) via `backStackEntry.arguments`
- [x] `NavGraph.kt`: tambah route `ocr`, OCR result navigate ke `Screen.Add.withOcr(...)` + pop OCR dari backstack
- [x] `OcrParserTest.kt`: 8 unit test (amount, date, title, fallback, empty)

### Bug fixes & UX — OCR flow (v3 polish)

- [x] `OcrViewModel.kt`: `@Volatile isStopped` flag — analyzer berhenti setelah first attempt, reset() clear flag → infinite loop putus
- [x] `OcrScreen.kt`: fullscreen `Box` (hapus `Scaffold`+`TopAppBar`), floating back button, `FILL_CENTER + COMPATIBLE_MODE` → kamera fullscreen
- [x] `NavGraph.kt`: `showBottomBar` exclude `Screen.Ocr.route` + `Screen.Add.route` (startsWith check) → double TopAppBar hilang
- [x] `NavGraph.kt`: FAB speed dial pill items — `AnimatedVisibility` slide-up/fade, `FabMenuItem` composable (icon + label pill, `secondaryContainer`, `extraLarge` shape)
- [-] `FloatingActionButtonMenu` (M3 Expressive) — API baru di M3 1.5.0-alpha, belum ada di BOM 2024.12.01 stable, tunda sampai stable release

### QA v3 OCR

- [ ] Build APK debug sukses
- [ ] Test manual: scan struk → prefill form transaksi

---

## Rencana v4 — Voice Input

### Android v4 Voice (Planned)

- [x] `VoiceInputScreen.kt`: Android `SpeechRecognizer` (on-device) — minta RECORD_AUDIO permission, tampil waveform/listening overlay
- [x] `VoiceParser.kt`: regex/NLP parser — ekstrak nominal (`18000`, `18rb`, `18k`), tipe (`beli`, `bayar` = expense; `terima`, `dapat` = income), judul (sisa kalimat)
- [x] `VoiceViewModel.kt`: orchestrate SpeechRecognizer → VoiceParser → expose `VoiceUiState`
- [x] `NavGraph.kt`: tambah route `voice`, FAB speed dial item ketiga — **Input Suara** → `VoiceInputScreen`
- [x] `AddTransactionScreen.kt`: tambah prefill via nav args `voice_title/voice_amount/voice_type` (sama pola OCR)
- [x] `VoiceParserTest.kt`: unit test berbagai format kalimat
- [x] `AndroidManifest.xml`: tambah `RECORD_AUDIO` permission

### Catatan Teknis v4
- Tidak ada LLM — semua parsing on-device via regex + keyword matching
- SpeechRecognizer pakai `ACTION_RECOGNIZE_SPEECH` intent atau `RecognizerIntent`
- FAB akan punya 3 item: Scan Struk + Input Suara + Tambah Manual
- Pola prefill via nav args konsisten dengan v3 OCR