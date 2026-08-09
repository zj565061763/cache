package com.sd.demo.cache

import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import com.sd.lib.cache.CacheEntity
import com.sd.lib.cache.CacheLockLevel
import com.sd.lib.cache.FCache
import com.sd.lib.cache.get
import com.sd.lib.cache.put
import com.sd.lib.cache.remove
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.time.Duration.Companion.seconds

@RunWith(AndroidJUnit4::class)
class CacheKtxTest {
  @Test
  fun testCacheInstance() {
    val cache1 = FCache.getKtx(TestKtxModel::class.java)
    val cache2 = FCache.getKtx(TestKtxModel::class.java)
    assertEquals(true, cache1 === cache2)
  }

  @Test
  fun test() = runBlocking {
    val cache = FCache.getKtx(TestKtxModel::class.java)

    val key1 = "key1"
    val key2 = "key2"
    val model1 = TestKtxModel(name = "name1")
    val model2 = TestKtxModel(name = "name2")

    // test default value
    assertEquals(null, cache.edit { get(key1) })
    assertEquals(null, cache.edit { get(key2) })

    // test put and get
    assertEquals(true, cache.edit { put(key1, model1) })
    assertEquals(true, cache.edit { put(key2, model2) })
    assertEquals(model1, cache.edit { get(key1) })
    assertEquals(model2, cache.edit { get(key2) })

    // test keys
    cache.edit { keys() }.also {
      println(it)
      assertEquals(2, it.size)
      assertEquals(true, it.contains(key1))
      assertEquals(true, it.contains(key2))
    }

    // test remove and get
    cache.edit { remove(key1) }
    cache.edit { remove(key2) }
    assertEquals(null, cache.edit { get(key1) })
    assertEquals(null, cache.edit { get(key2) })
    assertEquals(0, cache.edit { keys() }.size)
  }

  @Test
  fun testFlow() = runBlocking {
    val cache = FCache.getKtx(TestKtxModel::class.java)
    val key = "key"
    cache.flowOf(key).test(timeout = TEST_TIMEOUT) {
      assertEquals(null, awaitItem())

      cache.edit {
        put(key, TestKtxModel())
        put(key, TestKtxModel())
      }
      assertEquals(TestKtxModel(), awaitItem())

      cache.edit {
        val model = checkNotNull(get(key))
        put(key, model.copy(name = "update"))
      }
      assertEquals("update", awaitItem()!!.name)

      cache.edit { remove(key) }
      assertEquals(null, awaitItem())
    }
  }

  /**
   * [com.sd.lib.cache.CacheKtx.flowOf]必须先注册回调再读取初始值，
   * 否则在这两步之间发生的写入既不会被回调感知，也不会体现在初始值里，收集者会一直停在旧值上。
   *
   * 注意：这个竞态没办法确定性地复现，这里靠多次循环把窗口撞开，它是回归保护而不是正确性证明。
   */
  @Test
  fun testFlowInitialValueNotStale() = runBlocking {
    val cache = FCache.getKtx(TestKtxRaceModel::class.java)
    val key = "testFlowInitialValueNotStale"
    // 用一个较大的旧值让"读初始值"这一步足够慢，
    // 并发的put会先卡在缓存锁上，等读完成后立刻写入，从而落在"读初始值"与"注册回调"之间
    val oldValue = TestKtxRaceModel(name = "x".repeat(200_000))

    repeat(50) { index ->
      assertEquals(true, cache.put(key, oldValue))
      val expect = TestKtxRaceModel(name = "value$index")
      coroutineScope {
        val deferred = async(Dispatchers.Default) {
          cache.flowOf(key).first { it == expect }
        }
        launch(Dispatchers.IO) { cache.put(key, expect) }
        // 修复前：写入落在窗口里时回调被丢弃，且初始值是旧的，这里会一直等到超时
        assertEquals(expect, withTimeout(10.seconds) { deferred.await() })
      }
    }
  }

  /** 去掉runBlocking之后，[com.sd.lib.cache.CacheKtx.edit]仍然要正确串行化 */
  @Test
  fun testEditSerialization() = runBlocking {
    val cache = FCache.getKtx(TestKtxCounterModel::class.java)
    val key = "testEditSerialization"
    cache.remove(key)

    val count = 200
    coroutineScope {
      repeat(count) {
        launch(Dispatchers.Default) {
          cache.edit {
            val old = get(key)?.count ?: 0
            put(key, TestKtxCounterModel(count = old + 1))
          }
        }
      }
    }
    assertEquals(count, cache.get(key)?.count)
  }

  /**
   * [com.sd.lib.cache.CacheLockLevel.CurrentProcess]下所有缓存共用一把锁，
   * 并发交叉[com.sd.lib.cache.CacheKtx.edit]不能死锁。
   */
  @Test
  fun testEditNoDeadlockAcrossCaches() = runBlocking {
    val cacheA = FCache.getKtx(TestKtxProcessLockModelA::class.java)
    val cacheB = FCache.getKtx(TestKtxProcessLockModelB::class.java)
    val key = "testEditNoDeadlockAcrossCaches"

    withTimeout(30.seconds) {
      coroutineScope {
        repeat(50) { index ->
          launch(Dispatchers.Default) {
            cacheA.edit { put(key, TestKtxProcessLockModelA(name = "a$index")) }
          }
          launch(Dispatchers.Default) {
            cacheB.edit { put(key, TestKtxProcessLockModelB(name = "b$index")) }
          }
        }
      }
    }

    assertEquals(true, cacheA.get(key) != null)
    assertEquals(true, cacheB.get(key) != null)
  }

  /**
   * 外部就地写入缓存文件（不经过临时文件重命名）走的是CLOSE_WRITE事件。
   * 库自己的写入永远是「临时文件+重命名」，产生的是MOVED_TO，所以这一位只有本用例覆盖，
   * 掩码里漏掉CLOSE_WRITE的话没有别的用例会失败。
   */
  @Test
  fun testInPlaceWriteNotified() = runBlocking {
    val cache = FCache.getKtx(TestKtxInPlaceModel::class.java)
    val key = "testInPlaceWriteNotified"

    cache.flowOf(key).test(timeout = TEST_TIMEOUT) {
      cache.remove(key)
      awaitItemUntil(null)

      val model1 = TestKtxInPlaceModel(name = "value1")
      assertEquals(true, cache.put(key, model1))
      assertEquals(model1, awaitItemUntil(model1))

      // 直接就地写文件，不重命名
      val model2 = TestKtxInPlaceModel(name = "inPlace")
      cacheFileOf(IN_PLACE_MODEL_ID, key).writeBytes("""{"name":"${model2.name}"}""".toByteArray())
      assertEquals(model2, awaitItemUntil(model2))
    }
  }

  /** 缓存文件被移出监听目录，对缓存来说等同于被删除 */
  @Test
  fun testMovedOutTreatedAsRemoved() = runBlocking {
    val cache = FCache.getKtx(TestKtxMovedOutModel::class.java)
    val key = "testMovedOutTreatedAsRemoved"

    cache.flowOf(key).test(timeout = TEST_TIMEOUT) {
      cache.remove(key)
      awaitItemUntil(null)

      val model = TestKtxMovedOutModel(name = "value")
      assertEquals(true, cache.put(key, model))
      assertEquals(model, awaitItemUntil(model))

      // 移到缓存根目录下，不在被监听的store目录里，才算真正移出
      val dest = cacheRootDirectory().resolve("movedOut.bak")
      assertEquals(true, cacheFileOf(MOVED_OUT_MODEL_ID, key).renameTo(dest))
      try {
        assertEquals(null, awaitItemUntil(null))
      } finally {
        dest.delete()
      }
    }
  }
}

@CacheEntity("TestKtxModel")
data class TestKtxModel(
  val name: String = "tom",
)

@CacheEntity("TestKtxRaceModel")
data class TestKtxRaceModel(
  val name: String = "tom",
)

@CacheEntity("TestKtxCounterModel")
data class TestKtxCounterModel(
  val count: Int = 0,
)

@CacheEntity(id = "TestKtxProcessLockModelA", lockLevel = CacheLockLevel.CurrentProcess)
data class TestKtxProcessLockModelA(
  val name: String = "tom",
)

@CacheEntity(id = "TestKtxProcessLockModelB", lockLevel = CacheLockLevel.CurrentProcess)
data class TestKtxProcessLockModelB(
  val name: String = "tom",
)

const val IN_PLACE_MODEL_ID = "TestKtxInPlaceModel"

@CacheEntity(IN_PLACE_MODEL_ID)
data class TestKtxInPlaceModel(
  val name: String = "tom",
)

const val MOVED_OUT_MODEL_ID = "TestKtxMovedOutModel"

@CacheEntity(MOVED_OUT_MODEL_ID)
data class TestKtxMovedOutModel(
  val name: String = "tom",
)