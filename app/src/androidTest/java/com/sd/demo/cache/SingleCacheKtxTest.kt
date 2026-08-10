package com.sd.demo.cache

import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import com.sd.lib.cache.CacheEntity
import com.sd.lib.cache.get
import com.sd.lib.cache.singleCacheKtx
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CopyOnWriteArrayList

@RunWith(AndroidJUnit4::class)
class SingleCacheKtxTest {

  @Test
  fun testUpdateAndFlow() = runBlocking {
    val cache = singleCacheKtx<TestSingleModel> { TestSingleModel() }

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
    val cache = singleCacheKtx<TestSingleRemoveModel> { TestSingleRemoveModel() }

    assertEquals(true, cache.update { null })

    cache.flow().test(timeout = TEST_TIMEOUT) {
      assertEquals(TestSingleRemoveModel(), awaitItem())

      assertEquals(true, cache.update { it.copy(name = "update") })
      assertEquals(TestSingleRemoveModel(name = "update"), awaitItem())

      assertEquals(true, cache.update { null })
      assertEquals(TestSingleRemoveModel(), awaitItem())
    }
  }

  /** get()用flow().first()获取当前值，memoryCache=false时走磁盘读取 */
  @Test
  fun testGet() = runBlocking {
    val cache = singleCacheKtx<TestSingleGetModel> { TestSingleGetModel() }

    // 无缓存时返回默认值
    assertEquals(true, cache.update { null })
    assertEquals(TestSingleGetModel(), cache.get())

    // 写入后立即可读
    assertEquals(true, cache.update { it.copy(name = "hello") })
    assertEquals(TestSingleGetModel(name = "hello"), cache.get())

    // 删除后还原为默认值
    assertEquals(true, cache.update { null })
    assertEquals(TestSingleGetModel(), cache.get())
  }

  /** get()在memoryCache=true时直接从热流的replay缓存中取值，不触发磁盘读取 */
  @Test
  fun testGetWithMemoryCache() = runBlocking {
    val cache = singleCacheKtx<TestSingleGetMemoryModel>(memoryCache = true) { TestSingleGetMemoryModel() }

    assertEquals(true, cache.update { null })
    assertEquals(TestSingleGetMemoryModel(), withTimeout(TEST_TIMEOUT) { cache.get() })

    assertEquals(true, cache.update { it.copy(name = "memory") })
    // 内存缓存，update后立即可通过get()读到新值
    assertEquals(TestSingleGetMemoryModel(name = "memory"), withTimeout(TEST_TIMEOUT) { cache.get() })
  }

  /** memoryCache为true时启用内存缓存，[com.sd.lib.cache.SingleCacheKtx.flow]返回热流 */
  @Test
  fun testMemoryCacheFlow() = runBlocking {
    val cache = singleCacheKtx<TestSingleMemoryModel>(memoryCache = true) { TestSingleMemoryModel() }

    assertEquals(true, cache.update { null })
    assertEquals(TestSingleMemoryModel(), withTimeout(TEST_TIMEOUT) { cache.get() })

    assertEquals(true, cache.update { it.copy(name = "memory") })
    // 内存缓存，update之后立即就能读到新值
    assertEquals(
      TestSingleMemoryModel(name = "memory"),
      withTimeout(TEST_TIMEOUT) { cache.get() },
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
    // 新API不支持自定义key，使用DEFAULT_SINGLE_CACHE_KEY
    val cache = singleCacheKtx<TestSingleExternalModel>(memoryCache = true) { TestSingleExternalModel() }

    val before = TestSingleExternalModel(name = "before")
    assertEquals(true, cache.update { before })

    cache.flow().test(timeout = TEST_TIMEOUT) {
      assertEquals(before, awaitItemUntil(before))

      val external = TestSingleExternalModel(name = "external")
      writeCacheFileDirectly(
        id = EXTERNAL_MODEL_ID,
        key = SINGLE_CACHE_KEY,
        json = """{"name":"${external.name}"}""",
      )
      assertEquals(external, awaitItemUntil(external))
    }
  }

  /**
   * 内存缓存有两个生产者：[com.sd.lib.cache.SingleCacheKtx.update]直接发射刚写入的值，
   * 以及监听到缓存变化后重新读盘再发射。
   * 如果读出的值要跨过缓冲才发射，一个"在途"的旧值就可能排在新值之后到达，
   * 内存里的值就会短暂地退回旧值。
   */
  @Test
  fun testMemoryCacheNoStaleEmission() = runBlocking {
    val cache = singleCacheKtx<TestSingleJitterModel>(memoryCache = true) { TestSingleJitterModel() }

    val count = 300
    assertEquals(true, cache.update { TestSingleJitterModel(seq = 0) })

    // 不能靠收集流来判断：内存缓存是replay=1 + DROP_OLDEST，抖动的两次发射间隔极短，
    // 收集者大概率只看到后一个，回退在流上看不出来。
    // 这里紧凑地采样replay缓存本身——被写坏的就是它。
    val violations = CopyOnWriteArrayList<String>()
    val sampler = launch(Dispatchers.Default) {
      var max = 0
      while (isActive) {
        val seq = cache.get().seq
        if (seq < max) violations.add("$max -> $seq")
        if (seq > max) max = seq
      }
    }
    try {
      repeat(count) { index ->
        assertEquals(true, cache.update { TestSingleJitterModel(seq = index + 1) })
      }
    } finally {
      sampler.cancel()
    }

    // update的序号严格递增，采样到的值不允许回退
    assertEquals(emptyList<String>(), violations.toList())
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

/** 与[com.sd.lib.cache.SingleCacheKtx]内部的DEFAULT_SINGLE_CACHE_KEY保持一致 */
const val SINGLE_CACHE_KEY = "com.sd.lib.cache.key.singlecache"

@CacheEntity(EXTERNAL_MODEL_ID)
data class TestSingleExternalModel(
  val name: String = "tom",
)

@CacheEntity("TestSingleJitterModel")
data class TestSingleJitterModel(
  val seq: Int = 0,
)

@CacheEntity("TestSingleGetModel")
data class TestSingleGetModel(
  val name: String = "tom",
)

@CacheEntity("TestSingleGetMemoryModel")
data class TestSingleGetMemoryModel(
  val name: String = "tom",
)
