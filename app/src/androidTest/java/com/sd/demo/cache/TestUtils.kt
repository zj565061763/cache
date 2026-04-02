package com.sd.demo.cache

import com.sd.lib.cache.CacheEntity
import com.sd.lib.cache.FCache
import org.junit.Assert.assertEquals

@CacheEntity("TestDefaultModel")
data class TestDefaultModel(
  val name: String = "tom",
)

fun <T> testCache(
  clazz: Class<T>,
  factory: (key: String) -> T,
) {
  val cache = FCache.get(clazz)

  val key1 = "TestKey1"
  val key2 = "TestKey2"

  val model1 = factory(key1)
  val model2 = factory(key2)

  // test default value
  assertEquals(null, cache.get(key1))
  assertEquals(null, cache.get(key2))

  // test put and get
  assertEquals(true, cache.put(key1, model1))
  assertEquals(true, cache.put(key2, model2))
  assertEquals(model1, cache.get(key1))
  assertEquals(model2, cache.get(key2))

  // test keys
  cache.keys().also {
    assertEquals(2, it.size)
    assertEquals(true, it.contains(key1))
    assertEquals(true, it.contains(key2))
  }

  // test remove and get
  assertEquals(true, cache.remove(key1))
  assertEquals(true, cache.remove(key2))

  assertEquals(null, cache.get(key1))
  assertEquals(null, cache.get(key2))
  assertEquals(0, cache.keys().size)
}