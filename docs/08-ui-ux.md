# 🎨 UI/UX Wireframes

Wireframe teks untuk semua halaman utama. Layout ditampilkan dengan ASCII art.

---

## Android App

### LoginScreen

```
┌─────────────────────────────┐
│                             │
│     Personal Finance        │
│                             │
│  ┌─────────────────────┐   │
│  │ Email               │   │
│  └─────────────────────┘   │
│  ┌─────────────────────┐   │
│  │ Password            │   │
│  └─────────────────────┘   │
│                             │
│  ┌─────────────────────┐   │
│  │      LOGIN          │   │
│  └─────────────────────┘   │
│                             │
└─────────────────────────────┘
```

---

### HomeScreen

```
┌─────────────────────────────┐
│  Juli 2026         [👤]     │
├─────────────────────────────┤
│ ┌─────────┐ ┌─────────┐    │
│ │Pemasukan│ │Pengeluar│    │
│ │Rp 5 jt  │ │Rp 1.2 jt│   │
│ └─────────┘ └─────────┘    │
│ ┌─────────────────────┐    │
│ │   Saldo: Rp 3.8 jt  │    │
│ └─────────────────────┘    │
├─────────────────────────────┤
│ Transaksi Terbaru           │
│ ─────────────────────────  │
│ 🍜 Makan siang  -25.000    │
│    Cash · Makan  03 Jul     │
│ ─────────────────────────  │
│ 💰 Gajian     +5.000.000   │
│    BCA · Gaji   01 Jul      │
│ ─────────────────────────  │
│ ☕ Kopi         -18.000    │
│    GoPay · Minum 01 Jul     │
├─────────────────────────────┤
│  [Home]  [+ Add] [History]  │
└─────────────────────────────┘
```

---

### AddTransactionScreen

```
┌─────────────────────────────┐
│  ← Tambah Transaksi         │
├─────────────────────────────┤
│                             │
│  [  Income  ] [ Expense  ]  │
│                             │
│  Judul                      │
│  ┌─────────────────────┐   │
│  │ Makan siang         │   │
│  └─────────────────────┘   │
│                             │
│  Nominal                    │
│  ┌─────────────────────┐   │
│  │ 25000               │   │
│  └─────────────────────┘   │
│                             │
│  Kategori        Wallet     │
│  ┌──────────┐ ┌──────────┐ │
│  │ Makan  ▼ │ │ Cash   ▼ │ │
│  └──────────┘ └──────────┘ │
│                             │
│  Tanggal                    │
│  ┌─────────────────────┐   │
│  │ 03 Juli 2026        │   │
│  └─────────────────────┘   │
│                             │
│  ┌─────────────────────┐   │
│  │       SIMPAN        │   │
│  └─────────────────────┘   │
└─────────────────────────────┘
```

---

### TransactionListScreen

```
┌─────────────────────────────┐
│  Riwayat                    │
├─────────────────────────────┤
│ [Juli ▼] [Semua ▼] [All ▼] │
│          tipe      wallet   │
├─────────────────────────────┤
│ 03 Jul                      │
│ 🍜 Makan siang  -25.000    │
│    Makan · Cash             │
│ ─────────────────────────  │
│ 01 Jul                      │
│ 💰 Gajian     +5.000.000   │
│    Gaji · BCA               │
│ ─────────────────────────  │
│ 01 Jul                      │
│ ☕ Kopi         -18.000    │
│    Minum · GoPay            │
├─────────────────────────────┤
│  [Home]  [+ Add] [History]  │
└─────────────────────────────┘
```

---

### WalletScreen

```
┌─────────────────────────────┐
│  Sumber Dana          [+]   │
├─────────────────────────────┤
│                             │
│  💵 Cash                    │
│     Rp 200.000              │
│                             │
│  🏦 BCA                     │
│     Rp 3.950.000            │
│                             │
│  📱 GoPay                   │
│     Rp 150.000              │
│                             │
│  📱 OVO                     │
│     Rp 75.000               │
│                             │
├─────────────────────────────┤
│  [Home]  [+ Add] [History]  │
└─────────────────────────────┘
```

---

## Dashboard Web

### Login Page

```
┌─────────────────────────────────────────┐
│                                         │
│           Personal Finance              │
│                                         │
│         ┌──────────────────┐           │
│         │ Email            │           │
│         └──────────────────┘           │
│         ┌──────────────────┐           │
│         │ Password         │           │
│         └──────────────────┘           │
│         ┌──────────────────┐           │
│         │      LOGIN       │           │
│         └──────────────────┘           │
│                                         │
└─────────────────────────────────────────┘
```

---

### Dashboard Page

```
┌────┬────────────────────────────────────┐
│    │  Dashboard      Juli 2026    [Budi]│
│    ├────────────────────────────────────┤
│ 📊 │ ┌──────────┐┌──────────┐┌────────┐│
│    │ │Pemasukan ││Pengeluar ││Saldo   ││
│Dash│ │Rp 5 jt   ││Rp 1.2 jt ││Rp 3.8jt││
│    │ └──────────┘└──────────┘└────────┘│
│ 📋 │                                    │
│    │ Trend Bulanan (bar chart)          │
│Trx │ ████░░░░░░░░░░░░░░░░░░░░░░         │
│    │ Jan Feb Mar Apr Mei Jun Jul        │
│ 📥 │                                    │
│    ├───────────────┬────────────────────┤
│Rprt│ Per Kategori  │ Per Wallet         │
│    │ (donut chart) │ Cash    200K       │
│    │               │ BCA   3.950K       │
│    │               │ GoPay   150K       │
└────┴───────────────┴────────────────────┘
```

---

### Transactions Page

```
┌────┬────────────────────────────────────┐
│    │  Transaksi                         │
│    ├────────────────────────────────────┤
│ 📊 │ [Juli ▼] [Semua ▼] [Semua ▼]      │
│    │           tipe       wallet        │
│Dash├────────────────────────────────────┤
│    │ Tgl    Judul       Kat  Wallet  Nom│
│ 📋 │ 03 Jul Makan siang Mkn  Cash   -25K│
│    │ 01 Jul Gajian      Gaji BCA   +5Jt │
│Trx │ 01 Jul Kopi        Min  GoPay  -18K│
│    │                                    │
│ 📥 │                                    │
│    │                                    │
│Rprt│                                    │
└────┴────────────────────────────────────┘
```

---

## Catatan UI/UX

- **Android:** Material Design 3, bottom navigation, bottom sheet untuk form tambah
- **Web:** Sidebar kiri fixed, content area scrollable, responsive (mobile-friendly)
- **Warna:** Hijau untuk income, merah untuk expense, biru untuk saldo
- **Typography:** Sans-serif, bersih, mudah dibaca
- **Charts:** Chart.js — bar chart untuk trend bulanan, donut chart untuk kategori
