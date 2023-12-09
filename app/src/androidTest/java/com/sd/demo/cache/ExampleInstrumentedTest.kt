package com.sd.demo.cache

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.sd.lib.cache.fCache
import org.junit.Assert
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
    fun testPutMulti() {
        val key = "TestKey"
        Assert.assertEquals(true, fCache.cacheInt().put(key, 1))
        Assert.assertEquals(true, fCache.cacheInt().put(key, 2))
        Assert.assertEquals(true, fCache.cacheInt().put(key, 3))
        Assert.assertEquals(true, fCache.cacheInt().put(key, 4))
        Assert.assertEquals(true, fCache.cacheInt().put(key, 5))

        Assert.assertEquals(5, fCache.cacheInt().get(key))
        Assert.assertEquals(true, fCache.cacheInt().contains(key))
    }

    @Test
    fun testCacheInt() {
        val key = "TestKey"

        // test get defaultValue
        fCache.cacheInt().remove(key)
        Assert.assertEquals(null, fCache.cacheInt().get(key))
        Assert.assertEquals(false, fCache.cacheInt().contains(key))

        // test put and get
        Assert.assertEquals(true, fCache.cacheInt().put(key, 1))
        Assert.assertEquals(1, fCache.cacheInt().get(key))
        Assert.assertEquals(true, fCache.cacheInt().contains(key))

        // test remove and get
        Assert.assertEquals(true, fCache.cacheInt().remove(key))
        Assert.assertEquals(null, fCache.cacheInt().get(key))
        Assert.assertEquals(false, fCache.cacheInt().contains(key))
    }

    @Test
    fun testCacheLong() {
        val key = "TestKey"

        // test get defaultValue
        fCache.cacheLong().remove(key)
        Assert.assertEquals(null, fCache.cacheLong().get(key))
        Assert.assertEquals(false, fCache.cacheLong().contains(key))

        // test put and get
        Assert.assertEquals(true, fCache.cacheLong().put(key, 1L))
        Assert.assertEquals(1L, fCache.cacheLong().get(key))
        Assert.assertEquals(true, fCache.cacheLong().contains(key))

        // test remove and get
        Assert.assertEquals(true, fCache.cacheLong().remove(key))
        Assert.assertEquals(null, fCache.cacheLong().get(key))
        Assert.assertEquals(false, fCache.cacheLong().contains(key))
    }

    @Test
    fun testCacheFloat() {
        val key = "TestKey"

        // test get defaultValue
        fCache.cacheFloat().remove(key)
        Assert.assertEquals(null, fCache.cacheFloat().get(key))
        Assert.assertEquals(false, fCache.cacheFloat().contains(key))

        // test put and get
        Assert.assertEquals(true, fCache.cacheFloat().put(key, 1.0f))
        Assert.assertEquals(1.0f, fCache.cacheFloat().get(key))
        Assert.assertEquals(true, fCache.cacheFloat().contains(key))

        // test remove and get
        Assert.assertEquals(true, fCache.cacheFloat().remove(key))
        Assert.assertEquals(null, fCache.cacheFloat().get(key))
        Assert.assertEquals(false, fCache.cacheFloat().contains(key))
    }

    @Test
    fun testCacheDouble() {
        val key = "TestKey"

        // test get defaultValue
        fCache.cacheDouble().remove(key)
        Assert.assertEquals(0.0, fCache.cacheDouble().get(key) ?: 0.0, 0.01)
        Assert.assertEquals(false, fCache.cacheDouble().contains(key))

        // test put and get
        Assert.assertEquals(true, fCache.cacheDouble().put(key, 1.0))
        Assert.assertEquals(1.0, fCache.cacheDouble().get(key) ?: 0.0, 0.01)
        Assert.assertEquals(true, fCache.cacheDouble().contains(key))

        // test remove and get
        Assert.assertEquals(true, fCache.cacheDouble().remove(key))
        Assert.assertEquals(0.0, fCache.cacheDouble().get(key) ?: 0.0, 0.01)
        Assert.assertEquals(false, fCache.cacheDouble().contains(key))
    }

    @Test
    fun testCacheBoolean() {
        val key = "TestKey"

        // test get defaultValue
        fCache.cacheBoolean().remove(key)
        Assert.assertEquals(null, fCache.cacheBoolean().get(key))
        Assert.assertEquals(false, fCache.cacheBoolean().contains(key))

        // test put and get
        Assert.assertEquals(true, fCache.cacheBoolean().put(key, true))
        Assert.assertEquals(true, fCache.cacheBoolean().get(key))
        Assert.assertEquals(true, fCache.cacheBoolean().contains(key))

        // test remove and get
        Assert.assertEquals(true, fCache.cacheBoolean().remove(key))
        Assert.assertEquals(null, fCache.cacheBoolean().get(key))
        Assert.assertEquals(false, fCache.cacheBoolean().contains(key))
    }

    @Test
    fun testCacheString() {
        val key = "TestKey"

        // test get defaultValue
        fCache.cacheString().remove(key)
        Assert.assertEquals(null, fCache.cacheString().get(key))
        Assert.assertEquals(false, fCache.cacheString().contains(key))

        // test put and get
        Assert.assertEquals(true, fCache.cacheString().put(key, "hello"))
        Assert.assertEquals("hello", fCache.cacheString().get(key))
        Assert.assertEquals(true, fCache.cacheString().contains(key))

        Assert.assertEquals(true, fCache.cacheString().put(key, ""))
        Assert.assertEquals("", fCache.cacheString().get(key))
        Assert.assertEquals(true, fCache.cacheString().contains(key))

        // test remove and get
        Assert.assertEquals(true, fCache.cacheString().remove(key))
        Assert.assertEquals(null, fCache.cacheString().get(key))
        Assert.assertEquals(false, fCache.cacheString().contains(key))
    }

    @Test
    fun testCacheBytes() {
        val key = "TestKey"

        // test get defaultValue
        fCache.cacheBytes().remove(key)
        Assert.assertEquals(null, fCache.cacheBytes().get(key))
        Assert.assertEquals(false, fCache.cacheBytes().contains(key))

        // test put and get
        Assert.assertEquals(true, fCache.cacheBytes().put(key, "hello".toByteArray()))
        Assert.assertEquals("hello", fCache.cacheBytes().get(key)?.let { String(it) })
        Assert.assertEquals(true, fCache.cacheBytes().contains(key))

        Assert.assertEquals(true, fCache.cacheBytes().put(key, "".toByteArray()))
        Assert.assertEquals(0, fCache.cacheBytes().get(key)?.size)
        Assert.assertEquals(true, fCache.cacheBytes().contains(key))

        // test remove and get
        Assert.assertEquals(true, fCache.cacheBytes().remove(key))
        Assert.assertEquals(null, fCache.cacheBytes().get(key))
        Assert.assertEquals(false, fCache.cacheBytes().contains(key))
    }

    @Test
    fun testCacheObject() {
        val model = TestModel()

        // test get defaultValue
        fCache.objectSingle(TestModel::class.java).remove()
        Assert.assertEquals(null, fCache.objectSingle(TestModel::class.java).get())
        Assert.assertEquals(false, fCache.objectSingle(TestModel::class.java).contains())

        // test put and get
        Assert.assertEquals(true, fCache.objectSingle(TestModel::class.java).put(model))
        Assert.assertEquals(model, fCache.objectSingle(TestModel::class.java).get())
        Assert.assertEquals(true, fCache.objectSingle(TestModel::class.java).contains())

        // test remove and get
        Assert.assertEquals(true, fCache.objectSingle(TestModel::class.java).remove())
        Assert.assertEquals(null, fCache.objectSingle(TestModel::class.java).get())
        Assert.assertEquals(false, fCache.objectSingle(TestModel::class.java).contains())
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
        Assert.assertEquals(null, fCache.objectMulti(TestModel::class.java).get(key1))
        Assert.assertEquals(null, fCache.objectMulti(TestModel::class.java).get(key2))
        Assert.assertEquals(false, fCache.objectMulti(TestModel::class.java).contains(key1))
        Assert.assertEquals(false, fCache.objectMulti(TestModel::class.java).contains(key2))

        // test put and get
        Assert.assertEquals(true, fCache.objectMulti(TestModel::class.java).put(key1, model1))
        Assert.assertEquals(true, fCache.objectMulti(TestModel::class.java).put(key2, model2))
        Assert.assertEquals(model1, fCache.objectMulti(TestModel::class.java).get(key1))
        Assert.assertEquals(model2, fCache.objectMulti(TestModel::class.java).get(key2))
        Assert.assertEquals(true, fCache.objectMulti(TestModel::class.java).contains(key1))
        Assert.assertEquals(true, fCache.objectMulti(TestModel::class.java).contains(key2))

        // test remove and get
        Assert.assertEquals(true, fCache.objectMulti(TestModel::class.java).remove(key1))
        Assert.assertEquals(true, fCache.objectMulti(TestModel::class.java).remove(key2))
        Assert.assertEquals(null, fCache.objectMulti(TestModel::class.java).get(key1))
        Assert.assertEquals(null, fCache.objectMulti(TestModel::class.java).get(key2))
        Assert.assertEquals(false, fCache.objectMulti(TestModel::class.java).contains(key1))
        Assert.assertEquals(false, fCache.objectMulti(TestModel::class.java).contains(key2))
    }
}