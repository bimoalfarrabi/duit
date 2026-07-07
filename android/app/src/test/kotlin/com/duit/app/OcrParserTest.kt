package com.duit.app

import com.duit.app.ui.ocr.OcrParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class OcrParserTest {

    @Test
    fun `extract amount from total line`() {
        val text = """
            Indomaret
            Aqua 600ml    8.000
            Roti Tawar    12.000
            Total         20.000
        """.trimIndent()
        val result = OcrParser.parse(text)
        assertEquals("20000", result.amount)
    }

    @Test
    fun `extract amount with Rp prefix`() {
        val text = "Total Rp 45.500"
        val result = OcrParser.parse(text)
        assertEquals("45500", result.amount)
    }

    @Test
    fun `extract date DD-MM-YYYY format`() {
        val text = "Tanggal: 07-07-2026\nTotal 15.000"
        val result = OcrParser.parse(text)
        assertEquals("2026-07-07", result.date)
    }

    @Test
    fun `extract date YYYY-MM-DD format`() {
        val text = "Date: 2026-07-07\nTotal 15.000"
        val result = OcrParser.parse(text)
        assertEquals("2026-07-07", result.date)
    }

    @Test
    fun `extract title skips skip keywords`() {
        val text = """
            Warung Makan Sederhana
            Total         25.000
        """.trimIndent()
        val result = OcrParser.parse(text)
        assertEquals("Warung Makan Sederhana", result.title)
    }

    @Test
    fun `fallback amount is largest number when no total line`() {
        val text = """
            Kopi         18.000
            Nasi Goreng  25.000
        """.trimIndent()
        val result = OcrParser.parse(text)
        assertEquals("25000", result.amount)
    }

    @Test
    fun `empty text returns empty parse result`() {
        val result = OcrParser.parse("")
        assertTrue(result.amount.isEmpty())
        assertTrue(result.title.isEmpty())
        assertTrue(result.date.isEmpty())
    }

    @Test
    fun `title max 50 chars`() {
        val longTitle = "A".repeat(100)
        val text = "$longTitle\nTotal 10.000"
        val result = OcrParser.parse(text)
        assertTrue(result.title.length <= 50)
    }
}
