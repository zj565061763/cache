package com.sd.lib.cache

open class CacheException(
  message: String = "",
  cause: Throwable? = null,
) : Exception(message, cause)

internal fun libException(
  message: String,
  cause: Throwable? = null,
): Nothing {
  throw CacheException(message, cause)
}

internal fun libError(message: String): Nothing {
  throw CacheError(message)
}

internal inline fun <R> libRunCatching(block: () -> R): Result<R> {
  return runCatching(block)
    .onFailure { CacheConfig.get().exceptionHandler.onException(it) }
}

internal class LibExceptionHandler(
  private val handler: CacheConfig.ExceptionHandler?,
) : CacheConfig.ExceptionHandler {
  override fun onException(error: Throwable) {
    if (error is CacheError) throw error
    if (error is Error) throw error
    handler?.onException(error)
  }
}

private class CacheError(message: String) : CacheException(message)