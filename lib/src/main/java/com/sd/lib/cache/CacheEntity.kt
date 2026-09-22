package com.sd.lib.cache

/**
 * 缓存实体。
 *
 * 混淆时只保留被标注的类，实体中用到的嵌套类和枚举需要自行添加`@Keep`，
 * 否则开启R8后可能崩溃或读不出旧缓存。
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class CacheEntity(
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
}