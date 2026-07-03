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
| password | VARCHAR(255) | Bcrypt |
| created_at | TIMESTAMP | |
| updated_at | TIMESTAMP | |

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
| balance | DECIMAL(15,2) DEFAULT 0 | Saldo awal (manual) |
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
