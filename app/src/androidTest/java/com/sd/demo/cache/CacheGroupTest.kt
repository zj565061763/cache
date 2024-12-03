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
        val cache = FCache.getActive()

        testCacheEmptyActiveGroup(cache)

        FCache.setActiveGroup("100")
        CacheTestUtils.testCacheObject(cache)
        CacheTestUtils.testCacheMultiObject(cache)

        FCache.setActiveGroup("")
        testCacheEmptyActiveGroup(cache)
    }
}

private fun testCacheEmptyActiveGroup(cache: Cache) {
    val key = "testCacheEmptyActiveGroup"

    val c = cache.multi(TestModel::class.java)
    c.remove(key)

    // test get defaultValue
    assertEquals(false, c.contains(key))
    assertEquals(null, c.get(key))

    // test put and get
    assertEquals(false, c.put(key, TestModel()))
    assertEquals(false, c.contains(key))
    assertEquals(null, c.get(key))
}