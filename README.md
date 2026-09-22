[![Maven Central](https://img.shields.io/maven-central/v/io.github.zj565061763.android/cache)](https://central.sonatype.com/search?q=g:io.github.zj565061763.android+cache)

# Gradle

```kotlin
implementation("io.github.zj565061763.android:cache:$version")
```

本库不会传递 Kotlin 协程依赖，接入方必须自行声明兼容版本的协程依赖。

版本更新记录请查看 [CHANGELOG.md](CHANGELOG.md)。

# 混淆

本库只保留 `@CacheEntity` 实体类本身。实体中用到的嵌套类和枚举（包括集合元素类型）需要自行保留，否则开启 R8 后：

- 读写含枚举的缓存会崩溃
- 嵌套类的字段名被混淆，版本更新后旧缓存可能读不出来

推荐添加 `@Keep`：

```kotlin
@CacheEntity(id = "UserProfile")
data class UserProfile(
  val level: Level = Level.Normal,
  val address: Address = Address(),
)

@Keep
enum class Level { Normal, Vip }

@Keep
data class Address(val city: String = "")
```

如果已发布的版本没有保留嵌套类，补加 `@Keep` 后字段名会变化，旧数据需要自行迁移。

# 多进程约束

本库支持多个进程按顺序读写同一缓存；每个进程都必须在自己的 `Application.onCreate()` 中调用 `CacheConfig.init(context)`。默认文件存储先写临时文件再重命名，正常完成的单次写入不会留下半写的正式缓存文件。

本库不提供跨进程互斥：多个进程同时写入同一 key 时，成功完成的单次写入仍是完整文件，但不保证写入顺序和最终值；`CacheKtx.edit`、`SingleCacheKtx.update` 的读改写也只在当前进程内原子化。需要跨进程并发访问时，调用方必须通过 IPC、文件锁或其他方式自行串行化。Flow 和内存缓存通过 `FileObserver` 异步感知其他进程的修改，不提供即时一致性。
