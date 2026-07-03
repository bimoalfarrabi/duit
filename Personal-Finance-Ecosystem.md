# Personal Finance Ecosystem

## Deskripsi

Aplikasi pencatatan pemasukan dan pengeluaran dengan **Android
(Kotlin)** sebagai media input, **Laravel** sebagai backend API, dan
**Astro** sebagai dashboard web.

## Tujuan

-   Mencatat transaksi dengan cepat.
-   Mendukung input manual, foto struk, dan suara.
-   Tetap dapat dijalankan menggunakan shared hosting.

## Tech Stack

  Komponen    Teknologi
  ----------- --------------------------------
  Mobile      Kotlin + Jetpack Compose
  OCR         Google ML Kit Text Recognition
  Voice       Android SpeechRecognizer
  Backend     Laravel REST API
  Database    MySQL
  Dashboard   Astro + Tailwind CSS
  Hosting     Shared Hosting

## Arsitektur

``` text
Android App
    |
    | REST API
    v
Laravel
    |
    v
MySQL
    |
    v
Astro Dashboard
```

## Fitur MVP

### Android

-   Login
-   Tambah pemasukan
-   Tambah pengeluaran
-   Pilih kategori
-   Riwayat transaksi
-   Sinkronisasi ke server

### Backend

-   REST API
-   Autentikasi
-   CRUD transaksi
-   CRUD kategori
-   Statistik

### Dashboard

-   Ringkasan saldo
-   Grafik bulanan
-   Grafik kategori
-   Riwayat transaksi
-   Export laporan

## Roadmap

### v1

-   Input manual
-   Dashboard
-   REST API

### v2

-   Budget bulanan
-   Target tabungan
-   Notifikasi

### v3

-   Scan struk menggunakan ML Kit
-   Parser struk

### v4

-   Input suara
-   Parser kalimat

### v5

-   Multi-user
-   Family wallet
-   Shared budget

## Alur OCR

``` text
Foto
 ↓
ML Kit OCR
 ↓
Teks
 ↓
Parser
 ↓
JSON
 ↓
Laravel API
```

## Alur Voice

``` text
Suara
 ↓
SpeechRecognizer
 ↓
Teks
 ↓
Parser
 ↓
Laravel API
```

## Parser

Contoh: - "kopi 18000" - "parkir 5000" - "gajian 5000000"

Parser menghasilkan: - Judul transaksi - Nominal - Jenis transaksi -
Kategori - Tanggal

## Struktur API

-   POST /api/login
-   GET /api/categories
-   POST /api/transactions
-   GET /api/transactions
-   GET /api/statistics

## Pengembangan Selanjutnya

-   Scan QRIS
-   Attachment nota
-   Rekening & e-wallet
-   Scheduled transaction
-   Export PDF/Excel
-   Multi-currency
-   Dark mode
-   PWA Dashboard

## Catatan

LLM tidak digunakan. OCR dilakukan di perangkat menggunakan Google ML
Kit dan pengenalan suara menggunakan Android SpeechRecognizer. Server
hanya menerima data hasil parsing sehingga cocok dijalankan pada shared
hosting.
