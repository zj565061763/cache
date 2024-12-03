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
    fun testCache() {
        testCache(
            clazz = TestDefaultModel::class.java,
            factory = { TestDefaultModel(name = it) },
        )
    }

    @Test
    fun testLimitCount() {
        val cache = FCache.get(TestDefaultModelLimitCount::class.java)

        assertEquals(true, cache.keys().isEmpty())

        cache.put("1", TestDefaultModelLimitCount())
        cache.put("2", TestDefaultModelLimitCount())
        assertEquals(true, cache.contains("1"))
        assertEquals(true, cache.contains("2"))

        cache.put("3", TestDefaultModelLimitCount())
        assertEquals(false, cache.contains("1"))
        assertEquals(true, cache.contains("2"))
        assertEquals(true, cache.contains("3"))

        cache.put("4", TestDefaultModelLimitCount())
        assertEquals(false, cache.contains("1"))
        assertEquals(false, cache.contains("2"))
        assertEquals(true, cache.contains("3"))
        assertEquals(true, cache.contains("4"))
    }
}