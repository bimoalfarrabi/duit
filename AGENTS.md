# AGENTS.md — Personal Finance Ecosystem

Panduan untuk AI agent saat bekerja di repo ini.

---

## Struktur Repo

Monorepo dengan 3 sub-project independen:

```
personal-finance/
├── backend/        # Laravel REST API (PHP)
├── web/            # Astro SSR dashboard (Node.js)
├── android/        # Kotlin + Jetpack Compose
└── docs/           # Semua dokumentasi — BACA DULU sebelum implementasi
```

---

## Environment Lokal

- **Backend (Laravel):** Pakai PHP dan MySQL dari **XAMPP**. Apache jalan di port 80, MySQL di port 3306.
- **Web (Astro):** Node.js 20+, jalankan dengan `npm run dev`.
- **Android:** Android Studio, emulator atau device fisik.

Jangan assume environment lain (Docker, Valet, Herd) kecuali user bilang berbeda.

---

## Dokumentasi Wajib Dibaca Sebelum Implementasi

Sebelum menyentuh kode apapun, baca dokumen yang relevan:

| Pekerjaan | Baca dulu |
|-----------|-----------|
| Semua | [`docs/ROADMAP.md`](docs/ROADMAP.md) |
| Fitur baru / bug | [`docs/prd/v1-mvp.md`](docs/prd/v1-mvp.md) |
| Arsitektur / desain | [`docs/superpowers/specs/2026-07-03-personal-finance-ecosystem-design.md`](docs/superpowers/specs/2026-07-03-personal-finance-ecosystem-design.md) |
| Implementasi Laravel | [`docs/superpowers/plans/2026-07-03-laravel-api-plan.md`](docs/superpowers/plans/2026-07-03-laravel-api-plan.md) |
| Implementasi Astro | [`docs/superpowers/plans/2026-07-03-astro-dashboard-plan.md`](docs/superpowers/plans/2026-07-03-astro-dashboard-plan.md) |
| Implementasi Android | [`docs/superpowers/plans/2026-07-03-android-app-plan.md`](docs/superpowers/plans/2026-07-03-android-app-plan.md) |

---

## Update Dokumentasi

**Setelah setiap perubahan**, update dokumen yang terpengaruh:

- Fitur baru → update PRD (`docs/prd/`) dan implementation plan yang relevan
- Perubahan schema DB → update design spec dan plan Laravel
- Perubahan API endpoint → update design spec, PRD, dan plan Android (ApiService)
- Perubahan business rule → update semua dokumen yang menyebutnya
- Tambah/hapus screen Android → update plan Android
- Tambah/hapus halaman Astro → update plan Astro

Jangan pernah biarkan kode dan dokumentasi tidak sinkron.

---

## Commit Message

Format: **Conventional Commits** dengan deskripsi penjelasan di body.

```
<type>(<scope>): <judul singkat>

<deskripsi: apa yang berubah, kenapa, dan dampaknya>
```

**Type yang dipakai:**
- `feat` — fitur baru
- `fix` — bug fix
- `docs` — perubahan dokumentasi saja
- `refactor` — perubahan kode tanpa mengubah behavior
- `test` — tambah atau perbaiki test
- `chore` — update dependency, config, tooling

**Scope:** `backend`, `web`, `android`, `docs`

**Contoh:**
```
feat(backend): add wallet CRUD endpoints

Menambahkan endpoint GET/POST/PUT/DELETE untuk resource wallets.
Cash wallet dibuat otomatis saat register via User::created event.
Validasi: user hanya bisa punya 1 wallet bertipe cash.
```

```
fix(android): wallet picker not loading in AddTransactionScreen

WalletRepository tidak di-inject ke AddTransactionViewModel.
Tambah @Inject constructor dan bind di NetworkModule.
```

---

## Skill yang Harus Digunakan

Selalu load skill yang relevan sebelum mulai kerja:

| Konteks | Skill |
|---------|-------|
| Backend Laravel (model, controller, Sanctum, Eloquent) | `laravel-specialist` |
| Android (Kotlin, Jetpack Compose, navigation, architecture) | `android-clean-architecture`, `mobile-android-design` |
| UI/UX (layout, warna, spacing, komponen) | `ui-ux-pro-max`, `make-interfaces-feel-better` |
| Frontend Astro/web | `design-taste-frontend`, `frontend-design` |
| Fitur baru yang kompleks | `feature-research` sebelum implementasi |
| Bug yang sulit | `systematic-debugging` |
| Security (auth, input validation) | `owasp-security` |
| Eksplorasi codebase | `explore` |

---

## Konvensi Kode

### Laravel (backend/)
- Controller hanya di `app/Http/Controllers/Api/`
- Semua response pakai format: `{ "data": ..., "message": ..., "status": true }`
- Semua route API di bawah `middleware('auth:sanctum')` kecuali `register` dan `login`
- Ownership check wajib di setiap endpoint yang akses resource user lain
- Validasi input pakai `$request->validate([...])`

### Astro (web/)
- Semua data fetch **server-side** di frontmatter — tidak ada client-side fetch
- Auth via HTTP-only cookie `auth_token`
- Helper API di `src/lib/api.ts` — gunakan fungsi `apiFetch` yang sudah ada

### Android (android/)
- Architecture: UI → ViewModel → Repository (3 layer, tidak lebih)
- Token disimpan di `EncryptedSharedPreferences` via `TokenStorage`
- Semua network call lewat `ApiService` di `data/remote/`
- Error handling: tampil Snackbar, tidak crash

---

## Batasan v1 (Jangan Implementasi Sebelum Waktunya)

Fitur ini ada di roadmap tapi **bukan v1** — tolak jika diminta sebelum v1 selesai:

- OCR scan struk → v3
- Voice input → v4
- Budget & target tabungan → v2
- Notifikasi → v2
- Offline mode / Room database → v2
- Export PDF/Excel → v2+
- Family wallet / shared budget → v5

---

## Business Rules Penting

- **Wallet cash:** max 1 per user, dibuat otomatis saat register, tidak bisa dihapus
- **Wallet bank/ewallet:** tidak terbatas per user
- **Transaksi:** wajib ada `wallet_id` dan `category_id`
- **Data isolation:** semua query HARUS filter by `user_id` — user A tidak boleh akses data user B
- **Tidak ada LLM:** semua parsing (OCR, voice) dilakukan on-device di Android
- **Shared hosting:** tidak ada queue worker, tidak ada Redis, semua request sinkron

---

## Urutan Development

1. **Laravel API** — selesaikan dulu sebelum mulai Astro atau Android
2. **Astro Dashboard** — bisa paralel setelah endpoint auth + statistics siap
3. **Android App** — bisa mulai setelah auth + transaction endpoint siap
