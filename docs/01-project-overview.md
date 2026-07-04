# 📖 Duit — Project Overview

> **Tagline:** "Duitmu, di tanganmu."

## Visi

Ekosistem pencatatan keuangan personal yang cepat, terpusat, dan bisa di-self-host. Input dari Android, analisis di web.

## Tujuan

- Catat transaksi dalam < 30 detik dari HP
- Lacak pemasukan dan pengeluaran per kategori dan sumber dana
- Lihat analisis dan grafik di dashboard web
- Berjalan di shared hosting biasa (tidak perlu VPS)

## Komponen

| Komponen | Teknologi | Peran |
|----------|-----------|-------|
| Backend | Laravel 12 + MySQL | REST API, single source of truth |
| Dashboard | Astro SSR + Tailwind | Visualisasi read-only |
| Mobile | Kotlin + Jetpack Compose | Input utama |

## Fitur v1 (MVP)

### Backend
- Autentikasi multi-user (register, login, logout)
- CRUD transaksi dengan kategori dan sumber dana
- CRUD kategori (income/expense)
- CRUD wallet/sumber dana (cash singleton, bank/ewallet multiple)
- Statistik: summary bulanan, by-category, by-wallet, trend 12 bulan
- Export transaksi ke CSV

### Dashboard Web
- Login via token Laravel
- Ringkasan saldo + grafik bulanan + grafik per kategori
- Ringkasan saldo per wallet
- Riwayat transaksi + filter (bulan, tipe, wallet)
- Link download export CSV

### Aplikasi Android
- Login
- Home: saldo hari ini + 5 transaksi terbaru
- Tambah transaksi: judul, nominal, tipe, kategori, wallet, tanggal
- Riwayat transaksi + filter bulan + filter wallet
- Manajemen kategori
- Manajemen wallet

## Target Pengguna

Personal — satu orang atau keluarga kecil (multi-user didesain tapi bukan fitur utama v1).

## Constraints

- Shared hosting: PHP, MySQL, tanpa queue worker, tanpa Redis
- Tidak menggunakan LLM (semua parsing on-device di v3/v4)
- Android sebagai satu-satunya input di v1 — web read-only

## Success Criteria v1

- [ ] Register + login berfungsi di Android dan web
- [ ] Transaksi dari Android muncul di dashboard web
- [ ] Grafik bulanan akurat
- [ ] Filter per wallet berfungsi
- [ ] Semua endpoint 401 tanpa token
- [ ] Data user A tidak bisa diakses user B
- [ ] Deploy berhasil di shared hosting
