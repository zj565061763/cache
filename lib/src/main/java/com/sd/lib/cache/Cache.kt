package com.sd.lib.cache

interface Cache<T> {
  fun put(key: String, value: T?): Boolean
  fun get(key: String): T?
  fun remove(key: String)
  fun contains(key: String): Boolean
  fun keys(): List<String>
}