package com.fanwe.www.cache;

import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.fanwe.lib.cache.FDisk;
import com.fanwe.www.cache.converter.GlobalEncryptConverter;
import com.fanwe.www.cache.converter.GsonObjectConverter;

public class MainActivity extends AppCompatActivity implements View.OnClickListener
{
    private static final String TAG = "MainActivity";
    private String key = "key";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FDisk.init(this); //初始化

        /**
         * 当数据量较大的时候建议用XXXObject方法，性能会比XXXSerializable好非常多
         * 如果要用XXXObject方法，需要配置对象转换器
         *
         * 用魅族MX6测试，测试对象中的map有10000条数据，测试结果如下：
         *
         * Fastjson1.1.62版本对象转换器:
         * putObject在70毫秒左右
         * getObject在140毫秒左右
         *
         * Gson2.8.1版本对象转换器:
         * putObject在65毫秒左右
         * getObject在140毫秒左右
         *
         * putSerializable在600毫秒左右
         * getSerializable在700毫秒左右
         */
        FDisk.setGlobalObjectConverter(new GsonObjectConverter());//配置Gson对象转换器
        FDisk.setGlobalEncryptConverter(new GlobalEncryptConverter()); //如果需要加解密，需要配置加解密转换器

        //不同的open方法可以关联不同的目录
        FDisk.open();                    // 外部存储"Android/data/包名/files/disk_file"目录
        FDisk.open("hello");             // 外部存储"Android/data/包名/files/hello"目录
        FDisk.openCache();               // 外部存储"Android/data/包名/cache/disk_cache"目录
        FDisk.openCache("hello");        // 外部存储"Android/data/包名/cache/hello"目录

        FDisk.openInternal();             // 内部存储"/data/包名/files/disk_file"目录
        FDisk.openInternal("hello");      // 内部存储"/data/包名/files/hello"目录
        FDisk.openInternalCache();        // 内部存储"/data/包名/cache/disk_cache"目录
        FDisk.openInternalCache("hello"); // 内部存储"/data/包名/cache/hello"目录
        FDisk.openDir(Environment.getExternalStorageDirectory()); //关联指定的目录

        putData();
        printData();
    }

    private void putData()
    {
        FDisk.open().cacheInteger().put(key, 1);
        FDisk.open().cacheLong().put(key, 2L);
        FDisk.open().cacheFloat().put(key, 3.3F);
        FDisk.open().cacheDouble().put(key, 4.4444D);
        FDisk.open().cacheBoolean().put(key, true);
        FDisk.open().cacheString().put(key, "hello String");
        FDisk.open().cacheSerializable(TestModel.class).put(new TestModel());
        FDisk.open().setEncrypt(true).cacheObject(TestModel.class).put(new TestModel()); //加密实体
    }

    private void printData()
    {
        Log.i(TAG, "getInt:" + FDisk.open().cacheInteger().get(key));
        Log.i(TAG, "getLong:" + FDisk.open().cacheLong().get(key));
        Log.i(TAG, "getFloat:" + FDisk.open().cacheFloat().get(key));
        Log.i(TAG, "getDouble:" + FDisk.open().cacheDouble().get(key));
        Log.i(TAG, "getBoolean:" + FDisk.open().cacheBoolean().get(key));
        Log.i(TAG, "getString:" + FDisk.open().cacheString().get(key));
        Log.i(TAG, "getSerializable:" + FDisk.open().cacheSerializable(TestModel.class).get());
        Log.i(TAG, "getObject:" + FDisk.open().cacheObject(TestModel.class).get());
    }

    private TestModel mTestModel = new TestModel();

    @Override
    public void onClick(View v)
    {
        SDTimeLogger.test("putObject", new Runnable()
        {
            @Override
            public void run()
            {
                FDisk.open().setMemorySupport(false).cacheObject(TestModel.class).put(mTestModel);
            }
        });
        SDTimeLogger.test("getObject", new Runnable()
        {
            @Override
            public void run()
            {
                FDisk.open().setMemorySupport(false).cacheObject(TestModel.class).get();
            }
        });

        SDTimeLogger.test("putSerializable", new Runnable()
        {
            @Override
            public void run()
            {
                FDisk.open().setMemorySupport(false).cacheSerializable(TestModel.class).put(mTestModel);
            }
        });
        SDTimeLogger.test("getSerializable", new Runnable()
        {
            @Override
            public void run()
            {
                FDisk.open().setMemorySupport(false).cacheSerializable(TestModel.class).get();
            }
        });
    }
}
