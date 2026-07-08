# AGENTS.md — Duit

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

- **Backend (Laravel):** Pakai PHP dan MySQL dari **XAMPP**. PHP binary di `/opt/lampp/bin/php`, MySQL di `/opt/lampp/bin/mysql`. Apache jalan di port 80, MySQL di port 3306.
- **Web (Astro):** Node.js 20+, jalankan dengan `npm run dev`.
- **Android:** Android Studio, emulator atau device fisik.

Jangan assume environment lain (Docker, Valet, Herd) kecuali user bilang berbeda.

**Jangan jalankan server secara otomatis.** AI agent tidak boleh menjalankan `php artisan serve` atau `npm run dev` tanpa perintah eksplisit dari user. Cukup beritahu user perintah yang perlu dijalankan.

**Menjalankan perintah PHP/Laravel:** Selalu pakai `/opt/lampp/bin/php`, bukan `php` system. Contoh:
```bash
/opt/lampp/bin/php artisan test
/opt/lampp/bin/php artisan migrate
```

**Testing:** Test suite pakai MySQL XAMPP (`duit_test` database). Database `duit_test` harus sudah dibuat sebelum test pertama kali dijalankan:
```bash
/opt/lampp/bin/mysql -u root -e "CREATE DATABASE IF NOT EXISTS duit_test CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
```
Config test ada di `backend/phpunit.xml` — `DB_CONNECTION=mysql`, `DB_DATABASE=duit_test`.

---

## Dokumentasi Wajib Dibaca Sebelum Implementasi

Sebelum menyentuh kode apapun, baca dokumen yang relevan:

| Pekerjaan | Baca dulu |
|-----------|-----------|
| Semua | [`docs/01-project-overview.md`](docs/01-project-overview.md) + [`docs/07-roadmap.md`](docs/07-roadmap.md) |
| Fitur baru / bug | [`docs/09-tasks.md`](docs/09-tasks.md) + [`docs/01-project-overview.md`](docs/01-project-overview.md) |
| Arsitektur / desain | [`docs/02-architecture.md`](docs/02-architecture.md) |
| Implementasi Laravel | [`docs/04-backend-api.md`](docs/04-backend-api.md) + [`docs/06-database.md`](docs/06-database.md) |
| Implementasi Astro | [`docs/05-dashboard.md`](docs/05-dashboard.md) + [`docs/08-ui-ux.md`](docs/08-ui-ux.md) + [`DESIGN.md`](DESIGN.md) |
| Implementasi Android | [`docs/03-mobile-app.md`](docs/03-mobile-app.md) + [`DESIGN.md`](DESIGN.md) |
| UI/UX & design system | [`DESIGN.md`](DESIGN.md) + [`docs/08-ui-ux.md`](docs/08-ui-ux.md) |

---

## Update Dokumentasi

**Setelah setiap perubahan**, update dokumen yang terpengaruh:

- Fitur baru → update [`docs/09-tasks.md`](docs/09-tasks.md) dan [`docs/07-roadmap.md`](docs/07-roadmap.md)
- Perubahan schema DB → update [`docs/06-database.md`](docs/06-database.md) dan [`docs/04-backend-api.md`](docs/04-backend-api.md)
- Perubahan API endpoint → update [`docs/04-backend-api.md`](docs/04-backend-api.md) dan [`docs/03-mobile-app.md`](docs/03-mobile-app.md)
- Perubahan business rule → update semua dokumen yang menyebutnya
- Tambah/hapus screen Android → update [`docs/03-mobile-app.md`](docs/03-mobile-app.md)
- Tambah/hapus halaman Astro → update [`docs/05-dashboard.md`](docs/05-dashboard.md)

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

## Batasan (Jangan Implementasi Sebelum Waktunya)

Fitur ini ada di roadmap tapi **belum waktunya** — tolak jika diminta sebelum versinya:

- Export PDF/Excel → v2+
- Family wallet / shared budget → v5

---

## Progress v4 ✅ Done

### Selesai
- ✅ `VoiceParser.kt`: regex ekstrak nominal, tipe, judul dari speech text (on-device, no LLM)
- ✅ `VoiceViewModel.kt`: `SpeechRecognizer` via `DisposableEffect`, locale `id-ID`
- ✅ `VoiceInputScreen.kt`: fullscreen, pola OcrScreen, mic button + hasil teks + konfirmasi
- ✅ `AndroidManifest.xml`: permission `RECORD_AUDIO`
- ✅ `NavGraph.kt`: route `voice`, `Screen.Voice`, FAB item 3 (Mic icon), exclude voice dari `showBottomBar`, voice args di `Screen.Add`
- ✅ `AddTransactionScreen.kt`: param `voicePrefill: Triple<String,String,String>?` + `LaunchedEffect(voicePrefill)` apply title/amount/type
- ✅ `VoiceParserTest.kt`: 12 unit test

### Catatan Teknis
- Entry point: FAB HomeScreen item ke-3 (Mic icon) → VoiceInputScreen → AddTransactionScreen prefill
- `SpeechRecognizer` API native Android, tidak ada dependency tambahan
- Parsing: amount (angka + kata seperti "lima puluh ribu"), type (income/expense keywords), title (sisa teks)

---

## Progress v3 ✅ Done

### Selesai
- ✅ `OcrParser.kt`: regex ekstrak nominal, tanggal, judul (on-device, no LLM)
- ✅ `OcrViewModel.kt`: ML Kit TextRecognition + `@Volatile isStopped` flag (infinite loop fix)
- ✅ `OcrScreen.kt`: fullscreen CameraX preview (`FILL_CENTER + COMPATIBLE_MODE`), floating back button
- ✅ `NavGraph.kt`: route `ocr`, OCR result navigate ke `AddTransactionScreen` via nav args `ocr_title/ocr_amount/ocr_date`
- ✅ `AddTransactionScreen.kt`: prefill dari nav args, `onNavigateToOcr` dihapus
- ✅ FAB speed dial di HomeScreen: pill items (`AnimatedVisibility` slide-up/fade) — Scan Struk + Tambah Manual
- ✅ `OcrParserTest.kt`: 8 unit test

### Catatan Teknis
- FAB menu pakai custom `FabMenuItem` composable (M3 1.3.x / BOM 2024.12.01 stable)
- `FloatingActionButtonMenu` dari M3 Expressive ditunda → masih di 1.5.0-alpha (unreleased)
- OCR entry point: FAB HomeScreen → bukan dari `AddTransactionScreen` TopAppBar

---

## Progress v2 ✅ Done

### Selesai
- ✅ Password reset via email (`PasswordResetController`)
- ✅ Email verification (`EmailVerificationController`)
- ✅ Two-factor authentication TOTP (`TwoFactorController`)
- ✅ Android: `TotpScreen` + `TotpViewModel` untuk 2FA login flow
- ✅ Budget bulanan per kategori (backend + Android)
- ✅ Target tabungan dengan progress tracker (backend + Android)
- ✅ Notifikasi lokal: budget hampir habis, target tercapai (WorkManager)
- ✅ Local cache Android (offline-first) + Room database

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

---

## Aturan Wajib untuk AI Agent

### Testing

Setiap perubahan di codebase — terutama yang menyentuh fitur — **wajib disertai unit test**.

- **Laravel**: pakai PHPUnit (`tests/Feature/` untuk endpoint, `tests/Unit/` untuk logic)
- **Android**: pakai JUnit4 + Mockito untuk ViewModel dan Repository
- **Astro**: tidak ada unit test di v1 (SSR frontmatter trivial), tapi logic helper di `src/lib/` wajib di-test jika ada

Tujuan: mendeteksi error dan bug tanpa harus menyentuh user.

### Roadmap

Ikuti urutan versi di [`docs/07-roadmap.md`](docs/07-roadmap.md). Jangan implementasi fitur v2+ sebelum semua item v1 selesai. Jika user meminta fitur yang belum waktunya, tolak dan tunjukkan di roadmap versi berapa fitur itu dijadwalkan.

### Update Versi Dokumentasi

Setelah menyelesaikan implementasi fitur atau milestone, update status di dokumen yang relevan:

- Tandai task selesai di [`docs/09-tasks.md`](docs/09-tasks.md)
- Update progress/status di [`docs/07-roadmap.md`](docs/07-roadmap.md) jika milestone tercapai
- Update versi atau catatan perubahan di doc yang terpengaruh (schema, endpoint, screen)

Setiap keputusan teknis yang tidak trivial **wajib dikonfirmasi ke user** sebelum diimplementasi.

Yang termasuk decision making:
- Pilihan library atau dependency baru
- Perubahan struktur database atau schema
- Perubahan flow auth atau security
- Trade-off arsitektur (performa vs simplisitas, dll)
- Sesuatu yang tidak ada di dokumentasi dan butuh interpretasi

Format konfirmasi:
```
Saya akan [deskripsi keputusan].
Alasan: [kenapa].
Alternatif: [opsi lain jika ada].
Lanjut?
```
