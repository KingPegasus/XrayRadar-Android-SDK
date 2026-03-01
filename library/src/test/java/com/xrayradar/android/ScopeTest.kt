package com.xrayradar.android

import com.xrayradar.android.internal.Scope
import com.xrayradar.android.internal.model.BreadcrumbPayload
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ScopeTest {
    @Test
    fun `breadcrumbs keep most recent max`() {
        val scope = Scope(maxBreadcrumbs = 2)
        scope.addBreadcrumb(BreadcrumbPayload("t1", "a"))
        scope.addBreadcrumb(BreadcrumbPayload("t2", "b"))
        scope.addBreadcrumb(BreadcrumbPayload("t3", "c"))

        val breadcrumbs = scope.getBreadcrumbs()
        assertEquals(2, breadcrumbs.size)
        assertEquals("b", breadcrumbs[0].message)
        assertEquals("c", breadcrumbs[1].message)
    }

    @Test
    fun `clearBreadcrumbs removes all`() {
        val scope = Scope(maxBreadcrumbs = 5)
        scope.addBreadcrumb(BreadcrumbPayload("t1", "a"))
        scope.clearBreadcrumbs()
        assertTrue(scope.getBreadcrumbs().isEmpty())
    }

    @Test
    fun `setMaxBreadcrumbs trims excess`() {
        val scope = Scope(maxBreadcrumbs = 5)
        repeat(5) { scope.addBreadcrumb(BreadcrumbPayload("t$it", "m$it")) }
        scope.setMaxBreadcrumbs(2)
        assertEquals(2, scope.getBreadcrumbs().size)
        assertEquals("m3", scope.getBreadcrumbs()[0].message)
        assertEquals("m4", scope.getBreadcrumbs()[1].message)
    }

    @Test
    fun `setUser setTag setExtra setContext and buildContexts`() {
        val scope = Scope(maxBreadcrumbs = 2)
        scope.environment = "staging"
        scope.release = "1.0"
        scope.serverName = "test"
        scope.setUser(mapOf("id" to "u1", "email" to "u@x.com"))
        scope.setTag("tag1", "value1")
        scope.setExtra("extra1", 42)
        scope.setContext("custom", mapOf("k" to "v"))

        val runtime = buildJsonObject { put("name", "android"); put("version", "12") }
        val os = buildJsonObject { put("name", "Android"); put("version", "12"); put("api_level", 31) }
        val device = buildJsonObject { put("manufacturer", "M"); put("model", "X"); put("brand", "B") }
        val app = buildJsonObject { put("package_name", "pkg"); put("version_name", "1.0"); put("version_code", 1L) }

        val ctx = scope.buildContexts(runtime, os, device, app)
        assertEquals("staging", (ctx["environment"] as? JsonPrimitive)?.content)
        assertEquals("1.0", (ctx["release"] as? JsonPrimitive)?.content)
        assertEquals("test", (ctx["server_name"] as? JsonPrimitive)?.content)
        assertTrue(ctx.containsKey("user"))
        assertTrue(ctx.containsKey("tags"))
        assertTrue(ctx.containsKey("extra"))
        assertTrue(ctx.containsKey("runtime"))
        assertTrue(ctx.containsKey("custom"))
    }

    @Test
    fun `setUser null clears user`() {
        val scope = Scope(maxBreadcrumbs = 2)
        scope.setUser(mapOf("id" to "u1"))
        scope.setUser(null)
        val runtime = buildJsonObject { put("name", "a"); put("version", "1") }
        val os = buildJsonObject { put("name", "Android"); put("version", "1"); put("api_level", 1) }
        val device = buildJsonObject { put("manufacturer", "M"); put("model", "M"); put("brand", "B") }
        val app = buildJsonObject { put("package_name", "p"); put("version_name", "1"); put("version_code", 1L) }
        val ctx = scope.buildContexts(runtime, os, device, app)
        assertNull(ctx["user"])
    }
}
