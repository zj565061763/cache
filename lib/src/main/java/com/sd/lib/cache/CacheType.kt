package com.sd.lib.cache

/**
 * DefaultGroup缓存，[id]在该组中不能重复，
 * [limitCount]大于0时表示限制缓存个数，小于等于0时表示不限制缓存个数。
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class DefaultGroupCache(
    val id: String,
    val limitCount: Int = 0,
)

/**
 * ActiveGroup缓存，[id]在该组中不能重复，
 * [limitCount]大于0时表示限制缓存个数，小于等于0时表示不限制缓存个数。
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class ActiveGroupCache(
    val id: String,
    val limitCount: Int = 0,
)