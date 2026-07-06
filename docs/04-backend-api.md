# 🌐 Backend API

## Stack

- Laravel 12, PHP 8.2
- MySQL 8.0+
- Laravel Sanctum (auth)
- Shared hosting compatible

## Setup

```bash
composer create-project laravel/laravel backend
composer require laravel/sanctum
php artisan vendor:publish --provider="Laravel\Sanctum\SanctumServiceProvider"
php artisan migrate
```

## Response Format

Semua response pakai format konsisten:

```json
{ "data": {...}, "message": "OK", "status": true }
```

Error validasi → HTTP 422 (Laravel default)
Ownership violation → HTTP 403

## Endpoints

### Auth

| Method | Endpoint | Auth | Deskripsi |
|--------|----------|------|-----------|
| POST | `/api/auth/register` | ❌ | Daftar akun baru |
| POST | `/api/auth/login` | ❌ | Login, return token (atau temp_token jika 2FA aktif) |
| POST | `/api/auth/logout` | ✅ | Revoke token |
| GET | `/api/auth/me` | ✅ | Data user aktif |
| POST | `/api/auth/forgot-password` | ❌ | Kirim link reset password ke email (throttle 5/mnt) |
| POST | `/api/auth/reset-password` | ❌ | Reset password via token dari email |
| POST | `/api/auth/email/verification-notification` | ✅ | Kirim ulang link verifikasi email (throttle 3/mnt) |
| GET | `/api/auth/email/verify/{id}/{hash}` | ✅ | Verifikasi email via signed URL |
| POST | `/api/auth/two-factor-authentication` | ✅ | Enable 2FA — return secret + QR URL |
| POST | `/api/auth/two-factor-authentication/confirm` | ✅ | Konfirmasi TOTP code untuk aktivasi 2FA |
| DELETE | `/api/auth/two-factor-authentication` | ✅ | Disable 2FA (butuh TOTP code) |
| POST | `/api/auth/two-factor-challenge` | ❌ | Verifikasi TOTP saat login (pakai temp_token) |

**Login response normal:**
```json
{ "data": { "token": "1|abc...", "user": { ... } }, "status": true }
```

**Login response jika 2FA aktif:**
```json
{ "data": { "requires_2fa": true, "temp_token": "2|xyz..." }, "status": true }
```
`temp_token` hanya valid 5 menit dan hanya bisa dipakai untuk `/two-factor-challenge`.

---

### Wallets (Sumber Dana)

| Method | Endpoint | Auth | Deskripsi |
|--------|----------|------|-----------|
| GET | `/api/wallets` | ✅ | List wallet milik user |
| POST | `/api/wallets` | ✅ | Buat wallet baru |
| PUT | `/api/wallets/{id}` | ✅ | Update wallet |
| DELETE | `/api/wallets/{id}` | ✅ | Hapus wallet |

**Business rules:**
- `cash`: max 1 per user, dibuat otomatis saat register → `POST` dengan `type=cash` ditolak 422 jika sudah ada
- `bank`/`ewallet`: tidak terbatas
- Cash wallet tidak bisa dihapus → `DELETE` pada cash wallet return 403

**Wallet object:**
```json
{ "id": 1, "name": "Cash", "type": "cash", "color": "#4CAF50", "icon": "wallet", "balance": 0 }
```

---

### Categories

| Method | Endpoint | Auth | Deskripsi |
|--------|----------|------|-----------|
| GET | `/api/categories` | ✅ | List kategori milik user |
| POST | `/api/categories` | ✅ | Buat kategori baru |
| PUT | `/api/categories/{id}` | ✅ | Update kategori |
| DELETE | `/api/categories/{id}` | ✅ | Hapus kategori |

**Category object:**
```json
{ "id": 1, "name": "Makan", "type": "expense", "color": "#FF5722", "icon": "food" }
```

---

### Transactions

| Method | Endpoint | Auth | Deskripsi |
|--------|----------|------|-----------|
| GET | `/api/transactions` | ✅ | List transaksi (dengan filter) |
| POST | `/api/transactions` | ✅ | Tambah transaksi |
| PUT | `/api/transactions/{id}` | ✅ | Update transaksi |
| DELETE | `/api/transactions/{id}` | ✅ | Hapus transaksi |

**Filter params (GET):** `?month=7&year=2026&type=expense&category_id=1&wallet_id=2`

**Transaction request:**
```json
{
  "title": "Makan siang",
  "amount": 25000,
  "type": "expense",
  "category_id": 1,
  "wallet_id": 1,
  "date": "2026-07-03",
  "note": "Nasi padang"
}
```

**Transaction object:**
```json
{
  "id": 1,
  "title": "Makan siang",
  "amount": 25000,
  "type": "expense",
  "date": "2026-07-03",
  "note": "Nasi padang",
  "category": { "id": 1, "name": "Makan", "type": "expense" },
  "wallet": { "id": 1, "name": "Cash", "type": "cash" }
}
```

---

### Statistics

| Method | Endpoint | Auth | Response |
|--------|----------|------|----------|
| GET | `/api/statistics/summary` | ✅ | `{ income, expense, balance }` bulan itu |
| GET | `/api/statistics/by-category` | ✅ | Breakdown per kategori |
| GET | `/api/statistics/by-wallet` | ✅ | Breakdown per wallet |
| GET | `/api/statistics/monthly` | ✅ | Array 12 bulan |

**Params:** `?month=7&year=2026` (summary, by-category, by-wallet) · `?year=2026` (monthly)

**Summary response:**
```json
{ "data": { "income": 5000000, "expense": 1200000, "balance": 3800000 }, "status": true }
```

**By-category response:**
```json
{
  "data": [
    { "category_id": 1, "category_name": "Makan", "category_type": "expense", "total": 350000 },
    { "category_id": 2, "category_name": "Transport", "category_type": "expense", "total": 120000 },
    { "category_id": 3, "category_name": "Gaji", "category_type": "income", "total": 5000000 }
  ]
}
```

**By-wallet response:**
```json
{
  "data": [
    { "wallet_id": 1, "wallet_name": "Cash", "wallet_type": "cash", "income": 0, "expense": 150000, "balance": -150000 },
    { "wallet_id": 2, "wallet_name": "BCA", "wallet_type": "bank", "income": 5000000, "expense": 1050000, "balance": 3950000 }
  ]
}
```

**Monthly response:**
```json
{
  "data": [
    { "month": 1, "income": 4500000, "expense": 1100000 },
    { "month": 2, "income": 4800000, "expense": 1300000 }
  ]
}
```

---

### Budget

| Method | Endpoint | Auth | Deskripsi |
|--------|----------|------|-----------|
| GET | `/api/budgets` | ✅ | List budget bulan ini + spent per kategori |
| POST | `/api/budgets` | ✅ | Buat atau update budget (upsert by category+month+year) |
| DELETE | `/api/budgets/{id}` | ✅ | Hapus budget |

**Params GET:** `?month=7&year=2026` (default: bulan & tahun sekarang)

**Response GET:**
```json
{
  "data": [
    {
      "id": 1,
      "category_id": 3,
      "category": { "id": 3, "name": "Makan", ... },
      "month": 7,
      "year": 2026,
      "amount": "500000.00",
      "spent": 125000
    }
  ]
}
```

**Business rules:**
- Satu budget per `(user, category, month, year)` — `POST` upsert, tidak ada `PUT`
- `spent` dihitung live dari `transactions` (tidak disimpan di DB)
- Ownership check: `category_id` harus milik user yang sama

---

### Savings Goals

| Method | Endpoint | Auth | Deskripsi |
|--------|----------|------|-----------|
| GET | `/api/savings` | ✅ | List semua savings goal milik user |
| POST | `/api/savings` | ✅ | Buat savings goal baru |
| PUT | `/api/savings/{id}` | ✅ | Update goal (nama, topup `current_amount`, deadline) |
| DELETE | `/api/savings/{id}` | ✅ | Hapus savings goal |

**Response:**
```json
{
  "data": {
    "id": 1,
    "name": "Laptop baru",
    "target_amount": "10000000.00",
    "current_amount": "3000000.00",
    "deadline": "2026-12-31",
    "is_completed": false
  }
}
```

**Business rules:**
- `current_amount` diupdate manual via `PUT` (topup)
- Auto-complete: `is_completed = true` jika `current_amount >= target_amount`

---

### Export

| Method | Endpoint | Auth | Deskripsi |
|--------|----------|------|-----------|
| GET | `/api/export/transactions` | ✅ | Download CSV transaksi |

**Params:** `?month=7&year=2026&format=csv`

**Response:** File download CSV dengan kolom: `date, title, category, wallet, type, amount, note`

---

## Struktur Controller

```
app/Http/Controllers/Api/
├── AuthController.php
├── WalletController.php
├── CategoryController.php
├── TransactionController.php
├── StatisticsController.php
└── ExportController.php
```

## Security

- Semua route kecuali `register` dan `login` di bawah `middleware('auth:sanctum')`
- Setiap endpoint ownership-check: query selalu filter `user_id = auth()->id()`
- Input validasi via `$request->validate([...])`
- `.htaccess` forward `Authorization` header (wajib di shared hosting)
