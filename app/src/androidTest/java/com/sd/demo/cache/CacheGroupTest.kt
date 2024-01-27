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
    fun testCurrentGroup() {
        val cache = FCache.currentGroup().unlimited("testCurrentGroup")

        testCacheIntCurrentGroup(cache)

        FCache.setCurrentGroup("100")
        CacheTestUtils.testCacheInt(cache)

        FCache.setCurrentGroup("")
        testCacheIntCurrentGroup(cache)
    }
}

private fun testCacheIntCurrentGroup(cache: Cache) {
    val key = "testCacheIntCurrentGroup"
    val c = cache.cInt()

    // test get defaultValue
    c.remove(key)
    Assert.assertEquals(false, c.contains(key))
    Assert.assertEquals(null, c.get(key))

    // test put and get
    Assert.assertEquals(false, c.put(key, 1))
    Assert.assertEquals(false, c.contains(key))
    Assert.assertEquals(null, c.get(key))
}