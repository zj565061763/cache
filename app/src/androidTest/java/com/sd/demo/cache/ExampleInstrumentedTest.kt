package com.sd.demo.cache

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.sd.lib.cache.Cache
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
class ExampleInstrumentedTest {
    @Test
    fun testCacheInt() {
        val key = "TestKey"
        Assert.assertEquals(Int.MIN_VALUE, FCache.disk().cacheInteger().get(key, Int.MIN_VALUE))

        Assert.assertEquals(true, FCache.disk().cacheInteger().put(key, 1))
        Assert.assertEquals(1, FCache.disk().cacheInteger().get(key, Int.MIN_VALUE))

        Assert.assertEquals(true, FCache.disk().cacheInteger().remove(key))
        Assert.assertEquals(Int.MIN_VALUE, FCache.disk().cacheInteger().get(key, Int.MIN_VALUE))
    }

    private fun <T> testCommonCache(cache: Cache.CommonCache<T>) {
        // test put

    }
}