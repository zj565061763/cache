package com.sd.demo.cache

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.Messenger
import android.os.SystemClock
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.sd.lib.cache.FCache
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MultiProcessCacheWriteTest {
  /** 一个进程初始化store时，不能清理其他进程遗留或正在使用的临时文件。 */
  @Test
  fun testInitializationKeepsOtherProcessTempFile() = runBlocking {
    val context = InstrumentationRegistry.getInstrumentation().targetContext
    val directory = cacheStoreDirectory(MULTI_PROCESS_TEMP_CLEANUP_MODEL_ID)
    directory.deleteRecursively()
    directory.mkdirs()
    val mainProcessTempFile = currentProcessTempFile(
      id = MULTI_PROCESS_TEMP_CLEANUP_MODEL_ID,
      marker = "active",
    )
    mainProcessTempFile.writeBytes("writing".toByteArray())

    val connection = CacheWriterServiceConnection()
    val intent = Intent(context, MultiProcessCacheWriteService::class.java)
    assertEquals(true, context.bindService(intent, connection, Context.BIND_AUTO_CREATE))
    try {
      val remote = withTimeout(TEST_TIMEOUT) { connection.awaitMessenger() }
      val remoteResult = CompletableDeferred<Boolean>()
      val replyTo = resultMessenger(
        responseWhat = MultiProcessCacheWriteService.RESPONSE_INITIALIZE,
        result = remoteResult,
      )
      remote.send(MultiProcessCacheWriteService.newInitializeMessage(replyTo))

      assertEquals(true, withTimeout(TEST_TIMEOUT) { remoteResult.await() })
      assertEquals(true, mainProcessTempFile.exists())
    } finally {
      context.unbindService(connection)
      mainProcessTempFile.delete()
      directory.deleteRecursively()
    }
  }

  /** 同一个key被两个进程并发写入时，两次写入都应成功，最终文件也必须是一个完整值。 */
  @Test
  fun testConcurrentPutFromTwoProcesses() = runBlocking {
    val context = InstrumentationRegistry.getInstrumentation().targetContext
    val connection = CacheWriterServiceConnection()
    val intent = Intent(context, MultiProcessCacheWriteService::class.java)
    assertEquals(true, context.bindService(intent, connection, Context.BIND_AUTO_CREATE))

    try {
      val remote = withTimeout(TEST_TIMEOUT) { connection.awaitMessenger() }
      val cache = FCache.get(MultiProcessWriteModel::class.java)

      repeat(REPEAT_COUNT) { index ->
        val key = "multi_process_${System.nanoTime()}_$index"
        val mainModel = newMultiProcessWriteModel(MAIN_OWNER, PAYLOAD_SIZE)
        val remoteModel = newMultiProcessWriteModel(REMOTE_OWNER, PAYLOAD_SIZE)
        val remoteResult = CompletableDeferred<Boolean>()
        val replyTo = resultMessenger(
          responseWhat = MultiProcessCacheWriteService.RESPONSE_WRITE,
          result = remoteResult,
        )
        val startAt = SystemClock.elapsedRealtime() + START_DELAY_MILLIS

        remote.send(
          MultiProcessCacheWriteService.newWriteMessage(
            key = key,
            owner = REMOTE_OWNER,
            payloadSize = PAYLOAD_SIZE,
            startAt = startAt,
            replyTo = replyTo,
          )
        )

        try {
          val mainResult = withContext(Dispatchers.IO) {
            waitUntil(startAt)
            cache.put(key, mainModel)
          }
          assertEquals("主进程第${index + 1}次写入失败", true, mainResult)
          assertEquals(
            "远端进程第${index + 1}次写入失败",
            true,
            withTimeout(TEST_TIMEOUT) { remoteResult.await() },
          )

          val actual = cache.get(key)
          assertEquals(
            "第${index + 1}次并发写入产生了不完整文件",
            true,
            actual == mainModel || actual == remoteModel,
          )
        } finally {
          cache.remove(key)
        }
      }
    } finally {
      context.unbindService(connection)
    }
  }
}

private fun resultMessenger(
  responseWhat: Int,
  result: CompletableDeferred<Boolean>,
): Messenger {
  return Messenger(Handler(Looper.getMainLooper()) { message ->
    if (message.what == responseWhat) {
      result.complete(message.arg1 == 1)
      true
    } else {
      false
    }
  })
}

private class CacheWriterServiceConnection : ServiceConnection {
  private val _messenger = CompletableDeferred<Messenger>()

  override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
    if (service != null) _messenger.complete(Messenger(service))
  }

  override fun onServiceDisconnected(name: ComponentName?) {
    if (!_messenger.isCompleted) {
      _messenger.completeExceptionally(IllegalStateException("测试服务连接已断开"))
    }
  }

  suspend fun awaitMessenger(): Messenger = _messenger.await()
}

private fun waitUntil(startAt: Long) {
  while (true) {
    val remaining = startAt - SystemClock.elapsedRealtime()
    if (remaining <= 0) return
    Thread.sleep(remaining.coerceAtMost(10))
  }
}

private const val MAIN_OWNER = "main"
private const val REMOTE_OWNER = "remote"
private const val PAYLOAD_SIZE = 2 * 1024 * 1024
private const val REPEAT_COUNT = 20
private const val START_DELAY_MILLIS = 200L
