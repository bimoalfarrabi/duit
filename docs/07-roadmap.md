# 🚀 Roadmap

## Status

| Versi | Fokus | Status |
|-------|-------|--------|
| **v1** | Input manual, Laravel API, Astro dashboard | ✅ Live |
| **v2** | Budget & savings | ✅ Done |
| **v3** | OCR scan struk | ✅ Done |
| **v4** | Voice input | ✅ Done |
| **v5** | Multi-user lanjutan | 🚧 In Progress (backend done) |

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
- Prefill `AddTransactionScreen` langsung via nav args
- Entry point: FAB speed dial di HomeScreen (bukan dari form)

**Tidak ada LLM** — semua parsing on-device.

---

## v4 — Voice

**Goal:** Input transaksi via suara.

- Android SpeechRecognizer → teks (on-device)
- Parser kalimat: `"kopi 18000"` → `{ title, amount, type }`
- Konfirmasi sebelum submit
- Entry point: FAB speed dial di HomeScreen — item ketiga setelah Scan Struk + Tambah Manual

**Tidak ada LLM** — semua parsing on-device.

---

## v5 — Multi-user Lanjutan

**Goal:** Keuangan bersama.

**Arsitektur:** Per-wallet sharing via pivot `wallet_user` (bukan household terpisah).
`wallets.user_id` tetap owner; member masuk pivot. Undangan via email + accept flow.

**Backend (✅ Done):**
- Shared wallet: owner bisa mengundang user lain via email
- Invite flow: `POST /wallets/{wallet}/invitations` → email → `POST /invitations/{token}/accept|decline`
- Undangan berlaku 7 hari, token `Str::random(64)`
- Permission: owner = full control (edit/hapus wallet, invite, remove member); member = lihat wallet + lihat/buat transaksi
- Transaksi difilter per accessible wallet (owned + shared), backward-compatible untuk user non-shared
- Member management: `GET /wallets/{wallet}/members`, `DELETE /wallets/{wallet}/members/{member}` (owner-only)

**Belum (backlog):**
- Android UI untuk sharing (invite screen, pending invitations, member list)
- Shared budget (desain belum diputuskan)
- Notifikasi aktivitas member

---

## Prinsip

- Tidak ada LLM — parsing on-device
- Shared hosting friendly — tanpa queue, tanpa Redis
- Android = input utama, web = analisis (read-only v1–v4)
