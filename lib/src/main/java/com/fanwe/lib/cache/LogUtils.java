package com.fanwe.lib.cache;

import android.util.Log;

/**
 * Created by Administrator on 2017/9/20.
 */

class LogUtils
{
    public static final String TAG = "FDisk";

    private static boolean mIsDebug;

    public static void setDebug(boolean debug)
    {
        mIsDebug = debug;
    }

    public static boolean isDebug()
    {
        return mIsDebug;
    }

    public static void i(String msg)
    {
        if (!isDebug())
        {
            return;
        }
        Log.i(TAG, msg);
    }

    public static void e(String msg)
    {
        if (!isDebug())
        {
            return;
        }
        Log.e(TAG, msg);
    }
}
