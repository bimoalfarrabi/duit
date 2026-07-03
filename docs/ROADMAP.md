# Personal Finance Ecosystem — Roadmap

## Versi & Scope

| Versi | Fokus | Status |
|-------|-------|--------|
| [v1 — MVP](#v1) | Input manual, Laravel API, Astro dashboard | 🔵 Planned |
| [v2 — Budget & Savings](#v2) | Budget bulanan, target tabungan, notifikasi | ⬜ Backlog |
| [v3 — OCR](#v3) | Scan struk via Google ML Kit | ⬜ Backlog |
| [v4 — Voice](#v4) | Input suara via Android SpeechRecognizer | ⬜ Backlog |
| [v5 — Multi-user](#v5) | Family wallet, shared budget | ⬜ Backlog |

---

## v1 — MVP

**Goal:** Ekosistem bisa dipakai — catat transaksi manual, lihat di dashboard.

### Backend (Laravel)
- Auth (register, login, logout)
- CRUD transaksi
- CRUD kategori
- CRUD sumber dana (wallet: cash singleton, bank/ewallet multiple)
- Statistik (summary, by-category, by-wallet, monthly)

### Web (Astro SSR)
- Login
- Dashboard: ringkasan saldo per wallet, grafik bulanan, grafik kategori
- Riwayat transaksi + filter (bulan, tipe, wallet)
- Export laporan (link ke endpoint Laravel)

### Android (Kotlin + Jetpack Compose)
- Login
- Home screen: saldo per wallet + transaksi terbaru
- Tambah transaksi (manual) dengan pilih wallet
- Riwayat transaksi + filter per wallet
- Manajemen kategori
- Manajemen wallet (cash singleton, bank/ewallet multiple)

---

## v2 — Budget & Savings

**Goal:** Kontrol keuangan proaktif — budget, target, pengingat.

- Budget bulanan per kategori
- Target tabungan dengan progress tracker
- Notifikasi: budget hampir habis, target tercapai
- Local cache (offline-first untuk Android)

---

## v3 — OCR

**Goal:** Input struk fisik tanpa ketik manual.

- Kamera → Google ML Kit Text Recognition
- Parser regex: ekstrak nominal, judul, tanggal dari teks OCR
- Review screen sebelum submit ke API
- Server hanya terima JSON hasil parsing (tidak ada processing di server)

---

## v4 — Voice

**Goal:** Input transaksi via suara ("kopi 18000").

- Android SpeechRecognizer → teks
- Parser kalimat sederhana: `{judul} {nominal}` → JSON
- Contoh: "parkir 5000" → `{ title: "parkir", amount: 5000, type: "expense" }`
- Konfirmasi sebelum submit

---

## v5 — Multi-user Lanjutan

**Goal:** Keuangan bersama — keluarga atau pasangan.

- Family wallet: satu wallet bersama
- Shared budget: budget yang bisa dilihat semua member
- Permission: owner vs member
- Notifikasi aktivitas member

---

## Prinsip

- **Tidak ada LLM** — semua parsing on-device, server hanya terima data
- **Shared hosting friendly** — tidak ada queue worker, tidak ada Redis
- **Android sebagai input utama** — web hanya untuk analisis (read-only di v1-v4)
