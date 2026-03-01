package com.xrayradar.android

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert.assertEquals
import org.junit.Test

class NetworkBreadcrumbInterceptorTest {

    @Test
    fun `interceptor proceeds and returns response`() {
        val server = MockWebServer()
        server.enqueue(MockResponse().setResponseCode(200).setBody("ok"))
        server.start()
        try {
            val client = OkHttpClient.Builder()
                .addInterceptor(NetworkBreadcrumbInterceptor())
                .build()
            val request = Request.Builder().url(server.url("/")).build()
            val response = client.newCall(request).execute()
            assertEquals(200, response.code)
            assertEquals("ok", response.body?.string())
        } finally {
            server.shutdown()
        }
    }

    @Test
    fun `interceptor rethrows on failure and does not crash`() {
        val server = MockWebServer()
        server.enqueue(MockResponse().setResponseCode(500))
        server.start()
        try {
            val client = OkHttpClient.Builder()
                .addInterceptor(NetworkBreadcrumbInterceptor())
                .build()
            val request = Request.Builder().url(server.url("/")).build()
            val response = client.newCall(request).execute()
            assertEquals(500, response.code)
        } finally {
            server.shutdown()
        }
    }

    @Test
    fun `interceptor works with URL containing query string`() {
        val server = MockWebServer()
        server.enqueue(MockResponse().setResponseCode(200))
        server.start()
        try {
            val client = OkHttpClient.Builder()
                .addInterceptor(NetworkBreadcrumbInterceptor())
                .build()
            val urlWithQuery = server.url("/path?foo=bar&baz=1")
            val request = Request.Builder().url(urlWithQuery).get().build()
            val response = client.newCall(request).execute()
            assertEquals(200, response.code)
        } finally {
            server.shutdown()
        }
    }
}
