package com.sd.demo.cache

import androidx.test.ext.junit.runners.AndroidJUnit4
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
class ExampleInstrumentedTest {

    @Test
    fun testCacheInt() {
        val key = "TestKey"
        val cache = fCache.cacheInt()

        // test get defaultValue
        cache.remove(key)
        assertEquals(null, cache.get(key))
        assertEquals(false, cache.contains(key))

        // test put and get
        assertEquals(true, cache.put(key, 1))
        assertEquals(1, cache.get(key))
        assertEquals(true, cache.contains(key))

        // test remove and get
        assertEquals(true, cache.remove(key))
        assertEquals(null, cache.get(key))
        assertEquals(false, cache.contains(key))

        assertEquals(false, cache.remove(key))
        assertEquals(null, cache.get(key))
        assertEquals(false, cache.contains(key))
    }

    @Test
    fun testCacheLong() {
        val key = "TestKey"
        val cache = fCache.cacheLong()

        // test get defaultValue
        cache.remove(key)
        assertEquals(null, cache.get(key))
        assertEquals(false, cache.contains(key))

        // test put and get
        assertEquals(true, cache.put(key, 1L))
        assertEquals(1L, cache.get(key))
        assertEquals(true, cache.contains(key))

        // test remove and get
        assertEquals(true, cache.remove(key))
        assertEquals(null, cache.get(key))
        assertEquals(false, cache.contains(key))

        assertEquals(false, cache.remove(key))
        assertEquals(null, cache.get(key))
        assertEquals(false, cache.contains(key))
    }

    @Test
    fun testCacheFloat() {
        val key = "TestKey"
        val cache = fCache.cacheFloat()

        // test get defaultValue
        cache.remove(key)
        assertEquals(null, cache.get(key))
        assertEquals(false, cache.contains(key))

        // test put and get
        assertEquals(true, cache.put(key, 1.0f))
        assertEquals(1.0f, cache.get(key))
        assertEquals(true, cache.contains(key))

        // test remove and get
        assertEquals(true, cache.remove(key))
        assertEquals(null, cache.get(key))
        assertEquals(false, cache.contains(key))

        assertEquals(false, cache.remove(key))
        assertEquals(null, cache.get(key))
        assertEquals(false, cache.contains(key))
    }

    @Test
    fun testCacheDouble() {
        val key = "TestKey"
        val cache = fCache.cacheDouble()

        // test get defaultValue
        cache.remove(key)
        assertEquals(0.0, cache.get(key) ?: 0.0, 0.01)
        assertEquals(false, cache.contains(key))

        // test put and get
        assertEquals(true, cache.put(key, 1.0))
        assertEquals(1.0, cache.get(key) ?: 0.0, 0.01)
        assertEquals(true, cache.contains(key))

        // test remove and get
        assertEquals(true, cache.remove(key))
        assertEquals(0.0, cache.get(key) ?: 0.0, 0.01)
        assertEquals(false, cache.contains(key))

        assertEquals(false, cache.remove(key))
        assertEquals(0.0, cache.get(key) ?: 0.0, 0.01)
        assertEquals(false, cache.contains(key))
    }

    @Test
    fun testCacheBoolean() {
        val key = "TestKey"

        // test get defaultValue
        fCache.cacheBoolean().remove(key)
        assertEquals(null, fCache.cacheBoolean().get(key))
        assertEquals(false, fCache.cacheBoolean().contains(key))

        // test put and get
        assertEquals(true, fCache.cacheBoolean().put(key, true))
        assertEquals(true, fCache.cacheBoolean().get(key))
        assertEquals(true, fCache.cacheBoolean().contains(key))

        // test remove and get
        assertEquals(true, fCache.cacheBoolean().remove(key))
        assertEquals(null, fCache.cacheBoolean().get(key))
        assertEquals(false, fCache.cacheBoolean().contains(key))
    }

    @Test
    fun testCacheString() {
        val key = "TestKey"

        // test get defaultValue
        fCache.cacheString().remove(key)
        assertEquals(null, fCache.cacheString().get(key))
        assertEquals(false, fCache.cacheString().contains(key))

        // test put and get
        assertEquals(true, fCache.cacheString().put(key, "hello"))
        assertEquals("hello", fCache.cacheString().get(key))
        assertEquals(true, fCache.cacheString().contains(key))

        assertEquals(true, fCache.cacheString().put(key, ""))
        assertEquals("", fCache.cacheString().get(key))
        assertEquals(true, fCache.cacheString().contains(key))

        // test remove and get
        assertEquals(true, fCache.cacheString().remove(key))
        assertEquals(null, fCache.cacheString().get(key))
        assertEquals(false, fCache.cacheString().contains(key))
    }

    @Test
    fun testCacheBytes() {
        val key = "TestKey"

        // test get defaultValue
        fCache.cacheBytes().remove(key)
        assertEquals(null, fCache.cacheBytes().get(key))
        assertEquals(false, fCache.cacheBytes().contains(key))

        // test put and get
        assertEquals(true, fCache.cacheBytes().put(key, "hello".toByteArray()))
        assertEquals("hello", fCache.cacheBytes().get(key)?.let { String(it) })
        assertEquals(true, fCache.cacheBytes().contains(key))

        assertEquals(true, fCache.cacheBytes().put(key, "".toByteArray()))
        assertEquals(0, fCache.cacheBytes().get(key)?.size)
        assertEquals(true, fCache.cacheBytes().contains(key))

        // test remove and get
        assertEquals(true, fCache.cacheBytes().remove(key))
        assertEquals(null, fCache.cacheBytes().get(key))
        assertEquals(false, fCache.cacheBytes().contains(key))
    }

    @Test
    fun testCacheObject() {
        val model = TestModel()

        // test get defaultValue
        fCache.objectSingle(TestModel::class.java).remove()
        assertEquals(null, fCache.objectSingle(TestModel::class.java).get())
        assertEquals(false, fCache.objectSingle(TestModel::class.java).contains())

        // test put and get
        assertEquals(true, fCache.objectSingle(TestModel::class.java).put(model))
        assertEquals(model, fCache.objectSingle(TestModel::class.java).get())
        assertEquals(true, fCache.objectSingle(TestModel::class.java).contains())

        // test remove and get
        assertEquals(true, fCache.objectSingle(TestModel::class.java).remove())
        assertEquals(null, fCache.objectSingle(TestModel::class.java).get())
        assertEquals(false, fCache.objectSingle(TestModel::class.java).contains())
    }

    @Test
    fun testCacheMultiObject() {
        val key1 = "TestKey1"
        val key2 = "TestKey2"

        val model1 = TestModel("TestModel1")
        val model2 = TestModel("TestModel2")

        // test get defaultValue
        fCache.objectMulti(TestModel::class.java).let {
            it.remove(key1)
            it.remove(key2)
        }
        assertEquals(null, fCache.objectMulti(TestModel::class.java).get(key1))
        assertEquals(null, fCache.objectMulti(TestModel::class.java).get(key2))
        assertEquals(false, fCache.objectMulti(TestModel::class.java).contains(key1))
        assertEquals(false, fCache.objectMulti(TestModel::class.java).contains(key2))

        // test put and get
        assertEquals(true, fCache.objectMulti(TestModel::class.java).put(key1, model1))
        assertEquals(true, fCache.objectMulti(TestModel::class.java).put(key2, model2))
        assertEquals(model1, fCache.objectMulti(TestModel::class.java).get(key1))
        assertEquals(model2, fCache.objectMulti(TestModel::class.java).get(key2))
        assertEquals(true, fCache.objectMulti(TestModel::class.java).contains(key1))
        assertEquals(true, fCache.objectMulti(TestModel::class.java).contains(key2))

        // test remove and get
        assertEquals(true, fCache.objectMulti(TestModel::class.java).remove(key1))
        assertEquals(true, fCache.objectMulti(TestModel::class.java).remove(key2))
        assertEquals(null, fCache.objectMulti(TestModel::class.java).get(key1))
        assertEquals(null, fCache.objectMulti(TestModel::class.java).get(key2))
        assertEquals(false, fCache.objectMulti(TestModel::class.java).contains(key1))
        assertEquals(false, fCache.objectMulti(TestModel::class.java).contains(key2))
    }
}