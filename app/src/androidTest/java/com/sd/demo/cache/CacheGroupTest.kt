package com.sd.demo.cache

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.sd.lib.cache.Cache
import com.sd.lib.cache.FCache
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CacheGroupTest {
    @Test
    fun testActiveGroup() {
        val cache = FCache.getActive()

        testCacheEmptyActiveGroup(cache)

        FCache.setActiveGroup("100")
        CacheTestUtils.testCacheMultiObject(cache)

        FCache.setActiveGroup("")
        testCacheEmptyActiveGroup(cache)
    }
}

private fun testCacheEmptyActiveGroup(cache: Cache) {
    val key = "testCacheEmptyActiveGroup"
    val c = cache.multi(TestModel::class.java)

    // test get defaultValue
    c.remove(key)
    Assert.assertEquals(false, c.contains(key))
    Assert.assertEquals(null, c.get(key))

    // test put and get
    Assert.assertEquals(false, c.put(key, TestModel()))
    Assert.assertEquals(false, c.contains(key))
    Assert.assertEquals(null, c.get(key))
}