package com.xrayradar.android

import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.Response

class NetworkBreadcrumbInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val start = System.nanoTime()
        return try {
            val response = chain.proceed(request)
            val elapsedMs = (System.nanoTime() - start) / 1_000_000
            XrayRadar.addBreadcrumb(
                message = "${request.method} ${sanitizeUrl(request.url)}",
                type = "http",
                category = "http.client",
                data = mapOf(
                    "status_code" to response.code,
                    "duration_ms" to elapsedMs,
                    "method" to request.method,
                ),
            )
            response
        } catch (e: Exception) {
            val elapsedMs = (System.nanoTime() - start) / 1_000_000
            XrayRadar.addBreadcrumb(
                message = "${request.method} ${sanitizeUrl(request.url)} failed",
                type = "http",
                level = "error",
                category = "http.client",
                data = mapOf(
                    "error" to (e.message ?: e::class.java.simpleName),
                    "duration_ms" to elapsedMs,
                    "method" to request.method,
                ),
            )
            throw e
        }
    }

    private fun sanitizeUrl(url: HttpUrl): String = url.newBuilder().query(null).build().toString()
}
