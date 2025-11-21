package com.sd.lib.cache

/**
 * DefaultGroup缓存，[id]在该组中不能重复
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class DefaultGroupCache(
  val id: String,
)

/**
 * ActiveGroup缓存，[id]在该组中不能重复
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class ActiveGroupCache(
  val id: String,
)