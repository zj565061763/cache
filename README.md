
## 简单demo
```java
public class MainActivity extends AppCompatActivity
{
    private static final String TAG = "MainActivity";
    private String key = "key";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SDDiskCache.init(this); //初始化
        SDDiskCache.setGlobalObjectConverter(new GlobalObjectConverter());//如果要用XXXObject方法，需要配置Object对象转换器
        SDDiskCache.setGlobalEncryptConverter(new GlobalEncryptConverter()); //如果需要加解密，需要配置加解密转换器


        // 不同的open方法可以关联不同的目录
        SDDiskCache.openCache();        //"Android/data/包名/cache/cache"
        SDDiskCache.openCache("hello"); //"Android/data/包名/cache/hello"

        SDDiskCache.open();             //"Android/data/包名/files/files"
        SDDiskCache.open("hello");      //"Android/data/包名/files/hello"

        SDDiskCache.openDir(Environment.getExternalStorageDirectory()); //关联指定的目录

        SDDiskCache.open().putInt(key, 1);
        SDDiskCache.open().putLong(key, 2);
        SDDiskCache.open().putFloat(key, 3.3f);
        SDDiskCache.open().putDouble(key, 4.4444d);
        SDDiskCache.open().putBoolean(key, true);
        SDDiskCache.open().putString(key, "hello String", true); //加密字符串
        SDDiskCache.open().putSerializable(new TestModel());
        SDDiskCache.open().putObject(new TestModel(), true); //加密实体

        print();
    }

    private void print()
    {
        Log.i(TAG, "getInt:" + SDDiskCache.open().getInt(key, 0));
        Log.i(TAG, "getLong:" + SDDiskCache.open().getLong(key, 0));
        Log.i(TAG, "getFloat:" + SDDiskCache.open().getFloat(key, 0));
        Log.i(TAG, "getDouble:" + SDDiskCache.open().getDouble(key, 0));
        Log.i(TAG, "getBoolean:" + SDDiskCache.open().getBoolean(key, false));
        Log.i(TAG, "getString:" + SDDiskCache.open().getString(key));
        Log.i(TAG, "getSerializable:" + SDDiskCache.open().getSerializable(TestModel.class));
        Log.i(TAG, "getObject:" + SDDiskCache.open().getObject(TestModel.class));
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        SDDiskCache.open().delete(); //删除该目录对应的所有缓存
    }
}
```

## 创建对象转换器
```java
public class GlobalObjectConverter implements IObjectConverter
{
    @Override
    public String objectToString(Object object)
    {
        return JSON.toJSONString(object); //对象转json
    }

    @Override
    public <T> T stringToObject(String string, Class<T> clazz)
    {
        return JSON.parseObject(string, clazz); //json转对象
    }
}
```

## 创建加解密转换器
```java
public class GlobalEncryptConverter implements IEncryptConverter
{
    @Override
    public String encrypt(String content)
    {
        return AESUtil.encrypt(content); //加密
    }

    @Override
    public String decrypt(String content)
    {
        return AESUtil.decrypt(content); //解密
    }
}
```
