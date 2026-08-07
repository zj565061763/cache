package com.sd.demo.cache

import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import com.sd.lib.cache.CacheEntity
import com.sd.lib.cache.FCacheKtx
import com.sd.lib.cache.asSingleCacheKtx
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SingleCacheKtxTest {

  @Test
  fun testUpdateAndFlow() = runBlocking {
    val cache = FCacheKtx.get(TestSingleModel::class.java)
      .asSingleCacheKtx { TestSingleModel() }

    // 复位为默认缓存
    assertEquals(true, cache.update { null })

    cache.flow().test(timeout = TEST_TIMEOUT) {
      assertEquals(TestSingleModel(), awaitItem())

      assertEquals(true, cache.update { it.copy(name = "update") })
      assertEquals(TestSingleModel(name = "update"), awaitItem())

      // block的入参是当前缓存
      assertEquals(true, cache.update { TestSingleModel(name = "${it.name}-again") })
      assertEquals(TestSingleModel(name = "update-again"), awaitItem())
    }
  }

  /** [com.sd.lib.cache.SingleCacheKtx.update]返回null会删除缓存，还原为默认缓存 */
  @Test
  fun testUpdateReturnNullRemoves() = runBlocking {
    val cache = FCacheKtx.get(TestSingleRemoveModel::class.java)
      .asSingleCacheKtx { TestSingleRemoveModel() }

    assertEquals(true, cache.update { null })

    cache.flow().test(timeout = TEST_TIMEOUT) {
      assertEquals(TestSingleRemoveModel(), awaitItem())

      assertEquals(true, cache.update { it.copy(name = "update") })
      assertEquals(TestSingleRemoveModel(name = "update"), awaitItem())

      assertEquals(true, cache.update { null })
      assertEquals(TestSingleRemoveModel(), awaitItem())
    }
  }

  /** memoryCache为true时启用内存缓存，[com.sd.lib.cache.SingleCacheKtx.flow]返回热流 */
  @Test
  fun testMemoryCacheFlow() = runBlocking {
    val cache = FCacheKtx.get(TestSingleMemoryModel::class.java)
      .asSingleCacheKtx(memoryCache = true) { TestSingleMemoryModel() }

    assertEquals(true, cache.update { null })
    assertEquals(TestSingleMemoryModel(), withTimeout(TEST_TIMEOUT) { cache.flow().first() })

    assertEquals(true, cache.update { it.copy(name = "memory") })
    // 内存缓存，update之后立即就能读到新值
    assertEquals(
      TestSingleMemoryModel(name = "memory"),
      withTimeout(TEST_TIMEOUT) { cache.flow().first() },
    )
  }

  /**
   * 内存缓存的热流要能收到外部写入。
   *
   * [com.sd.lib.cache.SingleCacheKtx.update]写入的值是直接tryEmit进热流的，走不到磁盘那条路径，
   * 所以这里绕过CacheStore直接写文件——这个值只能靠常驻在GlobalScope上的收集协程送进热流。
   */
  @Test
  fun testMemoryCacheReceivesExternalWrite() = runBlocking {
    val key = "testMemoryCacheReceivesExternalWrite"
    val cache = FCacheKtx.get(TestSingleExternalModel::class.java)
      .asSingleCacheKtx(key = key, memoryCache = true) { TestSingleExternalModel() }

    val before = TestSingleExternalModel(name = "before")
    assertEquals(true, cache.update { before })

    cache.flow().test(timeout = TEST_TIMEOUT) {
      assertEquals(before, awaitItemUntil(before))

      val external = TestSingleExternalModel(name = "external")
      writeCacheFileDirectly(
        id = EXTERNAL_MODEL_ID,
        key = key,
        json = """{"name":"${external.name}"}""",
      )
      assertEquals(external, awaitItemUntil(external))
    }
  }
}

@CacheEntity("TestSingleModel")
data class TestSingleModel(
  val name: String = "tom",
)

@CacheEntity("TestSingleRemoveModel")
data class TestSingleRemoveModel(
  val name: String = "tom",
)

@CacheEntity("TestSingleMemoryModel")
data class TestSingleMemoryModel(
  val name: String = "tom",
)

const val EXTERNAL_MODEL_ID = "TestSingleExternalModel"

@CacheEntity(EXTERNAL_MODEL_ID)
data class TestSingleExternalModel(
  val name: String = "tom",
)
