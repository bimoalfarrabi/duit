package com.duit.app.ui.voice

/**
 * Parser on-device untuk teks hasil SpeechRecognizer.
 * Ekstrak: nominal, tipe (income/expense), judul transaksi.
 * ponytail: regex + keyword matching, no LLM, same pattern as OcrParser.
 */
object VoiceParser {

    data class ParseResult(
        val amount: String = "",
        val title: String = "",
        val type: String = "expense" // default expense
    )

    // Nominal: angka biasa (18000), shorthand (18rb/18k/18ribu), opsional "Rp" di depan
    private val amountRegex = Regex(
        """(?:Rp\.?\s*)?(\d+(?:[.,]\d+)*)\s*(rb|ribu|k|jt|juta)?""",
        RegexOption.IGNORE_CASE
    )

    // Kata kunci income — urutan penting: lebih spesifik dulu
    private val incomeKeywords = setOf(
        "terima", "dapat", "dapet", "masuk", "gajian", "gaji",
        "transfer masuk", "cashback", "refund", "dikembalikan"
    )

    // Kata kunci expense — dipakai sebagai fallback identifier, bukan filter
    private val expenseKeywords = setOf(
        "beli", "bayar", "bayar", "beli", "jajan", "makan", "minum",
        "isi", "top up", "topup", "kirim", "transfer"
    )

    // Kata yang perlu dibuang dari judul (kata fungsional)
    private val stripWords = setOf(
        "beli", "bayar", "untuk", "sama", "dengan", "di", "ke", "dari",
        "terima", "dapat", "dapet", "tadi", "tuh", "nih", "ya", "dong",
        "senilai", "seharga", "sebesar", "rp", "ribu", "rb", "juta", "jt"
    )

    fun parse(rawText: String): ParseResult {
        val text = rawText.trim().lowercase()
        val amount = extractAmount(text)
        val type = extractType(text)
        val title = extractTitle(text, amount)
        return ParseResult(amount = amount, title = title, type = type)
    }

    private fun extractAmount(text: String): String {
        val match = amountRegex.find(text) ?: return ""
        val digits = match.groupValues[1].replace(".", "").replace(",", "")
        val multiplier = when (match.groupValues[2].lowercase()) {
            "rb", "ribu", "k" -> 1_000L
            "jt", "juta"      -> 1_000_000L
            else              -> 1L
        }
        return (digits.toLongOrNull()?.times(multiplier))?.toString() ?: ""
    }

    private fun extractType(text: String): String {
        // ponytail: income check first — if any income keyword present, it's income
        if (incomeKeywords.any { text.contains(it) }) return "income"
        return "expense"
    }

    private fun extractTitle(text: String, amount: String): String {
        // Buang angka dan shorthand nominal dari teks
        var cleaned = amountRegex.replace(text, " ")

        // Buang kata-kata fungsional
        val words = cleaned.split(Regex("\\s+"))
            .map { it.trim().trimEnd('.', ',', '?', '!') }
            .filter { it.isNotBlank() && it !in stripWords && it.length > 1 }

        val title = words.joinToString(" ").trim()
        return title.take(50).replaceFirstChar { it.uppercase() }
    }
}
