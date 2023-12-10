package com.sd.demo.cache

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.sd.lib.cache.FCache
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LimitTest {

    @Test
    fun testLimitCount() {
        val cache = FCache.limitCount(10, "testLimitCount").cString()
        repeat(10) { index ->
            val key = index.toString()
            val value = index.toString()
            assertEquals(true, cache.put(key, value))
            assertEquals(true, cache.contains(key))
            assertEquals(value, cache.get(key))
        }
    }
}