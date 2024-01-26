package com.sd.demo.cache

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.sd.lib.cache.CacheFactory
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
    private val _cache = FCache.get()

    private val _testEmptyByteArray = true
    private val _testEmptyString = true

    @Test
    fun testCacheInt() {
        CacheTestUtils.testCacheInt(FCache.get())
    }

    @Test
    fun testCacheLong() {
        CacheTestUtils.testCacheLong(FCache.get())
    }

    @Test
    fun testCacheFloat() {
        CacheTestUtils.testCacheFloat(FCache.get())
    }

    @Test
    fun testCacheDouble() {
        CacheTestUtils.testCacheDouble(FCache.get())
    }

    @Test
    fun testCacheBoolean() {
        CacheTestUtils.testCacheBoolean(FCache.get())
    }

    @Test
    fun testCacheString() {
        CacheTestUtils.testCacheString(FCache.get(), _testEmptyString)
    }

    @Test
    fun testCacheBytes() {
        CacheTestUtils.testCacheBytes(FCache.get(), _testEmptyByteArray)
    }

    @Test
    fun testCacheObject() {
        CacheTestUtils.testCacheObject(FCache.get())
    }

    @Test
    fun testCacheMultiObject() {
        CacheTestUtils.testCacheMultiObject(FCache.get())
    }

    @Test
    fun testLimitCount() {
        val limit = 10
        val cache = CacheFactory.groupDefault().limitCount("limitCount", limit)
        CacheTestUtils.testLimitCount(cache, limit)
    }
}