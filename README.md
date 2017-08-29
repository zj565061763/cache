# Cache
内部基于DiskLruCache实现的本地缓存帮助类，支持关联自定义的缓存目录

## 常用方法
```java
SDDiskCache.init(this); //初始化
SDDiskCache.setGlobalObjectConverter(new GlobalObjectConverter()); //设置全局对象转换器，必须设置
SDDiskCache.setGlobalEncryptConverter(new GlobalEncryptConverter()); //设置全局加解密转换器，如果不需要加解密，可以不设置

TestModel model = new TestModel(); //创建实体
SDDiskCache.open().putObject(model, true); //保存实体，以加密方式保存

TestModel modelCached = SDDiskCache.open().getObject(TestModel.class); //查询保存的实体

//支持int,long,float,double,boolean,String等类型，更多方法见源码
```
