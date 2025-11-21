package com.sd.lib.cache.store

import android.content.Context
import com.sd.lib.cache.libException
import java.io.File

internal object EmptyActiveGroupCacheStore : CacheStore {
  override fun init(context: Context, directory: File, group: String, id: String) {
    libException("Empty active group CacheStore.init()")
  }

  override fun putCache(key: String, value: ByteArray) {
    libException("Empty active group CacheStore.putCache()")
  }

  override fun getCache(key: String): ByteArray? {
    libException("Empty active group CacheStore.getCache()")
  }

  override fun removeCache(key: String) {
    libException("Empty active group CacheStore.removeCache()")
  }

  override fun containsCache(key: String): Boolean {
    libException("Empty active group CacheStore.containsCache()")
  }

  override fun keys(): List<String> {
    libException("Empty active group CacheStore.keys()")
  }

  override fun close() {
    libException("Empty active group CacheStore.close()")
  }
}