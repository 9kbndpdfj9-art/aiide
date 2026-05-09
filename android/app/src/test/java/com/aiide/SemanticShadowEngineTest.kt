package com.aiide

import org.junit.Test
import org.junit.Assert.*

class SemanticShadowEngineTest {

    @Test
    fun testAnalyze() {
        val engine = SemanticShadowEngine(android.content.Context::class.java.newInstance())
        val result = engine.analyze("fun main() { println(\"Hello\") }")

        assertNotNull(result)
        assertTrue(result.patterns.contains("oop") || result.patterns.contains("loop"))
    }

    @Test
    fun testCache() {
        val engine = SemanticShadowEngine(android.content.Context::class.java.newInstance())
        engine.cache("test_key", "test_value", setOf("test"))

        val cached = engine.get("test_key")
        assertEquals("test_value", cached)
    }

    @Test
    fun testStats() {
        val engine = SemanticShadowEngine(android.content.Context::class.java.newInstance())
        val stats = engine.getStats()

        assertNotNull(stats)
        assertTrue(stats.has("cache_size"))
    }
}
