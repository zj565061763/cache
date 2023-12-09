package com.sd.lib.cache.impl

import com.google.gson.Gson
import com.sd.lib.cache.Cache.ObjectConverter

internal class GsonObjectConverter : ObjectConverter {
    private val _gson = Gson()

    override fun encode(value: Any, clazz: Class<*>): ByteArray {
        return _gson.toJson(value).toByteArray()
    }

    override fun <T> decode(bytes: ByteArray, clazz: Class<T>): T {
        return _gson.fromJson(String(bytes), clazz)
    }
}