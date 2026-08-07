package com.sd.demo.cache

import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import com.sd.lib.cache.CacheEntity
import com.sd.lib.cache.FCacheKtx
import com.sd.lib.cache.asSingleCacheKtx
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
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

  /** 传入coroutineScope时启用内存缓存，[com.sd.lib.cache.SingleCacheKtx.flow]返回热流 */
  @Test
  fun testMemoryCacheFlow() = runBlocking {
    val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    try {
      val cache = FCacheKtx.get(TestSingleMemoryModel::class.java)
        .asSingleCacheKtx(coroutineScope = scope) { TestSingleMemoryModel() }

      assertEquals(true, cache.update { null })
      assertEquals(TestSingleMemoryModel(), withTimeout(TEST_TIMEOUT) { cache.flow().first() })

      assertEquals(true, cache.update { it.copy(name = "memory") })
      // 内存缓存，update之后立即就能读到新值
      assertEquals(
        TestSingleMemoryModel(name = "memory"),
        withTimeout(TEST_TIMEOUT) { cache.flow().first() },
      )
    } finally {
      scope.cancel()
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
