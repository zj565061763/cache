package com.sd.lib.cache

import android.content.Context
import com.sd.lib.cache.store.CacheStore
import java.io.File

object FCache {
    /** DefaultGroup */
    private const val DEFAULT_GROUP = "com.sd.lib.cache.default.group"
    /** DefaultGroup的[CacheStoreFactory] */
    private val _defaultGroupCacheStoreFactory = CacheStoreFactory(DEFAULT_GROUP)

    /** ActiveGroup */
    private var _activeGroup = ""
    /** ActiveGroup的[CacheStoreFactory] */
    private var _activeGroupCacheStoreFactory: CacheStoreFactory? = null

    /**
     * 获取[clazz]对应的[Cache]
     */
    @JvmStatic
    fun <T> get(clazz: Class<T>): Cache<T> {
        val defaultType = clazz.getAnnotation(DefaultGroupCacheType::class.java)
        val activeType = clazz.getAnnotation(ActiveGroupCacheType::class.java)

        when {
            defaultType == null && activeType == null -> {
                throw IllegalArgumentException("Annotation ${DefaultGroupCacheType::class.java.simpleName} or ${ActiveGroupCacheType::class.java.simpleName} was not found in $clazz")
            }
            defaultType != null && activeType != null -> {
                throw IllegalArgumentException("Can not use both ${DefaultGroupCacheType::class.java.simpleName} and ${ActiveGroupCacheType::class.java.simpleName} in $clazz")
            }
        }

        defaultType?.also { type ->
            return CacheImpl(
                clazz = clazz,
                cacheStoreOwner = cacheStoreOwnerForDefaultGroup(
                    id = type.id,
                    cacheSizePolicy = type.limitCount.cacheSizePolicy(),
                    clazz = clazz,
                )
            )
        }

        activeType?.also { type ->
            return CacheImpl(
                clazz = clazz,
                cacheStoreOwner = cacheStoreOwnerForActiveGroup(
                    id = type.id,
                    cacheSizePolicy = type.limitCount.cacheSizePolicy(),
                    clazz = clazz,
                )
            )
        }

        error("This should not happen")
    }

    @JvmStatic
    fun getActiveGroup(): String {
        synchronized(CacheLock) {
            return _activeGroup
        }
    }

    @JvmStatic
    fun setActiveGroup(group: String) {
        require(group != DEFAULT_GROUP) { "Require not default group" }
        synchronized(CacheLock) {
            _activeGroup = group
        }
    }

    private fun cacheStoreOwnerForDefaultGroup(
        id: String,
        cacheSizePolicy: CacheSizePolicy,
        clazz: Class<*>,
    ): CacheStoreOwner {
        require(id.isNotEmpty()) { "id is empty" }
        return CacheStoreOwner {
            _defaultGroupCacheStoreFactory.create(
                id = id,
                cacheSizePolicy = cacheSizePolicy,
                clazz = clazz,
            )
        }
    }

    private fun cacheStoreOwnerForActiveGroup(
        id: String,
        cacheSizePolicy: CacheSizePolicy,
        clazz: Class<*>,
    ): CacheStoreOwner {
        require(id.isNotEmpty()) { "id is empty" }
        return CacheStoreOwner {
            getActiveGroupCacheStore(
                id = id,
                cacheSizePolicy = cacheSizePolicy,
                clazz = clazz,
            )
        }
    }

    private fun getActiveGroupCacheStore(
        id: String,
        cacheSizePolicy: CacheSizePolicy,
        clazz: Class<*>,
    ): CacheStore {
        val activeGroup = _activeGroup
        if (activeGroup.isEmpty()) {
            _activeGroupCacheStoreFactory?.close()
            _activeGroupCacheStoreFactory = null
            return EmptyActiveGroupCacheStore
        }

        val storeFactory = _activeGroupCacheStoreFactory?.takeIf { it.group == activeGroup }
            ?: CacheStoreFactory(activeGroup).also { newFactory ->
                _activeGroupCacheStoreFactory?.close()
                _activeGroupCacheStoreFactory = newFactory
            }

        return storeFactory.create(
            id = id,
            cacheSizePolicy = cacheSizePolicy,
            clazz = clazz,
        )
    }
}

internal val CacheLock: Any = FCache

private fun Int.cacheSizePolicy(): CacheSizePolicy {
    val count = this
    return if (count > 0) CacheSizePolicy.LimitCount(count)
    else CacheSizePolicy.Unlimited
}

private object EmptyActiveGroupCacheStore : CacheStore {
    override fun init(context: Context, directory: File, group: String, id: String) {
        notifyException("Empty active group CacheStore.init()")
    }

    override fun putCache(key: String, value: ByteArray): Boolean {
        notifyException("Empty active group CacheStore.putCache()")
        return false
    }

    override fun getCache(key: String): ByteArray? {
        notifyException("Empty active group CacheStore.getCache()")
        return null
    }

    override fun removeCache(key: String) {
        notifyException("Empty active group CacheStore.removeCache()")
    }

    override fun containsCache(key: String): Boolean {
        notifyException("Empty active group CacheStore.containsCache()")
        return false
    }

    override fun keys(): List<String> {
        notifyException("Empty active group CacheStore.keys()")
        return emptyList()
    }

    override fun close() {
        notifyException("Empty active group CacheStore.close()")
    }

    private fun notifyException(message: String) {
        libNotifyException(CacheException(message))
    }
}