# Duit

> **"Duitmu, di tanganmu."**

Ekosistem pencatatan keuangan personal yang cepat, terpusat, dan bisa di-self-host. Input dari Android, analisis di dashboard web.

## Deskripsi Singkat

Duit adalah aplikasi keuangan personal tiga komponen (backend API + dashboard web + app Android) yang dirancang untuk berjalan di shared hosting biasa. Catat transaksi dalam hitungan detik dari HP, lalu lihat ringkasan, grafik, dan riwayat di dashboard web. Tanpa LLM, tanpa Redis, tanpa queue worker — semua sinkron dan sederhana.

## Tech Stack

| Komponen | Teknologi | Versi |
|----------|-----------|-------|
| **Backend** | Laravel + PHP + MySQL | Laravel 12, PHP 8.2, MySQL 8 |
| **Dashboard Web** | Astro + Tailwind CSS | Astro 7, Tailwind 4 |
| **App Android** | Kotlin + Jetpack Compose | Kotlin 2.0, Compose BOM 2024.12, Material 3 Expressive |
| **Auth** | Laravel Sanctum (token-based) | — |
| **DI (Android)** | Hilt | — |
| **Testing** | PHPUnit (backend), JUnit4 + Mockito (Android) | — |

## Alur dan Perencanaan

### Arsitektur

```
┌─────────────┐      ┌──────────────┐      ┌─────────────┐
│   Android   │      │   Laravel    │      │    MySQL    │
│  (Kotlin)   │ ───→ │     API      │ ───→ │  Database   │
│  Input data │      │  Single SoT  │      │             │
└─────────────┘      └──────┬───────┘      └─────────────┘
                            │
                            ▼
                     ┌──────────────┐
                     │     Astro    │
                     │  Dashboard   │
                     │  (read-only) │
                     └──────────────┘
```

**Prinsip arsitektur:**
- Android = input utama
- Web = analisis (read-only v1–v4)
- Backend = single source of truth
- Tidak ada LLM — semua parsing on-device di versi mendatang
- Shared hosting friendly — tanpa queue, tanpa Redis

### Roadmap

| Versi | Fokus | Status |
|-------|-------|--------|
| **v1** | Input manual, Laravel API, Astro dashboard | 🟡 Pending Deploy |
| **v2** | Budget & savings, Room database, notifikasi | ⬜ Backlog |
| **v3** | OCR scan struk (on-device ML Kit) | ⬜ Backlog |
| **v4** | Voice input (on-device SpeechRecognizer) | ⬜ Backlog |
| **v5** | Multi-user lanjutan (family wallet) | ⬜ Backlog |

Detail roadmap: [`docs/07-roadmap.md`](docs/07-roadmap.md)

### Business Rules Penting

- **Wallet cash:** maksimal 1 per user, dibuat otomatis saat register, tidak bisa dihapus
- **Wallet bank/ewallet:** tidak terbatas per user
- **Transaksi:** wajib ada `wallet_id` dan `category_id`
- **Data isolation:** semua query HARUS filter by `user_id` — user A tidak bisa akses data user B

## Cara Instalasi

### Prasyarat

- PHP 8.2+ dengan ekstensi: `pdo_mysql`, `mbstring`, `openssl`, `json`
- MySQL 8+ (atau MariaDB 10.4+)
- Composer 2+
- Node.js 20+ dan npm 10+
- Android Studio (Koala+ / 2024.1+) dengan Android SDK 35
- XAMPP (disarankan untuk lokal) — Apache port 80, MySQL port 3306

### 1. Clone Repository

```bash
git clone https://github.com/USERNAME/personal-finance.git
cd personal-finance
```

### 2. Setup Backend (Laravel)

```bash
cd backend
composer install
cp .env.example .env
php artisan key:generate
```

Edit `.env`:

```env
APP_URL=http://localhost:8000
DB_CONNECTION=mysql
DB_HOST=127.0.0.1
DB_PORT=3306
DB_DATABASE=duit
DB_USERNAME=root
DB_PASSWORD=

SANCTUM_STATEFUL_DOMAINS=localhost:4321
SESSION_DOMAIN=localhost
```

Jalankan migrasi dan seed:

```bash
php artisan migrate --seed
php artisan serve   # jalan di http://localhost:8000
```

Verifikasi: `php artisan test` — harus 19/19 passed.

### 3. Setup Dashboard Web (Astro)

```bash
cd ../web
npm install
cp .env.example .env
```

Edit `.env`:

```env
API_BASE_URL=http://localhost:8000/api
```

Jalankan:

```bash
npm run dev     # jalan di http://localhost:4321
```

Build production: `npm run build`

### 4. Setup App Android

1. Buka folder `android/` di Android Studio
2. Tunggu Gradle sync selesai
3. Update `NetworkModule.kt` dengan URL backend:
   ```kotlin
   // android/app/src/main/kotlin/com/duit/app/data/di/NetworkModule.kt
   private const val BASE_URL = "http://10.0.2.2:8000/api/"   // emulator
   // atau IP host untuk device fisik
   ```
4. Jalankan di emulator atau device fisik (min SDK 31, target SDK 35)

**Catatan:** Emulator mengakses host via `10.0.2.2`. Device fisik butuh IP LAN host + port forwarding.

### 5. Konfigurasi CORS (untuk Astro Dashboard)

Pastikan `config/cors.php` di backend mengizinkan domain Astro:

```php
'allowed_origins' => [
    'http://localhost:4321',
    // tambahkan domain production di sini
],
```

### 6. Verifikasi End-to-End

1. Register user baru di Android app
2. Tambah transaksi (misal: "Kopi", 18000, expense)
3. Buka dashboard web di browser, login dengan user yang sama
4. Cek transaksi muncul di dashboard

Jika semua berfungsi, instalasi sukses.

## Struktur Repo

```
personal-finance/
├── backend/        # Laravel REST API (PHP)
├── web/            # Astro SSR dashboard (Node.js)
├── android/        # Kotlin + Jetpack Compose
├── docs/           # Semua dokumentasi
├── DESIGN.md       # Design system
├── AGENTS.md       # Panduan AI agent
└── README.md       # File ini
```

## Dokumentasi

- [Project Overview](docs/01-project-overview.md)
- [Arsitektur](docs/02-architecture.md)
- [Mobile App](docs/03-mobile-app.md)
- [Backend API](docs/04-backend-api.md)
- [Dashboard Web](docs/05-dashboard.md)
- [Database](docs/06-database.md)
- [Roadmap](docs/07-roadmap.md)
- [UI/UX](docs/08-ui-ux.md)
- [Tasks](docs/09-tasks.md)
- [Design System](DESIGN.md)

## License

Proyek ini dilisensikan di bawah lisensi MIT — lihat file [LICENSE](LICENSE).
