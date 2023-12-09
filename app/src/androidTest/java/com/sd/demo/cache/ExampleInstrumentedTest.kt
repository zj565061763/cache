package com.sd.demo.cache

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.sd.lib.cache.FCache
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
        Assert.assertEquals(true, FCache.disk().cacheInt().put(key, 1))
        Assert.assertEquals(true, FCache.disk().cacheInt().put(key, 2))
        Assert.assertEquals(true, FCache.disk().cacheInt().put(key, 3))
        Assert.assertEquals(true, FCache.disk().cacheInt().put(key, 4))
        Assert.assertEquals(true, FCache.disk().cacheInt().put(key, 5))

        Assert.assertEquals(5, FCache.disk().cacheInt().get(key))
        Assert.assertEquals(true, FCache.disk().cacheInt().contains(key))
    }

    @Test
    fun testCacheInt() {
        val key = "TestKey"

        // test get defaultValue
        FCache.disk().cacheInt().remove(key)
        Assert.assertEquals(null, FCache.disk().cacheInt().get(key))
        Assert.assertEquals(false, FCache.disk().cacheInt().contains(key))

        // test put and get
        Assert.assertEquals(true, FCache.disk().cacheInt().put(key, 1))
        Assert.assertEquals(1, FCache.disk().cacheInt().get(key))
        Assert.assertEquals(true, FCache.disk().cacheInt().contains(key))

        // test remove and get
        Assert.assertEquals(true, FCache.disk().cacheInt().remove(key))
        Assert.assertEquals(null, FCache.disk().cacheInt().get(key))
        Assert.assertEquals(false, FCache.disk().cacheInt().contains(key))
    }

    @Test
    fun testCacheLong() {
        val key = "TestKey"

        // test get defaultValue
        FCache.disk().cacheLong().remove(key)
        Assert.assertEquals(null, FCache.disk().cacheLong().get(key))
        Assert.assertEquals(false, FCache.disk().cacheLong().contains(key))

        // test put and get
        Assert.assertEquals(true, FCache.disk().cacheLong().put(key, 1L))
        Assert.assertEquals(1L, FCache.disk().cacheLong().get(key))
        Assert.assertEquals(true, FCache.disk().cacheLong().contains(key))

        // test remove and get
        Assert.assertEquals(true, FCache.disk().cacheLong().remove(key))
        Assert.assertEquals(null, FCache.disk().cacheLong().get(key))
        Assert.assertEquals(false, FCache.disk().cacheLong().contains(key))
    }

    @Test
    fun testCacheFloat() {
        val key = "TestKey"

        // test get defaultValue
        FCache.disk().cacheFloat().remove(key)
        Assert.assertEquals(null, FCache.disk().cacheFloat().get(key))
        Assert.assertEquals(false, FCache.disk().cacheFloat().contains(key))

        // test put and get
        Assert.assertEquals(true, FCache.disk().cacheFloat().put(key, 1.0f))
        Assert.assertEquals(1.0f, FCache.disk().cacheFloat().get(key))
        Assert.assertEquals(true, FCache.disk().cacheFloat().contains(key))

        // test remove and get
        Assert.assertEquals(true, FCache.disk().cacheFloat().remove(key))
        Assert.assertEquals(null, FCache.disk().cacheFloat().get(key))
        Assert.assertEquals(false, FCache.disk().cacheFloat().contains(key))
    }

    @Test
    fun testCacheDouble() {
        val key = "TestKey"

        // test get defaultValue
        FCache.disk().cacheDouble().remove(key)
        Assert.assertEquals(0.0, FCache.disk().cacheDouble().get(key) ?: 0.0, 0.01)
        Assert.assertEquals(false, FCache.disk().cacheDouble().contains(key))

        // test put and get
        Assert.assertEquals(true, FCache.disk().cacheDouble().put(key, 1.0))
        Assert.assertEquals(1.0, FCache.disk().cacheDouble().get(key) ?: 0.0, 0.01)
        Assert.assertEquals(true, FCache.disk().cacheDouble().contains(key))

        // test remove and get
        Assert.assertEquals(true, FCache.disk().cacheDouble().remove(key))
        Assert.assertEquals(0.0, FCache.disk().cacheDouble().get(key) ?: 0.0, 0.01)
        Assert.assertEquals(false, FCache.disk().cacheDouble().contains(key))
    }

    @Test
    fun testCacheBoolean() {
        val key = "TestKey"

        // test get defaultValue
        FCache.disk().cacheBoolean().remove(key)
        Assert.assertEquals(null, FCache.disk().cacheBoolean().get(key))
        Assert.assertEquals(false, FCache.disk().cacheBoolean().contains(key))

        // test put and get
        Assert.assertEquals(true, FCache.disk().cacheBoolean().put(key, true))
        Assert.assertEquals(true, FCache.disk().cacheBoolean().get(key))
        Assert.assertEquals(true, FCache.disk().cacheBoolean().contains(key))

        // test remove and get
        Assert.assertEquals(true, FCache.disk().cacheBoolean().remove(key))
        Assert.assertEquals(null, FCache.disk().cacheBoolean().get(key))
        Assert.assertEquals(false, FCache.disk().cacheBoolean().contains(key))
    }

    @Test
    fun testCacheString() {
        val key = "TestKey"

        // test get defaultValue
        FCache.disk().cacheString().remove(key)
        Assert.assertEquals(null, FCache.disk().cacheString().get(key))
        Assert.assertEquals(false, FCache.disk().cacheString().contains(key))

        // test put and get
        Assert.assertEquals(true, FCache.disk().cacheString().put(key, "hello"))
        Assert.assertEquals("hello", FCache.disk().cacheString().get(key))
        Assert.assertEquals(true, FCache.disk().cacheString().contains(key))

        Assert.assertEquals(true, FCache.disk().cacheString().put(key, ""))
        Assert.assertEquals("", FCache.disk().cacheString().get(key))
        Assert.assertEquals(true, FCache.disk().cacheString().contains(key))

        // test remove and get
        Assert.assertEquals(true, FCache.disk().cacheString().remove(key))
        Assert.assertEquals(null, FCache.disk().cacheString().get(key))
        Assert.assertEquals(false, FCache.disk().cacheString().contains(key))
    }

    @Test
    fun testCacheBytes() {
        val key = "TestKey"

        // test get defaultValue
        FCache.disk().cacheBytes().remove(key)
        Assert.assertEquals(null, FCache.disk().cacheBytes().get(key))
        Assert.assertEquals(false, FCache.disk().cacheBytes().contains(key))

        // test put and get
        Assert.assertEquals(true, FCache.disk().cacheBytes().put(key, "hello".toByteArray()))
        Assert.assertEquals("hello", FCache.disk().cacheBytes().get(key)?.let { String(it) })
        Assert.assertEquals(true, FCache.disk().cacheBytes().contains(key))

        Assert.assertEquals(true, FCache.disk().cacheBytes().put(key, "".toByteArray()))
        Assert.assertEquals(0, FCache.disk().cacheBytes().get(key)?.size)
        Assert.assertEquals(true, FCache.disk().cacheBytes().contains(key))

        // test remove and get
        Assert.assertEquals(true, FCache.disk().cacheBytes().remove(key))
        Assert.assertEquals(null, FCache.disk().cacheBytes().get(key))
        Assert.assertEquals(false, FCache.disk().cacheBytes().contains(key))
    }

    @Test
    fun testCacheObject() {
        val model = TestModel()

        // test get defaultValue
        FCache.disk().cacheObject().remove(TestModel::class.java)
        Assert.assertEquals(null, FCache.disk().cacheObject().get(TestModel::class.java))
        Assert.assertEquals(false, FCache.disk().cacheObject().contains(TestModel::class.java))

        // test put and get
        Assert.assertEquals(true, FCache.disk().cacheObject().put(model))
        Assert.assertEquals(model, FCache.disk().cacheObject().get(TestModel::class.java))
        Assert.assertEquals(true, FCache.disk().cacheObject().contains(TestModel::class.java))

        // test remove and get
        Assert.assertEquals(true, FCache.disk().cacheObject().remove(TestModel::class.java))
        Assert.assertEquals(null, FCache.disk().cacheObject().get(TestModel::class.java))
        Assert.assertEquals(false, FCache.disk().cacheObject().contains(TestModel::class.java))
    }

    @Test
    fun testCacheMultiObject() {
        val key1 = "TestKey1"
        val key2 = "TestKey2"

        val model1 = TestModel("TestModel1")
        val model2 = TestModel("TestModel2")

        // test get defaultValue
        FCache.disk().objectMulti(TestModel::class.java).let {
            it.remove(key1)
            it.remove(key2)
        }
        Assert.assertEquals(null, FCache.disk().objectMulti(TestModel::class.java).get(key1))
        Assert.assertEquals(null, FCache.disk().objectMulti(TestModel::class.java).get(key2))
        Assert.assertEquals(false, FCache.disk().objectMulti(TestModel::class.java).contains(key1))
        Assert.assertEquals(false, FCache.disk().objectMulti(TestModel::class.java).contains(key2))

        // test put and get
        Assert.assertEquals(true, FCache.disk().objectMulti(TestModel::class.java).put(key1, model1))
        Assert.assertEquals(true, FCache.disk().objectMulti(TestModel::class.java).put(key2, model2))
        Assert.assertEquals(model1, FCache.disk().objectMulti(TestModel::class.java).get(key1))
        Assert.assertEquals(model2, FCache.disk().objectMulti(TestModel::class.java).get(key2))
        Assert.assertEquals(true, FCache.disk().objectMulti(TestModel::class.java).contains(key1))
        Assert.assertEquals(true, FCache.disk().objectMulti(TestModel::class.java).contains(key2))

        // test remove and get
        Assert.assertEquals(true, FCache.disk().objectMulti(TestModel::class.java).remove(key1))
        Assert.assertEquals(true, FCache.disk().objectMulti(TestModel::class.java).remove(key2))
        Assert.assertEquals(null, FCache.disk().objectMulti(TestModel::class.java).get(key1))
        Assert.assertEquals(null, FCache.disk().objectMulti(TestModel::class.java).get(key2))
        Assert.assertEquals(false, FCache.disk().objectMulti(TestModel::class.java).contains(key1))
        Assert.assertEquals(false, FCache.disk().objectMulti(TestModel::class.java).contains(key2))
    }
}