package com.sd.lib.cache

import android.content.Context
import com.sd.lib.cache.simple.GsonObjectConverter
import com.sd.lib.cache.store.UnlimitedDiskCacheStore
import java.io.File

class CacheConfig private constructor(builder: Builder) {
    val context: Context
    val objectConverter: Cache.ObjectConverter
    val encryptConverter: Cache.EncryptConverter?
    val exceptionHandler: Cache.ExceptionHandler
    val cacheStore: Cache.CacheStore

    init {
        context = checkNotNull(builder._context) { "context is null" }
        objectConverter = builder._objectConverter ?: GsonObjectConverter()
        encryptConverter = builder._encryptConverter
        exceptionHandler = builder._exceptionHandler ?: Cache.ExceptionHandler { }
        cacheStore = builder._cacheStore ?: UnlimitedDiskCacheStore(File(context.filesDir, "f_disk_file"))
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
        fun setObjectConverter(converter: Cache.ObjectConverter?): Builder {
            _objectConverter = converter
            return this
        }

        /**
         * 设置加解密转换器
         */
        fun setEncryptConverter(converter: Cache.EncryptConverter?): Builder {
            _encryptConverter = converter
            return this
        }

        /**
         * 设置异常处理对象
         */
        fun setExceptionHandler(handler: Cache.ExceptionHandler?): Builder {
            _exceptionHandler = handler
            return this
        }

        /**
         * 设置缓存
         */
        fun setCacheStore(store: Cache.CacheStore?): Builder {
            _cacheStore = store
            return this
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