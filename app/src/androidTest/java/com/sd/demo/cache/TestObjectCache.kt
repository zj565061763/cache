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
class TestObjectCache {
    @Test
    fun testCacheObject() {
        val model = TestModel()

        // test get defaultValue
        FCache.disk().cacheObject().remove(TestModel::class.java)
        Assert.assertEquals(null, FCache.disk().cacheObject().get(TestModel::class.java))

        // test put and get
        Assert.assertEquals(true, FCache.disk().cacheObject().put(model))
        Assert.assertEquals(model, FCache.disk().cacheObject().get(TestModel::class.java))

        // test remove and get
        Assert.assertEquals(true, FCache.disk().cacheObject().remove(TestModel::class.java))
        Assert.assertEquals(null, FCache.disk().cacheObject().get(TestModel::class.java))
    }

    @Test
    fun testCacheMultiObject() {
        val key1 = "TestKey1"
        val key2 = "TestKey2"

        val model1 = TestModel("TestModel1")
        val model2 = TestModel("TestModel2")

        // test get defaultValue
        FCache.disk().cacheMultiObject(TestModel::class.java).let {
            it.remove(key1)
            it.remove(key2)
        }
        Assert.assertEquals(null, FCache.disk().cacheMultiObject(TestModel::class.java).get(key1))
        Assert.assertEquals(null, FCache.disk().cacheMultiObject(TestModel::class.java).get(key2))

        // test put and get
        Assert.assertEquals(true, FCache.disk().cacheMultiObject(TestModel::class.java).put(key1, model1))
        Assert.assertEquals(true, FCache.disk().cacheMultiObject(TestModel::class.java).put(key2, model2))
        Assert.assertEquals(model1, FCache.disk().cacheMultiObject(TestModel::class.java).get(key1))
        Assert.assertEquals(model2, FCache.disk().cacheMultiObject(TestModel::class.java).get(key2))

        // test remove and get
        Assert.assertEquals(true, FCache.disk().cacheMultiObject(TestModel::class.java).remove(key1))
        Assert.assertEquals(true, FCache.disk().cacheMultiObject(TestModel::class.java).remove(key2))
        Assert.assertEquals(null, FCache.disk().cacheMultiObject(TestModel::class.java).get(key1))
        Assert.assertEquals(null, FCache.disk().cacheMultiObject(TestModel::class.java).get(key2))
    }
}