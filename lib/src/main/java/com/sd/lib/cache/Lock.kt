package com.sd.lib.cache

/**
 * 缓存锁等级
 */
enum class CacheLockLevel {
  /** 当前进程当前缓存 */
  CurrentProcessCurrentCache,

  /** 当前进程当前组 */
  CurrentProcessCurrentGroup,

  /** 当前进程 */
  CurrentProcess,
}

internal fun <T> CacheImpl<*>.lockCache(block: () -> T): T = synchronized(lock) { block() }