package com.sd.demo.cache

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.sd.lib.cache.FCache
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LimitTest {

    @Test
    fun testLimitByte() {
        val key = "TestKey"
        val cache = FCache.limitByte(100 * 1024, "testLimitByte")
    }
}