[![](https://jitpack.io/v/zj565061763/cache.svg)](https://jitpack.io/#zj565061763/cache)

# About

对象持久化库，方便本地存取对象

* 支持自定义数据格式，默认为Json格式
* 支持自定义底层存储，例如使用腾讯[MMKV](https://github.com/Tencent/MMKV)
* 支持LRU算法的缓存个数限制
* 支持缓存分组，例如根据不同账号设置缓存

# Sample

### 初始化

```kotlin
CacheConfig.init(
    CacheConfig.Builder()
        // 设置缓存目录(可选)
        .setDirectory(getExternalFilesDir("app_cache")!!)

        // 设置对象转换器(可选)
        .setObjectConverter(AppObjectConverter())

        // 设置异常处理(可选)
        .setExceptionHandler(AppExceptionHandler())

        // 设置缓存仓库(可选)
        .setExceptionHandler(AppCacheStore::class.java)

        // Context
        .build(this)
)
```

### 缓存分组

缓存支持分组，分为`DefaultGroup(默认组)`和`ActiveGroup(激活组)`。

`DefaultGroup`可用于保存公共的配置信息，始终处于可用状态。

`ActiveGroup`可用于保存指定用户的配置信息，默认为空不可用。

例如：当用户登录成功后，通过`FCache.setActiveGroup("用户ID")`方法把用户ID设置为激活组，此时`ActiveGroup`缓存处于可用状态，
退出登录后，通过`FCache.setActiveGroup("")`方法把`ActiveGroup`设置为空，此时`ActiveGroup`缓存处于不可用状态，
不可用状态时调用`ActiveGroup`缓存的方法都会失败，但不会闪退。

#### 缓存对象

```kotlin
/**
 * DefaultGroup(默认组)缓存
 */
@DefaultGroupCache(id = "DefaultModel")
data class DefaultModel(
    val name: String = "tom",
)

/**
 * ActiveGroup(激活组)缓存
 */
@ActiveGroupCache(id = "ActiveModel")
data class ActiveModel(
    val name: String = "tom",
)
```

缓存对象需要加`DefaultGroupCache`或者`ActiveGroupCache`注解，并指定`id`，在同一个分组中，`id`不能重复。<br>
这两个注解中都有`limitCount`用于限制缓存个数，小于等于0表示不限制，默认不限制个数。

#### 获取缓存

```kotlin
// 获取DefaultModel缓存
val cache = FCache.get(DefaultModel::class.java)

// 设置缓存
cache.put("key", DefaultModel())

// 获取缓存
cache.get("key")

// 移除缓存
cache.remove("key")

// 是否有缓存
cache.contains("key")
```