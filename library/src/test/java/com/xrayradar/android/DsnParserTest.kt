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

    @Test
    fun `parse dsn with port`() {
        val out = parseDsn("https://ingest.example.com:8080/99")
        assertEquals("https://ingest.example.com:8080", out.serverUrl)
        assertEquals("99", out.projectId)
    }

    @Test
    fun `parse dsn with trailing slash`() {
        val out = parseDsn("https://xrayradar.com/my_project/")
        assertEquals("https://xrayradar.com", out.serverUrl)
        assertEquals("my_project", out.projectId)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `reject dsn with missing project id`() {
        parseDsn("https://xrayradar.com/")
    }

    @Test(expected = IllegalArgumentException::class)
    fun `reject dsn with blank host`() {
        parseDsn("https:///project")
    }
}
