package com.sd.demo.cache

import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import com.sd.lib.cache.DefaultGroupCache
import com.sd.lib.cache.FCacheKtx
import com.sd.lib.cache.update
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CacheKtxTest {
    @Test
    fun testCacheInstance() {
        val cache1 = FCacheKtx.get(TestKtxModel::class.java)
        val cache2 = FCacheKtx.get(TestKtxModel::class.java)
        assertEquals(true, cache1 === cache2)
    }

    @Test
    fun test() = runTest {
        val cache = FCacheKtx.get(TestKtxModel::class.java)

        val key1 = "key1"
        val key2 = "key2"
        val model1 = TestKtxModel(name = "name1")
        val model2 = TestKtxModel(name = "name2")

        // test default value
        assertEquals(false, cache.edit { contains(key1) })
        assertEquals(false, cache.edit { contains(key2) })
        assertEquals(null, cache.edit { get(key1) })
        assertEquals(null, cache.edit { get(key2) })

        // test put and get
        assertEquals(true, cache.edit { put(key1, model1) })
        assertEquals(true, cache.edit { put(key2, model2) })
        assertEquals(true, cache.edit { contains(key1) })
        assertEquals(true, cache.edit { contains(key2) })
        assertEquals(model1, cache.edit { get(key1) })
        assertEquals(model2, cache.edit { get(key2) })

        // test keys
        cache.edit { keys() }.also {
            assertEquals(2, it.size)
            assertEquals(true, it.contains(key1))
            assertEquals(true, it.contains(key2))
        }

        // test remove and get
        cache.edit { remove(key1) }
        cache.edit { remove(key2) }
        assertEquals(false, cache.edit { contains(key1) })
        assertEquals(false, cache.edit { contains(key2) })
        assertEquals(null, cache.edit { get(key1) })
        assertEquals(null, cache.edit { get(key2) })
        assertEquals(0, cache.edit { keys() }.size)
    }

    @Test
    fun testFlow() = runTest {
        val cache = FCacheKtx.get(TestKtxModel::class.java)
        val key = "key"
        cache.flowOf(key).test {
            assertEquals(null, awaitItem())

            cache.edit {
                put(key, TestKtxModel())
                put(key, TestKtxModel())
            }
            assertEquals(TestKtxModel(), awaitItem())

            cache.update(key) { it.copy(name = "update") }
            assertEquals("update", awaitItem()!!.name)

            cache.edit { remove(key) }
            assertEquals(null, awaitItem())
        }
    }
}

@DefaultGroupCache("TestKtxModel")
data class TestKtxModel(
    val name: String = "tom",
)