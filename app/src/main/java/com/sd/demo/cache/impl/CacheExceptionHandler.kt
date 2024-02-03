package com.sd.demo.cache.impl

import com.sd.demo.cache.logMsg
import com.sd.lib.cache.Cache

class CacheExceptionHandler : Cache.ExceptionHandler {
    override fun onException(error: Throwable) {
        logMsg { "error:${error}" }
    }
}