package com.duit.app

import com.duit.app.ui.voice.VoiceParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class VoiceParserTest {

    @Test
    fun `parse plain number as expense`() {
        val result = VoiceParser.parse("kopi 18000")
        assertEquals("18000", result.amount)
        assertEquals("expense", result.type)
    }

    @Test
    fun `parse rb shorthand`() {
        val result = VoiceParser.parse("beli kopi 18rb")
        assertEquals("18000", result.amount)
        assertEquals("expense", result.type)
    }

    @Test
    fun `parse ribu shorthand`() {
        val result = VoiceParser.parse("makan siang 25 ribu")
        assertEquals("25000", result.amount)
    }

    @Test
    fun `parse k shorthand`() {
        val result = VoiceParser.parse("parkir 5k")
        assertEquals("5000", result.amount)
    }

    @Test
    fun `parse juta shorthand`() {
        val result = VoiceParser.parse("gaji 5 juta")
        assertEquals("5000000", result.amount)
        assertEquals("income", result.type)
    }

    @Test
    fun `income keyword terima`() {
        val result = VoiceParser.parse("terima transfer 500 ribu")
        assertEquals("income", result.type)
        assertEquals("500000", result.amount)
    }

    @Test
    fun `income keyword dapat`() {
        val result = VoiceParser.parse("dapat cashback 20rb")
        assertEquals("income", result.type)
    }

    @Test
    fun `expense is default when no income keyword`() {
        val result = VoiceParser.parse("bayar listrik 150000")
        assertEquals("expense", result.type)
        assertEquals("150000", result.amount)
    }

    @Test
    fun `title strips functional words`() {
        val result = VoiceParser.parse("beli kopi susu 18000")
        assertTrue(result.title.isNotBlank())
        assertTrue(!result.title.lowercase().contains("beli"))
    }

    @Test
    fun `title capped at 50 chars`() {
        val result = VoiceParser.parse("beli sesuatu yang namanya sangat panjang sekali karena memang begitu adanya 50000")
        assertTrue(result.title.length <= 50)
    }

    @Test
    fun `empty input returns default parse result`() {
        val result = VoiceParser.parse("")
        assertTrue(result.amount.isEmpty())
        assertEquals("expense", result.type)
    }

    @Test
    fun `jt shorthand same as juta`() {
        val result = VoiceParser.parse("bonus 2jt")
        assertEquals("2000000", result.amount)
    }
}
