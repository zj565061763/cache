package com.sd.lib.cache.store

import android.content.Context
import com.tencent.mmkv.MMKV
import com.tencent.mmkv.MMKVLogLevel
import java.io.File
import java.security.MessageDigest
import java.util.concurrent.atomic.AtomicBoolean

/**
 * 基于[https://github.com/Tencent/MMKV]实现的仓库
 */
class DefaultCacheStore : CacheStore {
    private var _initFlag = false

    private lateinit var _directory: File
    private lateinit var _group: String
    private lateinit var _id: String

    private var _mmkv: MMKV? = null

    private val mmkv: MMKV
        get() {
            _mmkv?.let { return it }
            val id = md5(_id)
            val groupDir = _directory.resolve(md5(_group)).absolutePath
            return MMKV.mmkvWithID(id, groupDir).also { _mmkv = it }
        }

    override fun init(
        context: Context,
        directory: File,
        group: String,
        id: String,
    ) {
        initMMKV(context, directory)

        if (_initFlag) error("CacheStore has already been initialized.")

        _directory = directory
        _group = group
        _id = id

        _initFlag = true
    }

    override fun putCache(key: String, value: ByteArray): Boolean {
        return mmkv.encode(key, value)
    }

    override fun getCache(key: String): ByteArray? {
        return mmkv.decodeBytes(key)
    }

    override fun removeCache(key: String) {
        mmkv.remove(key)
    }

    override fun containsCache(key: String): Boolean {
        return mmkv.contains(key)
    }

    override fun keys(): Array<String>? {
        return mmkv.allKeys()
    }

    override fun close() {
        _mmkv?.close()
        _mmkv = null
    }

    companion object {
        private val sInitFlag = AtomicBoolean(false)

        fun initMMKV(context: Context, directory: File) {
            if (sInitFlag.compareAndSet(false, true)) {
                MMKV.initialize(context, directory.absolutePath, MMKVLogLevel.LevelNone)
            }
        }
    }
}

private fun md5(input: String): String {
    return MessageDigest.getInstance("MD5")
        .digest(input.toByteArray())
        .joinToString("") { "%02X".format(it) }
}