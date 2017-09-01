package com.fanwe.www.cache;

import android.util.Log;

/**
 * 计时打印帮助类
 */
public class SDTimeLogger
{
    private static final String TAG = "SDTimeLogger";
    private static final String DEFAULT_PREFIX = " Time:";

    public static void test(Runnable runnable)
    {
        test(null, runnable);
    }

    public static void test(String prefix, Runnable runnable)
    {
        if (prefix == null)
        {
            prefix = DEFAULT_PREFIX;
        } else
        {
            prefix = prefix + DEFAULT_PREFIX;
        }
        long startTime = System.currentTimeMillis();
        runnable.run();
        long time = System.currentTimeMillis() - startTime;
        Log.i(TAG, prefix + time);
    }
}
