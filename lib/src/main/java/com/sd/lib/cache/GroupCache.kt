package com.sd.lib.cache

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class GroupCache(
  /** 缓存id，在该组中不能重复 */
  val id: String,
  /** 缓存组 */
  val group: String = "com.sd.lib.cache.group.default",
  /** 锁等级 */
  val lockLevel: CacheLockLevel = CacheLockLevel.CurrentProcessCurrentCache,
)

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

  /** 多进程 */
  MultiProcess,
}