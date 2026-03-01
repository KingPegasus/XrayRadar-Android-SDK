package com.xrayradar.android.internal

import org.junit.Assert.assertEquals
import org.junit.Test

class NormalizeLevelTest {

    @Test
    fun `fatal normalizes to fatal`() {
        assertEquals("fatal", normalizeLevel("fatal"))
    }

    @Test
    fun `error normalizes to error`() {
        assertEquals("error", normalizeLevel("error"))
    }

    @Test
    fun `warning normalizes to warning`() {
        assertEquals("warning", normalizeLevel("warning"))
    }

    @Test
    fun `info normalizes to info`() {
        assertEquals("info", normalizeLevel("info"))
    }

    @Test
    fun `debug normalizes to debug`() {
        assertEquals("debug", normalizeLevel("debug"))
    }

    @Test
    fun `uppercase ERROR normalizes to error`() {
        assertEquals("error", normalizeLevel("ERROR"))
    }

    @Test
    fun `unknown level defaults to error`() {
        assertEquals("error", normalizeLevel("custom"))
        assertEquals("error", normalizeLevel(""))
    }
}
