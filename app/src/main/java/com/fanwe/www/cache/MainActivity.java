package com.fanwe.www.cache;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import com.fanwe.library.cache.SDDiskCache;

public class MainActivity extends AppCompatActivity
{
    private static final String TAG = "MainActivity";

    private TextView tv_info;
    private String key;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tv_info = (TextView) findViewById(R.id.tv_info);

        SDDiskCache.init(this); //初始化

        SDDiskCache.open().putInt(key, 10);

        startThread1();
        startThread2();
        startThread3();
    }

    private void startThread1()
    {
        new Thread("thread 1")
        {
            @Override
            public void run()
            {
                mRunnable1.run();
                super.run();
            }
        }.start();
    }

    private void startThread2()
    {
        new Thread("thread 2")
        {
            @Override
            public void run()
            {
                mRunnable2.run();
                super.run();
            }
        }.start();
    }

    private void startThread3()
    {
        new Thread("thread 3")
        {
            @Override
            public void run()
            {
                mRunnable3.run();
                super.run();
            }
        }.start();
    }

    private Object locker = new Object();

    private Runnable mRunnable1 = new Runnable()
    {
        @Override
        public void run()
        {
            for (int i = 0; i < 500; i++)
            {
//                synchronized (locker)
//                {
                    SDDiskCache.open().putInt(key, SDDiskCache.open().getInt(key, 0) + 1);
//                }
            }
        }
    };

    private Runnable mRunnable2 = new Runnable()
    {
        @Override
        public void run()
        {
            for (int i = 0; i < 500; i++)
            {
//                synchronized (locker)
//                {
                    SDDiskCache.open().putInt(key, SDDiskCache.open().getInt(key, 0) - 1);
//                }
            }
        }
    };

    private Runnable mRunnable3 = new Runnable()
    {
        @Override
        public void run()
        {
            for (int i = 0; i < 2000; i++)
            {
                Log.e(TAG, "read finish-------------:" + SDDiskCache.open().getInt(key, 0));
                try
                {
                    Thread.sleep(5);
                } catch (Exception e)
                {
                }
            }
        }
    };


    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        SDDiskCache.open().delete(); //删除该目录对应的所有缓存
    }
}
