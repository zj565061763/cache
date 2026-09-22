# Repository Guidelines

## 项目定位与目录

本仓库提供 Android 键值缓存库，Maven 坐标为 `io.github.zj565061763.android:cache`，最低支持 API 21，使用 SDK 35 编译。库代码在 `lib/src/main/java/com/sd/lib/cache/`，示例在 `app/src/main/`，全部自动化测试在 `app/src/androidTest/`，没有本地 JVM 测试。依赖版本在 `gradle/libs.versions.toml`，发布坐标及版本在 `lib/gradle.properties`。

核心调用链为：`CacheConfig → FCache → CacheImpl/CacheKtxImpl → CacheStore → FileCacheStore`。修改前先定位责任层，避免把存储、序列化、同步和响应式通知混在一起。

## 初始化与公开 API

`CacheConfig.init(context)` 必须在每个进程的 `Application.onCreate()` 中调用一次；重复初始化会失败。默认使用 `FileCacheStore` 和 Moshi。自定义 `CacheStoreFactory` 每次应返回新仓库，自定义 `ObjectConverter` 必须线程安全，自定义 `ExceptionHandler` 不应再次调用缓存 API 造成递归。

```kotlin
@CacheEntity(id = "UserProfile", group = "com.example.account")
data class UserProfile(val name: String = "")

val cache = FCache.get(UserProfile::class.java)       // 同步 API
val cacheKtx = FCache.getKtx(UserProfile::class.java) // Flow/协程 API
val single = singleCacheKtx<UserProfile>(memoryCache = true) { UserProfile() }
```

`@CacheEntity` 为运行时注解，`id` 和 `group` 不得为空白或含非法 UTF-16；同一组内一个 `id` 只能绑定一个实体类型，不同组可以复用 `id`。`FCache` 按实体 `Class` 缓存 `Cache` 和 `CacheKtx` 实例。`put` 不接受 `null`，删除必须调用 `remove`。

`lib` 中的 `kotlinx-coroutines` 有意使用 `compileOnly`，不作为 Maven 传递依赖；接入方必须自行声明兼容版本的 `kotlinx-coroutines-android`。除非项目依赖策略明确变更，否则不要将其改成 `api`/`implementation`，也不要在审查中把它列为依赖遗漏。

## 并发、Flow 与单值缓存契约

`CacheLockLevel` 的三个级别分别为当前进程内的“当前缓存”“当前组”“整个进程”，均不提供跨进程互斥。跨进程只保证按顺序读写、单次写入的文件完整性和 `FileObserver` 的最终通知；多进程并发写入、跨进程 `edit/update` 原子性、结果顺序和即时一致性不在保证范围内，审查时不要把缺少跨进程锁或并发删除缓存目录列为缺陷。

- `Cache.put/get/remove/keys` 在选定锁上串行访问仓库；不存在的 key 执行 `remove` 也返回 `true`。
- `CacheKtx.edit` 将整个非挂起块切到 `Dispatchers.IO` 并持有同一把锁，适合原子读改写。块内嵌套访问锁不同的其他缓存时，反向嵌套会死锁，属于调用方责任；需要跨缓存原子操作时应共享同组锁或进程锁。库内部不得在持有缓存锁时隐式获取调用方可见的锁，避免调用方未嵌套也死锁。
- `flowOf(key)` 在每个事件后重新读盘，并通过 `distinctUntilChanged` 去重；不要用它断言底层事件次数。仓库读取失败时 Flow 保留当前值，仅首次读取失败发射 `null`；解码失败视为无缓存，`SingleCacheKtx.update` 仅在仓库读取失败时返回 `false`。
- `SingleCacheKtx.update` 的 lambda 返回 `null` 表示删除。`memoryCache=false` 时每次调用都返回新实例并同步调用一次 `getDefault`，`flow()` 为冷流；`true` 时同类型共享进程级实例和 `SharedFlow(replay=1)`，`getDefault` 只在首次创建时同步调用一次，慢订阅者允许跳过中间值。

修改锁、回调注册顺序、初始值读取或热流初始化时，必须覆盖并发首次订阅、快速连续更新、多个订阅者和目录删除后的恢复。

## 异常模型

普通存储或序列化异常经 `libRunCatching` 转发给 `ExceptionHandler`：`put/remove` 降级为 `false`，`get` 为 `null`，`keys` 为空列表。编码器返回空字节属于 `CacheException`，空文件读取则视为无缓存。缺少注解、`id/group` 非法会在取得实例时立即抛出 `IllegalArgumentException`；同组重复 `id`、内部配置错误和所有 JVM `Error` 必须继续抛出，不得被静默降级。新增入口应保持这一区分。

## 文件格式与监听协议

默认目录是 `filesDir/sd.lib.cache/<md5(group)>/<md5(id)>/`。Key 以 UTF-8 编码后转换成无填充 URL-safe Base64，生成 `<key>.cache`；key 上限为 186 个 UTF-8 字节。`keys()` 只返回文件名能解码为规范 Base64 及合法 UTF-8 的非空普通 `.cache` 文件。

写入时排他创建 `.sd-cache-<进程名MD5>-<随机值>.tmp`，写完后重命名为目标缓存文件。每个缓存首次访问创建仓库时，只清理当前进程前缀的残留临时文件，不得清理其他进程的临时文件。监听仅处理 `CLOSE_WRITE`、`MOVED_TO`、`MOVED_FROM`、`DELETE`、`DELETE_SELF` 和 `MOVE_SELF`：移出等同删除，目录删除或移动触发 `onCleared`。监听失效后，`put/get/remove/keys` 任一路径都必须重建目录并重新注册监听。修改目录、哈希、编码、后缀、写入顺序或事件映射均属于持久化兼容性变更，需要迁移说明及恢复测试。

## 构建与验证

在仓库根目录使用 Gradle Wrapper：

```bash
./gradlew :lib:assembleRelease       # 构建发布 AAR
./gradlew :app:assembleDebug         # 构建示例 APK
./gradlew lint                       # 执行 Android Lint
./gradlew :app:connectedAndroidTest  # 在设备或模拟器运行完整测试
```

运行单个类时追加：

```bash
-Pandroid.testInstrumentationRunnerArguments.class=com.sd.demo.cache.CacheTest
```

测试使用 AndroidX JUnit4 和 Turbine。`CacheTest` 覆盖基础、key 边界、损坏数据和临时文件；`CacheErrorTest` 覆盖配置错误；`CacheKtxTest` 覆盖锁及事件竞态；`SingleCacheKtxTest` 覆盖冷热流；`FileCacheStoreRecoveryTest` 覆盖目录异常；`MultiProcessCacheWriteTest` 覆盖多进程写入。文件监听和 IO 使用真实时间，因此采用 `runBlocking`、`TEST_TIMEOUT`（15 秒）和 `awaitItemUntil`，不要替换为虚拟时间 `runTest`。新增测试模型应使用唯一 `id`，并在 `finally` 中清理文件或订阅任务。

## 编码、兼容与提交规范

遵循 Kotlin 官方风格和现有两空格缩进：类型使用 `PascalCase`，成员使用 `camelCase`，常量使用 `UPPER_SNAKE_CASE`，多行参数保留尾逗号。公开行为添加 KDoc，实现细节优先使用 `internal`/`private`。依赖版本统一放入版本目录。若修改 `@CacheEntity` 或反射模型，同时检查 `consumer-rules.pro`。消费规则只保留实体类本身，嵌套类和枚举由接入方保留（见 README）；示例 app 未开启混淆，测试覆盖不到这类问题。

提交信息格式为 `type(scope): 中文描述`，如 `fix(lib): 忽略非缓存文件条目`，一个提交只处理一个主题。Pull Request 需说明受影响模块、行为变化、验证命令及关联 Issue。公开 API 或磁盘格式变化必须在 `CHANGELOG.md` 提供迁移说明。发布前同步 `VERSION_NAME`、变更日志和 Maven 配置；不要提交签名密钥、仓库凭据、`local.properties` 或 `build/` 产物。
