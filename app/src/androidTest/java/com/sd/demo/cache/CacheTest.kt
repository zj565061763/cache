package com.sd.demo.cache

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.sd.lib.cache.FCache
import org.junit.Assert.assertEquals
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
    fun testEmptyKey() {
        FCache.getDefault().multi(TestModel::class.java).let { c ->
            assertEquals(false, c.put("", TestModel()))
            assertEquals(null, c.get(""))
            assertEquals(false, c.contains(""))
            c.remove("")
        }
    }

    @Test
    fun testCacheObject() {
        CacheTestUtils.testCacheObject(FCache.getDefault())
    }

    @Test
    fun testCacheMultiObject() {
        CacheTestUtils.testCacheMultiObject(FCache.getDefault())
    }

    @Test
    fun testLimitCount() {
        val limit = 10
        val cache = FCache.defaultGroupFactory().limitCount("testLimitCount", limit)
        CacheTestUtils.testLimitCount(cache, limit)
    }
}