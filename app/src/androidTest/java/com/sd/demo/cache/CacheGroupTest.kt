package com.sd.demo.cache

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.sd.lib.cache.Cache
import com.sd.lib.cache.FCache
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CacheGroupTest {
    @Test
    fun testActiveGroup() {
        val cache = FCache.get(TestActiveModel::class.java)
        testCacheEmptyActiveGroup(cache)

        FCache.setActiveGroup("test")
        testCache(
            clazz = TestActiveModel::class.java,
            factory = { TestActiveModel(name = it) },
        )

        FCache.setActiveGroup("")
        testCacheEmptyActiveGroup(cache)
    }
}

private fun testCacheEmptyActiveGroup(cache: Cache<TestActiveModel>) {
    val key = "testCacheEmptyActiveGroup"

    // test defaultValue
    assertEquals(false, cache.contains(key))
    assertEquals(null, cache.get(key))
    assertEquals(0, cache.keys().size)

    // test put and get
    assertEquals(false, cache.put(key, TestActiveModel()))
    assertEquals(false, cache.contains(key))
    assertEquals(null, cache.get(key))
    assertEquals(0, cache.keys().size)

    cache.remove(key)
}