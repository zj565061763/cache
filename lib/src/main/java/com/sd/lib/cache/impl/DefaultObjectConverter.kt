package com.sd.lib.cache.impl

import com.sd.lib.cache.Cache.ObjectConverter
import com.sd.lib.moshi.fMoshi

internal class DefaultObjectConverter : ObjectConverter {
    override fun <T> encode(value: T, clazz: Class<T>): ByteArray {
        return fMoshi.adapter(clazz).toJson(value).toByteArray()
    }

    override fun <T> decode(bytes: ByteArray, clazz: Class<T>): T {
        return checkNotNull(
            fMoshi.adapter(clazz).fromJson(bytes.decodeToString())
        )
    }
}