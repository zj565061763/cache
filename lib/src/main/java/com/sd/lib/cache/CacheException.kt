package com.sd.lib.cache

open class CacheException(
    message: String = "",
    cause: Throwable? = null,
) : Exception(message, cause)

class CacheError(message: String) : CacheException(message)

internal fun libNotifyException(error: Throwable) {
    CacheConfig.get().exceptionHandler.onException(error)
}

internal fun libError(message: String): Nothing {
    throw CacheError(message)
}

internal class LibExceptionHandler(
    private val handler: Cache.ExceptionHandler?
) : Cache.ExceptionHandler {
    override fun onException(error: Throwable) {
        if (error is CacheError) throw error
        handler?.onException(error)
    }
}