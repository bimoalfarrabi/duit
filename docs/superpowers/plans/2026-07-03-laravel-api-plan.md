# Implementation Plan — Laravel API (backend/)

**Versi:** v1 MVP
**Tanggal:** 2026-07-03
**Referensi:** [PRD v1](../prd/v1-mvp.md) · [Design Spec](../specs/2026-07-03-personal-finance-ecosystem-design.md)

---

## Scope

Laravel REST API sebagai backend tunggal untuk Android dan Astro dashboard. Multi-user, Sanctum auth, MySQL, shared hosting compatible.

---

## Prerequisites

- PHP 8.2+, Composer
- MySQL database
- Laravel 11 installer

---

## Fase 1 — Setup Project

- [ ] `composer create-project laravel/laravel backend`
- [ ] Konfigurasi `.env`: DB_*, APP_URL
- [ ] Install Sanctum: `composer require laravel/sanctum`
- [ ] Publish Sanctum config: `php artisan vendor:publish --provider="Laravel\Sanctum\SanctumServiceProvider"`
- [ ] Tambah `HasApiTokens` ke model `User`
- [ ] Tambah `Sanctum::actingAs` middleware di `bootstrap/app.php`

---

## Fase 2 — Database & Models

### Migrations (urutan)
- [ ] `users` — sudah ada dari Laravel, tambah kolom `name`
- [ ] `categories` — `id, user_id, name, type ENUM('income','expense'), color, icon, timestamps`
- [ ] `wallets` — `id, user_id, name, type ENUM('cash','bank','ewallet'), color, icon, balance DECIMAL(15,2) DEFAULT 0, timestamps`
- [ ] `transactions` — `id, user_id, category_id, wallet_id, title, amount DECIMAL(15,2), type ENUM('income','expense'), date DATE, note TEXT nullable, timestamps`

### Models
- [ ] `User` — `HasApiTokens`, `HasMany(Category)`, `HasMany(Wallet)`, `HasMany(Transaction)`
- [ ] `Category` — `BelongsTo(User)`, `HasMany(Transaction)`, fillable: `[name, type, color, icon]`
- [ ] `Wallet` — `BelongsTo(User)`, `HasMany(Transaction)`, fillable: `[name, type, color, icon, balance]`
  - Scope: saat register → auto-create 1 wallet cash via `User::created` event
  - Validasi: tolak `POST /api/wallets` dengan `type=cash` jika user sudah punya 1
- [ ] `Transaction` — `BelongsTo(User)`, `BelongsTo(Category)`, `BelongsTo(Wallet)`, fillable: `[category_id, wallet_id, title, amount, type, date, note]`

---

## Fase 3 — Auth Endpoints

File: `app/Http/Controllers/Api/AuthController.php`

- [ ] `POST /api/auth/register` — validasi, buat user, return token
- [ ] `POST /api/auth/login` — validasi kredensial, return token
- [ ] `POST /api/auth/logout` — revoke current token (`$request->user()->currentAccessToken()->delete()`)
- [ ] `GET /api/auth/me` — return `$request->user()`

Routes di `routes/api.php`:
```php
Route::prefix('auth')->group(function () {
    Route::post('register', [AuthController::class, 'register']);
    Route::post('login', [AuthController::class, 'login']);
    Route::middleware('auth:sanctum')->group(function () {
        Route::post('logout', [AuthController::class, 'logout']);
        Route::get('me', [AuthController::class, 'me']);
    });
});
```

---

## Fase 4 — Category Endpoints

File: `app/Http/Controllers/Api/CategoryController.php`

- [ ] Semua routes di bawah `middleware('auth:sanctum')`
- [ ] `GET /api/categories` — `Category::where('user_id', auth()->id())->get()`
- [ ] `POST /api/categories` — validasi + `user()->categories()->create($data)`
- [ ] `PUT /api/categories/{category}` — policy: pastikan `$category->user_id === auth()->id()`
- [ ] `DELETE /api/categories/{category}` — sama, soft-delete tidak perlu di v1

---

## Fase 5 — Transaction Endpoints

File: `app/Http/Controllers/Api/TransactionController.php`

- [ ] `GET /api/transactions` — filter `month`, `year`, `type`, `category_id`; selalu scope ke `user_id`
- [ ] `POST /api/transactions` — validasi: `title required`, `amount numeric|min:0`, `type in:income,expense`, `date date`, `category_id exists`
- [ ] `PUT /api/transactions/{transaction}` — ownership check
- [ ] `DELETE /api/transactions/{transaction}` — ownership check

---

## Fase 6 — Statistics Endpoints

File: `app/Http/Controllers/Api/StatisticsController.php`

- [ ] `GET /api/statistics/summary?month=&year=` → `{ income, expense, balance }`
  - Query: `SUM(amount) WHERE type = 'income'` dan `SUM(amount) WHERE type = 'expense'` untuk bulan/tahun yang diminta
- [ ] `GET /api/statistics/by-category?month=&year=` → array `{ category_id, category_name, type, total }`
  - Join `transactions` + `categories`, group by `category_id`
- [ ] `GET /api/statistics/by-wallet?month=&year=` → array `{ wallet_id, wallet_name, wallet_type, income, expense, balance }`
  - Join `transactions` + `wallets`, group by `wallet_id`
- [ ] `GET /api/statistics/monthly?year=` → array 12 elemen `{ month, income, expense }`
  - Loop atau group by `MONTH(date)`

---

## Fase 6b — Export Endpoint

File: `app/Http/Controllers/Api/ExportController.php`

- [ ] `GET /api/export/transactions?month=&year=&format=csv`
  - Query transactions milik user, filter bulan/tahun
  - Generate CSV dengan kolom: `date, title, category, wallet, type, amount, note`
  - Return `StreamedResponse` dengan header `Content-Disposition: attachment; filename=transactions-{year}-{month}.csv`
  - Format `csv` saja di v1 (PDF masuk v2+)

---

## Fase 7 — API Resources & Response Format

- [ ] `UserResource` — `id, name, email`
- [ ] `CategoryResource` — `id, name, type, color, icon`
- [ ] `TransactionResource` — `id, title, amount, type, date, note, category`
- [ ] Semua response pakai format konsisten:
```json
{ "data": {...}, "message": "...", "status": true }
```
- [ ] Error response 422 untuk validasi (Laravel default sudah handle ini)
- [ ] 403 untuk ownership violation

---

## Fase 8 — CORS & Shared Hosting

- [ ] Set `SANCTUM_STATEFUL_DOMAINS` di `.env` untuk domain Astro
- [ ] `config/cors.php` — allow origins untuk Astro domain
- [ ] `.htaccess` — pastikan `Authorization` header diteruskan (shared hosting sering strip ini)
- [ ] Test dengan `php artisan serve` dulu, lalu di shared hosting

---

## Fase 9 — Testing

- [ ] Feature test: register + login + get token
- [ ] Feature test: create transaction, pastikan user lain tidak bisa akses
- [ ] Feature test: statistics/summary return data yang benar
- [ ] `php artisan test`

---

## Checklist Final

- [ ] `php artisan migrate` berjalan bersih
- [ ] Semua endpoint return 401 tanpa token
- [ ] User A tidak bisa akses data User B
- [ ] `php artisan test` hijau
