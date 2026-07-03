# PRD — v1 MVP

**Versi:** 1.0
**Tanggal:** 2026-07-03
**Status:** Draft
**Referensi:** [Design Spec](../superpowers/specs/2026-07-03-personal-finance-ecosystem-design.md) · [Roadmap](../ROADMAP.md)

---

## Problem Statement

Tidak ada cara cepat dan terpusat untuk mencatat pemasukan dan pengeluaran harian yang bisa diakses dari HP dan dianalisis lewat web. Spreadsheet manual tidak praktis di mobile; app yang ada terlalu kompleks atau tidak bisa di-self-host.

## Goal

Ekosistem minimal yang berfungsi penuh: catat transaksi dari Android, lihat analisis di web, semua data tersimpan di server sendiri.

## Success Criteria

- User bisa register dan login dari Android maupun web
- User bisa tambah transaksi (pemasukan/pengeluaran) dari Android dalam < 30 detik
- Dashboard web menampilkan saldo, grafik bulanan, dan riwayat transaksi yang akurat
- Semua komponen berjalan di shared hosting

---

## Komponen

### 1. Laravel API (`backend/`)

**Tech:** Laravel 11, PHP 8.2+, MySQL, Sanctum

#### Auth
| Endpoint | Deskripsi |
|----------|-----------|
| `POST /api/auth/register` | Daftar akun baru |
| `POST /api/auth/login` | Login, return token |
| `POST /api/auth/logout` | Revoke token |
| `GET /api/auth/me` | Data user aktif |

#### Kategori
| Endpoint | Deskripsi |
|----------|-----------|
| `GET /api/categories` | List kategori milik user |
| `POST /api/categories` | Buat kategori baru |
| `PUT /api/categories/{id}` | Update kategori |
| `DELETE /api/categories/{id}` | Hapus kategori |

Kategori punya: `name`, `type` (income/expense), `color`, `icon`.

#### Sumber Dana (Wallet)
| Endpoint | Deskripsi |
|----------|-----------|
| `GET /api/wallets` | List wallet milik user |
| `POST /api/wallets` | Buat wallet baru |
| `PUT /api/wallets/{id}` | Update wallet |
| `DELETE /api/wallets/{id}` | Hapus wallet |

Wallet punya: `name`, `type` (cash/bank/ewallet), `color`, `icon`, `balance` (saldo awal).

**Business rules:**
- `cash` — maksimal 1 per user. Dibuat otomatis saat register, tidak bisa dihapus.
- `bank` dan `ewallet` — tidak terbatas, user bebas tambah sebanyak akun yang dimiliki.

#### Transaksi
| Endpoint | Deskripsi |
|----------|-----------|
| `GET /api/transactions` | List transaksi, filter: `?month=&year=&type=&category_id=&wallet_id=` |
| `POST /api/transactions` | Tambah transaksi (wajib sertakan `wallet_id`) |
| `PUT /api/transactions/{id}` | Update transaksi |
| `DELETE /api/transactions/{id}` | Hapus transaksi |

#### Statistik
| Endpoint | Response |
|----------|----------|
| `GET /api/statistics/summary?month=&year=` | `{ income, expense, balance }` bulan itu |
| `GET /api/statistics/by-category?month=&year=` | Breakdown nominal per kategori |
| `GET /api/statistics/monthly?year=` | Array 12 bulan `{ month, income, expense }` |

#### Database Schema
```sql
users          — id, name, email, password, timestamps
categories     — id, user_id, name, type, color, icon, timestamps
wallets        — id, user_id, name, type ENUM('cash','bank','ewallet'), color, icon, balance DECIMAL(15,2) DEFAULT 0, timestamps
transactions   — id, user_id, category_id, wallet_id, title, amount, type, date, note, timestamps
```

#### Constraints
- Shared hosting: tidak ada queue, tidak ada Redis, semua sinkron
- Data terisolasi per user — semua query filter by `user_id`
- Token auth: Bearer untuk Android, HTTP-only cookie untuk Astro

---

### 2. Astro Dashboard (`web/`)

**Tech:** Astro 4+ SSR (`output: 'server'`), Tailwind CSS, Chart.js

#### Halaman

| Route | Deskripsi |
|-------|-----------|
| `/` | Redirect ke `/dashboard` atau `/login` |
| `/login` | Form email + password |
| `/dashboard` | Ringkasan saldo, grafik bulanan, grafik per kategori |
| `/transactions` | Tabel riwayat + filter bulan/tipe/kategori |
| `/reports` | Link download export dari Laravel |

#### Auth Flow
1. `/login` submit ke `POST /api/auth/login` (Laravel)
2. Token disimpan di HTTP-only cookie (server-side Astro)
3. Middleware baca cookie, forward sebagai Bearer ke setiap request Laravel
4. Tidak ada token → redirect ke `/login`

#### Data Fetching
- Semua data di-fetch **server-side** di Astro frontmatter
- Tidak ada client-side fetch, tidak ada state management
- Chart.js di-init via `<script>` tag dengan data JSON yang di-inject dari server

#### Batasan v1
- **Read-only** — tidak ada form tambah/edit transaksi dari web
- Tidak ada real-time update
- Export: redirect ke endpoint Laravel

---

### 3. Android App (`android/`)

**Tech:** Kotlin, Jetpack Compose, Retrofit, Hilt, EncryptedSharedPreferences

#### Architecture
UI → ViewModel → Repository → Retrofit (API)

#### Auth
- Login: `POST /api/auth/login` → simpan token di EncryptedSharedPreferences
- OkHttp Interceptor: inject `Authorization: Bearer {token}` di setiap request
- Tidak ada token → arahkan ke LoginScreen

#### Screens

| Screen | Deskripsi |
|--------|-----------|
| `LoginScreen` | Email + password |
| `HomeScreen` | Saldo hari ini, 5 transaksi terbaru |
| `AddTransactionScreen` | Form: judul, nominal, tipe, kategori, tanggal |
| `TransactionListScreen` | List transaksi, filter per bulan |
| `CategoryScreen` | List kategori + tambah kategori baru |

#### Navigation
Bottom nav: **Home / Add / History**

#### Offline
Tidak ada di v1. No connection → error Snackbar. Cache masuk v2.

---

## Out of Scope (v1)

- OCR scan struk → v3
- Voice input → v4
- Budget & target tabungan → v2
- Notifikasi → v2
- Offline mode → v2
- Multi-currency, export PDF/Excel, PWA, QRIS, family wallet → v5+

---

## Acceptance Criteria

- [ ] Register + login berfungsi di Android dan web
- [ ] Transaksi yang ditambah dari Android muncul di dashboard web
- [ ] Grafik bulanan menampilkan data yang benar
- [ ] Filter transaksi per bulan berfungsi
- [ ] Semua endpoint terproteksi auth (401 tanpa token)
- [ ] Data user A tidak bisa diakses user B
- [ ] Deploy berhasil di shared hosting (PHP shared)
