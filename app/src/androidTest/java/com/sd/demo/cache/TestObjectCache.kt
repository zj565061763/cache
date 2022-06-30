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
        val testModel = TestModel()

        // test get defaultValue
        FCache.disk().cacheObject().remove(TestModel::class.java)
        Assert.assertEquals(null, FCache.disk().cacheObject().get(TestModel::class.java))

        // test put and get
        Assert.assertEquals(true, FCache.disk().cacheObject().put(testModel))
        Assert.assertEquals(testModel, FCache.disk().cacheObject().get(TestModel::class.java))

        // test remove and get
        Assert.assertEquals(true, FCache.disk().cacheObject().remove(TestModel::class.java))
        Assert.assertEquals(null, FCache.disk().cacheObject().get(TestModel::class.java))
    }
}