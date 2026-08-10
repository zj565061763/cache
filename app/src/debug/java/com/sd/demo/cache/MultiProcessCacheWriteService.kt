package com.sd.demo.cache

import android.app.Service
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.Message
import android.os.Messenger
import android.os.SystemClock
import com.sd.lib.cache.CacheEntity
import com.sd.lib.cache.FCache

/** 供instrumentation测试在独立进程中发起缓存写入。 */
class MultiProcessCacheWriteService : Service() {
  private val _messenger = Messenger(Handler(Looper.getMainLooper()) { message ->
    when (message.what) {
      COMMAND_WRITE -> handleWrite(message)
      COMMAND_INITIALIZE -> handleInitialize(message)
      else -> false
    }
  })

  override fun onBind(intent: Intent?): IBinder = _messenger.binder

  companion object {
    const val COMMAND_WRITE = 1
    const val RESPONSE_WRITE = 2
    const val COMMAND_INITIALIZE = 3
    const val RESPONSE_INITIALIZE = 4
    const val EXTRA_KEY = "key"
    const val EXTRA_OWNER = "owner"
    const val EXTRA_PAYLOAD_SIZE = "payload_size"
    const val EXTRA_START_AT = "start_at"

    fun newWriteMessage(
      key: String,
      owner: String,
      payloadSize: Int,
      startAt: Long,
      replyTo: Messenger,
    ): Message {
      return Message.obtain(null, COMMAND_WRITE).apply {
        data = Bundle().apply {
          putString(EXTRA_KEY, key)
          putString(EXTRA_OWNER, owner)
          putInt(EXTRA_PAYLOAD_SIZE, payloadSize)
          putLong(EXTRA_START_AT, startAt)
        }
        this.replyTo = replyTo
      }
    }

    fun newInitializeMessage(replyTo: Messenger): Message {
      return Message.obtain(null, COMMAND_INITIALIZE).apply {
        this.replyTo = replyTo
      }
    }
  }

  private fun handleWrite(message: Message): Boolean {
    val replyTo = message.replyTo ?: return false
    val key = message.data.getString(EXTRA_KEY) ?: return false
    val owner = message.data.getString(EXTRA_OWNER) ?: return false
    val payloadSize = message.data.getInt(EXTRA_PAYLOAD_SIZE)
    val startAt = message.data.getLong(EXTRA_START_AT)

    Thread({
      val cache = FCache.get(MultiProcessWriteModel::class.java)
      val model = newMultiProcessWriteModel(owner, payloadSize)
      waitUntil(startAt)
      val result = cache.put(key, model)
      sendResult(replyTo, RESPONSE_WRITE, result)
    }, "cache-test-writer").start()
    return true
  }

  private fun handleInitialize(message: Message): Boolean {
    val replyTo = message.replyTo ?: return false
    Thread({
      val cache = FCache.get(MultiProcessTempCleanupModel::class.java)
      val result = cache.put(INITIALIZE_KEY, MultiProcessTempCleanupModel())
      cache.remove(INITIALIZE_KEY)
      sendResult(replyTo, RESPONSE_INITIALIZE, result)
    }, "cache-test-initializer").start()
    return true
  }
}

@CacheEntity("MultiProcessWriteModel")
data class MultiProcessWriteModel(
  val owner: String = "",
  val payload: String = "",
)

const val MULTI_PROCESS_TEMP_CLEANUP_MODEL_ID = "MultiProcessTempCleanupModel"

@CacheEntity(MULTI_PROCESS_TEMP_CLEANUP_MODEL_ID)
data class MultiProcessTempCleanupModel(
  val value: String = "value",
)

fun newMultiProcessWriteModel(owner: String, payloadSize: Int): MultiProcessWriteModel {
  return MultiProcessWriteModel(
    owner = owner,
    payload = owner.first().toString().repeat(payloadSize),
  )
}

private fun waitUntil(startAt: Long) {
  while (true) {
    val remaining = startAt - SystemClock.elapsedRealtime()
    if (remaining <= 0) return
    Thread.sleep(remaining.coerceAtMost(10))
  }
}

private fun sendResult(replyTo: Messenger, what: Int, result: Boolean) {
  val response = Message.obtain(null, what).apply {
    arg1 = if (result) 1 else 0
  }
  runCatching { replyTo.send(response) }
}

private const val INITIALIZE_KEY = "initialize"
