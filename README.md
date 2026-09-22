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

支持多个进程按顺序读写同一缓存，每个进程都要在 `Application.onCreate()` 中调用 `CacheConfig.init(context)`。

- 写入先写临时文件再重命名，不会留下写了一半的缓存文件
- 不提供跨进程互斥，多个进程同时写同一 key 时，最终值不确定
- `CacheKtx.edit`、`SingleCacheKtx.update` 只在当前进程内是原子的
- 需要跨进程并发写入时，请自行串行化，例如使用 IPC 或文件锁
- 其他进程的修改通过文件监听异步通知，Flow 会稍后收到更新
