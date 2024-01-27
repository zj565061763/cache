package com.sd.lib.cache

class CacheException(
    message: String = "",
    cause: Throwable? = null,
) : Exception(message, cause)

internal fun Cache.ExceptionHandler.notifyException(error: Throwable) {
    if (error is CacheException) throw error
    onException(error)
}