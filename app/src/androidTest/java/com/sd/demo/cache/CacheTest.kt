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
    fun testCacheObject() {
        TestUtils.testCacheObject(FCache.getDefault())
    }

    @Test
    fun testCacheMultiObject() {
        TestUtils.testCacheMultiObject(FCache.getDefault())
    }

    @Test
    fun testLimitCount() {
        val cache = FCache.defaultGroupFactory().limitCount("testLimitCount", 2)

        val multi = cache.multi(TestModel::class.java)
        assertEquals(true, multi.keys().isEmpty())

        multi.put("1", TestModel(name = "1"))
        multi.put("2", TestModel(name = "2"))
        assertEquals(true, multi.contains("1"))
        assertEquals(true, multi.contains("2"))

        multi.put("3", TestModel(name = "3"))
        assertEquals(false, multi.contains("1"))
        assertEquals(true, multi.contains("2"))
        assertEquals(true, multi.contains("3"))
    }
}