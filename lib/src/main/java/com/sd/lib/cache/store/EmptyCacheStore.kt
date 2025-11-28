package com.sd.lib.cache.store

import android.content.Context
import com.sd.lib.cache.libError
import com.sd.lib.cache.libException
import java.io.File

internal object EmptyCacheStore : CacheStore {
  override fun init(context: Context, directory: File) {
    libError("EmptyCacheStore.init()")
  }

  override fun putCache(key: String, value: ByteArray) {
    libException("EmptyCacheStore.putCache()")
  }

  override fun getCache(key: String): ByteArray? {
    libException("EmptyCacheStore.getCache()")
  }

  override fun removeCache(key: String) {
    libException("EmptyCacheStore.removeCache()")
  }

  override fun containsCache(key: String): Boolean {
    libException("EmptyCacheStore.containsCache()")
  }

  override fun keys(): List<String> {
    libException("EmptyCacheStore.keys()")
  }

  override fun close() {
    libError("EmptyCacheStore.close()")
  }

  override fun setCacheChangeCallback(callback: CacheStore.CacheChangeCallback) = Unit
}