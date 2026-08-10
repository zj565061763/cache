package com.sd.demo.cache

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.sd.lib.cache.CacheEntity
import com.sd.lib.cache.CacheException
import com.sd.lib.cache.FCache
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

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
    val key = "k".repeat(186)
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
    val key = "k".repeat(187)

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

    // 62个汉字186字节，正好在上限内
    val key = "缓".repeat(62)
    assertEquals(186, key.toByteArray().size)
    assertEquals(true, cache.put(key, model))
    assertEquals(model, cache.get(key))
    assertEquals(true, cache.remove(key))

    // 63个汉字189字节，超了
    val tooLongKey = "缓".repeat(63)
    assertEquals(189, tooLongKey.toByteArray().size)

    CacheErrors.clear()
    assertEquals(false, cache.put(tooLongKey, model))
    // 必须断言异常的内容：如果长度检查错写成key.length，63也没超186，
    // put同样会因为文件名过长而返回false，只看返回值区分不出来
    val tooLong = CacheErrors.list().filter {
      it is CacheException && it.message.orEmpty().contains("too long")
    }
    assertEquals(1, tooLong.size)
  }

  /** 目录里混进名字能通过Base64解码、但解出来不是合法UTF-8的文件时，keys()不能把它当成有效key */
  @Test
  fun testInvalidUtf8FilenameIgnored() {
    val cache = FCache.get(TestInvalidFilenameModel::class.java)
    val key = "validKey"
    assertEquals(true, cache.put(key, TestInvalidFilenameModel()))

    // "____"是4个合法的URL_SAFE Base64字符（_是索引63），解码得到[0xFF,0xFF,0xFF]，
    // 而0xFF在UTF-8里永远不是合法字节
    val garbage = cacheStoreDirectory(INVALID_FILENAME_MODEL_ID).resolve("____.cache")
    garbage.writeBytes(byteArrayOf(1))
    try {
      assertEquals(listOf(key), cache.keys())
    } finally {
      garbage.delete()
      cache.remove(key)
    }
  }

  /** 磁盘上的缓存数据损坏时，get()应返回null并把解码异常转发给ExceptionHandler，而不是抛出 */
  @Test
  fun testCorruptedDataReturnsNull() {
    val cache = FCache.get(TestCorruptModel::class.java)
    val key = "testCorruptedData"

    assertEquals(true, cache.put(key, TestCorruptModel(name = "valid")))
    // 绕过CacheStore直接写坏数据
    writeCacheFileDirectly(id = CORRUPT_MODEL_ID, key = key, json = "{invalid")

    CacheErrors.clear()
    assertEquals(null, cache.get(key))

    // 解码失败是普通异常（Moshi的JsonEncodingException），应该被转发给ExceptionHandler而不是抛给调用方
    val errors = CacheErrors.list().filter { it is IOException }
    assertEquals(true, errors.isNotEmpty())
  }

  /** 同一个id在不同group下互不冲突，各自独立读写 */
  @Test
  fun testSameIdDifferentGroupNoConflict() {
    val cacheA = FCache.get(TestSameIdGroupAModel::class.java)
    val cacheB = FCache.get(TestSameIdGroupBModel::class.java)

    val key = "key"
    assertEquals(true, cacheA.put(key, TestSameIdGroupAModel(name = "a")))
    assertEquals(true, cacheB.put(key, TestSameIdGroupBModel(name = "b")))

    // 相同的key在各自的group下互不干扰
    assertEquals("a", cacheA.get(key)?.name)
    assertEquals("b", cacheB.get(key)?.name)
    assertEquals(listOf(key), cacheA.keys())
    assertEquals(listOf(key), cacheB.keys())

    // 删除互不影响
    assertEquals(true, cacheA.remove(key))
    assertEquals(null, cacheA.get(key))
    assertEquals("b", cacheB.get(key)?.name)
    assertEquals(true, cacheB.remove(key))
  }

  /** 残留的临时文件不能出现在keys()中 */
  @Test
  fun testTempFileNotInKeys() {
    val cache = FCache.get(TestTempFileModel::class.java)
    val key = "testTempFile"

    assertEquals(true, cache.put(key, TestTempFileModel(name = "value")))

    // 手动制造一个残留的临时文件（模拟进程在上一次写入中途被杀）
    val tempFile = currentProcessTempFile(TEMP_FILE_MODEL_ID, marker = "residual")
    tempFile.writeBytes("garbage".toByteArray())
    try {
      assertEquals(listOf(key), cache.keys())
    } finally {
      tempFile.delete()
      cache.remove(key)
    }
  }

  /** store初始化时应清理掉残留的临时文件 */
  @Test
  fun testTempFileCleanedAtInit() {
    val cache = FCache.get(TestTempCleanModel::class.java)
    val key = "testTempClean"

    // 预置目录和残留临时文件，模拟上一次进程崩溃
    val dir = cacheStoreDirectory(TEMP_CLEAN_MODEL_ID)
    dir.mkdirs()
    val stale = currentProcessTempFile(TEMP_CLEAN_MODEL_ID, marker = "stale")
    stale.writeBytes("garbage".toByteArray())

    try {
      // 首次操作触发store初始化，残留临时文件应被清理
      assertEquals(true, cache.put(key, TestTempCleanModel(name = "value")))
      assertEquals(false, stale.exists())
    } finally {
      stale.delete()
      cache.remove(key)
    }
  }
}

@CacheEntity("TestKeyLengthModel")
data class TestKeyLengthModel(
  val name: String = "tom",
)

const val INVALID_FILENAME_MODEL_ID = "TestInvalidFilenameModel"

@CacheEntity(INVALID_FILENAME_MODEL_ID)
data class TestInvalidFilenameModel(
  val name: String = "tom",
)

const val CORRUPT_MODEL_ID = "TestCorruptModel"

@CacheEntity(CORRUPT_MODEL_ID)
data class TestCorruptModel(
  val name: String = "",
)

private const val SAME_ID = "SameIdSharedId"
private const val GROUP_A = "com.sd.lib.cache.group.sameid_test_a"
private const val GROUP_B = "com.sd.lib.cache.group.sameid_test_b"

@CacheEntity(id = SAME_ID, group = GROUP_A)
data class TestSameIdGroupAModel(
  val name: String = "",
)

@CacheEntity(id = SAME_ID, group = GROUP_B)
data class TestSameIdGroupBModel(
  val name: String = "",
)

const val TEMP_FILE_MODEL_ID = "TestTempFileModel"

@CacheEntity(TEMP_FILE_MODEL_ID)
data class TestTempFileModel(
  val name: String = "tom",
)

const val TEMP_CLEAN_MODEL_ID = "TestTempCleanModel"

@CacheEntity(TEMP_CLEAN_MODEL_ID)
data class TestTempCleanModel(
  val name: String = "tom",
)
