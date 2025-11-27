package com.sd.lib.cache.store

import android.content.Context
import android.util.Base64
import java.io.File

abstract class DirectoryCacheStore : CacheStore {
  private lateinit var _directory: File

  protected val directory: File
    get() = _directory

  final override fun init(context: Context, directory: File) {
    _directory = directory
    initImpl(context, _directory)
  }

  final override fun putCache(key: String, value: ByteArray) {
    return putCacheImpl(fileOf(key), value)
  }

  final override fun getCache(key: String): ByteArray? {
    return getCacheImpl(fileOf(key))
  }

  final override fun removeCache(key: String) {
    removeCacheImpl(fileOf(key))
  }

  final override fun containsCache(key: String): Boolean {
    return containsCacheImpl(fileOf(key))
  }

  final override fun keys(): List<String> {
    val list = _directory.list()
    if (list.isNullOrEmpty()) return emptyList()
    return list.mapNotNull { filename ->
      try {
        filename?.decodeKey()
      } catch (e: IllegalArgumentException) {
        runCatching { _directory.resolve(filename).deleteRecursively() }
        null
      }
    }
  }

  override fun close() = Unit

  private fun fileOf(key: String): File {
    return _directory.resolve(key.encodeKey())
  }

  protected open fun initImpl(context: Context, directory: File) = Unit
  protected abstract fun putCacheImpl(file: File, value: ByteArray)
  protected abstract fun getCacheImpl(file: File): ByteArray?
  protected open fun removeCacheImpl(file: File) = file.deleteRecursively()
  protected open fun containsCacheImpl(file: File): Boolean = file.isFile
}

private fun String.encodeKey(): String {
  val input = this.toByteArray()
  val flag = Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING
  return Base64.encode(input, flag).decodeToString()
}

@Throws(IllegalArgumentException::class)
private fun String.decodeKey(): String {
  val input = this.toByteArray()
  val flag = Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING
  return Base64.decode(input, flag).decodeToString()
}