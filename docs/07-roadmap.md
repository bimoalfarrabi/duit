# 🚀 Roadmap

## Status

| Versi | Fokus | Status |
|-------|-------|--------|
| **v1** | Input manual, Laravel API, Astro dashboard | ✅ Live |
| **v2** | Budget & savings | 🔄 In Progress |
| **v3** | OCR scan struk | ⬜ Backlog |
| **v4** | Voice input | ⬜ Backlog |
| **v5** | Multi-user lanjutan | ⬜ Backlog |

---

## v1 — MVP

**Goal:** Ekosistem minimal yang berfungsi penuh.

- Auth multi-user (register, login)
- CRUD transaksi dengan kategori dan wallet
- CRUD kategori (income/expense)
- CRUD wallet: cash (singleton), bank/ewallet (multiple)
- Statistik: summary bulanan, by-category, by-wallet, trend 12 bulan
- Export CSV
- Dashboard web read-only: saldo, grafik, riwayat, filter
- Android: tambah transaksi manual, riwayat, kategori, wallet

---

## v2 — Budget & Savings

**Goal:** Kontrol keuangan proaktif.

- Budget bulanan per kategori
- Target tabungan dengan progress tracker
- Notifikasi: budget hampir habis, target tercapai
- Local cache Android (offline-first)
- Room database di Android
- **Auth lanjutan** (manual, tanpa Fortify):
  - Password reset via email (token-based, expire 1 jam)
  - Email verification saat register
  - Two-factor authentication (TOTP / Google Authenticator)

---

## v3 — OCR

**Goal:** Input struk fisik tanpa ketik manual.

- Kamera → Google ML Kit Text Recognition (on-device)
- Parser regex: ekstrak nominal, judul, tanggal
- Review screen sebelum submit
- Server hanya terima JSON hasil parsing

---

## v4 — Voice

**Goal:** Input transaksi via suara.

- Android SpeechRecognizer → teks (on-device)
- Parser kalimat: `"kopi 18000"` → `{ title, amount, type }`
- Konfirmasi sebelum submit

**Tidak ada LLM** — semua parsing on-device.

---

## v5 — Multi-user Lanjutan

**Goal:** Keuangan bersama.

- Family wallet (satu wallet bersama)
- Shared budget
- Permission: owner vs member
- Notifikasi aktivitas member

---

## Prinsip

- Tidak ada LLM — parsing on-device
- Shared hosting friendly — tanpa queue, tanpa Redis
- Android = input utama, web = analisis (read-only v1–v4)
