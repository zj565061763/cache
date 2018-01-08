package com.fanwe.www.cache;

import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.fanwe.lib.cache.FDisk;
import com.fanwe.www.cache.converter.FastjsonObjectConverter;
import com.fanwe.www.cache.converter.GlobalEncryptConverter;

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
        FDisk.setDebug(true);

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
        FDisk.setGlobalObjectConverter(new FastjsonObjectConverter());//配置Fastjson对象转换器
//        FDisk.setGlobalObjectConverter(new GsonObjectConverter());//配置Gson对象转换器
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
        FDisk.open().putInt(key, 1);
        FDisk.open().putLong(key, 2);
        FDisk.open().putFloat(key, 3.3f);
        FDisk.open().putDouble(key, 4.4444d);
        FDisk.open().putBoolean(key, true);
        FDisk.open().putString(key, "hello String");
        FDisk.open().putSerializable(new TestModel());
        FDisk.open().setEncrypt(true).putObject(new TestModel()); //加密实体
    }

    private void printData()
    {
        Log.i(TAG, "getInt:" + FDisk.open().getInt(key, 0));
        Log.i(TAG, "getLong:" + FDisk.open().getLong(key, 0));
        Log.i(TAG, "getFloat:" + FDisk.open().getFloat(key, 0));
        Log.i(TAG, "getDouble:" + FDisk.open().getDouble(key, 0));
        Log.i(TAG, "getBoolean:" + FDisk.open().getBoolean(key, false));
        Log.i(TAG, "getString:" + FDisk.open().getString(key));
        Log.i(TAG, "getSerializable:" + FDisk.open().getSerializable(TestModel.class));
        Log.i(TAG, "getObject:" + FDisk.open().getObject(TestModel.class));
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
                FDisk.open().setMemorySupport(false).putObject(mTestModel);
            }
        });
        SDTimeLogger.test("getObject", new Runnable()
        {
            @Override
            public void run()
            {
                FDisk.open().setMemorySupport(false).getObject(TestModel.class);
            }
        });

        SDTimeLogger.test("putSerializable", new Runnable()
        {
            @Override
            public void run()
            {
                FDisk.open().setMemorySupport(false).putSerializable(mTestModel);
            }
        });
        SDTimeLogger.test("getSerializable", new Runnable()
        {
            @Override
            public void run()
            {
                FDisk.open().setMemorySupport(false).getSerializable(TestModel.class);
            }
        });
    }
}
