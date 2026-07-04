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
- [ ] `npm create astro@latest web`
- [ ] Install `@astrojs/node`, `tailwindcss`, `@astrojs/tailwind`
- [ ] Set `output: 'server'`, `adapter: node()`
- [ ] `.env`: `API_BASE_URL`

### Auth
- [ ] Middleware `src/middleware/index.ts` — cookie check + redirect
- [ ] API helper `src/lib/api.ts` — `apiFetch<T>()`

### Halaman
- [ ] `/login` — form + set cookie
- [ ] `/dashboard` — summary cards + charts + wallet breakdown
- [ ] `/transactions` — tabel + filter form
- [ ] `/reports` — link download CSV
- [ ] `/api/logout.ts` — hapus cookie + redirect

### Komponen
- [ ] `DashboardLayout.astro` — sidebar + top bar
- [ ] `StatCard.astro` — card KPI
- [ ] `TransactionRow.astro` — baris transaksi

### Charts
- [ ] Grafik batang bulanan (Chart.js)
- [ ] Grafik donut per kategori (Chart.js)
- [ ] Card saldo per wallet

### QA
- [ ] `npm run build` sukses
- [ ] `/dashboard` redirect ke `/login` tanpa token
- [ ] Data match API
- [ ] Tidak ada client-side fetch

---

## 3. Android App (`android/`)

### Setup
- [ ] Buat project: Empty Activity, Kotlin, Compose
- [ ] Tambah dependencies: Hilt, Retrofit, OkHttp, Navigation, Security
- [ ] `@HiltAndroidApp` di Application class
- [ ] `BASE_URL` di `BuildConfig`

### Network Layer
- [ ] `ApiService.kt` — semua endpoint termasuk wallet
- [ ] `AuthInterceptor.kt` — inject Bearer token
- [ ] `NetworkModule.kt` — provide OkHttp + Retrofit + ApiService

### Data Layer
- [ ] `TokenStorage.kt` — EncryptedSharedPreferences wrapper
- [ ] `AuthRepository.kt`
- [ ] `CategoryRepository.kt`
- [ ] `WalletRepository.kt`
- [ ] `TransactionRepository.kt`
- [ ] `StatisticsRepository.kt`

### Domain Models
- [ ] `User`, `Category`, `Wallet`, `Transaction`, `Summary`

### Navigation
- [ ] `AppNavigation.kt` — AuthGraph + MainGraph
- [ ] Bottom nav: Home / Add / History / Wallet
- [ ] Startup: cek token → route ke graph yang tepat

### Screens
- [ ] `LoginScreen` + `LoginViewModel`
- [ ] `HomeScreen` + `HomeViewModel`
- [ ] `AddTransactionScreen` + `AddTransactionViewModel` (include wallet picker)
- [ ] `TransactionListScreen` + `TransactionListViewModel` (filter wallet)
- [ ] `CategoryScreen` + `CategoryViewModel`
- [ ] `WalletScreen` + `WalletViewModel` (cash singleton check)

### QA
- [ ] `./gradlew assembleDebug` sukses
- [ ] Login + tambah transaksi flow end-to-end
- [ ] Cash wallet hanya muncul 1 di WalletScreen
- [ ] No-internet → Snackbar, tidak crash

---

## Done Criteria (v1 Complete)

- [ ] Semua endpoint Laravel → `php artisan test` hijau
- [ ] Deploy backend ke shared hosting → berfungsi
- [ ] Astro build + deploy → `npm run build` sukses
- [ ] Android APK debug build sukses
- [ ] End-to-end: tambah transaksi di Android → muncul di dashboard web
- [ ] Data isolation: user A tidak bisa lihat data user B
