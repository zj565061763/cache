package com.sd.demo.cache.impl

import com.sd.lib.cache.Cache
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

class MoshiObjectConverter : Cache.ObjectConverter {
    private val _moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()

    override fun <T> encode(value: T, clazz: Class<T>): ByteArray {
        val adapter = _moshi.adapter(clazz)
        val json = adapter.toJson(value)
        return json.toByteArray()
    }

    override fun <T> decode(bytes: ByteArray, clazz: Class<T>): T {
        val adapter = _moshi.adapter(clazz)
        val json = bytes.decodeToString()
        return adapter.fromJson(json)!!
    }
}