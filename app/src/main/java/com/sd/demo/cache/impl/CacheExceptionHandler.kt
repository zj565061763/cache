package com.sd.demo.cache.impl

import android.util.Log
import com.sd.demo.cache.logMsg
import com.sd.lib.cache.Cache

class CacheExceptionHandler : Cache.ExceptionHandler {
    override fun onException(error: Throwable) {
        logMsg { "error:${Log.getStackTraceString(error)}" }
    }
}