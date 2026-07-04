# DESIGN.md — Duit

Design system untuk Android (Jetpack Compose) dan Astro web dashboard.
Semua keputusan visual mengacu ke dokumen ini. Jika ada konflik antara DESIGN.md dan implementasi, DESIGN.md yang benar.

---

## Brand

**Nama:** Duit
**Tagline:** "Duitmu, di tanganmu."
**Karakter:** Casual tapi serius — kata sehari-hari yang semua orang Indonesia paham, dipasangkan dengan desain yang rapi dan terpercaya.

---

## Visual Direction

**Style:** Clean & Minimal
**Mood:** Tenang, terpercaya, fungsional — seperti rekening bank yang rapi
**Referensi:** Wallet by BudgetBakers, Money Manager, Linear

Tidak ada dekorasi berlebihan. Setiap elemen punya fungsi. Whitespace adalah fitur, bukan kekosongan.

---

## Color Palette

### Token Semantik

| Token | Hex | Tailwind | Penggunaan |
|-------|-----|----------|------------|
| `color-primary` | `#10b981` | `emerald-500` | CTA button, active state, income indicator |
| `color-primary-dark` | `#059669` | `emerald-600` | Hover state untuk primary |
| `color-primary-light` | `#d1fae5` | `emerald-100` | Background chip income, badge |
| `color-danger` | `#ef4444` | `red-500` | Expense indicator, error state |
| `color-danger-light` | `#fee2e2` | `red-100` | Background chip expense |
| `color-surface` | `#ffffff` | `white` | Card background, sheet background |
| `color-background` | `#f8fafc` | `slate-50` | Page background |
| `color-border` | `#e2e8f0` | `slate-200` | Divider, card border |
| `color-text-primary` | `#0f172a` | `slate-900` | Heading, label penting |
| `color-text-secondary` | `#64748b` | `slate-500` | Subtext, tanggal, kategori |
| `color-text-muted` | `#94a3b8` | `slate-400` | Placeholder, disabled text |

### Aturan Warna

- **Jangan gunakan raw hex** di komponen — selalu pakai token di atas
- **Income** selalu `color-primary` (hijau), **expense** selalu `color-danger` (merah)
- **Tidak ada warna lain** untuk income/expense — konsistensi lebih penting dari variasi
- Saldo positif: `color-primary`, saldo negatif: `color-danger`

---

## Typography

**Font:** Inter (Google Fonts)

```
https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap
```

### Type Scale

| Role | Size | Weight | Line Height | Penggunaan |
|------|------|--------|-------------|------------|
| Display | 32px / sp32 | 700 | 1.2 | Nominal besar di HomeScreen |
| Heading 1 | 24px / sp24 | 700 | 1.3 | Judul halaman |
| Heading 2 | 20px / sp20 | 700 | 1.4 | Section header |
| Heading 3 | 16px / sp16 | 600 | 1.5 | Card title, label field |
| Body | 14px / sp14 | 400 | 1.6 | Deskripsi transaksi, body text |
| Caption | 12px / sp12 | 400 | 1.5 | Tanggal, metadata, helper text |
| Label | 12px / sp12 | 500 | 1.4 | Badge, chip, tag |

### Aturan Tipografi

- **Minimum body text: 14px/sp14** — jangan lebih kecil kecuali caption
- **Nominal rupiah** pakai tabular figures (font-variant-numeric: tabular-nums) supaya tidak jumping
- **Format nominal:** `Rp 1.250.000` (titik sebagai pemisah ribuan, bukan koma)
- **Truncate** dengan ellipsis jika teks overflow, jangan wrap nama transaksi ke 2 baris

---

## Spacing System

Berbasis kelipatan 4dp/px.

| Token | Value | Penggunaan |
|-------|-------|------------|
| `space-1` | 4dp | Gap antar icon dan label |
| `space-2` | 8dp | Padding kecil, gap antar chip |
| `space-3` | 12dp | Gap antar elemen dalam card |
| `space-4` | 16dp | Padding card, section padding horizontal |
| `space-5` | 20dp | Gap antar card |
| `space-6` | 24dp | Section spacing |
| `space-8` | 32dp | Page-level vertical spacing |

---

## Elevation & Shadow

| Level | CSS / Android | Penggunaan |
|-------|---------------|------------|
| 0 | Flat, no shadow | Background, divider |
| 1 | `shadow-sm` / elevation 1 | Card default |
| 2 | `shadow-md` / elevation 2 | Card hover, dropdown |
| 3 | `shadow-lg` / elevation 4 | Bottom sheet, modal |

---

## Border Radius

| Token | Value | Penggunaan |
|-------|-------|------------|
| `radius-sm` | 6px/dp | Badge, chip, tag |
| `radius-md` | 12px/dp | Input field |
| `radius-lg` | 16px/dp | Button (M3 Expressive) |
| `radius-xl` | 20px/dp | Card (M3 Expressive) |
| `radius-2xl` | 28px/dp | Bottom sheet, large card |
| `radius-full` | 9999px | Avatar, FAB, pill button |

> **Android (M3 Expressive):** `ExpressiveShapes` di `Theme.kt` — medium=16dp (button), large=20dp (card), extraLarge=28dp.

---

## Komponen

### Button

**Primary:**
- Background: `color-primary` (#10b981)
- Text: white, weight 600
- Padding: 12dp vertical, 24dp horizontal
- Radius: `radius-md`
- Hover/pressed: `color-primary-dark` (#059669)
- Disabled: opacity 0.5

**Secondary/Outline:**
- Border: `color-border`, 1px
- Text: `color-text-primary`
- Background: transparent

**Destructive:**
- Background: `color-danger`
- Gunakan hanya untuk hapus/logout — pisahkan secara visual dari aksi biasa

### Card

```
background: color-surface (#ffffff)
border: 1px solid color-border (#e2e8f0)
border-radius: radius-md (10dp)
padding: space-4 (16dp)
shadow: elevation 1
```

### Input Field

```
background: color-surface
border: 1px solid color-border
border-radius: radius-md
padding: 12dp vertical, 16dp horizontal
min-height: 48dp (Android) / 44px (Web)

focus: border-color → color-primary, outline 2px color-primary/20
error: border-color → color-danger
```

Label selalu di atas field — bukan placeholder-only.

### Transaction Item (List)

```
┌──────────────────────────────────────┐
│ [icon]  Makan siang          -25.000 │  ← text-primary / color-danger
│         Cash · Makan  03 Jul         │  ← text-secondary, caption
└──────────────────────────────────────┘
```

- Icon: category color, 40×40dp dengan background rounded
- Nominal income: `color-primary`, selalu dengan `+`
- Nominal expense: `color-danger`, selalu dengan `-`
- Tap area minimum: 56dp tinggi

### Summary Card (HomeScreen / Dashboard)

```
┌────────────────────┐
│ Pemasukan          │
│ Rp 5.000.000       │  ← Display size, color-primary
└────────────────────┘
```

3 kartu sejajar: Pemasukan (hijau), Pengeluaran (merah), Saldo (slate-900 atau sesuai nilai).

### Bottom Navigation (Android)

- Max 4 tab: Home, Tambah, Riwayat, Wallet
- Active: `color-primary`, filled icon
- Inactive: `color-text-muted`, outline icon
- Label selalu tampil (jangan icon-only)

---

## Ikonografi

**Library:** Lucide Icons (Android: lucide-android / Web: lucide-react atau SVG langsung)

- Stroke width: **1.5px** konsisten di semua ikon
- Size default: **24dp/px**
- Size kecil: **20dp/px** (dalam list item)
- Size besar: **32dp/px** (FAB, empty state)

**Jangan gunakan emoji sebagai ikon** — tidak bisa di-theme, tidak konsisten antar platform.

### Mapping Kategori → Ikon

| Kategori | Ikon (Lucide) |
|----------|---------------|
| Makan | `utensils` |
| Transport | `car` |
| Belanja | `shopping-bag` |
| Hiburan | `gamepad-2` |
| Kesehatan | `heart-pulse` |
| Tagihan | `receipt` |
| Gaji | `banknote` |
| Bisnis | `briefcase` |

---

## Animasi & Motion

- **Micro-interaction:** 150–200ms, `ease-out`
- **Screen transition:** 250–300ms, `ease-in-out`
- **Modal/bottom sheet:** slide up 300ms, `ease-out`
- **Jangan animasi** `width`, `height` — gunakan `transform` dan `opacity`
- **Respect** `prefers-reduced-motion` di web

---

## Android: Jetpack Compose + Material Design 3 Expressive

Aplikasi Android menggunakan **Material Design 3 Expressive** dengan **dynamic colors (Monet)** — warna UI mengikuti wallpaper user secara otomatis (Android 12+). M3 Expressive adalah evolusi MD3 yang lebih "human" dengan corner radius lebih besar, typography lebih bold, dan spacing lebih luas.

### Dynamic Color Setup

```kotlin
// MainActivity.kt
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PersonalFinanceTheme { /* ... */ }
        }
    }
}

// ui/theme/Theme.kt
@Composable
fun PersonalFinanceTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,  // Android 12+ Monet
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context)
            else dynamicLightColorScheme(context)
        }
        darkTheme -> darkColorScheme(
            primary = Color(0xFF10B981),
            secondary = Color(0xFF059669),
            error = Color(0xFFEF4444),
        )
        else -> lightColorScheme(
            primary = Color(0xFF10B981),
            secondary = Color(0xFF059669),
            error = Color(0xFFEF4444),
        )
    }
    MaterialTheme(
        colorScheme = colorScheme,
        typography = DuitTypography,
        shapes = ExpressiveShapes,   // M3 Expressive: medium=16dp, large=20dp, extraLarge=28dp
        content = content
    )
}
```

### Penggunaan Warna di Komponen

Selalu gunakan token dari `MaterialTheme.colorScheme` — jangan hardcode hex di komponen:

```kotlin
// Gunakan ini:
MaterialTheme.colorScheme.primary          // CTA, active state
MaterialTheme.colorScheme.surface          // Card background
MaterialTheme.colorScheme.onSurface        // Text di atas surface
MaterialTheme.colorScheme.surfaceVariant   // Card secondary, chip bg
MaterialTheme.colorScheme.error            // Expense indicator, error

// Fallback warna semantik jika tidak ada MD3 equivalent:
val IncomeGreen = Color(0xFF10B981)   // income indicator
val ExpenseRed = Color(0xFFEF4444)    // expense indicator
```

### Warna Income/Expense

MD3 tidak punya token semantik untuk income/expense — tetap hardcode dua ini karena bermakna finansial, bukan brand:

```kotlin
// ui/theme/Color.kt
val IncomeGreen = Color(0xFF10B981)
val IncomeGreenContainer = Color(0xFFD1FAE5)
val ExpenseRed = Color(0xFFEF4444)
val ExpenseRedContainer = Color(0xFFFEE2E2)
```

### MD3 Komponen yang Dipakai

| UI Element | MD3 Component |
|------------|---------------|
| Card | `ElevatedCard` / `OutlinedCard` |
| Input field | `OutlinedTextField` |
| Primary button | `Button` (filled) |
| Secondary button | `OutlinedButton` |
| FAB | `FloatingActionButton` |
| Bottom nav | `NavigationBar` + `NavigationBarItem` |
| Bottom sheet | `ModalBottomSheet` |
| List item | `ListItem` |
| Chip/badge | `AssistChip` / `FilterChip` |
| Dialog konfirmasi | `AlertDialog` |

### Typography (MD3 Expressive)

```kotlin
// ui/theme/Type.kt
val DuitTypography = Typography(
    displayLarge  = TextStyle(fontWeight = FontWeight.Bold, fontSize = 32.sp, lineHeight = 38.sp),
    headlineLarge = TextStyle(fontWeight = FontWeight.Bold, fontSize = 24.sp, lineHeight = 31.sp),
    headlineMedium = TextStyle(fontWeight = FontWeight.Bold, fontSize = 20.sp, lineHeight = 28.sp),
    titleMedium   = TextStyle(fontWeight = FontWeight.Medium, fontSize = 16.sp, lineHeight = 24.sp),
    bodyLarge     = TextStyle(fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 20.sp),
    bodyMedium    = TextStyle(fontWeight = FontWeight.Normal, fontSize = 12.sp, lineHeight = 16.sp),
    labelMedium   = TextStyle(fontWeight = FontWeight.Medium, fontSize = 11.sp, lineHeight = 16.sp),
)
```

### Min SDK

**Min SDK: 31 (Android 12)** — wajib untuk dynamic color (Monet). Target SDK: 35 (Android 16).

---

## Web (Astro + Tailwind)

Config Tailwind untuk token warna:

```js
// tailwind.config.mjs
colors: {
  primary: '#10b981',
  'primary-dark': '#059669',
  'primary-light': '#d1fae5',
  danger: '#ef4444',
  'danger-light': '#fee2e2',
  surface: '#ffffff',
  background: '#f8fafc',
  border: '#e2e8f0',
  'text-primary': '#0f172a',
  'text-secondary': '#64748b',
  'text-muted': '#94a3b8',
}
```

- Layout dashboard: sidebar kiri (64px collapsed / 240px expanded) + main content
- Max content width: `max-w-5xl` (1024px)
- Responsive breakpoints: 375 / 768 / 1024 / 1440

---

## Chart (Chart.js)

| Data | Tipe Chart | Keterangan |
|------|------------|------------|
| Trend bulanan 12 bulan | Bar chart | Grouped income (hijau) + expense (merah) |
| Breakdown per kategori | Donut chart | Max 8 kategori, sisanya "Lainnya" |
| Per wallet | Card list | Bukan chart — cukup teks + nominal |

**Warna chart:**
- Income: `#10b981` (emerald)
- Expense: `#ef4444` (red)
- Gridlines: `#e2e8f0` (slate-200, subtle)
- Teks axis: `#64748b` (slate-500)
- Selalu sertakan legend dan tooltip

---

## Empty States

Setiap list/chart yang bisa kosong wajib punya empty state:

```
[ikon besar, muted]
Belum ada transaksi
Tambah transaksi pertamamu
[CTA button jika relevan]
```

- Ikon: 48dp, `color-text-muted`
- Teks utama: Heading 3, `color-text-secondary`
- Teks sub: Body, `color-text-muted`

---

## Do & Don't

| ✅ Do | ❌ Don't |
|-------|---------|
| Gunakan token warna semantik | Hardcode hex langsung di komponen |
| Income hijau, expense merah — selalu | Gunakan warna berbeda untuk variasi |
| Label di atas semua input field | Placeholder-only sebagai label |
| SVG icon dari Lucide, stroke 1.5 | Emoji sebagai ikon navigasi/UI |
| Konfirmasi sebelum hapus transaksi | Hapus langsung tanpa dialog konfirmasi |
| Format `Rp 1.250.000` (titik) | Format `Rp 1,250,000` atau `1250000` |
| Touch target min 48dp | Tombol/tap area < 44dp |
