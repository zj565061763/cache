[![](https://jitpack.io/v/zj565061763/cache.svg)](https://jitpack.io/#zj565061763/cache)

# About

对象持久化库，方便本地存取对象

* 支持自定义数据格式，默认为Json格式
* 支持自定义底层存储，例如使用腾讯[MMKV](https://github.com/Tencent/MMKV)
* 支持单缓存和多缓存
* 支持LRU算法的缓存个数限制
* 支持缓存分组，例如根据不同账号信息保存缓存

# Sample

#### 初始化

```kotlin
CacheConfig.init(
    CacheConfig.Builder()
        // 设置缓存目录(可选参数)，默认为"Context.getFilesDir()/f_cache"
        .setDirectory(getExternalFilesDir("app_cache")!!)

        // 设置异常处理(可选参数)
        .setExceptionHandler(CacheExceptionHandler())

        // 设置对象转换器(可选参数)，默认为Gson转换器
        .setObjectConverter(MoshiObjectConverter())

        // Context
        .build(this)
)
```

#### 单缓存

单缓存是指一个类只能持久化一个对象，例如`TestModel`类，只能存一个该类的实例到本地，常用于保存App配置信息等。<br>
注意：库内部采用类的全类名做为缓存的`key`，所以修改类的名称或者包名后缓存会失效。

```kotlin
// 获取TestModel类对应的单缓存管理对象
val singleCache = FCache.getDefault().single(TestModel::class.java)

// 放入缓存对象
singleCache.put(TestModel())

// 获取缓存对象
singleCache.get()

// 移除缓存对象
singleCache.remove()

// 是否有缓存对象
singleCache.contains()
```

#### 多缓存

多缓存是指一个类可以持久化多个对象，例如`TestModel`类，可以根据`key`保存多个该类的实例到本地，常用于缓存具有唯一ID的对象，可以把唯一ID当作`key`。<br>
注意：库内部采用类的全类名计算缓存的`key`，所以修改类的名称或者包名后缓存会失效。

```kotlin
// 获取TestModel类对应的多缓存管理对象
val multiCache = FCache.getDefault().multi(TestModel::class.java)

// 根据key保存多个实例
multiCache.put("1", TestModel())
multiCache.put("2", TestModel())
multiCache.put("3", TestModel())

// 根据key获取缓存
multiCache.get("1")

// 根据key移除缓存
multiCache.remove("1")

// 是否有key对应的缓存
multiCache.contains("1")
```

# 缓存分组

缓存支持分组，分为`DefaultGroup(默认组)`和`CurrentGroup(当前组)`。

#### DefaultGroup(默认组)

`DefaultGroup`一直处于可用状态，常用于保存公用的配置信息或者缓存，即各个用户账号共享的信息。

```kotlin
// DefaultGroup，无限制大小的缓存
val cache = FCache.getDefault()

// DefaultGroup，id为"1"的无限制大小缓存
val cache1 = FCache.defaultGroup().unlimited("1")

// DefaultGroup，id为"2"的限制个数的缓存
val cache2 = FCache.defaultGroup().limitCount("2", 100)
```

#### CurrentGroup(当前组)

`CurrentGroup`默认为空，常用于保存当前用户自己的配置信息，当用户切换的时候，可以通过`FCache.setCurrentGroup("用户ID")`来设置当前组，
如果`CurrentGroup`为空的情况下通过`FCache.currentGroup()`获取的缓存对象调用相关Api都会失败。

```kotlin
// CurrentGroup，无限制大小的缓存
val cache = FCache.getCurrent()

// CurrentGroup，id为"1"的无限制大小缓存
val cache1 = FCache.currentGroup().unlimited("1")

// CurrentGroup，id为"2"的限制个数的缓存
val cache2 = FCache.currentGroup().limitCount("2", 100)
```

# 自定义数据格式

默认情况下，采用`Json`数据格式存储，默认实现类：[GsonObjectConverter](https://github.com/zj565061763/cache/blob/master/lib/src/main/java/com/sd/lib/cache/impl/GsonObjectConverter.kt)<br>
可以实现`Cache.ObjectConverter`接口，自定义数据格式，例如使用[moshi](https://github.com/square/moshi)的实现：[MoshiObjectConverter](https://github.com/zj565061763/cache/blob/master/app/src/main/java/com/sd/demo/cache/impl/MoshiObjectConverter.kt)

# 自定义底层存储

默认情况下，底层采用文件流存取数据，默认实现类：[FileCacheStore](https://github.com/zj565061763/cache/blob/master/lib/src/main/java/com/sd/lib/cache/store/FileCacheStore.kt)<br>
可以实现[CacheStore](https://github.com/zj565061763/cache/blob/master/lib/src/main/java/com/sd/lib/cache/store/CacheStore.kt)接口，自定义底层数据如何存储，例如使用[MMKV](https://github.com/Tencent/MMKV)的实现：[MMKVCacheStore](https://github.com/zj565061763/cache/blob/master/app/src/main/java/com/sd/demo/cache/impl/MMKVCacheStore.kt)