package com.sd.demo.cache

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.sd.lib.cache.CacheEntity
import com.sd.lib.cache.CacheException
import com.sd.lib.cache.FCache
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

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

  /**
   * 同一 group 内两个不同的类使用相同 id，首次执行缓存操作时应抛出 CacheException。
   * CacheError 是编程错误，不经过 ExceptionHandler，直接向调用方传播。
   */
  @Test
  fun testDuplicateIdThrows() {
    // ConflictModelA 先注册，首次操作正常
    val cacheA = FCache.get(ConflictModelA::class.java)
    assertEquals(true, cacheA.put("key", ConflictModelA()))

    // ConflictModelB 使用相同的 id + group，首次操作时应抛出 CacheException
    val cacheB = FCache.get(ConflictModelB::class.java)
    val ex = assertThrows(CacheException::class.java) {
      cacheB.put("key", ConflictModelB())
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

private const val CONFLICT_ID = "ConflictId"
private const val CONFLICT_GROUP = "com.sd.lib.cache.group.conflict_test"

@CacheEntity(id = CONFLICT_ID, group = CONFLICT_GROUP)
data class ConflictModelA(val name: String = "")

@CacheEntity(id = CONFLICT_ID, group = CONFLICT_GROUP)
data class ConflictModelB(val name: String = "")
