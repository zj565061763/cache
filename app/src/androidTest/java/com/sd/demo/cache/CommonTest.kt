package com.sd.demo.cache

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.sd.lib.cache.cacheObject
import com.sd.lib.cache.cacheObjects
import com.sd.lib.cache.fCache
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class CommonTest {
    private val _testEmptyByteArray = true
    private val _testEmptyString = true

    @Test
    fun testCacheInt() {
        val key = "TestKey"
        val cache = fCache.cInt()

        // test get defaultValue
        cache.remove(key)
        assertEquals(false, cache.contains(key))
        assertEquals(null, cache.get(key))

        // test put and get
        assertEquals(true, cache.put(key, 1))
        assertEquals(true, cache.contains(key))
        assertEquals(1, cache.get(key))

        // test remove and get
        cache.remove(key)
        assertEquals(false, cache.contains(key))
        assertEquals(null, cache.get(key))
    }

    @Test
    fun testCacheLong() {
        val key = "TestKey"
        val cache = fCache.cLong()

        // test get defaultValue
        cache.remove(key)
        assertEquals(false, cache.contains(key))
        assertEquals(null, cache.get(key))

        // test put and get
        assertEquals(true, cache.put(key, 1L))
        assertEquals(true, cache.contains(key))
        assertEquals(1L, cache.get(key))

        // test remove and get
        cache.remove(key)
        assertEquals(false, cache.contains(key))
        assertEquals(null, cache.get(key))
    }

    @Test
    fun testCacheFloat() {
        val key = "TestKey"
        val cache = fCache.cFloat()

        // test get defaultValue
        cache.remove(key)
        assertEquals(false, cache.contains(key))
        assertEquals(null, cache.get(key))

        // test put and get
        assertEquals(true, cache.put(key, 1.0f))
        assertEquals(true, cache.contains(key))
        assertEquals(1.0f, cache.get(key))

        // test remove and get
        cache.remove(key)
        assertEquals(false, cache.contains(key))
        assertEquals(null, cache.get(key))
    }

    @Test
    fun testCacheDouble() {
        val key = "TestKey"
        val cache = fCache.cDouble()

        // test get defaultValue
        cache.remove(key)
        assertEquals(false, cache.contains(key))
        assertEquals(0.0, cache.get(key) ?: 0.0, 0.01)

        // test put and get
        assertEquals(true, cache.put(key, 1.0))
        assertEquals(true, cache.contains(key))
        assertEquals(1.0, cache.get(key) ?: 0.0, 0.01)

        // test remove and get
        cache.remove(key)
        assertEquals(false, cache.contains(key))
        assertEquals(0.0, cache.get(key) ?: 0.0, 0.01)
    }

    @Test
    fun testCacheBoolean() {
        val key = "TestKey"
        val cache = fCache.cBoolean()

        // test get defaultValue
        cache.remove(key)
        assertEquals(false, cache.contains(key))
        assertEquals(null, cache.get(key))

        // test put and get
        assertEquals(true, cache.put(key, true))
        assertEquals(true, cache.contains(key))
        assertEquals(true, cache.get(key))

        // test remove and get
        cache.remove(key)
        assertEquals(false, cache.contains(key))
        assertEquals(null, cache.get(key))
    }

    @Test
    fun testCacheString() {
        val key = "TestKey"
        val cache = fCache.cString()

        // test get defaultValue
        cache.remove(key)
        assertEquals(false, cache.contains(key))
        assertEquals(null, cache.get(key))

        // test put and get
        assertEquals(true, cache.put(key, "hello"))
        assertEquals(true, cache.contains(key))
        assertEquals("hello", cache.get(key))

        if (_testEmptyString) {
            assertEquals(true, cache.put(key, ""))
            assertEquals(true, cache.contains(key))
            assertEquals("", cache.get(key))
        }

        // test remove and get
        cache.remove(key)
        assertEquals(false, cache.contains(key))
        assertEquals(null, cache.get(key))
    }

    @Test
    fun testCacheBytes() {
        val key = "TestKey"
        val cache = fCache.cBytes()

        // test get defaultValue
        cache.remove(key)
        assertEquals(false, cache.contains(key))
        assertEquals(null, cache.get(key))

        // test put and get
        assertEquals(true, cache.put(key, "hello".toByteArray()))
        assertEquals(true, cache.contains(key))
        assertEquals("hello", cache.get(key)?.let { String(it) })

        if (_testEmptyByteArray) {
            val emptyByteArray = "".toByteArray()
            assertEquals(true, cache.put(key, emptyByteArray))
            assertEquals(true, cache.contains(key))
            assertEquals(0, cache.get(key)?.size)
        }

        // test remove and get
        cache.remove(key)
        assertEquals(false, cache.contains(key))
        assertEquals(null, cache.get(key))
    }
}