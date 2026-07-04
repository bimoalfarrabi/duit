package com.duit.app.data.remote

import java.io.IOException

// ponytail: single extension, covers 99% of error cases. Add more codes if server adds new ones.
fun Throwable.toUserMessage(): String = when {
    this is IOException -> "Tidak ada koneksi internet"
    message?.contains("401") == true -> "Sesi habis, silakan login ulang"
    message?.contains("403") == true -> "Akses ditolak"
    message?.contains("404") == true -> "Data tidak ditemukan"
    message?.contains("422") == true -> "Data tidak valid"
    message?.contains("500") == true -> "Terjadi kesalahan server"
    !message.isNullOrBlank() -> message!!
    else -> "Terjadi kesalahan, coba lagi"
}
