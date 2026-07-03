# Implementation Plan — Android App (android/)

**Versi:** v1 MVP
**Tanggal:** 2026-07-03
**Referensi:** [PRD v1](../prd/v1-mvp.md) · [Design Spec](../specs/2026-07-03-personal-finance-ecosystem-design.md)
**Depends on:** Laravel API selesai dan running

---

## Scope

Android app input utama. Kotlin + Jetpack Compose, Clean Architecture ringan, Retrofit, Hilt. Input manual only (v1).

---

## Prerequisites

- Android Studio Hedgehog atau lebih baru
- Min SDK 26 (Android 8.0)
- Target SDK 34
- Kotlin 1.9+

---

## Fase 1 — Setup Project

- [ ] Buat project Android baru: Empty Activity, Kotlin, Jetpack Compose
- [ ] Tambah dependencies di `build.gradle.kts`:
  ```kotlin
  // Hilt
  implementation("com.google.dagger:hilt-android:2.51")
  kapt("com.google.dagger:hilt-compiler:2.51")
  // Retrofit + OkHttp
  implementation("com.squareup.retrofit2:retrofit:2.11.0")
  implementation("com.squareup.retrofit2:converter-gson:2.11.0")
  implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
  // Navigation Compose
  implementation("androidx.navigation:navigation-compose:2.7.7")
  // Security
  implementation("androidx.security:security-crypto:1.1.0-alpha06")
  ```
- [ ] Setup Hilt: `@HiltAndroidApp` di Application class
- [ ] `BASE_URL` di `local.properties` atau `BuildConfig`

---

## Fase 2 — Network Layer

### API Interface
File: `data/remote/ApiService.kt`

- [ ] Definisikan semua endpoint sebagai Retrofit interface:
  ```kotlin
  interface ApiService {
      @POST("auth/login") suspend fun login(@Body body: LoginRequest): LoginResponse
      @POST("auth/logout") suspend fun logout(): Unit
      @GET("auth/me") suspend fun me(): UserResponse
      @GET("categories") suspend fun getCategories(): List<CategoryResponse>
      @POST("categories") suspend fun createCategory(@Body body: CreateCategoryRequest): CategoryResponse
      @GET("transactions") suspend fun getTransactions(@QueryMap params: Map<String, String>): List<TransactionResponse>
      @POST("transactions") suspend fun createTransaction(@Body body: CreateTransactionRequest): TransactionResponse
      @GET("statistics/summary") suspend fun getSummary(@Query("month") month: Int, @Query("year") year: Int): SummaryResponse
  }
  ```

### Auth Interceptor
File: `data/remote/AuthInterceptor.kt`

- [ ] OkHttp Interceptor: baca token dari `EncryptedSharedPreferences`, inject `Authorization: Bearer` header
- [ ] Tidak ada token → lewatkan tanpa header (LoginScreen tidak perlu token)

### DI Module
File: `di/NetworkModule.kt`

- [ ] Provide `OkHttpClient` dengan `AuthInterceptor` + `HttpLoggingInterceptor`
- [ ] Provide `Retrofit` dengan base URL
- [ ] Provide `ApiService`

---

## Fase 3 — Data Layer

### Token Storage
File: `data/local/TokenStorage.kt`

- [ ] Wrapper `EncryptedSharedPreferences` untuk simpan/baca/hapus token
- [ ] Interface: `saveToken(token: String)`, `getToken(): String?`, `clearToken()`

### Repositories
- [ ] `AuthRepository` — login, logout, me
- [ ] `CategoryRepository` — getCategories, createCategory
- [ ] `TransactionRepository` — getTransactions, createTransaction
- [ ] `StatisticsRepository` — getSummary

Tiap repository hanya wrap ApiService + return `Result<T>` (stdlib, bukan custom class).

---

## Fase 4 — Domain Models

File: `domain/model/*.kt`

- [ ] `User(id, name, email)`
- [ ] `Category(id, name, type, color, icon)`
- [ ] `Transaction(id, title, amount, type, date, note, category)`
- [ ] `Summary(income, expense, balance)`

Simple data classes, tidak ada logic. Mapper dari response DTO ke domain model ada di Repository.

---

## Fase 5 — Navigation Setup

File: `ui/navigation/AppNavigation.kt`

- [ ] `NavHost` dengan dua graph: `AuthGraph` dan `MainGraph`
- [ ] `AuthGraph`: `LoginScreen`
- [ ] `MainGraph`: `HomeScreen`, `AddTransactionScreen`, `TransactionListScreen`, `CategoryScreen`
- [ ] Bottom nav bar dengan 3 tab: Home / Add / History
- [ ] Startup: cek token → ada → MainGraph, tidak ada → AuthGraph

---

## Fase 6 — LoginScreen

- [ ] `LoginViewModel`: state `email`, `password`, `isLoading`, `error`
- [ ] Call `AuthRepository.login()` → sukses → navigate ke HomeScreen + simpan token
- [ ] Error → tampil Snackbar
- [ ] Input validation sederhana: email tidak kosong, password minimal 6 karakter

---

## Fase 7 — HomeScreen

- [ ] `HomeViewModel`: fetch `getSummary(currentMonth, currentYear)` + `getTransactions(limit=5)`
- [ ] Tampil 3 summary card: Pemasukan / Pengeluaran / Saldo
- [ ] Tampil 5 transaksi terbaru
- [ ] Pull-to-refresh
- [ ] Error state: tampil retry button

---

## Fase 8 — AddTransactionScreen

- [ ] Form fields: judul (TextField), nominal (TextField numeric), tipe (SegmentedButton income/expense), kategori (DropdownMenu dari API), **wallet (DropdownMenu dari API)**, tanggal (DatePicker)
- [ ] `AddTransactionViewModel`: load categories + load wallets, handle submit
- [ ] Submit → `createTransaction()` → sukses → navigate back ke Home + refresh
- [ ] Validasi: judul tidak kosong, nominal > 0, kategori dipilih, wallet dipilih

---

## Fase 9 — TransactionListScreen

- [ ] Filter bar: bulan + tahun + wallet (dropdown sederhana)
- [ ] `LazyColumn` list transaksi
- [ ] `TransactionListViewModel`: fetch ulang saat filter berubah
- [ ] Tidak ada pagination di v1 — load semua sekaligus

---

## Fase 10 — CategoryScreen

- [ ] List kategori user
- [ ] FAB untuk tambah kategori baru (bottom sheet: nama, tipe, warna)
- [ ] `CategoryViewModel`: getCategories + createCategory

---

## Fase 11 — WalletScreen

- [ ] List wallet user (grouped atau flat, tampilkan tipe + saldo)
- [ ] FAB untuk tambah wallet baru (bottom sheet: nama, tipe, warna)
  - Tipe `cash`: tombol tambah disable jika sudah ada (max 1)
  - Tipe `bank`/`ewallet`: tidak terbatas
- [ ] `WalletViewModel`: getWallets + createWallet
- [ ] Wallet `cash` dibuat otomatis saat register — tidak tampilkan form buat cash baru jika sudah ada

---

## Checklist Final

- [ ] App build tanpa error (`./gradlew assembleDebug`)
- [ ] Login berhasil dan token tersimpan
- [ ] Tambah transaksi dari Android → muncul di Astro dashboard
- [ ] Wallet picker tampil di AddTransactionScreen
- [ ] Cash hanya muncul satu di WalletScreen
- [ ] Tidak ada koneksi → Snackbar error (tidak crash)
- [ ] Semua screen bisa di-navigate via bottom nav
