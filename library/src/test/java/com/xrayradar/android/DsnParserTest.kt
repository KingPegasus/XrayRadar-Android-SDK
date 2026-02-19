package com.xrayradar.android

import com.xrayradar.android.internal.parseDsn
import org.junit.Assert.assertEquals
import org.junit.Test

class DsnParserTest {
    @Test
    fun `parse valid dsn`() {
        val out = parseDsn("https://xrayradar.example.com/project_123")
        assertEquals("https://xrayradar.example.com", out.serverUrl)
        assertEquals("project_123", out.projectId)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `reject invalid dsn`() {
        parseDsn("not-a-url")
    }
}
