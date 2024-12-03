package com.sd.demo.cache

import com.sd.lib.cache.Cache
import org.junit.Assert.assertEquals

object TestUtils {
    fun testCacheObject(cache: Cache) {
        val model = DefaultModel(name = "testCacheObject")
        val c = cache.single(DefaultModel::class.java)

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

        val model1 = DefaultModel("TestModel1")
        val model2 = DefaultModel("TestModel2")

        val c = cache.multi(DefaultModel::class.java).apply {
            remove(key1)
            remove(key2)
        }

        // test get defaultValue
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
        c.keys().also {
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
        c.keys().also {
            assertEquals(0, it.size)
        }
    }
}