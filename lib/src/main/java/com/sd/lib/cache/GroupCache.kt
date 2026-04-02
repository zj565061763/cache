package com.sd.lib.cache

/**
 * DefaultGroup缓存
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class DefaultGroupCache(
  /** 缓存id，在该组中不能重复 */
  val id: String,
  /** 是否支持多进程 */
  val multiProcess: Boolean = false,
  /** 锁等级 */
  val lockLevel: CacheLockLevel = CacheLockLevel.CurrentProcessCurrentCache,
)