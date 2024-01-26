package com.sd.lib.cache.store

import android.content.Context
import java.io.File

internal object EmptyCacheStore : CacheStore {
    override fun init(context: Context, directory: File, id: String) = Unit
    override fun putCache(key: String, value: ByteArray): Boolean = false
    override fun getCache(key: String): ByteArray? = null
    override fun removeCache(key: String) = Unit
    override fun containsCache(key: String): Boolean = false
    override fun keys(): Array<String>? = null
    override fun close() = Unit
}