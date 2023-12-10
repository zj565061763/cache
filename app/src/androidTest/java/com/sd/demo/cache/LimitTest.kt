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

        val content = "1".repeat(1024)
        repeat(100) { index ->
            val key = index.toString()

            assertEquals(true, cache.put(key, content))
            assertEquals(true, cache.contains(key))
            assertEquals(content, cache.get(key))
        }
    }
}