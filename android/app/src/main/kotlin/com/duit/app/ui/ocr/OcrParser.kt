package com.duit.app.ui.ocr

/**
 * Parser on-device untuk teks hasil ML Kit OCR dari struk belanja.
 * Ekstrak: nominal, judul transaksi, tanggal.
 * ponytail: regex sederhana, cukup untuk struk Indonesia. Upgrade ke NLP jika false-positive tinggi.
 */
object OcrParser {

    data class ParseResult(
        val amount: String = "",
        val title: String = "",
        val date: String = ""
    )

    // Nominal: angka dengan/tanpa titik/koma pemisah ribuan, opsional "Rp" di depan
    private val amountRegex = Regex(
        """(?:Rp\.?\s*)?(\d{1,3}(?:[.,]\d{3})+|\d{4,})""",
        RegexOption.IGNORE_CASE
    )

    // Tanggal: DD/MM/YYYY, DD-MM-YYYY, YYYY-MM-DD
    private val dateRegex = Regex(
        """(\d{2})[/\-](\d{2})[/\-](\d{4})|(\d{4})[/\-](\d{2})[/\-](\d{2})"""
    )

    // Kata yang menunjukkan label bukan judul (skip baris ini)
    private val skipKeywords = setOf(
        "total", "subtotal", "diskon", "pajak", "ppn", "service", "change",
        "kembalian", "tunai", "cash", "debit", "kredit", "bayar", "payment",
        "harga", "qty", "jumlah", "no", "struk", "receipt", "invoice",
        "terima kasih", "thank you"
    )

    fun parse(rawText: String): ParseResult {
        val lines = rawText.lines().map { it.trim() }.filter { it.isNotBlank() }

        val amount = extractAmount(lines)
        val date = extractDate(rawText)
        val title = extractTitle(lines)

        return ParseResult(amount = amount, title = title, date = date)
    }

    private fun extractAmount(lines: List<String>): String {
        // Cari baris "total" atau nominal terbesar
        val totalLine = lines.firstOrNull { line ->
            line.lowercase().contains("total") && amountRegex.containsMatchIn(line)
        }
        if (totalLine != null) {
            return amountRegex.find(totalLine)?.groupValues?.get(1)
                ?.replace(".", "")?.replace(",", "") ?: ""
        }

        // Fallback: nominal terbesar di seluruh teks
        return lines
            .flatMap { line -> amountRegex.findAll(line).map { it.groupValues[1] } }
            .mapNotNull { it.replace(".", "").replace(",", "").toLongOrNull() }
            .maxOrNull()?.toString() ?: ""
    }

    private fun extractDate(text: String): String {
        val match = dateRegex.find(text) ?: return ""
        val groups = match.groupValues
        return if (groups[4].isNotEmpty()) {
            // YYYY-MM-DD → sudah format ISO
            "${groups[4]}-${groups[5]}-${groups[6]}"
        } else {
            // DD/MM/YYYY → convert ke YYYY-MM-DD
            "${groups[3]}-${groups[2]}-${groups[1]}"
        }
    }

    private fun extractTitle(lines: List<String>): String {
        // Cari baris pertama yang bukan label, bukan angka, dan cukup panjang
        return lines.firstOrNull { line ->
            val lower = line.lowercase()
            skipKeywords.none { lower.contains(it) } &&
                !line.all { it.isDigit() || it in ".,- " } &&
                line.length in 3..50
        }?.take(50) ?: ""
    }
}
