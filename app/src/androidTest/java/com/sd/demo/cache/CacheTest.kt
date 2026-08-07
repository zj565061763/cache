package com.sd.demo.cache

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.sd.lib.cache.CacheEntity
import com.sd.lib.cache.CacheException
import com.sd.lib.cache.FCache
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CacheTest {
  @Test
  fun testCacheInstance() {
    val cache1 = FCache.get(TestDefaultModel::class.java)
    val cache2 = FCache.get(TestDefaultModel::class.java)
    assertEquals(true, cache1 === cache2)
  }

  @Test
  fun testCache() {
    testCache(
      clazz = TestDefaultModel::class.java,
      factory = { TestDefaultModel(name = it) },
    )
  }

  /** key的长度上限之内可以正常使用，保证长度检查没有过严 */
  @Test
  fun testMaxLengthKey() {
    val cache = FCache.get(TestKeyLengthModel::class.java)
    // ASCII字符一个占一个字节
    val key = "k".repeat(183)
    val model = TestKeyLengthModel(name = "value")

    assertEquals(true, cache.put(key, model))
    assertEquals(model, cache.get(key))
    assertEquals(true, cache.keys().contains(key))
    assertEquals(true, cache.remove(key))
  }

  /** 超过长度上限时各个操作都要失败，并且异常里要说清楚原因 */
  @Test
  fun testTooLongKey() {
    val cache = FCache.get(TestKeyLengthModel::class.java)
    val key = "k".repeat(184)

    CacheErrors.clear()
    assertEquals(false, cache.put(key, TestKeyLengthModel()))
    assertEquals(null, cache.get(key))
    assertEquals(false, cache.remove(key))

    // 按内容过滤而不是数总数，避免被App里每秒轮询keys()的协程干扰
    val tooLong = CacheErrors.list().filter {
      it is CacheException && it.message.orEmpty().contains("too long")
    }
    assertEquals(3, tooLong.size)
  }

  /** 限制的是字节数不是字符数，UTF-8下一个汉字占3个字节 */
  @Test
  fun testChineseKeyLength() {
    val cache = FCache.get(TestKeyLengthModel::class.java)
    val model = TestKeyLengthModel(name = "value")

    // 61个汉字183字节，正好在上限内
    val key = "缓".repeat(61)
    assertEquals(183, key.toByteArray().size)
    assertEquals(true, cache.put(key, model))
    assertEquals(model, cache.get(key))
    assertEquals(true, cache.remove(key))

    // 62个汉字186字节，超了
    val tooLongKey = "缓".repeat(62)
    assertEquals(186, tooLongKey.toByteArray().size)

    CacheErrors.clear()
    assertEquals(false, cache.put(tooLongKey, model))
    // 必须断言异常的内容：如果长度检查错写成key.length，62也没超183，
    // put同样会因为文件名过长而返回false，只看返回值区分不出来
    val tooLong = CacheErrors.list().filter {
      it is CacheException && it.message.orEmpty().contains("too long")
    }
    assertEquals(1, tooLong.size)
  }
}

@CacheEntity("TestKeyLengthModel")
data class TestKeyLengthModel(
  val name: String = "tom",
)