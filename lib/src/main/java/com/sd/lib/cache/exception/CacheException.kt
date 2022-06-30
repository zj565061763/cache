package com.sd.lib.cache.exception

class CacheException(
    message: String = "",
    cause: Throwable? = null,
) : RuntimeException(message, cause)