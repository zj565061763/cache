package com.sd.lib.cache

open class CacheException(
  message: String = "",
  cause: Throwable? = null,
) : Exception(message, cause)

internal fun libNotifyException(error: Throwable) {
  CacheConfig.get().exceptionHandler.onException(error)
}

internal fun libError(message: String): Nothing {
  throw CacheError(message)
}

internal class LibExceptionHandler(
  private val handler: CacheConfig.ExceptionHandler?,
) : CacheConfig.ExceptionHandler {
  override fun onException(error: Throwable) {
    if (error is CacheError) throw error
    handler?.onException(error)
  }
}

/** 缓存错误，此异常不会被捕获 */
private class CacheError(message: String) : CacheException(message)