# 📱 Aplikasi Android

## Stack

- Kotlin + Jetpack Compose
- Architecture: UI → ViewModel → Repository (3 layer)
- Retrofit + OkHttp (HTTP)
- Hilt (DI)
- EncryptedSharedPreferences (token storage)
- Jetpack Navigation Compose

## Setup Project

- Min SDK: 26 (Android 8.0)
- Target SDK: 34
- Android Studio Hedgehog+

### Dependencies (`build.gradle.kts`)

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

## Auth

1. `LoginScreen` → `POST /api/auth/login`
2. Token → `EncryptedSharedPreferences` via `TokenStorage`
3. `AuthInterceptor` (OkHttp) → inject `Authorization: Bearer {token}` ke setiap request
4. Tidak ada token → redirect ke `LoginScreen`

## Navigation

Bottom nav bar — 4 tab: **Home / Add / History / Wallet**

- `AuthGraph`: `LoginScreen`
- `MainGraph`: `HomeScreen`, `AddTransactionScreen`, `TransactionListScreen`, `CategoryScreen`, `WalletScreen`
- Startup check: ada token → MainGraph, tidak ada → AuthGraph

## Screens

### LoginScreen
- Email + password
- Validasi: email tidak kosong, password min 6 karakter
- Error → Snackbar

### HomeScreen
- 3 summary card: Pemasukan / Pengeluaran / Saldo bulan ini
- 5 transaksi terbaru
- Pull-to-refresh

### AddTransactionScreen
- Fields: judul, nominal (numeric), tipe (income/expense), kategori (dropdown), wallet (dropdown), tanggal (DatePicker)
- Semua field wajib diisi
- Submit → sukses → back ke Home + refresh

### TransactionListScreen
- Filter: bulan + tahun + wallet
- LazyColumn tanpa pagination (v1)
- Refresh saat filter berubah

### CategoryScreen
- List kategori user
- FAB → bottom sheet tambah kategori (nama, tipe, warna)

### WalletScreen
- List wallet user (tipe + saldo)
- FAB → bottom sheet tambah wallet (nama, tipe, warna)
  - Tipe `cash`: disable jika sudah ada (max 1)
  - Tipe `bank`/`ewallet`: bebas

## API Interface (`data/remote/ApiService.kt`)

```kotlin
interface ApiService {
    @POST("auth/login") suspend fun login(@Body body: LoginRequest): LoginResponse
    @POST("auth/logout") suspend fun logout(): Unit
    @GET("auth/me") suspend fun me(): UserResponse
    @GET("categories") suspend fun getCategories(): List<CategoryResponse>
    @POST("categories") suspend fun createCategory(@Body body: CreateCategoryRequest): CategoryResponse
    @GET("wallets") suspend fun getWallets(): List<WalletResponse>
    @POST("wallets") suspend fun createWallet(@Body body: CreateWalletRequest): WalletResponse
    @GET("transactions") suspend fun getTransactions(@QueryMap params: Map<String, String>): List<TransactionResponse>
    @POST("transactions") suspend fun createTransaction(@Body body: CreateTransactionRequest): TransactionResponse
    @GET("statistics/summary") suspend fun getSummary(@Query("month") month: Int, @Query("year") year: Int): SummaryResponse
}
```

## Domain Models

```kotlin
data class User(val id: Int, val name: String, val email: String)
data class Category(val id: Int, val name: String, val type: String, val color: String, val icon: String)
data class Wallet(val id: Int, val name: String, val type: String, val color: String, val balance: Double)
data class Transaction(val id: Int, val title: String, val amount: Double, val type: String, val date: String, val note: String?, val category: Category, val wallet: Wallet)
data class Summary(val income: Double, val expense: Double, val balance: Double)
```

## Error Handling

- Tidak ada koneksi → Snackbar error
- 401 → redirect ke LoginScreen
- Tidak ada crash — semua network error ditangkap di Repository dan return `Result.failure()`

## Batasan v1

- Tidak ada offline mode — semua langsung ke API
- Tidak ada Room database
- Tidak ada OCR / voice input
