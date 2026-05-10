package com.aiide

import org.junit.Test
import org.junit.Assert.*

class SemanticShadowEngineTest {

    @Test
    fun testAnalyze() {
        val engine = SemanticShadowEngine()
        val result = engine.analyze("class Greeter { fun main() { println(\"Hello\") } }")

        assertNotNull(result)
        assertTrue(result.patterns.isNotEmpty())
    }

    @Test
    fun testCache() {
        val engine = SemanticShadowEngine()
        engine.cache("test_key", "test_value", setOf("test"))

        val cached = engine.get("test_key")
        assertEquals("test_value", cached)
    }

    @Test
    fun testStats() {
        val engine = SemanticShadowEngine()
        val stats = engine.getStats()

        assertNotNull(stats)
        assertTrue(stats.has("cache_size"))
    }
}
