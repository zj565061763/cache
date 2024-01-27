package com.sd.lib.cache

open class CacheException(
    message: String = "",
    cause: Throwable? = null,
) : Exception(message, cause)

class CacheError(
    message: String = "",
    cause: Throwable? = null,
) : CacheException(message = message, cause = cause)

internal fun libNotifyException(error: Throwable) {
    CacheConfig.get().exceptionHandler.onException(error)
}

internal fun Cache.ExceptionHandler?.libHandler(): Cache.ExceptionHandler {
    return if (this is LibExceptionHandler) this else LibExceptionHandler(this)
}

private class LibExceptionHandler(
    private val handler: Cache.ExceptionHandler?
) : Cache.ExceptionHandler {
    override fun onException(error: Throwable) {
        if (error is CacheError) throw error
        handler?.onException(error)
    }
}