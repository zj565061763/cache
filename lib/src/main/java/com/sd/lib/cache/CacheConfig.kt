package com.sd.lib.cache

import android.content.Context
import com.sd.lib.cache.simple.GsonObjectConverter
import com.sd.lib.cache.store.UnlimitedDiskCacheStore
import java.io.File

class CacheConfig private constructor(builder: Builder) {
    internal val objectConverter: Cache.ObjectConverter
    internal val encryptConverter: Cache.EncryptConverter?
    internal val exceptionHandler: Cache.ExceptionHandler
    internal val cacheStore: Cache.CacheStore

    init {
        val context = checkNotNull(builder._context) { "context is null" }
        objectConverter = builder._objectConverter ?: GsonObjectConverter()
        encryptConverter = builder._encryptConverter
        exceptionHandler = builder._exceptionHandler ?: Cache.ExceptionHandler { }
        cacheStore = builder._cacheStore ?: UnlimitedDiskCacheStore(File(context.filesDir, "f_disk_cache"))
    }

    class Builder {
        internal var _context: Context? = null
        internal var _objectConverter: Cache.ObjectConverter? = null
        internal var _encryptConverter: Cache.EncryptConverter? = null
        internal var _exceptionHandler: Cache.ExceptionHandler? = null
        internal var _cacheStore: Cache.CacheStore? = null

        /**
         * 设置对象转换器
         */
        fun setObjectConverter(converter: Cache.ObjectConverter?) = apply {
            _objectConverter = converter
        }

        /**
         * 设置加解密转换器
         */
        fun setEncryptConverter(converter: Cache.EncryptConverter?) = apply {
            _encryptConverter = converter
        }

        /**
         * 设置异常处理对象
         */
        fun setExceptionHandler(handler: Cache.ExceptionHandler?) = apply {
            _exceptionHandler = handler
        }

        /**
         * 设置缓存
         */
        fun setCacheStore(store: Cache.CacheStore?) = apply {
            _cacheStore = store
        }

        fun build(context: Context): CacheConfig {
            _context = context.applicationContext
            return CacheConfig(this)
        }
    }

    companion object {
        @Volatile
        @JvmStatic
        private var config: CacheConfig? = null

        /**
         * 初始化
         */
        @JvmStatic
        fun init(config: CacheConfig) {
            this.config = config
        }

        /**
         * 返回配置
         */
        @JvmStatic
        fun get(): CacheConfig {
            return checkNotNull(config) { "CacheConfig has not been init" }
        }
    }
}