package com.sd.lib.cache

import android.content.Context
import com.sd.lib.cache.store.SimpleDiskCacheStore
import java.io.File

class CacheConfig private constructor(builder: Builder) {
    val context: Context

    @JvmField
    val objectConverter: Cache.ObjectConverter?

    @JvmField
    val encryptConverter: Cache.EncryptConverter

    @JvmField
    val exceptionHandler: Cache.ExceptionHandler

    @JvmField
    val cacheStore: Cache.CacheStore

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
        @JvmStatic
        private var config: CacheConfig? = null

        /**
         * 初始化
         */
        @JvmStatic
        @Synchronized
        fun init(config: CacheConfig) {
            if (this.config == null) {
                this.config = config
            }
        }

        /**
         * 返回配置
         */
        @JvmStatic
        fun get(): CacheConfig {
            return requireNotNull(config) {
                "CacheConfig has not been init"
            }
        }
    }

    init {
        context = requireNotNull(builder._context) { "context is null" }
        objectConverter = builder._objectConverter
        encryptConverter = builder._encryptConverter ?: object : Cache.EncryptConverter {
            override fun encrypt(bytes: ByteArray): ByteArray {
                return bytes
            }

            override fun decrypt(bytes: ByteArray): ByteArray {
                return bytes
            }
        }
        exceptionHandler = builder._exceptionHandler ?: Cache.ExceptionHandler { }
        cacheStore = builder._cacheStore ?: SimpleDiskCacheStore(File(context.filesDir, "disk_file"))
    }
}