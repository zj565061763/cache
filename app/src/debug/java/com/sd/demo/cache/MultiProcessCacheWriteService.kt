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
    if (message.what != COMMAND_WRITE) return@Handler false

    val replyTo = message.replyTo ?: return@Handler false
    val key = message.data.getString(EXTRA_KEY) ?: return@Handler false
    val owner = message.data.getString(EXTRA_OWNER) ?: return@Handler false
    val payloadSize = message.data.getInt(EXTRA_PAYLOAD_SIZE)
    val startAt = message.data.getLong(EXTRA_START_AT)

    Thread({
      val cache = FCache.get(MultiProcessWriteModel::class.java)
      val model = newMultiProcessWriteModel(owner, payloadSize)
      waitUntil(startAt)
      val result = cache.put(key, model)
      val response = Message.obtain(null, RESPONSE_WRITE).apply {
        arg1 = if (result) 1 else 0
      }
      runCatching { replyTo.send(response) }
    }, "cache-test-writer").start()
    true
  })

  override fun onBind(intent: Intent?): IBinder = _messenger.binder

  companion object {
    const val COMMAND_WRITE = 1
    const val RESPONSE_WRITE = 2
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
  }
}

@CacheEntity("MultiProcessWriteModel")
data class MultiProcessWriteModel(
  val owner: String = "",
  val payload: String = "",
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
