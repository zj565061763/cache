package com.sd.demo.cache

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.sd.lib.cache.FCache
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class CacheTest {

    @Test
    fun testCacheInt() {
        logMsg { "1" }
        CacheTestUtils.testCacheInt(FCache.get())
    }

    @Test
    fun testCacheLong() {
        logMsg { "2" }
        CacheTestUtils.testCacheLong(FCache.get())
    }

    @Test
    fun testCacheFloat() {
        logMsg { "3" }
        CacheTestUtils.testCacheFloat(FCache.get())
    }

    @Test
    fun testCacheDouble() {
        logMsg { "4" }
        CacheTestUtils.testCacheDouble(FCache.get())
    }

    @Test
    fun testCacheBoolean() {
        logMsg { "5" }
        CacheTestUtils.testCacheBoolean(FCache.get())
    }

    @Test
    fun testCacheString() {
        logMsg { "6" }
        CacheTestUtils.testCacheString(FCache.get())
    }

    @Test
    fun testCacheBytes() {
        logMsg { "7" }
        CacheTestUtils.testCacheBytes(FCache.get())
    }

    @Test
    fun testCacheObject() {
        logMsg { "8" }
        CacheTestUtils.testCacheObject(FCache.get())
    }

    @Test
    fun testCacheMultiObject() {
        logMsg { "9" }
        CacheTestUtils.testCacheMultiObject(FCache.get())
    }

    @Test
    fun testLimitCount() {
        logMsg { "10" }
        val limit = 10
        val cache = FCache.defaultGroup().limitCount("limitCount", limit)
        CacheTestUtils.testLimitCount(cache, limit)
    }
}