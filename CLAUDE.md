# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目概述

Android 键值缓存库，发布于 Maven Central：`io.github.zj565061763.android:cache`。仓库包含两个模块：

- **`lib/`** — 库本体（命名空间 `com.sd.lib.cache`，`minSdk 21`）
- **`app/`** — Demo 应用及仪器化测试套件

## 构建命令

```bash
# 编译库的 release 产物
./gradlew :lib:assembleRelease

# 运行所有仪器化测试（需要连接设备或模拟器）
./gradlew :app:connectedAndroidTest

# 只运行某个测试类
./gradlew :app:connectedAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.sd.demo.cache.CacheTest

# 编译 Demo 应用
./gradlew :app:assembleDebug
```

所有测试均为**仪器化测试**（`androidTest`），不含本地 JVM 单元测试。

## 架构说明

### 初始化

必须在 `Application.onCreate` 中调用一次 `CacheConfig.init(context) { … }`，可配置：
- `CacheStoreFactory` — 默认使用 `FileCacheStore`
- `ObjectConverter` — 默认使用 Moshi JSON（`fMoshi`）
- `ExceptionHandler` — 接收非致命异常

### 声明缓存实体

用 `@CacheEntity` 标注数据类：

```kotlin
@CacheEntity(id = "MyModel", group = "com.example.mygroup")
data class MyModel(val name: String = "")
```

`id` 在同一 `group` 内不可重复。`group` 默认为 `"com.sd.lib.cache.group.default"`。

### 核心 API

| 入口 | 适用场景 |
|---|---|
| `FCache.get(clazz)` → `Cache<T>` | 同步访问 |
| `FCache.getKtx(clazz)` → `CacheKtx<T>` | 协程 / Flow 访问 |
| `singleCacheKtx<T>(…) { default }` | 单 key 缓存，可选内存热流 |

`Cache<T>` 的方法（`put/get/remove/keys`）**线程安全**，失败时返回 `false`/`null`/空列表，不向调用方抛出异常；异常统一转发给 `ExceptionHandler`。

`CacheKtx<T>` 是 `Cache<T>` 的协程封装：
- `flowOf(key)` — 冷流 `Flow<T?>`，每次缓存变化时重新发射
- `eventFlowOf(key)` — 冷流 `Flow<Unit>`，仅发射变化事件本身
- `edit { }` — 挂起块，在 `Dispatchers.IO` 上持锁执行 `Cache` 操作

`SingleCacheKtx<T>` 是固定单 key 的门面。启用 `memoryCache = true` 时，实例为进程级单例，底层由 `MutableSharedFlow(replay=1)` 驱动，`flow()` 返回热流。

### 存储实现（`FileCacheStore`）

文件存储路径：`filesDir/sd.lib.cache/<md5(group)>/<md5(id)>/`。  
Key 经 URL-safe Base64 编码后作为文件名，后缀为 `.cache`。  
写入采用原子操作：先写 `.tmp` 临时文件，再 `rename` 为正式文件。  
`FileObserver` 驱动响应式变更通知，监听掩码收窄为 `CLOSE_WRITE | MOVED_TO | MOVED_FROM | DELETE | DELETE_SELF | MOVE_SELF`，避免读操作产生噪声事件。

**Key 最大长度为 183 UTF-8 字节**（在 `fileOf()` 中校验），限制的是字节数而非字符数——一个汉字占 3 字节。

### 锁级别（`CacheLockLevel`）

通过 `@CacheEntity` 的 `lockLevel` 参数控制：

| 级别 | 说明 |
|---|---|
| `CurrentProcessCurrentCache`（默认）| 每个缓存实例独立锁 |
| `CurrentProcessCurrentGroup` | 同组所有缓存共享一把锁 |
| `CurrentProcess` | 全进程所有缓存共享一把锁 |

### 错误模型

- `CacheException` — 可恢复异常；被 `libRunCatching` 捕获后转发给 `ExceptionHandler`
- `CacheError`（内部子类）— 编程错误（重复 `id`、配置错误等）；**始终重新抛出**，绕过 handler
- `Error` 子类 — 始终重新抛出

`libRunCatching` 是唯一的统一 catch 点；不要在业务层对 `Cache` 调用套自己的 `try/catch`，除非确实需要原始异常。
