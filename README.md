
## 常用方法
```java
SDDiskCache.init(this); //初始化

TestModel model = new TestModel(); //创建实体
SDDiskCache.open().putSerializable(model); //保存实体

TestModel modelCached = SDDiskCache.open().getSerializable(TestModel.class); //查询保存的实体

//支持int,long,float,double,boolean,String等类型，更多方法见源码
```
