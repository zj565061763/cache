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
        val limit = 10
        val cache = FCache.limitCount(limit, "testLimitCount").cString()
        repeat(limit) { index ->
            val key = index.toString()
            val value = index.toString()
            assertEquals(true, cache.put(key, value))
            assertEquals(true, cache.contains(key))
            assertEquals(value, cache.get(key))
        }

        fun testOverLimit(index: Int) {
            val key = index.toString()
            val value = index.toString()

            assertEquals(false, cache.contains(key))
            assertEquals(null, cache.get(key))

            assertEquals(true, cache.put(key, value))
            assertEquals(true, cache.contains(key))
            assertEquals(value, cache.get(key))

            val removedKey = (limit - index).toString()
            assertEquals(false, cache.contains(removedKey))
            assertEquals(null, cache.get(removedKey))
        }

        repeat(10) { index ->
            testOverLimit(limit + index)
        }
    }
}