package com.sd.demo.cache.impl

import android.util.Log
import com.sd.demo.cache.logMsg
import com.sd.lib.cache.Cache

class CacheExceptionHandler : Cache.ExceptionHandler {
    override fun onException(e: Exception) {
        logMsg { "error:${Log.getStackTraceString(e)}" }
    }
}