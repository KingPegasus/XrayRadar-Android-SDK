package com.xrayradar.android.internal

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class JsonUtilTest {

    @Test
    fun `null to JsonElement is JsonNull`() {
        val result = null.toJsonElement()
        assertTrue(result is JsonNull)
    }

    @Test
    fun `string to JsonElement is JsonPrimitive`() {
        val result = "hello".toJsonElement()
        assertTrue(result is JsonPrimitive)
        assertEquals("hello", (result as JsonPrimitive).content)
    }

    @Test
    fun `number to JsonElement is JsonPrimitive`() {
        val result = 42.toJsonElement()
        assertTrue(result is JsonPrimitive)
        assertEquals("42", (result as JsonPrimitive).content)
    }

    @Test
    fun `boolean to JsonElement is JsonPrimitive`() {
        val result = true.toJsonElement()
        assertTrue(result is JsonPrimitive)
        assertEquals("true", (result as JsonPrimitive).content)
    }

    @Test
    fun `map to JsonElement is JsonObject`() {
        val result = mapOf("a" to 1, "b" to "two").toJsonElement()
        assertTrue(result is JsonObject)
        assertEquals(2, (result as JsonObject).size)
    }

    @Test
    fun `list to JsonElement is JsonArray`() {
        val result = listOf(1, "x", false).toJsonElement()
        assertTrue(result is JsonArray)
        assertEquals(3, (result as JsonArray).size)
    }

    @Test
    fun `array to JsonElement is JsonArray`() {
        val result = arrayOf("a", "b").toJsonElement()
        assertTrue(result is JsonArray)
        assertEquals(2, (result as JsonArray).size)
    }

    @Test
    fun `nested map and list`() {
        val input = mapOf("items" to listOf(1, 2), "name" to "test")
        val result = input.toJsonElement()
        assertTrue(result is JsonObject)
        val items = (result as JsonObject)["items"]
        assertTrue(items is JsonArray)
        assertEquals(2, (items as JsonArray).size)
    }
}
