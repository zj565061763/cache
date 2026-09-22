# Repository Guidelines

## 项目定位与目录

Android 键值缓存库，Maven 坐标 `io.github.zj565061763.android:cache`，最低 API 21，编译 SDK 35。

- 库代码：`lib/src/main/java/com/sd/lib/cache/`
- 示例：`app/src/main/`
- 测试：全部在 `app/src/androidTest/`，没有本地 JVM 测试
- 依赖版本：`gradle/libs.versions.toml`；发布坐标及版本：`lib/gradle.properties`

调用链：`CacheConfig → FCache → CacheImpl/CacheKtxImpl → CacheStore → FileCacheStore`。修改前先定位责任层，不要把存储、序列化、同步和通知混在一起。

## 初始化与公开 API

```kotlin
@CacheEntity(id = "UserProfile", group = "com.example.account")
data class UserProfile(val name: String = "")

val cache = FCache.get(UserProfile::class.java)       // 同步 API
val cacheKtx = FCache.getKtx(UserProfile::class.java) // Flow/协程 API
val single = singleCacheKtx<UserProfile>(memoryCache = true) { UserProfile() }
```

- `CacheConfig.init(context)` 在每个进程的 `Application.onCreate()` 中调用一次，重复调用会失败。
- 默认使用 `FileCacheStore` 和 Moshi。
- 自定义 `CacheStoreFactory` 每次返回新仓库；自定义 `ObjectConverter` 必须线程安全；自定义 `ExceptionHandler` 不能再调用缓存 API。
- `@CacheEntity` 的 `id`、`group` 不能为空白或含非法 UTF-16。
- 同一组内一个 `id` 只能绑定一个实体类型，不同组可以复用。
- `FCache` 按实体 `Class` 缓存 `Cache` 和 `CacheKtx` 实例。
- `put` 不接受 `null`，删除用 `remove`。
- `kotlinx-coroutines` 有意使用 `compileOnly`，由接入方自行声明。不要改成 `api`/`implementation`，审查时也不要列为依赖遗漏。

## 并发与 Flow

- `CacheLockLevel` 三个级别分别锁当前缓存、当前组、整个进程，都只在进程内有效。
- 跨进程只保证：按顺序读写、单次写入的文件完整、`FileObserver` 最终会通知。
- 跨进程并发写入、`edit/update` 原子性、写入顺序和即时一致性都不保证。审查时不要把缺少跨进程锁、并发删除缓存目录列为缺陷。
- `Cache.put/get/remove/keys` 在锁内访问仓库；删除不存在的 key 也返回 `true`。
- `CacheKtx.edit` 把整个块切到 `Dispatchers.IO` 并持有同一把锁，用于原子读改写。
- 在 `edit` 块内访问锁不同的其他缓存时，反向嵌套会死锁，属于调用方责任。需要跨缓存原子操作时，应共享同组锁或进程锁。
- 库内部不能在持有缓存锁时，再隐式获取调用方可见的锁。
- `flowOf(key)` 每次事件后重新读盘，并用 `distinctUntilChanged` 去重，不能用它断言事件次数。
- 仓库读取失败时，Flow 保留当前值；只有首次读取失败时发射 `null`。
- 解码失败视为无缓存。`SingleCacheKtx.update` 只在仓库读取失败时返回 `false`。
- `SingleCacheKtx.update` 的 lambda 返回 `null` 表示删除。
- `memoryCache=false`：每次调用都返回新实例，并调用一次 `getDefault`，`flow()` 为冷流。
- `memoryCache=true`：同类型共享进程级实例和 `SharedFlow(replay=1)`，`getDefault` 只在首次创建时调用，慢订阅者可能跳过中间值。

修改锁、回调注册顺序、初始值读取或热流初始化时，测试要覆盖：并发首次订阅、快速连续更新、多个订阅者、目录删除后恢复。

## 异常模型

- 普通存储或序列化异常经 `libRunCatching` 转给 `ExceptionHandler`，然后降级：`put/remove` 返回 `false`，`get` 返回 `null`，`keys` 返回空列表。
- 编码器返回空字节抛 `CacheException`；读到空文件视为无缓存。
- 缺少注解、`id/group` 非法时，取得实例时立即抛 `IllegalArgumentException`。
- 同组重复 `id`、内部配置错误和所有 JVM `Error` 必须继续抛出，不能降级。新增入口要保持这一区分。

## 文件格式与监听

- 目录：`filesDir/sd.lib.cache/<md5(group)>/<md5(id)>/`。
- 文件名：key 按 UTF-8 编码，转成无填充 URL-safe Base64，再加 `.cache`。key 最多 186 个 UTF-8 字节。
- `keys()` 只返回文件名是规范 Base64、解码为合法 UTF-8 的非空普通 `.cache` 文件。
- 写入：先排他创建 `.sd-cache-<进程名MD5>-<随机值>.tmp`，写完后重命名为缓存文件。
- 临时文件清理：每个缓存首次访问时，只清理当前进程前缀的残留文件，不能清理其他进程的。
- 监听事件：只处理 `CLOSE_WRITE`、`MOVED_TO`、`MOVED_FROM`、`DELETE`、`DELETE_SELF`、`MOVE_SELF`。文件移出等同删除；目录删除或移动触发 `onCleared`。
- 监听失效后，`put/get/remove/keys` 任一路径都要重建目录并重新注册监听。
- 修改目录、哈希、编码、后缀、写入顺序或事件映射都是持久化兼容性变更，需要迁移说明和恢复测试。

## 构建与验证

在仓库根目录执行（`gradlew` 没有执行权限时用 `sh gradlew`）：

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

测试使用 AndroidX JUnit4 和 Turbine：

| 测试类 | 覆盖范围 |
|---|---|
| `CacheTest` | 基础读写、key 边界、损坏数据、临时文件 |
| `CacheErrorTest` | 配置错误 |
| `CacheKtxTest` | 锁和事件竞态 |
| `SingleCacheKtxTest` | 冷热流 |
| `FileCacheStoreRecoveryTest` | 目录异常 |
| `MultiProcessCacheWriteTest` | 多进程写入 |

- 文件监听和 IO 用的是真实时间，所以使用 `runBlocking`、`TEST_TIMEOUT`（15 秒）和 `awaitItemUntil`，不要换成虚拟时间的 `runTest`。
- 新增测试模型使用唯一的 `id`，在 `finally` 中清理文件或订阅任务。

## 编码与提交

- 遵循 Kotlin 官方风格，两空格缩进，多行参数保留尾逗号。
- 公开行为添加 KDoc，实现细节优先用 `internal`/`private`。
- 依赖版本统一放入版本目录。
- 修改 `@CacheEntity` 或反射模型时，同时检查 `consumer-rules.pro`。
- 混淆规则只保留实体类本身，嵌套类和枚举由接入方保留（见 README）。示例 app 没开混淆，测试覆盖不到这类问题。
- 提交信息格式为 `type(scope): 中文描述`，例如 `fix(lib): 忽略非缓存文件条目`。一个提交只处理一个主题。
- Pull Request 说明受影响模块、行为变化、验证命令和关联 Issue。
- 公开 API 或磁盘格式变化，必须在 `CHANGELOG.md` 写迁移说明。
- 发布前同步 `VERSION_NAME`、变更日志和 Maven 配置。
- 不要提交签名密钥、仓库凭据、`local.properties` 或 `build/` 产物。
