# 🗄️ Database

## ERD (text)

```
users
  └─< categories (user_id)
  └─< wallets    (user_id)
  └─< transactions (user_id)

categories
  └─< transactions (category_id)

wallets
  └─< transactions (wallet_id)
```

## Tabel

### users

| Kolom | Tipe | Keterangan |
|-------|------|-----------|
| id | BIGINT UNSIGNED PK | Auto-increment |
| name | VARCHAR(255) | Nama user |
| email | VARCHAR(255) UNIQUE | Email login |
| email_verified_at | TIMESTAMP NULL | Null = belum verifikasi |
| password | VARCHAR(255) | Bcrypt |
| two_factor_secret | TEXT NULL | TOTP secret (terenkripsi di DB) |
| two_factor_confirmed_at | TIMESTAMP NULL | Null = 2FA belum dikonfirmasi |
| remember_token | VARCHAR(100) NULL | |
| created_at | TIMESTAMP | |
| updated_at | TIMESTAMP | |

---

### password_reset_tokens

| Kolom | Tipe | Keterangan |
|-------|------|-----------|
| email | VARCHAR(255) PK | Email user |
| token | VARCHAR(255) | Token reset (hashed) |
| created_at | TIMESTAMP NULL | Expire setelah 60 menit (default Laravel) |

---

### categories

| Kolom | Tipe | Keterangan |
|-------|------|-----------|
| id | BIGINT UNSIGNED PK | |
| user_id | BIGINT UNSIGNED FK | → users.id |
| name | VARCHAR(255) | Nama kategori |
| type | ENUM('income','expense') | Tipe kategori |
| color | VARCHAR(7) | Hex color, misal `#FF5722` |
| icon | VARCHAR(50) | Nama icon |
| created_at | TIMESTAMP | |
| updated_at | TIMESTAMP | |

---

### wallets

| Kolom | Tipe | Keterangan |
|-------|------|-----------|
| id | BIGINT UNSIGNED PK | |
| user_id | BIGINT UNSIGNED FK | → users.id |
| name | VARCHAR(255) | Nama wallet, misal "BCA", "GoPay" |
| type | ENUM('cash','bank','ewallet') | Tipe wallet |
| color | VARCHAR(7) | Hex color |
| icon | VARCHAR(50) | Nama icon |
| balance | DECIMAL(15,2) DEFAULT 0 | Saldo tersimpan — di-update saat transaksi create/update/delete |
| created_at | TIMESTAMP | |
| updated_at | TIMESTAMP | |

**Business rules:**
- `cash`: max 1 per user, dibuat otomatis saat register via `User::created` event
- `bank` / `ewallet`: tidak terbatas per user

---

### transactions

| Kolom | Tipe | Keterangan |
|-------|------|-----------|
| id | BIGINT UNSIGNED PK | |
| user_id | BIGINT UNSIGNED FK | → users.id |
| category_id | BIGINT UNSIGNED FK | → categories.id |
| wallet_id | BIGINT UNSIGNED FK | → wallets.id |
| title | VARCHAR(255) | Deskripsi singkat |
| amount | DECIMAL(15,2) | Nominal (selalu positif) |
| type | ENUM('income','expense') | Tipe transaksi |
| date | DATE | Tanggal transaksi |
| note | TEXT NULL | Catatan opsional |
| created_at | TIMESTAMP | |
| updated_at | TIMESTAMP | |

---

## Migrasi (urutan)

1. `create_users_table` — default Laravel
2. `create_categories_table`
3. `create_wallets_table`
4. `create_transactions_table`

## Indexing

Index yang direkomendasikan untuk performa query statistik:

```sql
-- Filter transaksi per user + bulan
INDEX idx_transactions_user_date ON transactions(user_id, date);
-- Filter per wallet
INDEX idx_transactions_wallet ON transactions(wallet_id);
-- Filter per kategori
INDEX idx_transactions_category ON transactions(category_id);
```

## Constraints

- Semua foreign key: `ON DELETE CASCADE` (hapus user → hapus semua datanya)
- `amount` selalu positif — tipe (income/expense) yang menentukan arah

---

## Business Rules: Balance Wallet

`wallets.balance` adalah nilai **tersimpan**, bukan computed:

- Saat wallet dibuat → `balance` diisi dari input user (saldo awal, default 0)
- Saat transaksi di-`POST` → `balance` di-update: `income` tambah, `expense` kurang
- Saat transaksi di-`PUT` → balance di-reverse dari nilai lama, lalu apply nilai baru
- Saat transaksi di-`DELETE` → balance di-reverse dari nilai transaksi yang dihapus
- Kalkulasi semua di `TransactionController`, bukan di model event

```php
// Contoh di TransactionController@store
$wallet = Wallet::findOrFail($request->wallet_id);
$delta = $request->type === 'income' ? $request->amount : -$request->amount;
$wallet->increment('balance', $delta);
```

---

### budgets

| Kolom | Tipe | Keterangan |
|-------|------|-----------|
| id | BIGINT UNSIGNED PK | |
| user_id | BIGINT UNSIGNED FK | → users.id |
| category_id | BIGINT UNSIGNED FK | → categories.id |
| month | TINYINT UNSIGNED | 1–12 |
| year | SMALLINT UNSIGNED | |
| amount | DECIMAL(15,2) | Budget yang ditetapkan |
| created_at | TIMESTAMP | |
| updated_at | TIMESTAMP | |

**Unique:** `(user_id, category_id, month, year)` — satu budget per kategori per bulan.
`spent` tidak disimpan di DB — dihitung live dari `transactions` saat GET.

---

### savings_goals

| Kolom | Tipe | Keterangan |
|-------|------|-----------|
| id | BIGINT UNSIGNED PK | |
| user_id | BIGINT UNSIGNED FK | → users.id |
| name | VARCHAR(255) | Nama target tabungan |
| target_amount | DECIMAL(15,2) | Nominal target |
| current_amount | DECIMAL(15,2) DEFAULT 0 | Nominal terkumpul (update manual) |
| deadline | DATE NULL | Tenggat waktu (opsional) |
| is_completed | BOOLEAN DEFAULT false | Auto-true jika current >= target |
| created_at | TIMESTAMP | |
| updated_at | TIMESTAMP | |

---

## Default Categories (Seed saat Register)

Dibuat otomatis via `UserObserver` atau `RegisterController` bersamaan dengan cash wallet.

| Nama | Tipe |
|------|------|
| Gaji | income |
| Bisnis | income |
| Makan | expense |
| Transport | expense |
| Belanja | expense |
| Hiburan | expense |
| Kesehatan | expense |
| Tagihan | expense |
