package com.sd.demo.cache

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.sd.lib.cache.FCache
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class TestCommonCache {
    @Test
    fun testCacheInt() {
        val key = "TestKey"

        // test get defaultValue
        FCache.disk().cacheInteger().remove(key)
        Assert.assertEquals(Int.MIN_VALUE, FCache.disk().cacheInteger().get(key, Int.MIN_VALUE))

        // test put and get
        Assert.assertEquals(true, FCache.disk().cacheInteger().put(key, 1))
        Assert.assertEquals(1, FCache.disk().cacheInteger().get(key, Int.MIN_VALUE))

        // test remove and get
        Assert.assertEquals(true, FCache.disk().cacheInteger().remove(key))
        Assert.assertEquals(Int.MIN_VALUE, FCache.disk().cacheInteger().get(key, Int.MIN_VALUE))
    }

    @Test
    fun testCacheLong() {
        val key = "TestKey"

        // test get defaultValue
        FCache.disk().cacheLong().remove(key)
        Assert.assertEquals(Long.MIN_VALUE, FCache.disk().cacheLong().get(key, Long.MIN_VALUE))

        // test put and get
        Assert.assertEquals(true, FCache.disk().cacheLong().put(key, 1L))
        Assert.assertEquals(1L, FCache.disk().cacheLong().get(key, Long.MIN_VALUE))

        // test remove and get
        Assert.assertEquals(true, FCache.disk().cacheLong().remove(key))
        Assert.assertEquals(Long.MIN_VALUE, FCache.disk().cacheLong().get(key, Long.MIN_VALUE))
    }

    @Test
    fun testCacheFloat() {
        val key = "TestKey"

        // test get defaultValue
        FCache.disk().cacheFloat().remove(key)
        Assert.assertEquals(Float.MIN_VALUE, FCache.disk().cacheFloat().get(key, Float.MIN_VALUE))

        // test put and get
        Assert.assertEquals(true, FCache.disk().cacheFloat().put(key, 1.0f))
        Assert.assertEquals(1.0f, FCache.disk().cacheFloat().get(key, Float.MIN_VALUE))

        // test remove and get
        Assert.assertEquals(true, FCache.disk().cacheFloat().remove(key))
        Assert.assertEquals(Float.MIN_VALUE, FCache.disk().cacheFloat().get(key, Float.MIN_VALUE))
    }

    @Test
    fun testCacheDouble() {
        val key = "TestKey"

        // test get defaultValue
        FCache.disk().cacheDouble().remove(key)
        Assert.assertEquals(Double.MIN_VALUE, FCache.disk().cacheDouble().get(key, Double.MIN_VALUE), 0.01)

        // test put and get
        Assert.assertEquals(true, FCache.disk().cacheDouble().put(key, 1.0))
        Assert.assertEquals(1.0, FCache.disk().cacheDouble().get(key, Double.MIN_VALUE), 0.01)

        // test remove and get
        Assert.assertEquals(true, FCache.disk().cacheDouble().remove(key))
        Assert.assertEquals(Double.MIN_VALUE, FCache.disk().cacheDouble().get(key, Double.MIN_VALUE), 0.01)
    }
}