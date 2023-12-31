package com.sd.demo.cache

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.sd.lib.cache.FCache
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
class CacheTest {
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
        assertEquals("hello", cache.get(key)?.decodeToString())

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

    @Test
    fun testCacheObject() {
        val model = TestModel()
        val cache = fCache.cObject(TestModel::class.java)

        // test get defaultValue
        cache.remove()
        assertEquals(false, cache.contains())
        assertEquals(null, cache.get())

        // test put and get
        assertEquals(true, cache.put(model))
        assertEquals(true, cache.contains())
        assertEquals(model, cache.get())

        // test remove and get
        cache.remove()
        assertEquals(false, cache.contains())
        assertEquals(null, cache.get())
    }

    @Test
    fun testCacheMultiObject() {
        val key1 = "TestKey1"
        val key2 = "TestKey2"

        val model1 = TestModel("TestModel1")
        val model2 = TestModel("TestModel2")

        val cache = fCache.cObjects(TestModel::class.java)

        // test get defaultValue
        cache.let {
            it.remove(key1)
            it.remove(key2)
        }
        assertEquals(false, cache.contains(key1))
        assertEquals(false, cache.contains(key2))
        assertEquals(null, cache.get(key1))
        assertEquals(null, cache.get(key2))

        // test put and get
        assertEquals(true, cache.put(key1, model1))
        assertEquals(true, cache.put(key2, model2))
        assertEquals(true, cache.contains(key1))
        assertEquals(true, cache.contains(key2))
        assertEquals(model1, cache.get(key1))
        assertEquals(model2, cache.get(key2))

        // test remove and get
        cache.remove(key1)
        cache.remove(key2)
        assertEquals(false, cache.contains(key1))
        assertEquals(false, cache.contains(key2))
        assertEquals(null, cache.get(key1))
        assertEquals(null, cache.get(key2))
    }

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