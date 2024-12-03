package com.sd.demo.cache

import com.sd.lib.cache.ActiveGroupCache
import com.sd.lib.cache.DefaultGroupCache
import com.sd.lib.cache.FCache
import org.junit.Assert.assertEquals

@DefaultGroupCache("TestDefaultModel")
data class TestDefaultModel(
    val name: String = "tom",
)

@DefaultGroupCache("TestDefaultModelLimitCount", limitCount = 2)
data class TestDefaultModelLimitCount(
    val name: String = "tom",
)

@ActiveGroupCache("TestActiveModel")
data class TestActiveModel(
    val name: String = "tom",
)

fun <T> testCache(
    clazz: Class<T>,
    factory: (key: String) -> T,
) {
    val cache = FCache.get(clazz)

    val key1 = "TestKey1"
    val key2 = "TestKey2"

    val model1 = factory(key1)
    val model2 = factory(key2)

    // test default value
    assertEquals(false, cache.contains(key1))
    assertEquals(false, cache.contains(key2))
    assertEquals(null, cache.get(key1))
    assertEquals(null, cache.get(key2))

    // test put and get
    assertEquals(true, cache.put(key1, model1))
    assertEquals(true, cache.put(key2, model2))
    assertEquals(true, cache.contains(key1))
    assertEquals(true, cache.contains(key2))
    assertEquals(model1, cache.get(key1))
    assertEquals(model2, cache.get(key2))

    // test keys
    cache.keys().also {
        assertEquals(2, it.size)
        assertEquals(true, it.contains(key1))
        assertEquals(true, it.contains(key2))
    }

    // test remove and get
    cache.remove(key1)
    cache.remove(key2)
    assertEquals(false, cache.contains(key1))
    assertEquals(false, cache.contains(key2))
    assertEquals(null, cache.get(key1))
    assertEquals(null, cache.get(key2))
    assertEquals(0, cache.keys().size)
}