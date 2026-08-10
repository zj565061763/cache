package com.sd.demo.cache

import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import com.sd.lib.cache.CacheEntity
import com.sd.lib.cache.get
import com.sd.lib.cache.singleCacheKtx
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.time.Duration.Companion.seconds

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

  /** memoryCache为true时，flow()返回热流，订阅后能持续收到update的变化 */
  @Test
  fun testMemoryCacheFlow() = runBlocking {
    val cache = singleCacheKtx<TestSingleMemoryModel>(memoryCache = true) { TestSingleMemoryModel() }

    assertEquals(true, cache.update { null })

    cache.flow().test(timeout = TEST_TIMEOUT) {
      assertEquals(TestSingleMemoryModel(), awaitItem())

      assertEquals(true, cache.update { it.copy(name = "first") })
      assertEquals(TestSingleMemoryModel(name = "first"), awaitItem())

      assertEquals(true, cache.update { it.copy(name = "second") })
      assertEquals(TestSingleMemoryModel(name = "second"), awaitItem())
    }
  }

  /**
   * 竞态场景：构造后 GlobalScope 协程尚未完成首次填充时，立即 update 写入新值，
   * 同时开始订阅。订阅者应收到 update 写入的新值，不能停在默认缓存或旧值上。
   *
   * memoryCache按Class缓存单例，所以同一类型只有一次真正的冷启动机会。
   */
  @Test
  fun testMemoryCacheFirstUpdateRaceWithSubscription() = runBlocking {
    val cache = singleCacheKtx<TestSingleRaceModel>(memoryCache = true) { TestSingleRaceModel() }
    val expected = TestSingleRaceModel(name = "race")

    coroutineScope {
      // 先让订阅者运行到等待初始化的位置，再立即写入。
      val deferred = async(Dispatchers.Default, start = CoroutineStart.UNDISPATCHED) {
        cache.flow().first { it == expected }
      }
      launch(Dispatchers.IO) { cache.update { expected } }
      assertEquals(expected, withTimeout(10.seconds) { deferred.await() })
    }
  }

  /**
   * 冷启动场景：memoryCache=true，磁盘已有缓存，热流尚未初始化（GlobalScope 协程未运行），
   * 此时调用 get() 应等待初始化完成。
   */
  @Test
  fun testMemoryCacheGetOnColdStart() = runBlocking {
    // 先用非内存缓存写入真实值（不触发内存缓存单例初始化）
    val diskCache = singleCacheKtx<TestSingleColdStartModel> { TestSingleColdStartModel() }
    val expected = TestSingleColdStartModel(name = "real")
    assertEquals(true, diskCache.update { expected })

    // 内存缓存单例：_hotFlow 可能为空，get() 应返回磁盘真实值而非默认缓存
    val cache = singleCacheKtx<TestSingleColdStartModel>(memoryCache = true) { TestSingleColdStartModel() }
    assertEquals(expected, withTimeout(TEST_TIMEOUT) { cache.get() })
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
      sampler.cancelAndJoin()
    }

    // update的序号严格递增，采样到的值不允许回退
    assertEquals(emptyList<String>(), violations.toList())
  }

  /**
   * memoryCache=true 时进程共享同一个实例，[getDefault]只在首次创建时调用一次，
   * 之后传入的[getDefault]被忽略，回退值保持为首次调用的结果。
   */
  @Test
  fun testGetDefaultCalledOnceWithMemoryCache() = runBlocking {
    var callCount = 0
    val cache1 = singleCacheKtx<TestSingleDefaultMemoryModel>(memoryCache = true) {
      callCount++
      TestSingleDefaultMemoryModel(name = "first")
    }
    // 第二次调用传入不同的默认值，应返回同一实例且不再调用 getDefault
    val cache2 = singleCacheKtx<TestSingleDefaultMemoryModel>(memoryCache = true) {
      callCount++
      TestSingleDefaultMemoryModel(name = "second")
    }
    assertEquals(true, cache1 === cache2)
    assertEquals(1, callCount)

    // 删除缓存后，回退值应是首次调用时的默认值（"first"），后续传入的 "second" 被忽略
    assertEquals(true, cache1.update { null })
    assertEquals(TestSingleDefaultMemoryModel(name = "first"), withTimeout(TEST_TIMEOUT) { cache1.get() })
  }

  /** memoryCache=false 时每次调用都返回新实例，且每次都会重新执行[getDefault] */
  @Test
  fun testGetDefaultCalledEachCallWithoutMemoryCache() = runBlocking {
    var callCount = 0
    val cache1 = singleCacheKtx<TestSingleDefaultDiskModel> {
      callCount++
      TestSingleDefaultDiskModel(name = "v$callCount")
    }
    val cache2 = singleCacheKtx<TestSingleDefaultDiskModel> {
      callCount++
      TestSingleDefaultDiskModel(name = "v$callCount")
    }
    assertEquals(false, cache1 === cache2)
    assertEquals(2, callCount)

    // 各实例使用各自的默认值
    assertEquals(true, cache1.update { null })
    assertEquals(TestSingleDefaultDiskModel(name = "v1"), cache1.get())
    assertEquals(TestSingleDefaultDiskModel(name = "v2"), cache2.get())
  }

  /**
   * memoryCache=true 时缓存目录被整体删除：per-file DELETE 事件和 DELETE_SELF 的 onCleared
   * 都会驱动收集协程重新读盘，读不到文件时热流发射 null，订阅者应收到默认值，之后 update 恢复。
   */
  @Test
  fun testMemoryCacheRecoverAfterDirectoryDelete() = runBlocking {
    val cache = singleCacheKtx<TestSingleMemoryDeleteModel>(memoryCache = true) { TestSingleMemoryDeleteModel() }

    assertEquals(true, cache.update { it.copy(name = "value") })

    cache.flow().test(timeout = TEST_TIMEOUT) {
      assertEquals(TestSingleMemoryDeleteModel(name = "value"), awaitItemUntil(TestSingleMemoryDeleteModel(name = "value")))

      // 删除整个缓存目录
      deleteCacheDirectory()
      assertEquals(TestSingleMemoryDeleteModel(), awaitItemUntil(TestSingleMemoryDeleteModel()))

      // 重新写入恢复
      assertEquals(true, cache.update { it.copy(name = "recovered") })
      assertEquals(TestSingleMemoryDeleteModel(name = "recovered"), awaitItemUntil(TestSingleMemoryDeleteModel(name = "recovered")))
    }
  }

  /**
   * 同一 memory 热流的多个订阅者都应该收到初始状态和最终状态。
   * SharedFlow使用DROP_OLDEST，慢订阅者允许跳过中间值，不要断言完整序列必须相同。
   */
  @Test
  fun testMemoryCacheMultipleSubscribers() = runBlocking {
    val cache = singleCacheKtx<TestSingleMultiSubModel>(memoryCache = true) { TestSingleMultiSubModel() }

    val init = TestSingleMultiSubModel(name = "init")
    assertEquals(true, cache.update { init })

    val resultsA = CopyOnWriteArrayList<TestSingleMultiSubModel>()
    val resultsB = CopyOnWriteArrayList<TestSingleMultiSubModel>()

    val jobA = launch(Dispatchers.Default) { cache.flow().collect { resultsA.add(it) } }
    val jobB = launch(Dispatchers.Default) { cache.flow().collect { resultsB.add(it) } }

    try {
      // 等两个订阅者都收到初始值再开始更新，保证两边从同一个起点开始
      withTimeout(TEST_TIMEOUT) {
        while (resultsA.firstOrNull() != init || resultsB.firstOrNull() != init) {
          delay(10)
        }
      }

      assertEquals(true, cache.update { it.copy(name = "update1") })
      assertEquals(true, cache.update { it.copy(name = "update2") })

      withTimeout(TEST_TIMEOUT) {
        while (resultsA.lastOrNull()?.name != "update2" || resultsB.lastOrNull()?.name != "update2") {
          delay(10)
        }
      }

      assertEquals(init, resultsA.first())
      assertEquals(init, resultsB.first())
      assertEquals("update2", resultsA.last().name)
      assertEquals("update2", resultsB.last().name)
    } finally {
      jobA.cancelAndJoin()
      jobB.cancelAndJoin()
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

@CacheEntity("TestSingleColdStartModel")
data class TestSingleColdStartModel(
  val name: String = "tom",
)

@CacheEntity("TestSingleRaceModel")
data class TestSingleRaceModel(
  val name: String = "tom",
)

@CacheEntity("TestSingleDefaultMemoryModel")
data class TestSingleDefaultMemoryModel(
  val name: String = "tom",
)

@CacheEntity("TestSingleDefaultDiskModel")
data class TestSingleDefaultDiskModel(
  val name: String = "tom",
)

@CacheEntity("TestSingleMemoryDeleteModel")
data class TestSingleMemoryDeleteModel(
  val name: String = "tom",
)

@CacheEntity("TestSingleMultiSubModel")
data class TestSingleMultiSubModel(
  val name: String = "tom",
)
