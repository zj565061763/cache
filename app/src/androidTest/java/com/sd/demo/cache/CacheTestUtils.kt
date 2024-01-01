package com.sd.demo.cache

import com.sd.lib.cache.Cache
import org.junit.Assert.assertEquals

object CacheTestUtils {

    fun testCacheInt(cache: Cache) {
        val key = "TestKey"
        val c = cache.cInt()

        // test get defaultValue
        c.remove(key)
        assertEquals(false, c.contains(key))
        assertEquals(null, c.get(key))

        // test put and get
        assertEquals(true, c.put(key, 1))
        assertEquals(true, c.contains(key))
        assertEquals(1, c.get(key))

        // test remove and get
        c.remove(key)
        assertEquals(false, c.contains(key))
        assertEquals(null, c.get(key))
    }

    fun testCacheLong(cache: Cache) {
        val key = "TestKey"
        val c = cache.cLong()

        // test get defaultValue
        c.remove(key)
        assertEquals(false, c.contains(key))
        assertEquals(null, c.get(key))

        // test put and get
        assertEquals(true, c.put(key, 1L))
        assertEquals(true, c.contains(key))
        assertEquals(1L, c.get(key))

        // test remove and get
        c.remove(key)
        assertEquals(false, c.contains(key))
        assertEquals(null, c.get(key))
    }

    fun testCacheFloat(cache: Cache) {
        val key = "TestKey"
        val c = cache.cFloat()

        // test get defaultValue
        c.remove(key)
        assertEquals(false, c.contains(key))
        assertEquals(null, c.get(key))

        // test put and get
        assertEquals(true, c.put(key, 1.0f))
        assertEquals(true, c.contains(key))
        assertEquals(1.0f, c.get(key))

        // test remove and get
        c.remove(key)
        assertEquals(false, c.contains(key))
        assertEquals(null, c.get(key))
    }

    fun testCacheDouble(cache: Cache) {
        val key = "TestKey"
        val c = cache.cDouble()

        // test get defaultValue
        c.remove(key)
        assertEquals(false, c.contains(key))
        assertEquals(0.0, c.get(key) ?: 0.0, 0.01)

        // test put and get
        assertEquals(true, c.put(key, 1.0))
        assertEquals(true, c.contains(key))
        assertEquals(1.0, c.get(key) ?: 0.0, 0.01)

        // test remove and get
        c.remove(key)
        assertEquals(false, c.contains(key))
        assertEquals(0.0, c.get(key) ?: 0.0, 0.01)
    }

    fun testCacheBoolean(cache: Cache) {
        val key = "TestKey"
        val c = cache.cBoolean()

        // test get defaultValue
        c.remove(key)
        assertEquals(false, c.contains(key))
        assertEquals(null, c.get(key))

        // test put and get
        assertEquals(true, c.put(key, true))
        assertEquals(true, c.contains(key))
        assertEquals(true, c.get(key))

        // test remove and get
        c.remove(key)
        assertEquals(false, c.contains(key))
        assertEquals(null, c.get(key))
    }

    fun testCacheString(cache: Cache, testEmpty: Boolean) {
        val key = "TestKey"
        val c = cache.cString()

        // test get defaultValue
        c.remove(key)
        assertEquals(false, c.contains(key))
        assertEquals(null, c.get(key))

        // test put and get
        assertEquals(true, c.put(key, "hello"))
        assertEquals(true, c.contains(key))
        assertEquals("hello", c.get(key))

        if (testEmpty) {
            assertEquals(true, c.put(key, ""))
            assertEquals(true, c.contains(key))
            assertEquals("", c.get(key))
        }

        // test remove and get
        c.remove(key)
        assertEquals(false, c.contains(key))
        assertEquals(null, c.get(key))
    }

    fun testCacheBytes(cache: Cache, testEmpty: Boolean) {
        val key = "TestKey"
        val c = cache.cBytes()

        // test get defaultValue
        c.remove(key)
        assertEquals(false, c.contains(key))
        assertEquals(null, c.get(key))

        // test put and get
        assertEquals(true, c.put(key, "hello".toByteArray()))
        assertEquals(true, c.contains(key))
        assertEquals("hello", c.get(key)?.decodeToString())

        if (testEmpty) {
            val emptyByteArray = "".toByteArray()
            assertEquals(true, c.put(key, emptyByteArray))
            assertEquals(true, c.contains(key))
            assertEquals(0, c.get(key)?.size)
        }

        // test remove and get
        c.remove(key)
        assertEquals(false, c.contains(key))
        assertEquals(null, c.get(key))
    }

    fun testCacheObject(cache: Cache) {
        val model = TestModel()
        val c = cache.cObject(TestModel::class.java)

        // test get defaultValue
        c.remove()
        assertEquals(false, c.contains())
        assertEquals(null, c.get())

        // test put and get
        assertEquals(true, c.put(model))
        assertEquals(true, c.contains())
        assertEquals(model, c.get())

        // test remove and get
        c.remove()
        assertEquals(false, c.contains())
        assertEquals(null, c.get())
    }

    fun testCacheMultiObject(cache: Cache) {
        val key1 = "TestKey1"
        val key2 = "TestKey2"

        val model1 = TestModel("TestModel1")
        val model2 = TestModel("TestModel2")

        val c = cache.cObjects(TestModel::class.java)

        // test get defaultValue
        c.let {
            it.remove(key1)
            it.remove(key2)
        }
        assertEquals(false, c.contains(key1))
        assertEquals(false, c.contains(key2))
        assertEquals(null, c.get(key1))
        assertEquals(null, c.get(key2))

        // test put and get
        assertEquals(true, c.put(key1, model1))
        assertEquals(true, c.put(key2, model2))
        assertEquals(true, c.contains(key1))
        assertEquals(true, c.contains(key2))
        assertEquals(model1, c.get(key1))
        assertEquals(model2, c.get(key2))

        // test keys
        c.keys().let {
            assertEquals(2, it.size)
            assertEquals(true, it.contains(key1))
            assertEquals(true, it.contains(key2))
        }

        // test remove and get
        c.remove(key1)
        c.remove(key2)
        assertEquals(false, c.contains(key1))
        assertEquals(false, c.contains(key2))
        assertEquals(null, c.get(key1))
        assertEquals(null, c.get(key2))

        // test keys
        c.keys().let {
            assertEquals(0, it.size)
        }
    }

    fun testLimitCount(cache: Cache, limit: Int) {
        val c = cache.cString()
        repeat(limit) { index ->
            val key = index.toString()
            val value = index.toString()
            assertEquals(true, c.put(key, value))
            assertEquals(true, c.contains(key))
            assertEquals(value, c.get(key))
        }

        fun testOverLimit(index: Int) {
            val key = index.toString()
            val value = index.toString()

            assertEquals(false, c.contains(key))
            assertEquals(null, c.get(key))

            assertEquals(true, c.put(key, value))
            assertEquals(true, c.contains(key))
            assertEquals(value, c.get(key))

            val removedKey = (limit - index).toString()
            assertEquals(false, c.contains(removedKey))
            assertEquals(null, c.get(removedKey))
        }

        repeat(10) { index ->
            testOverLimit(limit + index)
        }
    }
}