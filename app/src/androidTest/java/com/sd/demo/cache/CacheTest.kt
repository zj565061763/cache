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
    private val _cache = FCache.get()

    private val _testEmptyByteArray = true
    private val _testEmptyString = true

    @Test
    fun testCacheInt() {
        CacheTestUtils.testCacheInt(FCache.get())
        CacheTestUtils.testCacheInt(FCache.getMemory())
    }

    @Test
    fun testCacheLong() {
        CacheTestUtils.testCacheLong(FCache.get())
        CacheTestUtils.testCacheLong(FCache.getMemory())
    }

    @Test
    fun testCacheFloat() {
        CacheTestUtils.testCacheFloat(FCache.get())
        CacheTestUtils.testCacheFloat(FCache.getMemory())
    }

    @Test
    fun testCacheDouble() {
        CacheTestUtils.testCacheDouble(FCache.get())
        CacheTestUtils.testCacheDouble(FCache.getMemory())
    }

    @Test
    fun testCacheBoolean() {
        CacheTestUtils.testCacheBoolean(FCache.get())
        CacheTestUtils.testCacheBoolean(FCache.getMemory())
    }

    @Test
    fun testCacheString() {
        CacheTestUtils.testCacheString(FCache.get(), _testEmptyString)
        CacheTestUtils.testCacheString(FCache.getMemory(), _testEmptyString)
    }

    @Test
    fun testCacheBytes() {
        CacheTestUtils.testCacheBytes(FCache.get(), _testEmptyByteArray)
        CacheTestUtils.testCacheBytes(FCache.getMemory(), _testEmptyByteArray)
    }

    @Test
    fun testCacheObject() {
        CacheTestUtils.testCacheObject(FCache.get())
        CacheTestUtils.testCacheObject(FCache.getMemory())
    }

    @Test
    fun testCacheMultiObject() {
        CacheTestUtils.testCacheMultiObject(FCache.get())
        CacheTestUtils.testCacheMultiObject(FCache.getMemory())
    }

    @Test
    fun testLimitCount() {
        val limit = 10
        CacheTestUtils.testLimitCount(FCache.limitCount(limit, "limitCount"), limit)
        CacheTestUtils.testLimitCount(FCache.limitCountMemory(limit, "limitCountMemory"), limit)
    }
}