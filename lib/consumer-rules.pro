# 保留注解类本身
-keep @interface com.sd.lib.cache.CacheEntity

# 保留被该注解标注的类及其所有成员
-keep @com.sd.lib.cache.CacheEntity class * {*;}