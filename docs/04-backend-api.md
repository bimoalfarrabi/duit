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
| GET | `/api/wallets` | ✅ | List wallet yang bisa diakses (milik sendiri + shared) |
| POST | `/api/wallets` | ✅ | Buat wallet baru |
| PUT | `/api/wallets/{id}` | ✅ | Update wallet (owner-only) |
| DELETE | `/api/wallets/{id}` | ✅ | Hapus wallet (owner-only) |

**Business rules:**
- `cash`: max 1 per user, dibuat otomatis saat register → `POST` dengan `type=cash` ditolak 422 jika sudah ada
- `bank`/`ewallet`: tidak terbatas
- Cash wallet tidak bisa dihapus → `DELETE` pada cash wallet return 403
- `GET /api/wallets` mengembalikan wallet milik sendiri **dan** wallet yang di-share ke user (lihat Wallet Sharing)
- Update/hapus wallet hanya boleh oleh owner (`user_id`); member return 403

**Wallet object:**
```json
{ "id": 1, "name": "Cash", "type": "cash", "color": "#4CAF50", "icon": "wallet", "balance": 0, "is_owner": true, "is_shared": false }
```
- `is_owner`: `true` jika user yang login adalah owner wallet
- `is_shared`: hanya muncul untuk owner — `true` jika wallet punya minimal 1 member

---

### Wallet Sharing (v5)

Sharing per-wallet: owner mengundang user lain via email, member menerima undangan lalu bisa mengakses wallet. `wallets.user_id` tetap owner; member disimpan di pivot `wallet_user`.

| Method | Endpoint | Auth | Deskripsi |
|--------|----------|------|-----------|
| POST | `/api/wallets/{wallet}/invitations` | ✅ | Owner mengundang member via email |
| GET | `/api/invitations` | ✅ | Undangan pending yang ditujukan ke email user |
| POST | `/api/invitations/{token}/accept` | ✅ | Terima undangan → jadi member |
| POST | `/api/invitations/{token}/decline` | ✅ | Tolak undangan |
| GET | `/api/wallets/{wallet}/members` | ✅ | List owner + member wallet (siapa saja yang punya akses) |
| DELETE | `/api/wallets/{wallet}/members/{member}` | ✅ | Hapus member (owner-only) |

**Business rules:**
- Invite hanya boleh oleh owner → non-owner return 403
- Tidak bisa mengundang diri sendiri (422), member yang sudah ada (422), atau email dengan undangan pending (422)
- Token: `Str::random(64)`, berlaku 7 hari (`expires_at`)
- Undangan bisa untuk email yang belum terdaftar — user register dulu, lalu `GET /api/invitations` menampilkan undangan yang cocok dengan emailnya
- Accept/decline hanya boleh oleh user dengan email yang cocok (404 jika tidak); undangan kedaluwarsa/non-pending return 422
- Permission member: lihat wallet + lihat/buat transaksi. **Tidak bisa** edit/hapus wallet atau kelola member (403)
- Notifikasi email dikirim via `Notification::route('mail', $email)` (sinkron, sesuai constraint shared hosting)

**Invitation request (POST invite):**
```json
{ "email": "partner@example.com" }
```

**Invitation object:**
```json
{
  "id": 1,
  "wallet_id": 2,
  "wallet_name": "Belanja Bulanan",
  "inviter": { "id": 1, "name": "Budi" },
  "email": "partner@example.com",
  "status": "pending",
  "expires_at": "2026-07-20T08:28:00+00:00"
}
```

**Members response (GET members):**
```json
{
  "data": {
    "owner": { "id": 1, "name": "Budi", "email": "budi@example.com", "role": "owner" },
    "members": [
      { "id": 2, "name": "Sari", "email": "sari@example.com", "role": "member" }
    ]
  }
}
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

**v5 catatan:** `GET`/`POST` bekerja pada semua wallet yang bisa diakses (milik sendiri + shared). Member bisa membuat transaksi di shared wallet memakai kategorinya sendiri. `PUT`/`DELETE` tetap terbatas pada pembuat transaksi (`user_id`).

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
├── WalletInvitationController.php
├── WalletMemberController.php
├── CategoryController.php
├── TransactionController.php
├── StatisticsController.php
└── ExportController.php
```

## Security

- Semua route kecuali `register` dan `login` di bawah `middleware('auth:sanctum')`
- Setiap endpoint ownership-check: query selalu filter `user_id = auth()->id()`
- **Pengecualian v5 (shared wallet):** wallet & transaksi difilter per *accessible wallet* (owned + shared via pivot `wallet_user`), bukan hanya `user_id`. Mutasi wallet/member tetap owner-only.
- Input validasi via `$request->validate([...])`
- `.htaccess` forward `Authorization` header (wajib di shared hosting)
