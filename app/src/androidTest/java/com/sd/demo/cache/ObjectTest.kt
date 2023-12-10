package com.sd.demo.cache

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.sd.lib.cache.cacheObject
import com.sd.lib.cache.cacheObjects
import com.sd.lib.cache.fCache
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ObjectTest {

    @Test
    fun testCacheObject() {
        val model = TestModel()
        val cache = fCache.cacheObject<TestModel>()

        // test get defaultValue
        cache.remove()
        Assert.assertEquals(null, cache.get())
        Assert.assertEquals(false, cache.contains())

        // test put and get
        Assert.assertEquals(true, cache.put(model))
        Assert.assertEquals(true, cache.contains())
        Assert.assertEquals(model, cache.get())

        // test remove and get
        cache.remove()
        Assert.assertEquals(null, cache.get())
        Assert.assertEquals(false, cache.contains())
    }

    @Test
    fun testCacheMultiObject() {
        val key1 = "TestKey1"
        val key2 = "TestKey2"

        val model1 = TestModel("TestModel1")
        val model2 = TestModel("TestModel2")

        val cache = fCache.cacheObjects<TestModel>()

        // test get defaultValue
        cache.let {
            it.remove(key1)
            it.remove(key2)
        }
        Assert.assertEquals(null, cache.get(key1))
        Assert.assertEquals(null, cache.get(key2))
        Assert.assertEquals(false, cache.contains(key1))
        Assert.assertEquals(false, cache.contains(key2))

        // test put and get
        Assert.assertEquals(true, cache.put(key1, model1))
        Assert.assertEquals(true, cache.put(key2, model2))
        Assert.assertEquals(true, cache.contains(key1))
        Assert.assertEquals(true, cache.contains(key2))
        Assert.assertEquals(model1, cache.get(key1))
        Assert.assertEquals(model2, cache.get(key2))

        // test remove and get
        cache.remove(key1)
        cache.remove(key2)
        Assert.assertEquals(null, cache.get(key1))
        Assert.assertEquals(null, cache.get(key2))
        Assert.assertEquals(false, cache.contains(key1))
        Assert.assertEquals(false, cache.contains(key2))
    }
}