package com.sd.lib.cache.simple

import com.google.gson.Gson
import com.sd.lib.cache.Cache.ObjectConverter

internal class GsonObjectConverter : ObjectConverter {
    private val _gson = Gson()

    override fun objectToByte(value: Any): ByteArray {
        return _gson.toJson(value).toByteArray()
    }

    override fun <T> byteToObject(bytes: ByteArray, clazz: Class<T>): T {
        return _gson.fromJson(String(bytes), clazz)
    }
}