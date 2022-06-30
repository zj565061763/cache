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
        Assert.assertEquals(true, FCache.disk().cacheLong().put(key, 1))
        Assert.assertEquals(1, FCache.disk().cacheLong().get(key, Long.MIN_VALUE))

        // test remove and get
        Assert.assertEquals(true, FCache.disk().cacheLong().remove(key))
        Assert.assertEquals(Long.MIN_VALUE, FCache.disk().cacheLong().get(key, Long.MIN_VALUE))
    }
}