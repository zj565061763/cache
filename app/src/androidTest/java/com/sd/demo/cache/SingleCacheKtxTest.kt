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
import org.junit.Assert.assertNotSame
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread
import kotlin.coroutines.Continuation
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.intrinsics.COROUTINE_SUSPENDED
import kotlin.coroutines.intrinsics.startCoroutineUninterceptedOrReturn
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

  /** 内存缓存初始化完成后，get()直接读取replay值，不发生调度器切换 */
  @Test
  fun testMemoryCacheGetDoesNotSuspendAfterInitialized() = runBlocking {
    val cache = singleCacheKtx<TestSingleImmediateModel>(memoryCache = true) { TestSingleImmediateModel() }
    val expected = TestSingleImmediateModel(name = "memory")
    assertEquals(true, cache.update { expected })

    val block: suspend () -> TestSingleImmediateModel = { cache.get() }
    val result = block.startCoroutineUninterceptedOrReturn(
      object : Continuation<TestSingleImmediateModel> {
        override val context = EmptyCoroutineContext

        override fun resumeWith(result: Result<TestSingleImmediateModel>) = Unit
      }
    )

    assertNotSame(COROUTINE_SUSPENDED, result)
    assertEquals(expected, result)
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

  /** 仓库读取失败时无法确定旧值，update必须放弃，不能用默认值覆盖已有缓存 */
  @Test
  fun testUpdateAbortsOnReadFailure() = runBlocking {
    val cache = singleCacheKtx<TestSingleReadFailureModel> { TestSingleReadFailureModel() }
    val expected = TestSingleReadFailureModel(name = "value")
    assertEquals(true, cache.update { expected })

    val file = cacheFileOf(READ_FAILURE_MODEL_ID, SINGLE_CACHE_KEY)
    var invoked = false
    // 读取失败但重命名覆盖仍能成功，修复前会用默认值覆盖
    val result = withoutReadPermission(file) {
      cache.update {
        invoked = true
        it.copy(name = "overwrite")
      }
    }
    assertEquals(false, result)
    assertEquals(false, invoked)
    assertEquals(expected, cache.get())
  }

  /** 缓存无法解码时视为无缓存，update以默认值作为旧值并覆盖写入 */
  @Test
  fun testUpdateOverwritesUndecodableCache() = runBlocking {
    val cache = singleCacheKtx<TestSingleUndecodableModel> { TestSingleUndecodableModel() }
    // 先访问一次，确保缓存目录已创建
    assertEquals(true, cache.update { null })
    writeCacheFileDirectly(id = UNDECODABLE_MODEL_ID, key = SINGLE_CACHE_KEY, json = "not json")

    assertEquals(true, cache.update { it.copy(name = "${it.name}-update") })
    assertEquals(TestSingleUndecodableModel(name = "tom-update"), cache.get())
  }

  /** memoryCache=true时重新读盘失败，热流保留原值，不能退回默认值 */
  @Test
  fun testMemoryCacheKeepsValueOnReadFailure() = runBlocking {
    val cache = singleCacheKtx<TestSingleMemoryReadFailureModel>(memoryCache = true) { TestSingleMemoryReadFailureModel() }
    val before = TestSingleMemoryReadFailureModel(name = "before")
    assertEquals(true, cache.update { before })
    val file = cacheFileOf(MEMORY_READ_FAILURE_MODEL_ID, SINGLE_CACHE_KEY)

    cache.flow().test(timeout = TEST_TIMEOUT) {
      assertEquals(before, awaitItemUntil(before))

      withoutReadPermission(file) {
        // 就地写入触发CLOSE_WRITE，重新读盘必然失败
        file.writeBytes("""{"name":"${before.name}"}""".toByteArray())
        // 等待重新读盘完成，修复前这里会发射默认值
        delay(1000)
      }
      expectNoEvents()

      // 读取恢复后仍能收到新值
      val after = TestSingleMemoryReadFailureModel(name = "after")
      writeCacheFileDirectly(
        id = MEMORY_READ_FAILURE_MODEL_ID,
        key = SINGLE_CACHE_KEY,
        json = """{"name":"${after.name}"}""",
      )
      assertEquals(after, awaitItem())
    }
  }

  /** memoryCache=false时重新读盘失败，冷流保留原值，不能退回默认值 */
  @Test
  fun testDiskCacheKeepsValueOnReadFailure() = runBlocking {
    val cache = singleCacheKtx<TestSingleDiskReadFailureModel> { TestSingleDiskReadFailureModel() }
    val before = TestSingleDiskReadFailureModel(name = "before")
    assertEquals(true, cache.update { before })
    val file = cacheFileOf(DISK_READ_FAILURE_MODEL_ID, SINGLE_CACHE_KEY)

    cache.flow().test(timeout = TEST_TIMEOUT) {
      assertEquals(before, awaitItem())

      withoutReadPermission(file) {
        // 就地写入触发CLOSE_WRITE，重新读盘必然失败
        file.writeBytes("""{"name":"${before.name}"}""".toByteArray())
        // 等待重新读盘完成，修复前这里会发射默认值
        delay(1000)
      }
      expectNoEvents()

      val after = TestSingleDiskReadFailureModel(name = "after")
      assertEquals(true, cache.update { after })
      assertEquals(after, awaitItem())
    }
  }

  /** 某类型的getDefault阻塞时，不能阻塞其他类型创建内存单值缓存 */
  @Test
  fun testMemoryCacheCreationNotBlockedByOtherType() {
    val blockingEntered = CountDownLatch(1)
    val releaseBlocking = CountDownLatch(1)
    val otherCreated = CountDownLatch(1)

    // 使用普通线程，修复前被阻塞时测试能按时失败，不会一直等待
    thread {
      singleCacheKtx<TestSingleBlockingDefaultModel>(memoryCache = true) {
        blockingEntered.countDown()
        releaseBlocking.await(TEST_TIMEOUT.inWholeSeconds, TimeUnit.SECONDS)
        TestSingleBlockingDefaultModel()
      }
    }
    try {
      assertEquals(true, blockingEntered.await(10, TimeUnit.SECONDS))
      thread {
        singleCacheKtx<TestSingleUnblockedModel>(memoryCache = true) { TestSingleUnblockedModel() }
        otherCreated.countDown()
      }
      assertEquals(true, otherCreated.await(10, TimeUnit.SECONDS))
    } finally {
      releaseBlocking.countDown()
    }
  }

  /** getDefault抛异常时不保留失败的实例，下次调用重新创建 */
  @Test
  fun testMemoryCacheRetryAfterGetDefaultFailure() = runBlocking {
    val error = runCatching {
      singleCacheKtx<TestSingleDefaultFailureModel>(memoryCache = true) { error("getDefault failure") }
    }.exceptionOrNull()
    assertEquals("getDefault failure", error?.message)

    val cache = singleCacheKtx<TestSingleDefaultFailureModel>(memoryCache = true) {
      TestSingleDefaultFailureModel(name = "retry")
    }
    assertEquals(true, cache.update { null })
    assertEquals(TestSingleDefaultFailureModel(name = "retry"), withTimeout(TEST_TIMEOUT) { cache.get() })
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

@CacheEntity("TestSingleImmediateModel")
data class TestSingleImmediateModel(
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

const val READ_FAILURE_MODEL_ID = "TestSingleReadFailureModel"

@CacheEntity(READ_FAILURE_MODEL_ID)
data class TestSingleReadFailureModel(
  val name: String = "tom",
)

const val UNDECODABLE_MODEL_ID = "TestSingleUndecodableModel"

@CacheEntity(UNDECODABLE_MODEL_ID)
data class TestSingleUndecodableModel(
  val name: String = "tom",
)

const val MEMORY_READ_FAILURE_MODEL_ID = "TestSingleMemoryReadFailureModel"

@CacheEntity(MEMORY_READ_FAILURE_MODEL_ID)
data class TestSingleMemoryReadFailureModel(
  val name: String = "tom",
)

const val DISK_READ_FAILURE_MODEL_ID = "TestSingleDiskReadFailureModel"

@CacheEntity(DISK_READ_FAILURE_MODEL_ID)
data class TestSingleDiskReadFailureModel(
  val name: String = "tom",
)

@CacheEntity("TestSingleBlockingDefaultModel")
data class TestSingleBlockingDefaultModel(
  val name: String = "tom",
)

@CacheEntity("TestSingleUnblockedModel")
data class TestSingleUnblockedModel(
  val name: String = "tom",
)

@CacheEntity("TestSingleDefaultFailureModel")
data class TestSingleDefaultFailureModel(
  val name: String = "tom",
)

@CacheEntity("TestSingleMultiSubModel")
data class TestSingleMultiSubModel(
  val name: String = "tom",
)
