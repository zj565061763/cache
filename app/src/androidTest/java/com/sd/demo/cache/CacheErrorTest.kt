package com.sd.demo.cache

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.sd.lib.cache.CacheEntity
import com.sd.lib.cache.CacheException
import com.sd.lib.cache.FCache
import com.sd.lib.cache.singleCacheKtx
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.nio.charset.CharacterCodingException

@RunWith(AndroidJUnit4::class)
class FCacheErrorTest {

  /** 没有 @CacheEntity 注解时，FCache.get() 应立即抛出 IllegalArgumentException */
  @Test
  fun testNoAnnotationThrows() {
    assertThrows(IllegalArgumentException::class.java) {
      FCache.get(NoAnnotationModel::class.java)
    }
  }

  /** @CacheEntity.id 为空白时，FCache.get() 应立即抛出 IllegalArgumentException */
  @Test
  fun testBlankIdThrows() {
    assertThrows(IllegalArgumentException::class.java) {
      FCache.get(BlankIdModel::class.java)
    }
  }

  /** @CacheEntity.group 为空白时，FCache.get() 应立即抛出 IllegalArgumentException */
  @Test
  fun testBlankGroupThrows() {
    assertThrows(IllegalArgumentException::class.java) {
      FCache.get(BlankGroupModel::class.java)
    }
  }

  /** @CacheEntity.id 包含非法UTF-16时，FCache.get() 应立即抛出 IllegalArgumentException */
  @Test
  fun testInvalidUtf16IdThrows() {
    val error = assertThrows(IllegalArgumentException::class.java) {
      FCache.get(InvalidUtf16IdModel::class.java)
    }
    assertTrue(error.message.orEmpty().contains(".id contains invalid UTF-16"))
    assertTrue(error.cause is CharacterCodingException)
  }

  /** @CacheEntity.group 包含非法UTF-16时，FCache.get() 应立即抛出 IllegalArgumentException */
  @Test
  fun testInvalidUtf16GroupThrows() {
    val error = assertThrows(IllegalArgumentException::class.java) {
      FCache.get(InvalidUtf16GroupModel::class.java)
    }
    assertTrue(error.message.orEmpty().contains(".group contains invalid UTF-16"))
    assertTrue(error.cause is CharacterCodingException)
  }

  /**
   * 同一 group 内两个不同的类使用相同 id，创建第二个单值内存缓存时应抛出 CacheException。
   * CacheError 是编程错误，不经过 ExceptionHandler，直接向调用方传播。
   */
  @Test
  fun testDuplicateIdThrows() {
    // ConflictModelA 先注册
    FCache.get(ConflictModelA::class.java)

    // ConflictModelB 使用相同的 id + group，创建单值内存缓存时应同步抛出 CacheException
    val ex = assertThrows(CacheException::class.java) {
      singleCacheKtx<ConflictModelB>(memoryCache = true) { ConflictModelB() }
    }
    assertTrue(ex.message.orEmpty().contains(CONFLICT_ID))
  }
}

// 没有 @CacheEntity 注解
class NoAnnotationModel

@CacheEntity(id = "", group = "com.sd.lib.cache.group.default")
data class BlankIdModel(val name: String = "")

@CacheEntity(id = "BlankGroupModel", group = "")
data class BlankGroupModel(val name: String = "")

@CacheEntity(id = "\uD800", group = "com.sd.lib.cache.group.invalid_utf16_id")
data class InvalidUtf16IdModel(val name: String = "")

@CacheEntity(id = "InvalidUtf16GroupModel", group = "\uD800")
data class InvalidUtf16GroupModel(val name: String = "")

private const val CONFLICT_ID = "ConflictId"
private const val CONFLICT_GROUP = "com.sd.lib.cache.group.conflict_test"

@CacheEntity(id = CONFLICT_ID, group = CONFLICT_GROUP)
data class ConflictModelA(val name: String = "")

@CacheEntity(id = CONFLICT_ID, group = CONFLICT_GROUP)
data class ConflictModelB(val name: String = "")
