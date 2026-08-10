package com.sd.demo.cache

import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import com.sd.lib.cache.CacheEntity
import com.sd.lib.cache.FCache
import com.sd.lib.cache.get
import com.sd.lib.cache.keys
import com.sd.lib.cache.put
import com.sd.lib.cache.remove
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.time.Duration.Companion.seconds

/**
 * 缓存目录被删除之后，[android.os.FileObserver]的监听会失效，
 * 在同一路径重建的目录不会被自动监听，必须重新监听，否则Flow再也收不到任何缓存变化。
 */
@RunWith(AndroidJUnit4::class)
class FileCacheStoreRecoveryTest {

  private val _cache = FCache.getKtx(TestRecoveryModel::class.java)

  /** 缓存目录被删除后，通过写操作恢复监听 */
  @Test
  fun testRecoverOnPut() = runBlocking {
    val key = "testRecoverOnPut"
    _cache.flowOf(key).test(timeout = TEST_TIMEOUT) {
      _cache.remove(key)
      awaitItemUntil(null)

      val model1 = TestRecoveryModel(name = "value1")
      assertEquals(true, _cache.put(key, model1))
      assertEquals(model1, awaitItem())

      // 确认删掉的确实是这个缓存正在使用的目录
      assertEquals(true, cacheStoreDirectory(RECOVERY_MODEL_ID).isDirectory)
      deleteCacheDirectory()

      val model2 = TestRecoveryModel(name = "value2")
      assertEquals(true, _cache.put(key, model2))
      // 修复前：目录被重建但监听没有重新注册，这里会一直等到超时
      assertEquals(model2, awaitItemUntil(model2))
    }
  }

  /** 缓存目录被删除后，通过[com.sd.lib.cache.Cache.get]恢复监听 */
  @Test
  fun testRecoverOnGet() = runBlocking {
    testRecoverByRead(key = "testRecoverOnGet") {
      assertEquals(null, _cache.get(it))
    }
  }

  /** 缓存目录被删除后，通过[com.sd.lib.cache.Cache.keys]恢复监听 */
  @Test
  fun testRecoverOnKeys() = runBlocking {
    testRecoverByRead(key = "testRecoverOnKeys") {
      assertEquals(emptyList<String>(), _cache.keys())
    }
  }

  /** 缓存目录被删除后，通过[com.sd.lib.cache.Cache.remove]恢复监听 */
  @Test
  fun testRecoverOnRemove() = runBlocking {
    testRecoverByRead(key = "testRecoverOnRemove") {
      assertEquals(true, _cache.remove(it))
    }
  }

  /**
   * 目录被外部删除后又在同一路径重建，此时目录本身是存在的，
   * [checkDirectoryExist]那条"目录不存在"的路径不会被触发，
   * 只能靠[android.os.FileObserver]的DELETE_SELF事件察觉监听已失效。
   *
   * 注意：整个重建过程中不能有其他缓存操作在跑，否则它们会先一步把目录重建出来，
   * 变成走"目录不存在"那条路径，这个用例就失去意义了。所以这里先不收集Flow。
   */
  @Test
  fun testRecoverAfterExternalRecreate() = runBlocking {
    val key = "testRecoverAfterExternalRecreate"
    val model1 = TestRecoveryModel(name = "value1")
    assertEquals(true, _cache.put(key, model1))

    recreateCacheStoreDirectory(RECOVERY_MODEL_ID)
    // 监听失效是靠DELETE_SELF事件感知的，事件的投递是异步的，这里要等它到达。
    // 也就是说恢复是最终一致的：事件到达之前的那次操作仍然可能丢失通知。
    delay(1.seconds)

    _cache.flowOf(key).test(timeout = TEST_TIMEOUT) {
      // 目录被重建过，缓存文件已经没了
      assertEquals(null, awaitItem())

      // 绕过CacheStore直接写文件，新值只可能通过监听被感知到
      val model2 = TestRecoveryModel(name = "value2")
      writeCacheFileDirectly(
        id = RECOVERY_MODEL_ID,
        key = key,
        json = """{"name":"${model2.name}"}""",
      )
      // 修复前：目录还在，监听却指向已被删除的旧目录，这里会一直等到超时
      assertEquals(model2, awaitItem())
    }
  }

  /**
   * 缓存目录被整体移走（MOVE_SELF），里面的文件跟着目录一起走了，并没有被逐个删除，
   * 所以不会有任何per-file DELETE事件。此时只能靠
   * [com.sd.lib.cache.store.CacheStore.CacheChangeCallback.onCleared]通知订阅者重新读取，
   * 否则flow会一直停在目录移走前的旧值上。
   *
   * 用「移走整个目录」而不是「删除整个目录」是有意为之：删除会先逐个触发per-file DELETE，
   * 那条路径已经被[onRemove]覆盖，无法单独验证[onCleared]。
   */
  @Test
  fun testDirectoryMovedOutNotified() = runBlocking {
    val key = "testDirectoryMovedOutNotified"
    _cache.flowOf(key).test(timeout = TEST_TIMEOUT) {
      _cache.remove(key)
      awaitItemUntil(null)

      val model = TestRecoveryModel(name = "value")
      assertEquals(true, _cache.put(key, model))
      assertEquals(model, awaitItem())

      // 把整个缓存目录移走，触发MOVE_SELF；文件跟着走了，不会有per-file事件
      val dir = cacheStoreDirectory(RECOVERY_MODEL_ID)
      val dest = cacheRootDirectory().resolve("movedDir_${System.currentTimeMillis()}")
      assertEquals(true, dir.renameTo(dest))
      try {
        // 修复前：MOVE_SELF只置_watchValid=false就返回，订阅者收不到任何通知，
        // 会一直停在model上直到超时
        assertEquals(null, awaitItem())
      } finally {
        dest.deleteRecursively()
      }
    }
  }

  /** 缓存目录被删除后，各个读写操作的返回值语义不变 */
  @Test
  fun testReadWriteAfterDelete() = runBlocking {
    val key = "testReadWriteAfterDelete"
    val model = TestRecoveryModel(name = "value")

    assertEquals(true, _cache.put(key, model))
    assertEquals(model, _cache.get(key))

    deleteCacheDirectory()

    assertEquals(null, _cache.get(key))
    assertEquals(emptyList<String>(), _cache.keys())
    // 缓存不存在时remove返回true
    assertEquals(true, _cache.remove(key))

    assertEquals(true, _cache.put(key, model))
    assertEquals(model, _cache.get(key))
    assertEquals(listOf(key), _cache.keys())
    assertEquals(true, _cache.remove(key))
    assertEquals(null, _cache.get(key))
  }

  /**
   * 删除缓存目录后先执行[read]（读操作应当恢复监听），
   * 然后绕过[com.sd.lib.cache.store.CacheStore]直接写文件，
   * 这样新值只可能通过[android.os.FileObserver]的监听被感知到。
   */
  private suspend fun testRecoverByRead(
    key: String,
    read: suspend (String) -> Unit,
  ) {
    _cache.flowOf(key).test(timeout = TEST_TIMEOUT) {
      _cache.remove(key)
      awaitItemUntil(null)

      val model1 = TestRecoveryModel(name = "value1")
      assertEquals(true, _cache.put(key, model1))
      assertEquals(model1, awaitItem())

      assertEquals(true, cacheStoreDirectory(RECOVERY_MODEL_ID).isDirectory)
      deleteCacheDirectory()

      // 读操作恢复监听，同时把目录重新创建出来
      read(key)
      assertEquals(true, cacheStoreDirectory(RECOVERY_MODEL_ID).isDirectory)

      val model2 = TestRecoveryModel(name = "value2")
      writeCacheFileDirectly(
        id = RECOVERY_MODEL_ID,
        key = key,
        json = """{"name":"${model2.name}"}""",
      )
      // 修复前：读操作没有恢复监听，外部写入的值收不到，这里会一直等到超时
      assertEquals(model2, awaitItemUntil(model2))
    }
  }
}

const val RECOVERY_MODEL_ID = "TestRecoveryModel"

@CacheEntity(RECOVERY_MODEL_ID)
data class TestRecoveryModel(
  val name: String = "tom",
)
