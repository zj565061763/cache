package com.sd.demo.cache

import com.sd.lib.cache.Cache
import org.junit.Assert.assertEquals

object CacheTestUtils {

    fun testCacheObject(cache: Cache) {
        val model = TestModel()
        val c = cache.o(TestModel::class.java)

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

        val c = cache.oo(TestModel::class.java)

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
        val c = cache.oo(TestModel::class.java)

        repeat(limit) { index ->
            val key = index.toString()
            val value = TestModel(index.toString())
            assertEquals(true, c.put(key, value))
            assertEquals(value, c.get(key))
        }

        fun testOverLimit(index: Int) {
            val key = index.toString()
            val value = TestModel(index.toString())

            assertEquals(false, c.contains(key))
            assertEquals(null, c.get(key))

            assertEquals(true, c.put(key, value))
            assertEquals(true, c.contains(key))
            assertEquals(value, c.get(key))

            val removedKey = (limit - index).toString()
            assertEquals(false, c.contains(removedKey))
            assertEquals(null, c.get(removedKey))
        }

        repeat(limit) { index ->
            testOverLimit(limit + index)
        }
    }
}